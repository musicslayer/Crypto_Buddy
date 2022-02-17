package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.documentfile.provider.DocumentFile;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.util.FileUtil;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.red.FileEditText;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ExportDataFileDialog extends BaseDialog {
    public final static String DATA_FOLDER = "exports";

    String dataFolder;
    Uri uri;
    boolean isURI;

    public String user_FILENAME;
    public String user_FOLDERNAME;
    public Uri user_URI;
    public boolean user_ISURI;

    public ExportDataFileDialog(Activity activity) {
        super(activity);
    }

    public int getBaseViewID() {
        return R.id.export_data_file_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_export_data_file);

        ImageButton helpButton = findViewById(R.id.export_data_file_dialog_helpButton);
        helpButton.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View view) {
                HelpUtil.showHelp(ExportDataFileDialog.this.activity, R.raw.help_export_data);
            }
        });

        BaseDialogFragment chooseFolderDialogFragment = BaseDialogFragment.newInstance(ChooseFolderDialog.class, DATA_FOLDER);
        chooseFolderDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ChooseFolderDialog)dialog).isComplete) {
                    dataFolder = ((ChooseFolderDialog)dialog).user_FOLDERNAME;
                    uri = ((ChooseFolderDialog)dialog).user_URI;
                    isURI = ((ChooseFolderDialog)dialog).user_ISURI;

                    updateLayout();
                }
            }
        });
        chooseFolderDialogFragment.restoreListeners(activity, "choose_folder");

        Button B_CHOOSE = findViewById(R.id.export_data_file_dialog_chooseFolderButton);
        B_CHOOSE.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                chooseFolderDialogFragment.show(activity, "choose_folder");
            }
        });

        updateLayout();
    }

    public void updateLayout() {
        TextView T_FOLDER = findViewById(R.id.export_data_file_dialog_dataFolderTextView);
        if(dataFolder == null) {
            T_FOLDER.setText("No data folder selected.");
        }
        else {
            T_FOLDER.setText("Data Folder:\n" + dataFolder);
        }

        final FileEditText E_FILE = findViewById(R.id.export_data_file_dialog_fileEditText);

        BaseDialogFragment confirmFileOverwriteDialogFragment = BaseDialogFragment.newInstance(ConfirmFileOverwriteDialog.class);
        confirmFileOverwriteDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmFileOverwriteDialog)dialog).isComplete) {
                    user_FILENAME = E_FILE.getTextString();
                    user_FOLDERNAME = dataFolder;
                    user_URI = uri;
                    user_ISURI = isURI;

                    isComplete = true;
                    dismiss();
                }
            }
        });
        confirmFileOverwriteDialogFragment.restoreListeners(activity, "overwrite");

        Button B_CONFIRM = findViewById(R.id.export_data_file_dialog_confirmButton);
        B_CONFIRM.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                boolean isValid = E_FILE.test();

                if(dataFolder == null) {
                    ToastUtil.showToast(activity,"no_folder_selected");
                }
                else if(!isValid) {
                    ToastUtil.showToast(activity,"must_fill_inputs");
                }
                else {
                    String fileName = E_FILE.getTextString();

                    boolean exists;
                    boolean isFile;
                    if(isURI) {
                        DocumentFile documentFolder = DocumentFile.fromTreeUri(activity, uri);
                        DocumentFile oldDocumentFile = documentFolder.findFile(fileName);

                        exists = oldDocumentFile != null;
                        isFile = exists && oldDocumentFile.isFile();
                    }
                    else {
                        exists = FileUtil.exists(dataFolder, fileName);
                        isFile = exists && FileUtil.isFile(dataFolder, fileName);
                    }

                    if(exists) {
                        if(isFile) {
                            confirmFileOverwriteDialogFragment.show(activity, "overwrite");
                            return;
                        }
                        else {
                            ToastUtil.showToast(activity,"cannot_overwrite_folder");
                            return;
                        }
                    }

                    user_FILENAME = fileName;
                    user_FOLDERNAME = dataFolder;
                    user_URI = uri;
                    user_ISURI = isURI;

                    isComplete = true;
                    dismiss();
                }
            }
        });

        TextView T = findViewById(R.id.export_data_file_dialog_existingFilesTextView);
        if(dataFolder == null) {
            T.setVisibility(View.GONE);
        }
        else {
            T.setVisibility(View.VISIBLE);

            // These entries could be files or folders.
            ArrayList<File> existingFiles;
            ArrayList<File> existingFolders;
            if(isURI) {
                existingFiles = new ArrayList<>();
                existingFolders = new ArrayList<>();

                DocumentFile[] documentFiles = DocumentFile.fromTreeUri(activity, uri).listFiles();
                for(DocumentFile documentFile : documentFiles) {
                    if(documentFile.isFile()) {
                        existingFiles.add(new File(documentFile.getName()));
                    }
                    else {
                        existingFolders.add(new File(documentFile.getName()));
                    }
                }
            }
            else {
                existingFiles = FileUtil.getFiles(dataFolder);
                existingFolders = FileUtil.getFolders(dataFolder);
            }

            StringBuilder s = new StringBuilder();
            if(existingFolders == null) {
                s.append("(Problem accessing existing folders)");
            }
            else if(existingFolders.isEmpty()) {
                s.append("(No existing folders)");
            }
            else {
                s.append("Existing folders:");

                ArrayList<String> existingFolderNames = new ArrayList<>();
                for(File existingFolder : existingFolders) {
                    existingFolderNames.add(existingFolder.getName());
                }

                Collections.sort(existingFolderNames, Comparator.comparing(String::toLowerCase));

                for(String existingFolderName : existingFolderNames) {
                    s.append("\n").append(existingFolderName);
                }
            }

            s.append("\n\n");

            if(existingFiles == null) {
                s.append("(Problem accessing existing files)");
            }
            else if(existingFiles.isEmpty()) {
                s.append("(No existing files)");
            }
            else {
                s.append("Existing files:");

                ArrayList<String> existingFileNames = new ArrayList<>();
                for(File existingFile : existingFiles) {
                    existingFileNames.add(existingFile.getName());
                }

                Collections.sort(existingFileNames, Comparator.comparing(String::toLowerCase));

                for(String existingFileName : existingFileNames) {
                    s.append("\n").append(existingFileName);
                }
            }

            T.setText(s.toString());
        }
    }

    @Override
    public Bundle onSaveInstanceStateImpl(Bundle bundle) {
        bundle.putString("dataFolder", dataFolder);
        return bundle;
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            dataFolder = bundle.getString("dataFolder");
        }
    }
}
