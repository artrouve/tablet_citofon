package com.handsriver.concierge.parcels;

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
import com.handsriver.concierge.database.ConciergeContract.ParcelEntry;
import com.handsriver.concierge.database.ConciergeContract.ParcelTypeEntry;
import com.handsriver.concierge.database.ConciergeContract.ApartmentEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.SelectToDBRaw;
import com.handsriver.concierge.utilities.Utility;
import com.handsriver.concierge.visits.Visit;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by Created by alain_r._trouve_silva after 14-03-17.
 */

public class SearchParcelsListFragment extends Fragment {
    View rootView;
    SearchParcelsListFragment.Callback mCallback;
    ParcelAdapterSearchList parcelsAdapter;
    ListView viewParcels;
    SearchView searchParcels;
    ArrayList<Parcel> mParcels;


    public interface Callback{
        public void onItemSelectedParcels (Parcel parcel);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConciergeDbHelper helper= new ConciergeDbHelper(getContext());
        DatabaseManager.initializeInstance(helper);
        mParcels = new ArrayList<Parcel>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final String query = "SELECT " + ParcelEntry.TABLE_NAME + "." + ParcelEntry._ID + "," +
                ParcelEntry.COLUMN_UNIQUE_ID + "," +
                ParcelEntry.COLUMN_FULL_NAME + "," +
                ParcelEntry.COLUMN_OBSERVATIONS + "," +
                ParcelEntry.COLUMN_ENTRY_PARCEL + "," +
                ParcelEntry.COLUMN_EXIT_PARCEL + "," +
                ParcelTypeEntry.TABLE_NAME + "." + ParcelTypeEntry.COLUMN_PARCELTYPE_TYPE + "," +
                ApartmentEntry.TABLE_NAME + "." + ApartmentEntry.COLUMN_APARTMENT_NUMBER +
                " FROM " + ParcelEntry.TABLE_NAME + "," + ApartmentEntry.TABLE_NAME + "," + ParcelTypeEntry.TABLE_NAME +
                " WHERE " + ApartmentEntry.TABLE_NAME + "." + ApartmentEntry.COLUMN_APARTMENT_ID_SERVER + " = " + ParcelEntry.TABLE_NAME + "." + ParcelEntry.COLUMN_APARTMENT_ID +
                " AND " + ParcelEntry.TABLE_NAME + "." + ParcelEntry.COLUMN_PARCELTYPE_ID + " = " + ParcelTypeEntry.TABLE_NAME + "." + ParcelTypeEntry.COLUMN_PARCELTYPE_ID_SERVER +
                " ORDER BY " + ParcelEntry.COLUMN_ENTRY_PARCEL + " DESC";

        Cursor c;
        try {
            SelectToDBRaw selectParcels = new SelectToDBRaw(query,null);
            c = selectParcels.execute().get();
        }catch (Exception e){
            c = null;
        }

        if (c != null) {

            while (c.moveToNext()) {
                Parcel parcel = new Parcel();
                parcel.setId(c.getString(c.getColumnIndex(ParcelEntry._ID)));
                parcel.setUniqueId(c.getString(c.getColumnIndex(ParcelEntry.COLUMN_UNIQUE_ID)));
                parcel.setFullName(c.getString(c.getColumnIndex(ParcelEntry.COLUMN_FULL_NAME)));
                parcel.setObservations(c.getString(c.getColumnIndex(ParcelEntry.COLUMN_OBSERVATIONS)));
                parcel.setEntryParcel(c.getString(c.getColumnIndex(ParcelEntry.COLUMN_ENTRY_PARCEL)));
                parcel.setTypeParcel(c.getString(c.getColumnIndex(ParcelTypeEntry.COLUMN_PARCELTYPE_TYPE)));
                parcel.setExitParcel(c.getString(c.getColumnIndex(ParcelEntry.COLUMN_EXIT_PARCEL)));
                parcel.setApartmentNumber(c.getString(c.getColumnIndex(ApartmentEntry.COLUMN_APARTMENT_NUMBER)));
                mParcels.add(parcel);
            }
            c.close();
            parcelsAdapter = new ParcelAdapterSearchList(getActivity(),mParcels);
        }

        rootView = inflater.inflate(R.layout.fragment_list_search_parcels, container, false);

        viewParcels = (ListView) rootView.findViewById(R.id.listViewParcels);
        viewParcels.setAdapter(parcelsAdapter);

        searchParcels = (SearchView) rootView.findViewById(R.id.searchParcels);
        searchParcels.setIconified(true);

        searchParcels.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                parcelsAdapter.getFilter().filter(newText);
                return true;
            }
        });

        viewParcels.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Parcel parcel = (Parcel) parent.getItemAtPosition(position);

                if (parcel != null)
                {
                    mCallback.onItemSelectedParcels(parcel);
                }

            }
        });

        viewParcels.setOnTouchListener(new View.OnTouchListener() {
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
