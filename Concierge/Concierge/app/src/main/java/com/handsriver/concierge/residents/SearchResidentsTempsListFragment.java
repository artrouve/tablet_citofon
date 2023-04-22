package com.handsriver.concierge.residents;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeContract.ApartmentEntry;
import com.handsriver.concierge.database.ConciergeContract.ResidentTempEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.SelectToDBRaw;
import com.handsriver.concierge.utilities.Utility;

import java.util.ArrayList;

/**
 * Created by Created by alain_r._trouve_silva after 06-04-17.
 */

public class SearchResidentsTempsListFragment extends Fragment{
    View rootView;
    ResidentTempAdapterSearchList residentstempsAdapter;
    ListView viewResidentsTemps;
    SearchView searchResidentsTemps;
    private static final int IS_SYNC = 1;
    ArrayList<ResidentTemp> mResidenttemp;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConciergeDbHelper helper= new ConciergeDbHelper(getContext());
        DatabaseManager.initializeInstance(helper);
        mResidenttemp = new ArrayList<ResidentTemp>();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final String query = "SELECT " + ResidentTempEntry.TABLE_NAME + "." + ResidentTempEntry._ID + "," +
                ResidentTempEntry.COLUMN_FULL_NAME + "," +
                ResidentTempEntry.COLUMN_EMAIL + "," +
                ResidentTempEntry.COLUMN_PHONE + "," +
                ResidentTempEntry.COLUMN_RUT + "," +
                ResidentTempEntry.COLUMN_START_DATE + "," +
                ResidentTempEntry.COLUMN_END_DATE + "," +
                ResidentTempEntry.COLUMN_IS_SYNC + "," +
                ResidentTempEntry.COLUMN_IS_UPDATE + "," +
                ApartmentEntry.TABLE_NAME + "." + ApartmentEntry.COLUMN_APARTMENT_NUMBER +
                " FROM " + ResidentTempEntry.TABLE_NAME + "," + ApartmentEntry.TABLE_NAME +
                " WHERE " + ApartmentEntry.TABLE_NAME + "." + ApartmentEntry.COLUMN_APARTMENT_ID_SERVER + " = " + ResidentTempEntry.TABLE_NAME + "." + ResidentTempEntry.COLUMN_APARTMENT_ID +
                " AND " + ResidentTempEntry.COLUMN_IS_SYNC + " = " + IS_SYNC +
                " ORDER BY " + ApartmentEntry.TABLE_NAME + "." + ApartmentEntry.COLUMN_APARTMENT_NUMBER + " ASC";

        Cursor c;
        try {
            SelectToDBRaw selectResidentstemps = new SelectToDBRaw(query,null);
            c = selectResidentstemps.execute().get();
        }catch (Exception e){
            c = null;
        }

        if (c != null) {
            while (c.moveToNext()){
                ResidentTemp residenttemp = new ResidentTemp();
                residenttemp.setId(c.getLong(c.getColumnIndex(ResidentTempEntry._ID)));
                residenttemp.setFullName(c.getString(c.getColumnIndex(ResidentTempEntry.COLUMN_FULL_NAME)));
                residenttemp.setEmail(c.getString(c.getColumnIndex(ResidentTempEntry.COLUMN_EMAIL)));

                residenttemp.setPhone(c.getString(c.getColumnIndex(ResidentTempEntry.COLUMN_PHONE)));
                residenttemp.setRut(c.getString(c.getColumnIndex(ResidentTempEntry.COLUMN_RUT)));
                residenttemp.setStartDate(c.getString(c.getColumnIndex(ResidentTempEntry.COLUMN_START_DATE)));
                residenttemp.setEndDate(c.getString(c.getColumnIndex(ResidentTempEntry.COLUMN_END_DATE)));

                residenttemp.setIsSync(c.getString(c.getColumnIndex(ResidentTempEntry.COLUMN_IS_SYNC)));
                residenttemp.setIsUpdate(c.getString(c.getColumnIndex(ResidentTempEntry.COLUMN_IS_UPDATE)));
                residenttemp.setApartmentNumber(c.getString(c.getColumnIndex(ApartmentEntry.COLUMN_APARTMENT_NUMBER)));
                mResidenttemp.add(residenttemp);
            }
            c.close();
            residentstempsAdapter = new ResidentTempAdapterSearchList(getActivity(),mResidenttemp);
        }


        rootView = inflater.inflate(R.layout.fragment_list_search_residentstemps, container, false);

        viewResidentsTemps = (ListView) rootView.findViewById(R.id.listViewResidentsTemps);
        viewResidentsTemps.setAdapter(residentstempsAdapter);

        searchResidentsTemps = (SearchView) rootView.findViewById(R.id.searchResidentsTemps);
        searchResidentsTemps.setIconified(true);

        searchResidentsTemps.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                residentstempsAdapter.getFilter().filter(newText);
                return true;
            }
        });

        viewResidentsTemps.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Utility.hideKeyboard(v,getContext());
                return false;
            }
        });

        return rootView;
    }
}
