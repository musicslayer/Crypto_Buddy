package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SearchView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.crash.CrashSearchView;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.state.SearchStateObj;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.view.SelectAndSearchView;

import java.util.ArrayList;
import java.util.Collections;

public class SearchDialog extends BaseDialog {
    public ArrayList<Asset> assetArrayList;
    public ArrayList<String> options_symbols_SORTED;
    public ArrayList<String> options_symbols_LC_SORTED;
    public ArrayList<String> options_names_SORTED;
    public ArrayList<String> options_names_LC_SORTED;
    public boolean isNames = false;
    public String searchText = "";
    TableLayout table;

    public Asset user_OPTION;

    //public SearchDialog(Activity activity, ArrayList<Asset> assetArrayList, ArrayList<String> options_symbols, ArrayList<String> options_names) {
    public SearchDialog(Activity activity) {
        super(activity);

        SearchStateObj searchStateObj = SelectAndSearchView.searchStateObj[0];

        this.assetArrayList = searchStateObj.assetArrayList;

        // Searches are case insensitive, so store lowercase options as well. Also sort everything for better UX.
        this.options_symbols_SORTED = searchStateObj.options_symbols;
        this.options_names_SORTED = searchStateObj.options_names;

        sortAscendingByType(options_symbols_SORTED);
        sortAscendingByType(options_names_SORTED);

        this.options_symbols_LC_SORTED = new ArrayList<>();
        for(String o : this.options_symbols_SORTED) {
            this.options_symbols_LC_SORTED.add(o.toLowerCase());
        }

        this.options_names_LC_SORTED = new ArrayList<>();
        for(String o : this.options_names_SORTED) {
            this.options_names_LC_SORTED.add(o.toLowerCase());
        }

        sortAscendingByType(options_symbols_LC_SORTED);
        sortAscendingByType(options_names_LC_SORTED);
    }

    public static void sortAscendingByType(ArrayList<String> optionsArrayList) {
        Collections.sort(optionsArrayList, (a, b) -> {
            // Sort by length so shorter options will be higher. If longer options are desired, the user can use more letters to filter more.
            // If length is equal, just go alphabetically.
            int s = Integer.compare(a.length(), b.length());
            if(s != 0) {
                return s;
            }
            else {
                return a.toLowerCase().compareTo(b.toLowerCase());
            }
        });
    }

    public int getBaseViewID() {
        return R.id.search_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_search);

        ImageButton helpButton = findViewById(R.id.search_dialog_helpButton);
        helpButton.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View view) {
                HelpUtil.showHelp(SearchDialog.this.activity, R.raw.help_search);
            }
        });

        table = findViewById(R.id.search_dialog_tableLayout);

        Button B_TOGGLE = findViewById(R.id.search_dialog_toggleButton);
        B_TOGGLE.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                isNames = !isNames;
                updateLayout();
                updateList();
            }
        });

        SearchView searchView = findViewById(R.id.search_dialog_searchView);
        searchView.setQueryHint("Start typing to show options.");
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(new CrashSearchView.CrashOnQueryTextListener(this.activity) {
            @Override
            public boolean onQueryTextSubmitImpl(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChangeImpl(String newText) {
                searchText = newText;
                updateList();
                return false;
            }
        });

        updateLayout();
        updateList();
    }

    public void updateLayout() {
        Button B_TOGGLE = findViewById(R.id.search_dialog_toggleButton);
        if(isNames) {
            B_TOGGLE.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_toggle_on_24, 0, 0, 0);
            B_TOGGLE.setText("Names");
        }
        else {
            B_TOGGLE.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_toggle_off_24, 0, 0, 0);
            B_TOGGLE.setText("Symbols");
        }
    }

    public void updateList() {
        table.removeAllViews();

        // Make sure we don't accidentally return the entire list of options here.
        if(searchText.trim().isEmpty()) { return; }

        ArrayList<String> options_SORTED;
        ArrayList<String> options_LC_SORTED;
        if(isNames) {
            options_SORTED = options_names_SORTED;
            options_LC_SORTED = options_names_LC_SORTED;
        }
        else {
            options_SORTED = options_symbols_SORTED;
            options_LC_SORTED = options_symbols_LC_SORTED;
        }

        // For performance reasons, we have to limit the number of results we show.
        int numResults = 0;
        boolean maxReached = false;

        String[] searchTextWords = searchText.trim().toLowerCase().split(" ");

        for(int oIDX = 0; oIDX < options_LC_SORTED.size(); oIDX++) {
            final int oIDX_F = oIDX;
            String oL = options_LC_SORTED.get(oIDX);

            boolean shouldInclude = true;
            for(String word : searchTextWords) {
                if(word.isEmpty()) { continue; }

                if(!oL.contains(word)) {
                    shouldInclude = false;
                    break;
                }
            }

            if(shouldInclude) {
                numResults++;

                AppCompatButton B = new AppCompatButton(this.activity);
                B.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                B.setText(options_SORTED.get(oIDX_F));
                B.setOnClickListener(new CrashView.CrashOnClickListener(SearchDialog.this.activity) {
                    public void onClickImpl(View v) {
                        // Sort assetArrayList in the same way as the options we used so that the index lines up.
                        if(isNames) {
                            Collections.sort(assetArrayList, (a, b) -> {
                                // Sort by length so shorter options will be higher. If longer options are desired, the user can use more letters to filter more.
                                // If length is equal, just go alphabetically.
                                int s = Integer.compare(a.getDisplayName().length(), b.getDisplayName().length());
                                if(s != 0) {
                                    return s;
                                }
                                else {
                                    return a.getDisplayName().toLowerCase().compareTo(b.getDisplayName().toLowerCase());
                                }
                            });
                        }
                        else {
                            Collections.sort(assetArrayList, (a, b) -> {
                                // Sort by length so shorter options will be higher. If longer options are desired, the user can use more letters to filter more.
                                // If length is equal, just go alphabetically.
                                int s = Integer.compare(a.getName().length(), b.getName().length());
                                if(s != 0) {
                                    return s;
                                }
                                else {
                                    return a.getName().toLowerCase().compareTo(b.getName().toLowerCase());
                                }
                            });
                        }

                        user_OPTION = assetArrayList.get(oIDX_F);

                        isComplete = true;
                        dismiss();
                    }
                });

                TableRow TR = new TableRow(this.activity);
                TableRow.LayoutParams TRP = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);

                TR.addView(B, TRP);
                table.addView(TR);

                if(numResults == 100) {
                    maxReached = true;
                }
            }

            if(maxReached) {
                // Add something to the table indicating there would have been more results, and then stop looking.
                TableRow TR = new TableRow(this.activity);
                TableRow.LayoutParams TRP = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);

                TextView T = new TextView(this.activity);
                T.setText("(100 Results Max)");

                TR.addView(T, TRP);
                table.addView(TR);

                break;
            }
        }
    }

    @Override
    public Bundle onSaveInstanceStateImpl(Bundle bundle) {
        bundle.putBoolean("isNames", isNames);
        return bundle;
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            isNames = bundle.getBoolean("isNames");
        }
    }
}
