package com.musicslayer.cryptobuddy.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ExportDataFileDialog;
import com.musicslayer.cryptobuddy.dialog.ImportDataFileDialog;
import com.musicslayer.cryptobuddy.dialog.SelectDataTypesDialog;
import com.musicslayer.cryptobuddy.persistence.Persistence;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.util.ClipboardUtil;
import com.musicslayer.cryptobuddy.util.FileUtil;
import com.musicslayer.cryptobuddy.util.MessageUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.util.UriUtil;

import java.io.File;
import java.util.ArrayList;

// TODO Let user pick "Downloads" folder.
// TODO Show toast if there are no suitable folders to export/import to?

public class DataManagementActivity extends BaseActivity {
    String fileName;
    String folderName;
    Uri uri;
    boolean isURI;
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

        updateLayout();
    }

    public void updateLayout() {
        ArrayList<String> dataTypes = Persistence.getAllDataTypes();

        // Export to File
        BaseDialogFragment exportFile_selectDataTypesDialogFragment = BaseDialogFragment.newInstance(SelectDataTypesDialog.class, dataTypes);
        exportFile_selectDataTypesDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((SelectDataTypesDialog)dialog).isComplete) {
                    ArrayList<String> chosenDataTypes = ((SelectDataTypesDialog)dialog).user_CHOICES;
                    String json = Persistence.exportAllToJSON(DataManagementActivity.this, chosenDataTypes);

                    boolean isSuccess;
                    if(isURI) {
                        isSuccess = UriUtil.writeFile(DataManagementActivity.this, uri, fileName, json);
                    }
                    else {
                        isSuccess = FileUtil.writeFile(folderName, fileName, json) != null;
                    }

                    if(isSuccess) {
                        ToastUtil.showToast(activity,"export_file_success");
                    }
                    else {
                        ToastUtil.showToast(activity,"export_file_failed");
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
                    fileName = ((ExportDataFileDialog)dialog).user_FILENAME;
                    folderName = ((ExportDataFileDialog)dialog).user_FOLDERNAME;
                    uri = ((ExportDataFileDialog)dialog).user_URI;
                    isURI = ((ExportDataFileDialog)dialog).user_ISURI;

                    // Launch dialog to ask user which data types to import.
                    exportFile_selectDataTypesDialogFragment.show(DataManagementActivity.this, "select_export_file");
                }
            }
        });
        exportDataFileDialogFragment.restoreListeners(this, "export_file");

        Button B_EXPORT = findViewById(R.id.data_management_exportButton);
        B_EXPORT.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                exportDataFileDialogFragment.show(DataManagementActivity.this, "export_file");
            }
        });

        // Import from File
        DialogInterface.OnDismissListener importFile_selectDataTypesDialogFragmentListener = new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((SelectDataTypesDialog)dialog).isComplete) {
                    ArrayList<String> chosenDataTypes = ((SelectDataTypesDialog)dialog).user_CHOICES;
                    Persistence.importAllFromJSON(activity, chosenDataTypes, fileText);
                    ToastUtil.showToast(activity,"import_file_success");
                }
            }
        };

        BaseDialogFragment importDataFileDialogFragment = BaseDialogFragment.newInstance(ImportDataFileDialog.class);
        importDataFileDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ImportDataFileDialog)dialog).isComplete) {
                    ArrayList<String> dataTypes;

                    try {
                        // Check if file text can be parsed as JSON. If so, store the data type keys that are present.
                        fileText = FileUtil.readFile(((ImportDataFileDialog)dialog).user_FOLDERNAME, ((ImportDataFileDialog)dialog).user_FILENAME);
                        Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull(fileText);
                        dataTypes = o.keys();
                    }
                    catch(Exception ignored) {
                        ToastUtil.showToast(activity,"import_file_failed");
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

        Button B_IMPORT = findViewById(R.id.data_management_importButton);
        B_IMPORT.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                importDataFileDialogFragment.show(DataManagementActivity.this, "import_file");
            }
        });

        BaseDialogFragment importFile_selectDataTypesDialogFragment2 = (BaseDialogFragment) this.getSupportFragmentManager().findFragmentByTag("select_import_file");
        if (importFile_selectDataTypesDialogFragment2 != null) {
            importFile_selectDataTypesDialogFragment2.setOnDismissListener(importFile_selectDataTypesDialogFragmentListener);
        }

        // Export to Email
        BaseDialogFragment exportEmail_selectDataTypesDialogFragment = BaseDialogFragment.newInstance(SelectDataTypesDialog.class, dataTypes);
        exportEmail_selectDataTypesDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((SelectDataTypesDialog)dialog).isComplete) {
                    // Create temp file with exported data and email it.
                    ArrayList<String> chosenDataTypes = ((SelectDataTypesDialog)dialog).user_CHOICES;
                    ArrayList<File> fileArrayList = new ArrayList<>();
                    fileArrayList.add(FileUtil.writeTempFile(Persistence.exportAllToJSON(DataManagementActivity.this, chosenDataTypes)));
                    MessageUtil.sendEmail(DataManagementActivity.this, "", "Crypto Buddy - Exported Data", "Exported data is attached.", fileArrayList);
                }
            }
        });
        exportEmail_selectDataTypesDialogFragment.restoreListeners(this, "select_export_email");

        Button B_EMAIL = findViewById(R.id.data_management_emailButton);
        B_EMAIL.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                exportEmail_selectDataTypesDialogFragment.show(DataManagementActivity.this, "select_export_email");
            }
        });

        // Export to Clipboard
        BaseDialogFragment exportClipboard_selectDataTypesDialogFragment = BaseDialogFragment.newInstance(SelectDataTypesDialog.class, dataTypes);
        exportClipboard_selectDataTypesDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((SelectDataTypesDialog)dialog).isComplete) {
                    // Create temp file with exported data and email it.
                    ArrayList<String> chosenDataTypes = ((SelectDataTypesDialog)dialog).user_CHOICES;
                    ClipboardUtil.exportText(DataManagementActivity.this, "export_data", Persistence.exportAllToJSON(DataManagementActivity.this, chosenDataTypes));
                }
            }
        });
        exportClipboard_selectDataTypesDialogFragment.restoreListeners(this, "select_export_clipboard");

        Button B_COPY = findViewById(R.id.data_management_copyButton);
        B_COPY.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                exportClipboard_selectDataTypesDialogFragment.show(DataManagementActivity.this, "select_export_clipboard");
            }
        });

        // Import from Clipboard
        DialogInterface.OnDismissListener importClipboard_selectDataTypesDialogFragmentListener = new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((SelectDataTypesDialog)dialog).isComplete) {
                    ArrayList<String> chosenDataTypes = ((SelectDataTypesDialog)dialog).user_CHOICES;
                    Persistence.importAllFromJSON(activity, chosenDataTypes, clipboardText);
                    ToastUtil.showToast(activity,"import_clipboard_success");
                }
            }
        };

        Button B_PASTE = findViewById(R.id.data_management_pasteButton);
        B_PASTE.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                ArrayList<String> dataTypes;

                try {
                    // Check if clipboard text can be parsed as JSON. If so, store the data type keys that are present.
                    clipboardText = String.valueOf(ClipboardUtil.importText(DataManagementActivity.this));
                    Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull(clipboardText);
                    dataTypes = o.keys();
                }
                catch(Exception ignored) {
                    ToastUtil.showToast(activity,"import_clipboard_not_from_app");
                    return;
                }

                // Launch dialog to ask user which data types to import.
                BaseDialogFragment importClipboard_selectDataTypesDialogFragment = BaseDialogFragment.newInstance(SelectDataTypesDialog.class, dataTypes);
                importClipboard_selectDataTypesDialogFragment.setOnDismissListener(importClipboard_selectDataTypesDialogFragmentListener);
                importClipboard_selectDataTypesDialogFragment.show(DataManagementActivity.this, "select_import_clipboard");
            }
        });

        BaseDialogFragment importClipboard_selectDataTypesDialogFragment2 = (BaseDialogFragment) this.getSupportFragmentManager().findFragmentByTag("select_import_clipboard");
        if (importClipboard_selectDataTypesDialogFragment2 != null) {
            importClipboard_selectDataTypesDialogFragment2.setOnDismissListener(importClipboard_selectDataTypesDialogFragmentListener);
        }
    }

    @Override
    public void onSaveInstanceStateImpl(@NonNull Bundle bundle) {
        bundle.putString("fileName", fileName);
        bundle.putString("folderName", folderName);
        bundle.putString("fileText", fileText);
        bundle.putString("clipboardText", clipboardText);
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            fileName = bundle.getString("fileName");
            folderName = bundle.getString("folderName");
            fileText = bundle.getString("fileText");
            clipboardText = bundle.getString("clipboardText");
        }
    }
}