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
import com.handsriver.concierge.database.ConciergeContract.ParkingEntry;
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

public class SearchParkingListFragment extends Fragment {
    View rootView;
    ParkingAdapterSearchList parkingAdapter;
    ListView viewParking;
    SearchView searchParking;
    ArrayList<Parking> mParking;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConciergeDbHelper helper= new ConciergeDbHelper(getContext());
        DatabaseManager.initializeInstance(helper);
        mParking = new ArrayList<Parking>();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final String query = "SELECT " + ParkingEntry.TABLE_NAME + "." + ParkingEntry._ID + " AS " + ParkingEntry._ID + "," +
                ParkingEntry.TABLE_NAME + "." + ParkingEntry.COLUMN_PARKING_NUMBER + " AS " + ParkingEntry.COLUMN_PARKING_NUMBER + "," +
                ApartmentEntry.TABLE_NAME + "." + ApartmentEntry.COLUMN_APARTMENT_NUMBER + " AS " + ApartmentEntry.COLUMN_APARTMENT_NUMBER +
                " FROM " + ParkingEntry.TABLE_NAME + "," + ApartmentEntry.TABLE_NAME +
                " WHERE " + ApartmentEntry.TABLE_NAME + "." + ApartmentEntry.COLUMN_APARTMENT_ID_SERVER + " = " + ParkingEntry.TABLE_NAME + "." + ParkingEntry.COLUMN_APARTMENT_ID +
                " ORDER BY " + ParkingEntry.TABLE_NAME + "." + ParkingEntry.COLUMN_PARKING_NUMBER + " ASC";

        Cursor c;
        try {
            SelectToDBRaw selectParking = new SelectToDBRaw(query,null);
            c = selectParking.execute().get();
        }catch (Exception e){
            c = null;
        }

        if (c != null) {
            while (c.moveToNext()){
                Parking parking = new Parking();
                parking.setParkingNumber(c.getString(c.getColumnIndex(ParkingEntry.COLUMN_PARKING_NUMBER)));
                parking.setApartmentNumber(c.getString(c.getColumnIndex(ApartmentEntry.COLUMN_APARTMENT_NUMBER)));
                mParking.add(parking);
            }
            c.close();
            parkingAdapter = new ParkingAdapterSearchList(getActivity(),mParking);
        }

        rootView = inflater.inflate(R.layout.fragment_list_search_parking, container, false);

        viewParking = (ListView) rootView.findViewById(R.id.listViewParking);
        viewParking.setAdapter(parkingAdapter);

        searchParking = (SearchView) rootView.findViewById(R.id.searchParking);
        searchParking.setIconified(true);

        searchParking.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                parkingAdapter.getFilter().filter(newText);
                return true;
            }
        });

        viewParking.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Utility.hideKeyboard(v,getContext());
                return false;
            }
        });

        return rootView;
    }
}
