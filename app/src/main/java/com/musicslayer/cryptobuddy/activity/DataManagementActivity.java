package com.musicslayer.cryptobuddy.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashRunnable;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.data.persistent.PersistentDataStore;
import com.musicslayer.cryptobuddy.data.persistent.app.PersistentAppDataStore;
import com.musicslayer.cryptobuddy.data.persistent.app.Purchases;
import com.musicslayer.cryptobuddy.data.persistent.user.PersistentUserDataStore;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ConfirmDeleteDataDialog;
import com.musicslayer.cryptobuddy.dialog.ConfirmResetAppDialog;
import com.musicslayer.cryptobuddy.dialog.ExportDataFileDialog;
import com.musicslayer.cryptobuddy.dialog.ImportDataFileDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.dialog.SelectDataTypesDialog;
import com.musicslayer.cryptobuddy.file.UniversalFile;
import com.musicslayer.cryptobuddy.json.JSONWithNull;
import com.musicslayer.cryptobuddy.monetization.InAppPurchase;
import com.musicslayer.cryptobuddy.util.ClipboardUtil;
import com.musicslayer.cryptobuddy.util.FileUtil;
import com.musicslayer.cryptobuddy.util.MessageUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class DataManagementActivity extends BaseActivity {
    public UniversalFile universalFile;
    public UniversalFile universalFolder;
    String fileName;
    String fileText;
    String clipboardText;
    public ArrayList<String> chosenDataTypes;

    @Override
    public int getAdLayoutViewID() {
        return R.id.data_management_adLayout;
    }

    @Override
    public void onBackPressedImpl() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public int getProgressViewID() {
        return R.id.data_management_progressBar;
    }

    @Override
    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.activity_data_management);

        InAppPurchase.setInAppPurchaseListener(new InAppPurchase.InAppPurchaseListener() {
            @Override
            public void onInAppPurchase() {
                // Needed if the purchase update happened on another thread.
                runOnUiThread(new CrashRunnable(DataManagementActivity.this) {
                    @Override
                    public void runImpl() {
                        updateLayout();
                    }
                });
            }
        });

        ArrayList<String> visibleDataTypes = PersistentDataStore.getAllVisibleDataTypes();
        Collections.sort(visibleDataTypes);

        ProgressDialogFragment exportFile_progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        exportFile_progressDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(this) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                ProgressDialogFragment.updateProgressTitle("Exporting to File...");

                ProgressDialogFragment.updateProgressSubtitle("Exporting Data...");
                String json = PersistentDataStore.exportStoredDataToJSON(chosenDataTypes);
                ProgressDialogFragment.updateProgressSubtitle("Writing to File...");

                boolean isSuccess = false;
                if(universalFolder != null) {
                    isSuccess = universalFolder.writeContent(fileName, json);
                }
                else if(universalFile != null) {
                    isSuccess = universalFile.write(json);
                }

                ProgressDialogFragment.setValue(DataBridge.serializeValue(isSuccess, Boolean.class));
            }
        });
        exportFile_progressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                Boolean isSuccess_o = DataBridge.deserializeValue(ProgressDialogFragment.getValue(), Boolean.class);

                if(isSuccess_o != null && isSuccess_o) {
                    ToastUtil.showToast("export_file_success");
                }
                else {
                    ToastUtil.showToast("export_file_failed");
                }
            }
        });
        exportFile_progressDialogFragment.restoreListeners(this, "progress_export_file");

        // Export to File
        BaseDialogFragment exportFile_selectDataTypesDialogFragment = BaseDialogFragment.newInstance(SelectDataTypesDialog.class, visibleDataTypes);
        exportFile_selectDataTypesDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((SelectDataTypesDialog)dialog).isComplete) {
                    chosenDataTypes = ((SelectDataTypesDialog)dialog).user_CHOICES;
                    exportFile_progressDialogFragment.show(DataManagementActivity.this, "progress_export_file");
                }
            }
        });
        exportFile_selectDataTypesDialogFragment.restoreListeners(this, "select_export_file");

        BaseDialogFragment exportDataFileDialogFragment = BaseDialogFragment.newInstance(ExportDataFileDialog.class);
        exportDataFileDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ExportDataFileDialog)dialog).isComplete) {
                    universalFile = ((ExportDataFileDialog)dialog).user_UNIVERSALFILE;
                    universalFolder = ((ExportDataFileDialog)dialog).user_UNIVERSALFOLDER;
                    fileName = ((ExportDataFileDialog)dialog).user_FILENAME;

                    // Launch dialog to ask user which data types to import.
                    exportFile_selectDataTypesDialogFragment.show(DataManagementActivity.this, "select_export_file");
                }
            }
        });
        exportDataFileDialogFragment.restoreListeners(this, "export_file");

        Button B_EXPORT_FILE = findViewById(R.id.data_management_exportFileButton);
        B_EXPORT_FILE.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                if(Purchases.isUnlockImportExportPurchased()) {
                    exportDataFileDialogFragment.show(DataManagementActivity.this, "export_file");
                }
                else {
                    ToastUtil.showToast("unlock_import_export_required");
                }
            }
        });

        // Import from File
        ProgressDialogFragment importFile_importData_progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        importFile_importData_progressDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(this) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                ProgressDialogFragment.updateProgressTitle("Importing from File...");
                ProgressDialogFragment.updateProgressSubtitle("Importing Data...");

                PersistentDataStore.importStoredDataFromJSON(chosenDataTypes, fileText);
            }
        });
        importFile_importData_progressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                ToastUtil.showToast("import_file_success");
            }
        });
        importFile_importData_progressDialogFragment.restoreListeners(this, "progress_import_file_import_data");

        BaseDialogFragment importFile_selectDataTypesDialogFragment = BaseDialogFragment.newInstance(SelectDataTypesDialog.class, new ArrayList<String>());
        importFile_selectDataTypesDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((SelectDataTypesDialog)dialog).isComplete) {
                    chosenDataTypes = ((SelectDataTypesDialog)dialog).user_CHOICES;
                    importFile_importData_progressDialogFragment.show(DataManagementActivity.this, "progress_import_file_import_data");
                }
            }
        });
        importFile_selectDataTypesDialogFragment.restoreListeners(this, "select_import_file");

        ProgressDialogFragment importFile_readFile_progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        importFile_readFile_progressDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(this) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                ProgressDialogFragment.updateProgressTitle("Importing from File...");
                ProgressDialogFragment.updateProgressSubtitle("Reading from File...");

                ArrayList<String> dataTypes;
                try {
                    // Check if file text can be parsed as JSON. If so, store the data type keys that are present.
                    if(universalFolder != null) {
                        fileText = universalFolder.readContent(fileName);
                    }
                    else if (universalFile != null){
                        fileText = universalFile.read();
                    }
                    else {
                        throw new IllegalStateException();
                    }

                    JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull(fileText);
                    dataTypes = o.keys();
                    Collections.sort(dataTypes);
                }
                catch(Exception ignored) {
                    dataTypes = null;
                }

                ProgressDialogFragment.setValue(DataBridge.serializeArrayList(dataTypes, String.class));
            }
        });
        importFile_readFile_progressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                ArrayList<String> dataTypes = DataBridge.deserializeArrayList(ProgressDialogFragment.getValue(), String.class);

                if(dataTypes == null) {
                    ToastUtil.showToast("import_file_failed");
                }
                else {
                    // Launch dialog to ask user which data types to import.
                    importFile_selectDataTypesDialogFragment.updateArguments(SelectDataTypesDialog.class, dataTypes);
                    importFile_selectDataTypesDialogFragment.show(DataManagementActivity.this, "select_import_file");
                }
            }
        });
        importFile_readFile_progressDialogFragment.restoreListeners(this, "progress_import_file_read_file");

        BaseDialogFragment importDataFileDialogFragment = BaseDialogFragment.newInstance(ImportDataFileDialog.class);
        importDataFileDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ImportDataFileDialog)dialog).isComplete) {
                    universalFile = ((ImportDataFileDialog)dialog).user_UNIVERSALFILE;
                    universalFolder = ((ImportDataFileDialog)dialog).user_UNIVERSALFOLDER;
                    fileName = ((ImportDataFileDialog)dialog).user_FILENAME;

                    importFile_readFile_progressDialogFragment.show(DataManagementActivity.this, "progress_import_file_read_file");
                }
            }
        });
        importDataFileDialogFragment.restoreListeners(this, "import_file");

        Button B_IMPORT_FILE = findViewById(R.id.data_management_importFileButton);
        B_IMPORT_FILE.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                if(Purchases.isUnlockImportExportPurchased()) {
                    importDataFileDialogFragment.show(DataManagementActivity.this, "import_file");
                }
                else {
                    ToastUtil.showToast("unlock_import_export_required");
                }
            }
        });

        // Export to Clipboard
        ProgressDialogFragment exportClipboard_progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        exportClipboard_progressDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(this) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                ProgressDialogFragment.updateProgressTitle("Exporting to Clipboard...");
                ProgressDialogFragment.updateProgressSubtitle("Exporting Data...");

                String json = PersistentDataStore.exportStoredDataToJSON(chosenDataTypes);
                ProgressDialogFragment.setValue(DataBridge.serializeValue(json, String.class));
            }
        });
        exportClipboard_progressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                String json = DataBridge.deserializeValue(ProgressDialogFragment.getValue(), String.class);
                ClipboardUtil.exportText("export_data", json);
            }
        });
        exportClipboard_progressDialogFragment.restoreListeners(this, "progress_export_clipboard");

        BaseDialogFragment exportClipboard_selectDataTypesDialogFragment = BaseDialogFragment.newInstance(SelectDataTypesDialog.class, visibleDataTypes);
        exportClipboard_selectDataTypesDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((SelectDataTypesDialog)dialog).isComplete) {
                    chosenDataTypes = ((SelectDataTypesDialog)dialog).user_CHOICES;
                    exportClipboard_progressDialogFragment.show(DataManagementActivity.this, "progress_export_clipboard");
                }
            }
        });
        exportClipboard_selectDataTypesDialogFragment.restoreListeners(this, "select_export_clipboard");

        Button B_EXPORT_CLIPBOARD = findViewById(R.id.data_management_exportClipboardButton);
        B_EXPORT_CLIPBOARD.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                if(Purchases.isUnlockImportExportPurchased()) {
                    exportClipboard_selectDataTypesDialogFragment.show(DataManagementActivity.this, "select_export_clipboard");
                }
                else {
                    ToastUtil.showToast("unlock_import_export_required");
                }
            }
        });

        // Import from Clipboard
        ProgressDialogFragment importClipboard_importData_progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        importClipboard_importData_progressDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(this) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                ProgressDialogFragment.updateProgressTitle("Importing from Clipboard...");
                ProgressDialogFragment.updateProgressSubtitle("Importing Data...");

                PersistentDataStore.importStoredDataFromJSON(chosenDataTypes, clipboardText);
            }
        });
        importClipboard_importData_progressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                ToastUtil.showToast("import_clipboard_success");
            }
        });
        importClipboard_importData_progressDialogFragment.restoreListeners(this, "progress_import_clipboard_import_data");

        BaseDialogFragment importClipboard_selectDataTypesDialogFragment = BaseDialogFragment.newInstance(SelectDataTypesDialog.class, new ArrayList<String>());
        importClipboard_selectDataTypesDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((SelectDataTypesDialog)dialog).isComplete) {
                    chosenDataTypes = ((SelectDataTypesDialog)dialog).user_CHOICES;
                    importClipboard_importData_progressDialogFragment.show(DataManagementActivity.this, "progress_import_clipboard_import_data");
                }
            }
        });
        importClipboard_selectDataTypesDialogFragment.restoreListeners(this, "select_import_clipboard");

        Button B_IMPORT_CLIPBOARD = findViewById(R.id.data_management_importClipboardButton);
        B_IMPORT_CLIPBOARD.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                if(Purchases.isUnlockImportExportPurchased()) {
                    ArrayList<String> dataTypes;

                    try {
                        // Check if clipboard text can be parsed as JSON. If so, store the data type keys that are present.
                        clipboardText = String.valueOf(ClipboardUtil.importText());

                        JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull(clipboardText);
                        dataTypes = o.keys();
                        Collections.sort(dataTypes);
                    }
                    catch(Exception ignored) {
                        ToastUtil.showToast("import_clipboard_not_from_app");
                        return;
                    }

                    // Launch dialog to ask user which data types to import.
                    importClipboard_selectDataTypesDialogFragment.updateArguments(SelectDataTypesDialog.class, dataTypes);
                    importClipboard_selectDataTypesDialogFragment.show(DataManagementActivity.this, "select_import_clipboard");
                }
                else {
                    ToastUtil.showToast("unlock_import_export_required");
                }
            }
        });

        // Export to Email
        ProgressDialogFragment exportEmail_progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        exportEmail_progressDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(this) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                ProgressDialogFragment.updateProgressTitle("Exporting to Email...");
                ProgressDialogFragment.updateProgressSubtitle("Exporting Data...");

                String json = PersistentDataStore.exportStoredDataToJSON(chosenDataTypes);
                ProgressDialogFragment.setValue(DataBridge.serializeValue(json, String.class));
            }
        });
        exportEmail_progressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                String json = DataBridge.deserializeValue(ProgressDialogFragment.getValue(), String.class);

                // Create temp file with exported data and email it.
                ArrayList<File> fileArrayList = new ArrayList<>();
                fileArrayList.add(FileUtil.writeTempFile(json));
                MessageUtil.sendEmail(DataManagementActivity.this, "", "Crypto Buddy - Exported Data", "Exported data is attached.", fileArrayList);
            }
        });
        exportEmail_progressDialogFragment.restoreListeners(this, "progress_export_email");

        BaseDialogFragment exportEmail_selectDataTypesDialogFragment = BaseDialogFragment.newInstance(SelectDataTypesDialog.class, visibleDataTypes);
        exportEmail_selectDataTypesDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((SelectDataTypesDialog)dialog).isComplete) {
                    chosenDataTypes = ((SelectDataTypesDialog)dialog).user_CHOICES;
                    exportEmail_progressDialogFragment.show(DataManagementActivity.this, "progress_export_email");
                }
            }
        });
        exportEmail_selectDataTypesDialogFragment.restoreListeners(this, "select_export_email");

        Button B_EXPORT_EMAIL = findViewById(R.id.data_management_exportEmailButton);
        B_EXPORT_EMAIL.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                if(Purchases.isUnlockImportExportPurchased()) {
                    exportEmail_selectDataTypesDialogFragment.show(DataManagementActivity.this, "select_export_email");
                }
                else {
                    ToastUtil.showToast("unlock_import_export_required");
                }
            }
        });

        updateLayout();

        // Reset Data
        BaseDialogFragment confirmDeleteDataDialogFragment = BaseDialogFragment.newInstance(ConfirmDeleteDataDialog.class);
        confirmDeleteDataDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmDeleteDataDialog)dialog).isComplete) {
                    boolean isAppComplete = PersistentAppDataStore.resetAllStoredData(chosenDataTypes);
                    boolean isUserComplete = PersistentUserDataStore.resetAllStoredData(chosenDataTypes);

                    if(isAppComplete && isUserComplete) {
                        ToastUtil.showToast("delete_data");
                    }
                    else {
                        ToastUtil.showToast("delete_data_fail");
                    }
                }
            }
        });
        confirmDeleteDataDialogFragment.restoreListeners(this, "confirm_delete_data");

        BaseDialogFragment deleteData_selectDataTypesDialogFragment = BaseDialogFragment.newInstance(SelectDataTypesDialog.class, visibleDataTypes);
        deleteData_selectDataTypesDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((SelectDataTypesDialog)dialog).isComplete) {
                    chosenDataTypes = ((SelectDataTypesDialog)dialog).user_CHOICES;
                    confirmDeleteDataDialogFragment.show(DataManagementActivity.this, "confirm_delete_data");
                }
            }
        });
        deleteData_selectDataTypesDialogFragment.restoreListeners(this, "select_delete_data");

        Button B_DELETE_DATA = findViewById(R.id.data_management_deleteDataButton);
        B_DELETE_DATA.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                deleteData_selectDataTypesDialogFragment.show(DataManagementActivity.this, "select_delete_data");
            }
        });

        // Reset App
        BaseDialogFragment confirmResetAppDialogFragment = BaseDialogFragment.newInstance(ConfirmResetAppDialog.class);
        confirmResetAppDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmResetAppDialog)dialog).isComplete) {
                    boolean isAppComplete = PersistentAppDataStore.resetAllStoredData();
                    boolean isUserComplete = PersistentUserDataStore.resetAllStoredData();

                    if(isAppComplete && isUserComplete) {
                        ToastUtil.showToast("reset_app");
                    }
                    else {
                        ToastUtil.showToast("reset_app_fail");
                    }
                }
            }
        });
        confirmResetAppDialogFragment.restoreListeners(this, "confirm_reset_app");

        Button B_RESET_APP = findViewById(R.id.data_management_resetAppButton);
        B_RESET_APP.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                confirmResetAppDialogFragment.show(DataManagementActivity.this, "confirm_reset_app");
            }
        });
    }

    public void updateLayout() {
        Button B_EXPORT_FILE = findViewById(R.id.data_management_exportFileButton);
        Button B_IMPORT_FILE = findViewById(R.id.data_management_importFileButton);
        Button B_EXPORT_CLIPBOARD = findViewById(R.id.data_management_exportClipboardButton);
        Button B_IMPORT_CLIPBOARD = findViewById(R.id.data_management_importClipboardButton);
        Button B_EXPORT_EMAIL = findViewById(R.id.data_management_exportEmailButton);

        if(Purchases.isUnlockImportExportPurchased()) {
            B_EXPORT_FILE.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_insert_drive_file_24, 0, 0, 0);
            B_IMPORT_FILE.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_insert_drive_file_24, 0, 0, 0);
            B_EXPORT_CLIPBOARD.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_content_copy_24, 0, 0, 0);
            B_IMPORT_CLIPBOARD.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_content_paste_24, 0, 0, 0);
            B_EXPORT_EMAIL.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_email_24, 0, 0, 0);
        }
        else {
            B_EXPORT_FILE.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_lock_24, 0, 0, 0);
            B_IMPORT_FILE.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_lock_24, 0, 0, 0);
            B_EXPORT_CLIPBOARD.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_lock_24, 0, 0, 0);
            B_IMPORT_CLIPBOARD.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_lock_24, 0, 0, 0);
            B_EXPORT_EMAIL.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_lock_24, 0, 0, 0);
        }
    }

    @Override
    public void onSaveInstanceStateImpl(@NonNull Bundle bundle) {
        super.onSaveInstanceStateImpl(bundle);
        bundle.putParcelable("universalFile", universalFile);
        bundle.putParcelable("universalFolder", universalFolder);
        bundle.putString("fileName", fileName);
        bundle.putString("fileText", fileText);
        bundle.putString("clipboardText", clipboardText);
        bundle.putStringArrayList("chosenDataTypes", chosenDataTypes);
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        super.onRestoreInstanceStateImpl(bundle);
        if(bundle != null) {
            universalFile = bundle.getParcelable("universalFile");
            universalFolder = bundle.getParcelable("universalFolder");
            fileName = bundle.getString("fileName");
            fileText = bundle.getString("fileText");
            clipboardText = bundle.getString("clipboardText");
            chosenDataTypes = bundle.getStringArrayList("chosenDataTypes");
        }
    }
}