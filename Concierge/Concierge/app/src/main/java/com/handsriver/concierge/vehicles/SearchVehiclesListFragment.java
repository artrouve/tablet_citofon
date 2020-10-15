package com.handsriver.concierge.vehicles;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.handsriver.concierge.database.ConciergeContract.VehicleEntry;
import com.handsriver.concierge.database.ConciergeContract.ApartmentEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.SelectToDBRaw;
import com.handsriver.concierge.utilities.Utility;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by Created by alain_r._trouve_silva after 10-03-17.
 */

public class SearchVehiclesListFragment extends Fragment {

    View rootView;
    SearchVehiclesListFragment.Callback mCallback;
    VehicleAdapterSearchList vehiclesAdapter;
    ListView viewVehicles;
    SearchView searchVehicles;
    ArrayList<Vehicle> mVehicles;

    int hours;

    public interface Callback{
        public void onItemSelectedVehicle (Vehicle vehicle);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConciergeDbHelper helper= new ConciergeDbHelper(getContext());
        DatabaseManager.initializeInstance(helper);
        SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        hours = Integer.parseInt(settingsPrefs.getString(getString(R.string.pref_id_max_time_parking_key),"0"));
        mVehicles = new ArrayList<Vehicle>();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final String query = "SELECT " + VehicleEntry.TABLE_NAME + "." + VehicleEntry._ID + "," +
                VehicleEntry.COLUMN_LICENSE_PLATE + "," +
                VehicleEntry.COLUMN_ENTRY + "," +
                VehicleEntry.COLUMN_EXIT_DATE + "," +
                VehicleEntry.COLUMN_FINE_DATE + "," +
                VehicleEntry.COLUMN_PARKING_NUMBER + "," +
                ApartmentEntry.TABLE_NAME + "." + ApartmentEntry.COLUMN_APARTMENT_NUMBER +
                " FROM " + VehicleEntry.TABLE_NAME + "," + ApartmentEntry.TABLE_NAME +
                " WHERE " + ApartmentEntry.TABLE_NAME + "." + ApartmentEntry.COLUMN_APARTMENT_ID_SERVER + " = " + VehicleEntry.TABLE_NAME + "." + VehicleEntry.COLUMN_APARTMENT_ID +
                " ORDER BY " + VehicleEntry.COLUMN_ENTRY + " DESC";

        Cursor c;
        try {
            SelectToDBRaw selectVehicles = new SelectToDBRaw(query,null);
            c = selectVehicles.execute().get();
        }catch (Exception e){
            c = null;
        }

        if (c != null){
            while(c.moveToNext()){
                Vehicle vehicle = new Vehicle();
                vehicle.setId(c.getString(c.getColumnIndex(VehicleEntry._ID)));
                vehicle.setLicensePlate(c.getString(c.getColumnIndex(VehicleEntry.COLUMN_LICENSE_PLATE)));
                vehicle.setEntry(c.getString(c.getColumnIndex(VehicleEntry.COLUMN_ENTRY)));
                vehicle.setExitDate(c.getString(c.getColumnIndex(VehicleEntry.COLUMN_EXIT_DATE)));
                vehicle.setFineDate(c.getString(c.getColumnIndex(VehicleEntry.COLUMN_FINE_DATE)));
                vehicle.setParkingNumber(c.getString(c.getColumnIndex(VehicleEntry.COLUMN_PARKING_NUMBER)));
                vehicle.setApartmentNumber(c.getString(c.getColumnIndex(ApartmentEntry.COLUMN_APARTMENT_NUMBER)));
                mVehicles.add(vehicle);
            }
            c.close();
            vehiclesAdapter = new VehicleAdapterSearchList(getActivity(),mVehicles,hours);
        }


        rootView = inflater.inflate(R.layout.fragment_list_search_vehicles, container, false);

        viewVehicles = (ListView) rootView.findViewById(R.id.listViewVehicles);
        viewVehicles.setAdapter(vehiclesAdapter);

        searchVehicles = (SearchView) rootView.findViewById(R.id.searchVehicles);
        searchVehicles.setIconified(true);

        searchVehicles.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                vehiclesAdapter.getFilter().filter(newText);
                return true;
            }
        });

        viewVehicles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Vehicle vehicle = (Vehicle) parent.getItemAtPosition(position);

                if (vehicle != null)
                {
                    mCallback.onItemSelectedVehicle(vehicle);
                }

            }
        });

        viewVehicles.setOnTouchListener(new View.OnTouchListener() {
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
            mCallback = (SearchVehiclesListFragment.Callback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement Callback");
        }
    }
}

