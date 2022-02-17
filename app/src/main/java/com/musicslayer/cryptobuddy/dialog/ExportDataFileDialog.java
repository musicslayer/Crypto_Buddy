package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.documentfile.provider.DocumentFile;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.rich.RichStringBuilder;
import com.musicslayer.cryptobuddy.util.FileUtil;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.red.FileEditText;

import java.io.File;
import java.util.ArrayList;

public class ExportDataFileDialog extends BaseDialog {
    public final static String DATA_FOLDER = "exports";

    String dataFolder;
    Uri uri;
    boolean isURI;

    ArrayList<String> existingFileNames = new ArrayList<>();

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

        BaseDialogFragment chooseFolderDialogFragment = BaseDialogFragment.newInstance(ChooseFolderDialog.class);
        chooseFolderDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ChooseFolderDialog)dialog).isComplete) {
                    dataFolder = ((ChooseFolderDialog)dialog).user_FOLDERNAME + File.separatorChar;
                    uri = ((ChooseFolderDialog)dialog).user_URI;
                    isURI = ((ChooseFolderDialog)dialog).user_ISURI;

                    if(!isURI) {
                        dataFolder = dataFolder + DATA_FOLDER + File.separatorChar;
                    }

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
                    if(isURI) {
                        DocumentFile documentFolder = DocumentFile.fromTreeUri(activity, uri);
                        DocumentFile oldDocumentFile = documentFolder.findFile(fileName);
                        exists = oldDocumentFile != null;
                    }
                    else {
                        exists = FileUtil.exists(dataFolder, fileName);
                    }

                    if(exists) {
                        confirmFileOverwriteDialogFragment.show(activity, "overwrite");
                        return;
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

            ArrayList<File> existingFiles;
            if(isURI) {
                existingFiles = new ArrayList<>();

                DocumentFile[] documentFiles = DocumentFile.fromTreeUri(activity, uri).listFiles();
                for(DocumentFile documentFile : documentFiles) {
                    existingFiles.add(new File(documentFile.getName()));
                }
            }
            else {
                existingFiles = FileUtil.getFiles(dataFolder);
            }

            if(existingFiles == null) {
                String redText = RichStringBuilder.redText("Problem accessing existing files.");
                T.setText(Html.fromHtml(redText));
            }
            else if(existingFiles.isEmpty()) {
                T.setText("No existing files.");
            }
            else {
                StringBuilder s = new StringBuilder();
                s.append("Existing files:");
                for(File existingFile : existingFiles) {
                    existingFileNames.add(existingFile.getName());
                    s.append("\n").append(existingFile.getName());
                }

                T.setText(s.toString());
            }
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
