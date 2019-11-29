package com.handsriver.concierge.suppliers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.SearchView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.TextView;

import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeContract.SupplierEntry;
import com.handsriver.concierge.database.ConciergeContract.SupplierVisitsEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.SelectToDBRaw;
import com.handsriver.concierge.utilities.Utility;
import com.handsriver.concierge.vehicles.Vehicle;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by Created by alain_r._trouve_silva after 16-03-17.
 */

public class SearchSuppliersListFragment extends Fragment {
    View rootView;
    SearchSuppliersListFragment.Callback mCallback;
    private SupplierAdapterSearchList suppliersAdapter;
    private ListView viewSuppliers;
    SearchView searchSupplier;
    ArrayList<SupplierVisit> mVisit;


    public interface Callback{
        public void onItemSelectedSuppliers (SupplierVisit visit);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConciergeDbHelper helper= new ConciergeDbHelper(getContext());
        DatabaseManager.initializeInstance(helper);
        mVisit = new ArrayList<SupplierVisit>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final String query = "SELECT " + SupplierEntry.TABLE_NAME + "." + SupplierEntry._ID + "," +
                SupplierVisitsEntry.COLUMN_ENTRY  + "," +
                SupplierEntry.TABLE_NAME + "." + SupplierEntry.COLUMN_NAME_SUPPLIER + "," +
                SupplierVisitsEntry.COLUMN_GATEWAY_ID + "," +
                SupplierVisitsEntry.COLUMN_SUPPLIER_ID + "," +
                SupplierVisitsEntry.COLUMN_EXIT_SUPPLIER +
                " FROM " + SupplierVisitsEntry.TABLE_NAME + "," + SupplierEntry.TABLE_NAME +
                " WHERE " + SupplierVisitsEntry.TABLE_NAME + "." + SupplierVisitsEntry.COLUMN_SUPPLIER_ID + " = " + SupplierEntry.TABLE_NAME + "." + SupplierEntry.COLUMN_SUPPLIER_ID_SERVER +
                " GROUP BY " + SupplierVisitsEntry.COLUMN_ENTRY + "," + SupplierEntry.TABLE_NAME + "." + SupplierEntry.COLUMN_NAME_SUPPLIER + "," + SupplierVisitsEntry.COLUMN_GATEWAY_ID +
                " ORDER BY " + SupplierVisitsEntry.COLUMN_ENTRY + " DESC";

        Cursor c;
        try {
            SelectToDBRaw selectSuppliers = new SelectToDBRaw(query,null);
            c = selectSuppliers.execute().get();
        }catch (Exception e){
            c = null;
        }

        if (c != null) {
            while (c.moveToNext()){
                SupplierVisit visit = new SupplierVisit();
                visit.setEntrySupplier(c.getString(c.getColumnIndex(SupplierVisitsEntry.COLUMN_ENTRY)));
                visit.setNameSupplier(c.getString(c.getColumnIndex(SupplierEntry.COLUMN_NAME_SUPPLIER)));
                visit.setGatewayId(c.getString(c.getColumnIndex(SupplierVisitsEntry.COLUMN_GATEWAY_ID)));
                visit.setSupplierId(c.getString(c.getColumnIndex(SupplierVisitsEntry.COLUMN_SUPPLIER_ID)));
                visit.setExitSupplier(c.getString(c.getColumnIndex(SupplierVisitsEntry.COLUMN_EXIT_SUPPLIER)));
                mVisit.add(visit);
            }
            c.close();
            suppliersAdapter = new SupplierAdapterSearchList(getActivity(),mVisit);
        }


        rootView = inflater.inflate(R.layout.fragment_list_search_suppliers, container, false);

        viewSuppliers = (ListView) rootView.findViewById(R.id.listViewVisitsSuppliers);
        viewSuppliers.setAdapter(suppliersAdapter);

        searchSupplier = (SearchView) rootView.findViewById(R.id.searchSuppliers);
        searchSupplier.setIconified(true);

        searchSupplier.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                suppliersAdapter.getFilter().filter(newText);
                return true;
            }
        });

        viewSuppliers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SupplierVisit visit = (SupplierVisit) parent.getItemAtPosition(position);

                if (visit != null)
                {
                    mCallback.onItemSelectedSuppliers(visit);
                }

            }
        });

        viewSuppliers.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Utility.hideKeyboard(v,getContext());
                return false;
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (Callback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement Callback");
        }
    }
}
