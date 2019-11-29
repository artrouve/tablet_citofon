package com.handsriver.concierge.residents;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.TextView;

import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeContract.ResidentEntry;
import com.handsriver.concierge.database.ConciergeContract.ApartmentEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.SelectToDBRaw;
import com.handsriver.concierge.utilities.Utility;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by Created by alain_r._trouve_silva after 06-04-17.
 */

public class SearchResidentsListFragment extends Fragment{
    View rootView;
    ResidentAdapterSearchList residentsAdapter;
    ListView viewResidents;
    SearchView searchResidents;
    private static final int IS_SYNC = 1;
    ArrayList<Resident> mResident;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConciergeDbHelper helper= new ConciergeDbHelper(getContext());
        DatabaseManager.initializeInstance(helper);
        mResident = new ArrayList<Resident>();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final String query = "SELECT " + ResidentEntry.TABLE_NAME + "." + ResidentEntry._ID + "," +
                ResidentEntry.COLUMN_FULL_NAME + "," +
                ResidentEntry.COLUMN_EMAIL + "," +
                ResidentEntry.COLUMN_IS_SYNC + "," +
                ResidentEntry.COLUMN_IS_UPDATE + "," +
                ApartmentEntry.TABLE_NAME + "." + ApartmentEntry.COLUMN_APARTMENT_NUMBER +
                " FROM " + ResidentEntry.TABLE_NAME + "," + ApartmentEntry.TABLE_NAME +
                " WHERE " + ApartmentEntry.TABLE_NAME + "." + ApartmentEntry.COLUMN_APARTMENT_ID_SERVER + " = " + ResidentEntry.TABLE_NAME + "." + ResidentEntry.COLUMN_APARTMENT_ID +
                " AND " + ResidentEntry.COLUMN_IS_SYNC + " = " + IS_SYNC +
                " ORDER BY " + ApartmentEntry.TABLE_NAME + "." + ApartmentEntry.COLUMN_APARTMENT_NUMBER + " ASC";

        Cursor c;
        try {
            SelectToDBRaw selectResidents = new SelectToDBRaw(query,null);
            c = selectResidents.execute().get();
        }catch (Exception e){
            c = null;
        }

        if (c != null) {
            while (c.moveToNext()){
                Resident resident = new Resident();
                resident.setId(c.getLong(c.getColumnIndex(ResidentEntry._ID)));
                resident.setFullName(c.getString(c.getColumnIndex(ResidentEntry.COLUMN_FULL_NAME)));
                resident.setEmail(c.getString(c.getColumnIndex(ResidentEntry.COLUMN_EMAIL)));
                resident.setIsSync(c.getString(c.getColumnIndex(ResidentEntry.COLUMN_IS_SYNC)));
                resident.setIsUpdate(c.getString(c.getColumnIndex(ResidentEntry.COLUMN_IS_UPDATE)));
                resident.setApartmentNumber(c.getString(c.getColumnIndex(ApartmentEntry.COLUMN_APARTMENT_NUMBER)));
                mResident.add(resident);
            }
            c.close();
            residentsAdapter = new ResidentAdapterSearchList(getActivity(),mResident);
        }


        rootView = inflater.inflate(R.layout.fragment_list_search_residents, container, false);

        viewResidents = (ListView) rootView.findViewById(R.id.listViewResidents);
        viewResidents.setAdapter(residentsAdapter);

        searchResidents = (SearchView) rootView.findViewById(R.id.searchResidents);
        searchResidents.setIconified(true);

        searchResidents.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                residentsAdapter.getFilter().filter(newText);
                return true;
            }
        });

        viewResidents.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Utility.hideKeyboard(v,getContext());
                return false;
            }
        });

        return rootView;
    }
}
