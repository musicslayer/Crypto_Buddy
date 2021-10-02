package com.musicslayer.cryptobuddy.view.table;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.dialog.BaseDialog;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.filter.Filter;
import com.musicslayer.cryptobuddy.util.Serialization;
import com.musicslayer.cryptobuddy.util.Toast;

import java.util.ArrayList;

abstract public class Table extends TableLayout {
    abstract public BaseRow getRow(Context context, Transaction transaction);

    Context context;

    // Number of rows before the user input.
    final int numHeaderRows = 3;

    // Number of columns
    int numColumns = 0;

    // Type of each column
    ArrayList<String> columnTypes = new ArrayList<>();

    // Header of each column
    ArrayList<String> columnHeaders = new ArrayList<>();

    // Transactions represented by each row.
    public ArrayList<Transaction> transactionArrayList = new ArrayList<>();
    public ArrayList<Transaction> maskedTransactionArrayList = new ArrayList<>();

    // Filters
    ArrayList<Filter> filterArrayList = new ArrayList<>();

    // Currently sorted column.
    int sortingColumn;

    // 0 is descending, 1 no sorting, 2 is ascending.
    ArrayList<Integer> sortState = new ArrayList<>();
    final int[] sortIcons = new int[]{R.drawable.ic_baseline_arrow_drop_down_24, R.drawable.ic_baseline_arrow_drop_line_24, R.drawable.ic_baseline_arrow_drop_up_24};

    public TablePageView pageView;

    public Table(Context context) {
        this(context, null);
    }

    public Table(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.context = context;

        this.setOrientation(LinearLayout.VERTICAL);
        this.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        this.addView(new BaseFilterRow(context));
        this.addView(new BaseSortRow(context));
        this.addView(new BaseHeaderRow(context));
    }

    public void addColumn(String columnType, String columnHeader, String filterType, int initialSortState) {
        numColumns++;
        this.columnTypes.add(columnType);
        this.columnHeaders.add(columnHeader);
        this.filterArrayList.add(Filter.fromType(filterType, columnType));
        this.sortState.add(initialSortState);

        // Sort by the most recent column if it has a sorting state for "Descending" or "Ascending".
        if(initialSortState != 1) {
            sortingColumn = numColumns - 1;
            doSort();
        }

        redrawHeaderRows();
    }

    public void addRows(ArrayList<Transaction> transactionArrayList) {
        // Add all rows, and then do sorting and filtering.
        addRowsImpl(transactionArrayList);
        finishRows();
    }

    public void addRow(Transaction transaction) {
        addRowImpl(transaction);
        finishRows();

        if(transaction.isFiltered(filterArrayList, columnTypes)) {
            Toast.showToast("new_transaction_filtered");
        }
    }

    public void addRowsImpl(ArrayList<Transaction> transactionArrayList) {
        int minIdx = pageView == null ? 0 : pageView.getMinIdx();
        int maxIdx = pageView == null ? TablePageView.numItemsPerPage - 1 : pageView.getMaxIdx();

        for(int i = 0; i < transactionArrayList.size(); i++) {
            // Only draw current page of rows. If null, assume it's the first page.
            if(i >= minIdx && i <= maxIdx) {
                drawRow(transactionArrayList.get(i));
            }
            this.transactionArrayList.add(transactionArrayList.get(i));
            maskedTransactionArrayList.add(transactionArrayList.get(i));
        }
    }

    public void addRowImpl(Transaction transaction) {
        drawRow(transaction);
        this.transactionArrayList.add(transaction);
        maskedTransactionArrayList.add(transaction);
    }

    // Anything that adds rows should call this to finish the process.
    // These are expensive operations that should only be done once all new rows are added to the table.
    public void finishRows() {
        doSort();
        updateFilters();
        filterTable();

        pageView.setNumItems(maskedTransactionArrayList.size());
    }

    public void drawRow(Transaction transaction) {
        this.addView(getRow(context, transaction));
    }

    class BaseFilterRow extends TableRow {
        public BaseFilterRow(Context context) {
            super(context);
            setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
            this.makeRow(context);
        }

