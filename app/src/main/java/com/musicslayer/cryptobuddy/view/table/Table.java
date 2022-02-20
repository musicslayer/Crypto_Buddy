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
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashLinearLayout;
import com.musicslayer.cryptobuddy.crash.CrashTableLayout;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.dialog.ChoosePageDialog;
import com.musicslayer.cryptobuddy.dialog.FilterDialog;
import com.musicslayer.cryptobuddy.settings.setting.NumberTransactionsPerPageSetting;
import com.musicslayer.cryptobuddy.state.StateObj;
import com.musicslayer.cryptobuddy.transaction.AssetAmount;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.filter.Filter;
import com.musicslayer.cryptobuddy.data.bridge.Serialization;
import com.musicslayer.cryptobuddy.util.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;

abstract public class Table extends CrashTableLayout {
    abstract public BaseRow getRow(Transaction transaction);

    // Number of rows before the user input.
    final int numHeaderRows = 3;

    // Number of columns
    int numColumns = 0;

    // Type of each column
    ArrayList<String> columnTypes = new ArrayList<>();

    // Header of each column
    ArrayList<String> columnHeaders = new ArrayList<>();

    // Filters
    ArrayList<Filter> filterArrayList = new ArrayList<>();

    // Currently sorted column.
    int sortingColumn;

    // 0 is descending, 1 no sorting, 2 is ascending.
    // -1 means don't offer sorting.
    ArrayList<Integer> sortState = new ArrayList<>();
    final int[] sortIcons = new int[]{R.drawable.ic_baseline_arrow_drop_down_24, R.drawable.ic_baseline_arrow_drop_line_24, R.drawable.ic_baseline_arrow_drop_up_24};

    private Table.OnDeleteTransactionListener onDeleteTransactionListener;

    public TablePageView pageView;

    public Table(Context context) {
        this(context, null);
    }

