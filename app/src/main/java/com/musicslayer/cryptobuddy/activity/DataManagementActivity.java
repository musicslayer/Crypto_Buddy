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
import com.musicslayer.cryptobuddy.data.persistent.PersistentDataStore;
import com.musicslayer.cryptobuddy.data.persistent.app.Purchases;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ExportDataFileDialog;
import com.musicslayer.cryptobuddy.dialog.ImportDataFileDialog;
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

        ArrayList<String> exportableDataTypes = PersistentDataStore.getAllExportableDataTypes();
        Collections.sort(exportableDataTypes);

        // Export to File
        BaseDialogFragment exportFile_selectDataTypesDialogFragment = BaseDialogFragment.newInstance(SelectDataTypesDialog.class, exportableDataTypes);
        exportFile_selectDataTypesDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((SelectDataTypesDialog)dialog).isComplete) {
                    ArrayList<String> chosenDataTypes = ((SelectDataTypesDialog)dialog).user_CHOICES;
                    String json = PersistentDataStore.exportStoredDataToJSON(chosenDataTypes);

                    boolean isSuccess = false;
                    if(universalFolder != null) {
                        isSuccess = universalFolder.writeContent(fileName, json);
                    }
                    else if(universalFile != null) {
                        isSuccess = universalFile.write(json);
                    }

                    if(isSuccess) {
                        ToastUtil.showToast("export_file_success");
                    }
                    else {
                        ToastUtil.showToast("export_file_failed");
                    }
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
        DialogInterface.OnDismissListener importFile_selectDataTypesDialogFragmentListener = new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((SelectDataTypesDialog)dialog).isComplete) {
                    ArrayList<String> chosenDataTypes = ((SelectDataTypesDialog)dialog).user_CHOICES;
                    PersistentDataStore.importStoredDataFromJSON(chosenDataTypes, fileText);
                    ToastUtil.showToast("import_file_success");
                }
            }
        };

        BaseDialogFragment importDataFileDialogFragment = BaseDialogFragment.newInstance(ImportDataFileDialog.class);
        importDataFileDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ImportDataFileDialog)dialog).isComplete) {
                    universalFile = ((ImportDataFileDialog)dialog).user_UNIVERSALFILE;
                    universalFolder = ((ImportDataFileDialog)dialog).user_UNIVERSALFOLDER;
                    fileName = ((ImportDataFileDialog)dialog).user_FILENAME;

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
                        ToastUtil.showToast("import_file_failed");
                        return;
                    }

                    // Launch dialog to ask user which data types to import.
                    BaseDialogFragment importFile_selectDataTypesDialogFragment = BaseDialogFragment.newInstance(SelectDataTypesDialog.class, dataTypes);
                    importFile_selectDataTypesDialogFragment.setOnDismissListener(importFile_selectDataTypesDialogFragmentListener);
                    importFile_selectDataTypesDialogFragment.show(DataManagementActivity.this, "select_import_file");
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

        BaseDialogFragment importFile_selectDataTypesDialogFragment2 = (BaseDialogFragment) this.getSupportFragmentManager().findFragmentByTag("select_import_file");
        if (importFile_selectDataTypesDialogFragment2 != null) {
            importFile_selectDataTypesDialogFragment2.setOnDismissListener(importFile_selectDataTypesDialogFragmentListener);
        }

        // Export to Clipboard
        BaseDialogFragment exportClipboard_selectDataTypesDialogFragment = BaseDialogFragment.newInstance(SelectDataTypesDialog.class, exportableDataTypes);
        exportClipboard_selectDataTypesDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((SelectDataTypesDialog)dialog).isComplete) {
                    // Create temp file with exported data and email it.
                    ArrayList<String> chosenDataTypes = ((SelectDataTypesDialog)dialog).user_CHOICES;
                    ClipboardUtil.exportText("export_data", PersistentDataStore.exportStoredDataToJSON(chosenDataTypes));
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
        DialogInterface.OnDismissListener importClipboard_selectDataTypesDialogFragmentListener = new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((SelectDataTypesDialog)dialog).isComplete) {
                    ArrayList<String> chosenDataTypes = ((SelectDataTypesDialog)dialog).user_CHOICES;
                    PersistentDataStore.importStoredDataFromJSON(chosenDataTypes, clipboardText);
                    ToastUtil.showToast("import_clipboard_success");
                }
            }
        };

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
                    BaseDialogFragment importClipboard_selectDataTypesDialogFragment = BaseDialogFragment.newInstance(SelectDataTypesDialog.class, dataTypes);
                    importClipboard_selectDataTypesDialogFragment.setOnDismissListener(importClipboard_selectDataTypesDialogFragmentListener);
                    importClipboard_selectDataTypesDialogFragment.show(DataManagementActivity.this, "select_import_clipboard");
                }
                else {
                    ToastUtil.showToast("unlock_import_export_required");
                }
            }
        });

        BaseDialogFragment importClipboard_selectDataTypesDialogFragment2 = (BaseDialogFragment) this.getSupportFragmentManager().findFragmentByTag("select_import_clipboard");
        if (importClipboard_selectDataTypesDialogFragment2 != null) {
            importClipboard_selectDataTypesDialogFragment2.setOnDismissListener(importClipboard_selectDataTypesDialogFragmentListener);
        }

        // Export to Email
        BaseDialogFragment exportEmail_selectDataTypesDialogFragment = BaseDialogFragment.newInstance(SelectDataTypesDialog.class, exportableDataTypes);
        exportEmail_selectDataTypesDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((SelectDataTypesDialog)dialog).isComplete) {
                    // Create temp file with exported data and email it.
                    ArrayList<String> chosenDataTypes = ((SelectDataTypesDialog)dialog).user_CHOICES;
                    ArrayList<File> fileArrayList = new ArrayList<>();
                    fileArrayList.add(FileUtil.writeTempFile(PersistentDataStore.exportStoredDataToJSON(chosenDataTypes)));
                    MessageUtil.sendEmail(DataManagementActivity.this, "", "Crypto Buddy - Exported Data", "Exported data is attached.", fileArrayList);
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
        bundle.putParcelable("universalFile", universalFile);
        bundle.putParcelable("universalFolder", universalFolder);
        bundle.putString("fileName", fileName);
        bundle.putString("fileText", fileText);
        bundle.putString("clipboardText", clipboardText);
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            universalFile = bundle.getParcelable("universalFile");
            universalFolder = bundle.getParcelable("universalFolder");
            fileName = bundle.getString("fileName");
            fileText = bundle.getString("fileText");
            clipboardText = bundle.getString("clipboardText");
        }
    }
}