        public void makeRow(Context context) {
            this.setOrientation(LinearLayout.HORIZONTAL);
            this.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));

            LinearLayout[] l = new LinearLayout[numColumns];

            AppCompatButton[] b = new AppCompatButton[numColumns];
            TextView[] t = new TextView[numColumns];

            for(int i = 0; i < numColumns; i++) {
                l[i] = new LinearLayout(context);
                l[i].setOrientation(LinearLayout.VERTICAL);
                l[i].setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

                b[i] = new AppCompatButton(context);
                b[i].setText("Filter");
                b[i].setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_filter_list_24, 0, 0, 0);

                t[i] = new TextView(context);
                t[i].setBackgroundResource(R.drawable.border);

                l[i].addView(b[i]);
                l[i].addView(t[i]);

                Filter f = filterArrayList.get(i);
                if(f == null) {
                    l[i].setVisibility(INVISIBLE);
                }
                else {
                    final int ii = i;

                    t[i].setText(filterArrayList.get(i).getIncludedString());

                    BaseDialogFragment filterDialogFragment = f.getGenericDialogFragment();
                    filterDialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            if(((BaseDialog)dialog).isComplete) {
                                filterTable();
                                t[ii].setText(filterArrayList.get(ii).getIncludedString());
                                redrawHeaderRows();
                            }
                        }
                    });
                    filterDialogFragment.restoreListeners(context, "filter" + ii);

                    b[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            filterDialogFragment.show(context, "filter" + ii);
                        }
                    });
                }

                this.addView(l[i]);
            }
        }
    }

    class BaseSortRow extends TableRow {
        public BaseSortRow(Context context) {
            super(context);
            setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
            this.makeRow(context);
        }

        public void makeRow(Context context) {
            AppCompatButton[] b = new AppCompatButton[numColumns];

            for(int i = 0; i < numColumns; i++) {
                final int ii = i;

                b[i] = new AppCompatButton(context);
                b[i].setText("Sort");
                b[i].setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_sort_24, 0, sortIcons[sortState.get(i)], 0);

                b[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sortingColumn = ii;

                        for(int j = 0; j < numColumns; j++) {
                            if(j != ii) {
                                b[j].setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_sort_24, 0, R.drawable.ic_baseline_arrow_drop_line_24, 0);
                                sortState.set(j, 1);
                            }
                            else {
                                if(sortState.get(j) == 0) {
                                    b[j].setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_sort_24, 0, R.drawable.ic_baseline_arrow_drop_up_24, 0);
                                    sortState.set(j, 2);
                                }
                                else {
                                    b[j].setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_sort_24, 0, R.drawable.ic_baseline_arrow_drop_down_24, 0);
                                    sortState.set(j, 0);
                                }
                                doSort();
                            }
                        }
                    }
                });

                this.addView(b[i]);
            }
        }
    }

    class BaseHeaderRow extends TableRow {
        public BaseHeaderRow(Context context) {
            super(context);
            setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
            this.makeRow(context);
        }

        public void makeRow(Context context) {
            for(String h : columnHeaders) {
                TextView t = new TextView(context);
                t.setText(h);
                t.setBackgroundResource(R.drawable.border);
                this.addView(t);
            }
        }
    }

    abstract static class BaseRow extends TableRow {
        abstract public void makeRow(Context context, Transaction transaction);

        public BaseRow(Context context) {
            super(context);
        }

        public BaseRow(Context context, Transaction transaction) {
            super(context);
            setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
            this.makeRow(context, transaction);
        }
    }

    public void updateFilters() {
        for(int i = 0; i < filterArrayList.size(); i++) {
            if(filterArrayList.get(i) != null) {
                filterArrayList.get(i).updateFilterData(Transaction.getFilterDataForType(transactionArrayList, columnTypes.get(i)));
            }
        }
    }

    public void filterTable() {
        ArrayList<Transaction> newTransactionArrayList = new ArrayList<>();
        for(Transaction t : maskedTransactionArrayList) {
            if(!t.isFiltered(filterArrayList, columnTypes)) {
                newTransactionArrayList.add(t);
            }
        }

        if(pageView != null) {
            pageView.setNumItems(newTransactionArrayList.size());
        }

        redrawRows(newTransactionArrayList);
    }

    public ArrayList<Transaction> getFilteredMaskedTransactionArrayList() {
        ArrayList<Transaction> newTransactionArrayList = new ArrayList<>();
        for(Transaction t : maskedTransactionArrayList) {
            if(!t.isFiltered(filterArrayList, columnTypes)) {
                newTransactionArrayList.add(t);
            }
        }
        return newTransactionArrayList;
    }

    public void doSort() {
        if(sortState.get(sortingColumn) == 0) {
            Transaction.sortDescendingByType(maskedTransactionArrayList, columnTypes.get(sortingColumn));
        }
        else if(sortState.get(sortingColumn) == 2) {
            Transaction.sortAscendingByType(maskedTransactionArrayList, columnTypes.get(sortingColumn));
        }

        filterTable();
    }

    public void resetTable() {
        ViewGroup group = this;
        group.removeViews(numHeaderRows, group.getChildCount() - numHeaderRows);

        transactionArrayList = new ArrayList<>();
        maskedTransactionArrayList = new ArrayList<>();
        this.updateFilters();
        pageView.setNumItems(0);
    }

    public void redrawHeaderRows() {
        ViewGroup group = this;
        group.removeViews(0, numHeaderRows);

        this.addView(new BaseFilterRow(context), 0);
        this.addView(new BaseSortRow(context), 1);
        this.addView(new BaseHeaderRow(context), 2);
    }

    public void redrawRows() {
        redrawRows(transactionArrayList);
    }

    public void redrawRows(ArrayList<Transaction> newTransactionArrayList) {
        ViewGroup group = this;
        group.removeViews(numHeaderRows, group.getChildCount() - numHeaderRows);

        // Only draw current page of rows. If null, assume it's the first page.
        int minIdx = pageView == null ? 0 : pageView.getMinIdx();
        int maxIdx = pageView == null ? TablePageView.numItemsPerPage - 1 : pageView.getMaxIdx();
        for(int i = 0; i < newTransactionArrayList.size(); i++) {
            if(i >= minIdx && i <= maxIdx) {
                drawRow(newTransactionArrayList.get(i));
            }
        }
    }

    @Override
    public Parcelable onSaveInstanceState()
    {
        // Save dynamically added rows.
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState());
        bundle.putParcelable("pageView", pageView.onSaveInstanceState());

        bundle.putInt("sortingColumn", sortingColumn);
        bundle.putString("transactions", Serialization.serializeArrayList(transactionArrayList));
        bundle.putString("masked_transactions", Serialization.serializeArrayList(maskedTransactionArrayList));
        bundle.putSerializable("filters", filterArrayList);
        bundle.putIntegerArrayList("sortState", sortState);

        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state)
    {
        // Load dynamically added rows.
        if (state instanceof Bundle) // implicit null check
        {
            Bundle bundle = (Bundle) state;
            state = bundle.getParcelable("superState");

            sortingColumn = bundle.getInt("sortingColumn");
            filterArrayList = (ArrayList<Filter>) bundle.getSerializable("filters");
            transactionArrayList = Serialization.deserializeArrayList(bundle.getString("transactions"), Transaction.class);
            maskedTransactionArrayList = Serialization.deserializeArrayList(bundle.getString("masked_transactions"), Transaction.class);
            sortState = bundle.getIntegerArrayList("sortState");

            pageView.onRestoreInstanceState(bundle.getParcelable("pageView"));
            pageView.setNumItems(maskedTransactionArrayList.size());

            // Remove and add the filter and sort row
            ViewGroup group = this;
            group.removeViews(0, 2);
            group.addView(new BaseFilterRow(context), 0);
            group.addView(new BaseSortRow(context), 1);

            filterTable();
        }
        super.onRestoreInstanceState(state);
    }

    public static class TablePageView extends LinearLayout {
        public Table inner_table;
        public int currentPage = 1;
        public int lastPage;
        public int numItems;
        public static final int numItemsPerPage = 100;

        public FloatingActionButton fab_first;
        public FloatingActionButton fab_left;
        public FloatingActionButton fab_right;
        public FloatingActionButton fab_last;
        public TextView pageTextView;

        public TablePageView(Context context) {
            this(context, null);
        }

        public TablePageView(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            makeLayout(context);
        }

        public void setTable(Table inner_table) {
            this.inner_table = inner_table;
        }

        public void setNumItems(int numItems) {
            int oldNumItems = this.numItems;
            this.numItems = numItems;

            this.lastPage = ((numItems - 1) / numItemsPerPage) + 1;

            // If the number of items changed (i.e. due to a filter), just go back to first page.
            if(oldNumItems != numItems) {
                currentPage = 1;
            }

            updateLayout();
        }

        public int getMinIdx() {
            return (currentPage - 1) * numItemsPerPage;
        }

        public int getMaxIdx() {
            return (currentPage * numItemsPerPage) - 1;
        }

        public void makeLayout(Context context) {
            fab_first = new FloatingActionButton(context);
            fab_first.setImageResource(R.drawable.ic_baseline_first_page_24);
            fab_first.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(currentPage != 1) {
                        currentPage = 1;
                        updateLayout();
                        inner_table.filterTable();
                    }
                }
            });

            fab_left = new FloatingActionButton(context);
            fab_left.setImageResource(R.drawable.ic_baseline_chevron_left_24);
            fab_left.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(currentPage > 1) {
                        currentPage--;
                        updateLayout();
                        inner_table.filterTable();
                    }
                }
            });

            pageTextView = new TextView(context);
            pageTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            this.setGravity(Gravity.CENTER_VERTICAL);

            fab_right = new FloatingActionButton(context);
            fab_right.setImageResource(R.drawable.ic_baseline_chevron_right_24);
            fab_right.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(currentPage < lastPage) {
                        currentPage++;
                        updateLayout();
                        inner_table.filterTable();
                    }
                }
            });

            fab_last = new FloatingActionButton(context);
            fab_last.setImageResource(R.drawable.ic_baseline_last_page_24);
            fab_last.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(currentPage != lastPage) {
                        currentPage = lastPage;
                        updateLayout();
                        inner_table.filterTable();
                    }
                }
            });

            this.addView(fab_first);
            this.addView(fab_left);
            this.addView(pageTextView);
            this.addView(fab_right);
            this.addView(fab_last);

            updateLayout();
        }

        public void updateLayout() {
            pageTextView.setText(currentPage + "/" + lastPage);

            // Artificially make the text width match the floating action button width.
            pageTextView.setWidth(158);

            /*
            // Formula
            final int width = res.getConfiguration().screenWidthDp;
            final int height = res.getConfiguration().screenHeightDp;
            return Math.max(width, height) < 470 ? 113 : 158;
             */

            // Hide this view if there is only 1 page.
            if(lastPage < 2) {
                this.setVisibility(GONE);
            }
            else {
                this.setVisibility(VISIBLE);
            }
        }

        @Override
        public Parcelable onSaveInstanceState()
        {
            Bundle bundle = new Bundle();
            bundle.putParcelable("superState", super.onSaveInstanceState());
            bundle.putInt("currentPage", currentPage);
            bundle.putInt("lastPage", lastPage);
            bundle.putInt("numItems", numItems);

            return bundle;
        }

        @Override
        public void onRestoreInstanceState(Parcelable state)
        {
            if (state instanceof Bundle) // implicit null check
            {
                Bundle bundle = (Bundle) state;
                state = bundle.getParcelable("superState");
                currentPage = bundle.getInt("currentPage");
                lastPage = bundle.getInt("lastPage");
                numItems = bundle.getInt("numItems");

                updateLayout();
            }
            super.onRestoreInstanceState(state);
        }
    }
}