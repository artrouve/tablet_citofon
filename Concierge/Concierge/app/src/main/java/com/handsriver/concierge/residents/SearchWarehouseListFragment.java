package com.handsriver.concierge.residents;

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
import android.widget.FilterQueryProvider;
import android.widget.ListView;

import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeContract.WarehouseEntry;
import com.handsriver.concierge.database.ConciergeContract.ApartmentEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.SelectToDBRaw;
import com.handsriver.concierge.utilities.Utility;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by Created by alain_r._trouve_silva after 13-04-17.
 */

public class SearchWarehouseListFragment extends Fragment {
    View rootView;
    WarehouseAdapterSearchList warehouseAdapter;
    ListView viewWarehouse;
    SearchView searchWarehouse;
    ArrayList<Warehouse> mWarehouses;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConciergeDbHelper helper= new ConciergeDbHelper(getContext());
        DatabaseManager.initializeInstance(helper);
        mWarehouses = new ArrayList<Warehouse>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {



        final String query = "SELECT " + WarehouseEntry.TABLE_NAME + "." + WarehouseEntry._ID + " AS " + WarehouseEntry._ID + "," +
                WarehouseEntry.TABLE_NAME + "." + WarehouseEntry.COLUMN_WAREHOUSE_NUMBER + " AS " + WarehouseEntry.COLUMN_WAREHOUSE_NUMBER + "," +
                ApartmentEntry.TABLE_NAME + "." + ApartmentEntry.COLUMN_APARTMENT_NUMBER + " AS " + ApartmentEntry.COLUMN_APARTMENT_NUMBER +
                " FROM " + WarehouseEntry.TABLE_NAME + "," + ApartmentEntry.TABLE_NAME +
                " WHERE " + ApartmentEntry.TABLE_NAME + "." + ApartmentEntry.COLUMN_APARTMENT_ID_SERVER + " = " + WarehouseEntry.TABLE_NAME + "." + WarehouseEntry.COLUMN_APARTMENT_ID +
                " ORDER BY " + WarehouseEntry.TABLE_NAME + "." + WarehouseEntry.COLUMN_WAREHOUSE_NUMBER + " ASC";

        Cursor c;
        try {
            SelectToDBRaw selectParking = new SelectToDBRaw(query,null);
            c = selectParking.execute().get();
        }catch (Exception e){
            c = null;
        }

        if(c != null){
            while (c.moveToNext()){
                Warehouse warehouse = new Warehouse();
                warehouse.setWarehouseNumber(c.getString(c.getColumnIndex(WarehouseEntry.COLUMN_WAREHOUSE_NUMBER)));
                warehouse.setApartmentNumber(c.getString(c.getColumnIndex(ApartmentEntry.COLUMN_APARTMENT_NUMBER)));
                mWarehouses.add(warehouse);
            }
            c.close();
            warehouseAdapter = new WarehouseAdapterSearchList(getActivity(),mWarehouses);

        }

        rootView = inflater.inflate(R.layout.fragment_list_search_warehouses, container, false);

        viewWarehouse = (ListView) rootView.findViewById(R.id.listViewWarehouse);
        viewWarehouse.setAdapter(warehouseAdapter);

        searchWarehouse = (SearchView) rootView.findViewById(R.id.searchWarehouse);
        searchWarehouse.setIconified(true);

        searchWarehouse.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                warehouseAdapter.getFilter().filter(newText);
                return true;
            }
        });

        viewWarehouse.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Utility.hideKeyboard(v,getContext());
                return false;
            }
        });

        return rootView;
    }
}