    public Table(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

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
        this.filterArrayList.add(Filter.fromType(filterType));
        this.sortState.add(initialSortState);

        if(initialSortState != 1) {
            sortingColumn = numColumns - 1;
        }
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
            ToastUtil.showToast("new_transaction_filtered");
        }
    }

    public void addRowsImpl(ArrayList<Transaction> transactionArrayList) {
        for(int i = 0; i < transactionArrayList.size(); i++) {
            // Only draw current page of rows. If null, assume it's the first page.
            if(i >= pageView.getMinIdx() && i <= pageView.getMaxIdx()) {
                drawRow(transactionArrayList.get(i));
            }
            StateObj.transactionArrayList.add(transactionArrayList.get(i));
        }
    }

    public void addRowImpl(Transaction transaction) {
        drawRow(transaction);
        StateObj.transactionArrayList.add(transaction);
    }

    // Anything that adds rows should call this to finish the process.
    // These are expensive operations that should only be done once all new rows are added to the table.
    public void finishRows() {
        doSortImpl();
        updateFilters();
        filterTable();
    }

    public void drawRow(Transaction transaction) {
        this.addView(getRow(transaction));
    }

    class BaseFilterRow extends TableRow {
        public BaseFilterRow(Context context) {
            super(context);
            setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
            this.makeRow();
        }

        public void makeRow() {
            Context context = getContext();

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
                    filterDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
                        @Override
                        public void onDismissImpl(DialogInterface dialog) {
                            if(((FilterDialog)dialog).isComplete) {
                                // We need to update the filter because it is a different handle.
                                filterArrayList.set(ii, ((FilterDialog)dialog).filter);

                                filterTable();
                                t[ii].setText(filterArrayList.get(ii).getIncludedString());
                                redrawHeaderRows();
                            }
                        }
                    });
                    filterDialogFragment.restoreListeners(context, "filter" + ii);

                    b[i].setOnClickListener(new CrashView.CrashOnClickListener(context) {
                        @Override
                        public void onClickImpl(View view) {
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
            this.makeRow();
        }

        public void makeRow() {
            Context context = getContext();

            AppCompatButton[] b = new AppCompatButton[numColumns];

            for(int i = 0; i < numColumns; i++) {
                final int ii = i;

                b[i] = new AppCompatButton(context);
                b[i].setText("Sort");

                if(sortState.get(i) != -1) {
                    b[i].setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_sort_24, 0, sortIcons[sortState.get(i)], 0);
                    b[i].setOnClickListener(new CrashView.CrashOnClickListener(context) {
                        @Override
                        public void onClickImpl(View view) {
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
                }

                this.addView(b[i]);

                // Don't draw the button, but we still want it to take up space.
                if(sortState.get(i) == -1) {
                    b[i].setVisibility(INVISIBLE);
                }
            }
        }
    }

    class BaseHeaderRow extends TableRow {
        public BaseHeaderRow(Context context) {
            super(context);
            setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
            this.makeRow();
        }

        public void makeRow() {
            for(String h : columnHeaders) {
                TextView t = new TextView(getContext());
                t.setText(h);
                t.setBackgroundResource(R.drawable.border);
                this.addView(t);
            }
        }
    }

    abstract static class BaseRow extends TableRow {
        abstract public void makeRow(Transaction transaction);

        public BaseRow(Context context) {
            super(context);
        }

        public BaseRow(Context context, Transaction transaction) {
            super(context);
            setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
            this.makeRow(transaction);
        }
    }

    public void updateFilters() {
        for(int i = 0; i < filterArrayList.size(); i++) {
            if(filterArrayList.get(i) != null) {
                filterArrayList.get(i).updateFilterData(Transaction.getFilterDataForType(StateObj.transactionArrayList, columnTypes.get(i)));
            }
        }
    }

    public void filterTable() {
        ArrayList<Transaction> newTransactionArrayList = new ArrayList<>();
        for(Transaction t : StateObj.transactionArrayList) {
            if(!t.isFiltered(filterArrayList, columnTypes)) {
                newTransactionArrayList.add(t);
            }
        }

        // pageView may be null at this point when the Table is first inflated from XML.
        // Don't draw anything yet because we could be drawing extra things that aren't needed.
        if(pageView != null) {
            pageView.setNumItems(newTransactionArrayList.size());
            redrawRows(newTransactionArrayList);
        }
    }

    public ArrayList<Transaction> getFilteredTransactionArrayList() {
        ArrayList<Transaction> filteredTransactionArrayList = new ArrayList<>();
        for(Transaction t : StateObj.transactionArrayList) {
            if(!t.isFiltered(filterArrayList, columnTypes)) {
                filteredTransactionArrayList.add(t);
            }
        }
        return filteredTransactionArrayList;
    }

    public void doSortImpl() {
        // pageView may be null at this point when the Table is first inflated from XML.
        // Don't draw anything yet because we could be drawing extra things that aren't needed.
        if(pageView == null) {
            return;
        }

        if(sortState.get(sortingColumn) == 0) {
            Transaction.sortDescendingByType(StateObj.transactionArrayList, columnTypes.get(sortingColumn));
        }
        else if(sortState.get(sortingColumn) == 2) {
            Transaction.sortAscendingByType(StateObj.transactionArrayList, columnTypes.get(sortingColumn));
        }
    }

    public void doSort() {
        doSortImpl();
        filterTable();
    }

    public void resetTable() {
        ViewGroup group = this;
        group.removeViews(numHeaderRows, group.getChildCount() - numHeaderRows);

        StateObj.transactionArrayList = new ArrayList<>();
        this.updateFilters();
        pageView.setNumItems(0);
    }

    public void redrawHeaderRows() {
        ViewGroup group = this;
        group.removeViews(0, numHeaderRows);

        this.addView(new BaseFilterRow(getContext()), 0);
        this.addView(new BaseSortRow(getContext()), 1);
        this.addView(new BaseHeaderRow(getContext()), 2);
    }

    public void redrawRows(ArrayList<Transaction> newTransactionArrayList) {
        ViewGroup group = this;
        group.removeViews(numHeaderRows, group.getChildCount() - numHeaderRows);

        for(int i = 0; i < newTransactionArrayList.size(); i++) {
            if(i >= pageView.getMinIdx() && i <= pageView.getMaxIdx()) {
                drawRow(newTransactionArrayList.get(i));
            }
        }
    }

    public String getInfo() {
        // Get a String representation of this table's state.
        // Different than serialization because the info cannot be used to reconstruct the table.
        StringBuilder s = new StringBuilder();
        s.append("Table Info:").append("\n\n").append("Table Type: ").append(getClass().getSimpleName());

        // columnTypes
        s.append("\n\nColumn Types:");
        for(String column : columnTypes) {
            s.append("\n").append(column);
        }

        //columnHeaders
        s.append("\n\nColumn Headers:");
        for(String column : columnHeaders) {
            s.append("\n").append(column);
        }

        //transactionArrayList and Net Transaction Sums.
        ArrayList<Transaction> transactionArrayList = StateObj.transactionArrayList;
        s.append("\n\nTransaction Array List:\n");
        s.append(Serialization.serializeArrayList(transactionArrayList, Transaction.class));

        HashMap<Asset, AssetAmount> netTransactionsMap = Transaction.resolveAssets(transactionArrayList);
        s.append("\n\nNet Transaction Sums:");
        for(Asset asset : netTransactionsMap.keySet()) {
            AssetAmount assetAmount = netTransactionsMap.get(asset);
            AssetQuantity assetQuantity = new AssetQuantity(assetAmount, asset);
            s.append("\n    ").append(assetQuantity.toRawString());
        }

        //filteredTransactionArrayList and Net Filtered Transaction Sums.
        ArrayList<Transaction> filteredTransactionArrayList = getFilteredTransactionArrayList();
        s.append("\n\nFiltered Transaction Array List:\n");
        s.append(Serialization.serializeArrayList(filteredTransactionArrayList, Transaction.class));

        HashMap<Asset, AssetAmount> netFilteredTransactionsMap = Transaction.resolveAssets(filteredTransactionArrayList);
        s.append("\n\nNet Filtered Transaction Sums:");
        for(Asset asset : netFilteredTransactionsMap.keySet()) {
            AssetAmount assetAmount = netFilteredTransactionsMap.get(asset);
            AssetQuantity assetQuantity = new AssetQuantity(assetAmount, asset);
            s.append("\n    ").append(assetQuantity.toRawString());
        }

        //filterArrayList
        s.append("\n\nFilter Array List:\n");
        s.append(Serialization.serializeArrayList(filterArrayList, Filter.class));

        //sortingColumn
        s.append("\n\nSorting Column: ").append(sortingColumn);

        //sortState
        s.append("\n\nSort State: ").append(sortState);

        //pageView
        s.append("\n\n").append(pageView.getInfo());

        return s.toString();
    }

    @Override
    public Parcelable onSaveInstanceStateImpl(Parcelable state)
    {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", state);
        bundle.putParcelable("pageView", pageView.onSaveInstanceState());

        bundle.putInt("sortingColumn", sortingColumn);
        bundle.putParcelableArrayList("filters", filterArrayList);
        bundle.putIntegerArrayList("sortState", sortState);

        return bundle;
    }

    @Override
    public Parcelable onRestoreInstanceStateImpl(Parcelable state)
    {
        if (state instanceof Bundle) // implicit null check
        {
            Bundle bundle = (Bundle) state;
            state = bundle.getParcelable("superState");
            pageView.onRestoreInstanceState(bundle.getParcelable("pageView"));

            sortingColumn = bundle.getInt("sortingColumn");
            filterArrayList = bundle.getParcelableArrayList("filters");
            sortState = bundle.getIntegerArrayList("sortState");

            // Remove and add the filter and sort row
            Context context = getContext();

            ViewGroup group = this;
            group.removeViews(0, 2);
            group.addView(new BaseFilterRow(context), 0);
            group.addView(new BaseSortRow(context), 1);

            filterTable();
        }
        return state;
    }

    public void checkDeletion(Transaction transaction) {
        if(onDeleteTransactionListener != null) {
            onDeleteTransactionListener.onDeleteTransaction(this, transaction);
        }
    }

    public void setOnDeleteTransactionListener(Table.OnDeleteTransactionListener onDeleteTransactionListener) {
        this.onDeleteTransactionListener = onDeleteTransactionListener;
    }

    abstract public static class OnDeleteTransactionListener {
        abstract public void onDeleteTransaction(Table table, Transaction transaction);
    }

    public static class TablePageView extends CrashLinearLayout {
        public Table outer_table;
        BaseDialogFragment choosePageDialogFragment;
        public int currentPage = 1;
        public int lastPage;
        public int numItems;

        public FloatingActionButton fab_first;
        public FloatingActionButton fab_left;
        public FloatingActionButton fab_right;
        public FloatingActionButton fab_last;
        public PageTextView pageTextView;

        public TablePageView(Context context) {
            this(context, null);
        }

        public TablePageView(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            makeLayout();
        }

        public void setTable(Table inner_table) {
            this.outer_table = inner_table;
        }

        public int getNumberTransactionsPerPage() {
            return NumberTransactionsPerPageSetting.value;
        }

        public void setNumItems(int numItems) {
            this.numItems = numItems;

            int oldLastPage = this.lastPage;

            // Remember: If numItems = 0 then lastPage will be 0 instead of 1.
            this.lastPage = ((numItems - 1) / getNumberTransactionsPerPage()) + 1;

            // If the number of pages decreased, and we are past the new last page, then update our page to be the new last page.
            if(lastPage < oldLastPage) {
                currentPage = lastPage;
            }

            updateLayout();
        }

        public int getMinIdx() {
            return (currentPage - 1) * getNumberTransactionsPerPage();
        }

        public int getMaxIdx() {
            return (currentPage * getNumberTransactionsPerPage()) - 1;
        }

        public void makeLayout() {
            Context context = getContext();

            choosePageDialogFragment = BaseDialogFragment.newInstance(ChoosePageDialog.class, 1, lastPage);
            choosePageDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
                @Override
                public void onDismissImpl(DialogInterface dialog) {
                    if(((ChoosePageDialog)dialog).isComplete) {
                        currentPage = ((ChoosePageDialog)dialog).user_PAGE;
                        updateLayout();
                        outer_table.filterTable();
                    }
                }
            });
            choosePageDialogFragment.restoreListeners(context, "page");

            fab_first = new FloatingActionButton(context);
            fab_first.setImageResource(R.drawable.ic_baseline_first_page_24);
            fab_first.setOnClickListener(new CrashView.CrashOnClickListener(context) {
                @Override
                public void onClickImpl(View view) {
                    if(currentPage != 1) {
                        currentPage = 1;
                        updateLayout();
                        outer_table.filterTable();
                    }
                }
            });

            fab_left = new FloatingActionButton(context);
            fab_left.setImageResource(R.drawable.ic_baseline_chevron_left_24);
            fab_left.setOnClickListener(new CrashView.CrashOnClickListener(context) {
                @Override
                public void onClickImpl(View view) {
                    if(currentPage > 1) {
                        currentPage--;
                        updateLayout();
                        outer_table.filterTable();
                    }
                }
            });

            ContextThemeWrapper newContext = new ContextThemeWrapper(context, R.style.TextInverseTheme);
            pageTextView = new PageTextView(newContext);
            pageTextView.setGravity(Gravity.CENTER);
            pageTextView.setOnClickListener(new CrashView.CrashOnClickListener(context) {
                @Override
                public void onClickImpl(View view) {
                    choosePageDialogFragment.show(context, "page");
                }
            });

            fab_right = new FloatingActionButton(context);
            fab_right.setImageResource(R.drawable.ic_baseline_chevron_right_24);
            fab_right.setOnClickListener(new CrashView.CrashOnClickListener(context) {
                @Override
                public void onClickImpl(View view) {
                    if(currentPage < lastPage) {
                        currentPage++;
                        updateLayout();
                        outer_table.filterTable();
                    }
                }
            });

            fab_last = new FloatingActionButton(context);
            fab_last.setImageResource(R.drawable.ic_baseline_last_page_24);
            fab_last.setOnClickListener(new CrashView.CrashOnClickListener(context) {
                @Override
                public void onClickImpl(View view) {
                    if(currentPage != lastPage) {
                        currentPage = lastPage;
                        updateLayout();
                        outer_table.filterTable();
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
            pageTextView.setText(currentPage + "\n/" + lastPage);
            choosePageDialogFragment.updateArguments(ChoosePageDialog.class, 1, lastPage);

            // Hide this view if there is only 1 page.
            if(lastPage < 2) {
                this.setVisibility(GONE);
            }
            else {
                this.setVisibility(VISIBLE);
            }
        }

        public String getInfo() {
            // Get a String representation of this table's state.
            // Different than serialization because the info cannot be used to reconstruct the table.
            return "Table Page Info:" +
                    "\n--> Current Page: " + currentPage +
                    "\n--> Last Page: " + lastPage +
                    "\n--> Number of Items: " + numItems;
        }

        @Override
        public Parcelable onSaveInstanceStateImpl(Parcelable state)
        {
            Bundle bundle = new Bundle();
            bundle.putParcelable("superState", state);
            bundle.putInt("currentPage", currentPage);
            bundle.putInt("lastPage", lastPage);
            bundle.putInt("numItems", numItems);

            return bundle;
        }

        @Override
        public Parcelable onRestoreInstanceStateImpl(Parcelable state)
        {
            if (state instanceof Bundle) // implicit null check
            {
                Bundle bundle = (Bundle) state;
                state = bundle.getParcelable("superState");
                currentPage = bundle.getInt("currentPage");
                lastPage = bundle.getInt("lastPage");

                numItems = bundle.getInt("numItems");
                setNumItems(numItems); // Restores other state.

                updateLayout();
            }
            return state;
        }
    }
}