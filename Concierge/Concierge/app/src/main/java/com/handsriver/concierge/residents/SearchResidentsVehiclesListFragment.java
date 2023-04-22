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
import com.handsriver.concierge.database.ConciergeContract.ResidentVehicleEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.SelectToDBRaw;
import com.handsriver.concierge.utilities.Utility;

import java.util.ArrayList;

/**
 * Created by Created by alain_r._trouve_silva after 06-04-17.
 */

public class SearchResidentsVehiclesListFragment extends Fragment{
    View rootView;
    ResidentVehicleAdapterSearchList residentsvehiclesAdapter;
    ListView viewResidentsVehicles;
    SearchView searchResidentsVehicles;
    private static final int IS_SYNC = 1;
    ArrayList<ResidentVehicle> mResidentVehicle;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConciergeDbHelper helper= new ConciergeDbHelper(getContext());
        DatabaseManager.initializeInstance(helper);
        mResidentVehicle = new ArrayList<ResidentVehicle>();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final String query = "SELECT " + ResidentVehicleEntry.TABLE_NAME + "." + ResidentVehicleEntry._ID + "," +
                ResidentVehicleEntry.TABLE_NAME + "." +ResidentVehicleEntry.COLUMN_PLATE + "," +
                ResidentVehicleEntry.TABLE_NAME + "." +ResidentVehicleEntry.COLUMN_ACTIVE + "," +
                ResidentVehicleEntry.TABLE_NAME + "." +ResidentVehicleEntry.COLUMN_IS_SYNC + "," +
                ResidentVehicleEntry.TABLE_NAME + "." +ResidentVehicleEntry.COLUMN_IS_UPDATE + "," +
                ApartmentEntry.TABLE_NAME + "." + ApartmentEntry.COLUMN_APARTMENT_NUMBER +
                " FROM " + ResidentVehicleEntry.TABLE_NAME + "," + ApartmentEntry.TABLE_NAME +
                " WHERE " + ApartmentEntry.TABLE_NAME + "." + ApartmentEntry.COLUMN_APARTMENT_ID_SERVER + " = " + ResidentVehicleEntry.TABLE_NAME + "." + ResidentVehicleEntry.COLUMN_APARTMENT_ID +
                " AND " + ResidentVehicleEntry.COLUMN_IS_SYNC + " = " + IS_SYNC +
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
                ResidentVehicle residentvehicle = new ResidentVehicle();
                residentvehicle.setId(c.getLong(c.getColumnIndex(ResidentVehicleEntry._ID)));
                residentvehicle.setPlate(c.getString(c.getColumnIndex(ResidentVehicleEntry.COLUMN_PLATE)));
                residentvehicle.setActive(c.getString(c.getColumnIndex(ResidentVehicleEntry.COLUMN_ACTIVE)));


                residentvehicle.setIsSync(c.getString(c.getColumnIndex(ResidentVehicleEntry.COLUMN_IS_SYNC)));
                residentvehicle.setIsUpdate(c.getString(c.getColumnIndex(ResidentVehicleEntry.COLUMN_IS_UPDATE)));
                residentvehicle.setApartmentNumber(c.getString(c.getColumnIndex(ApartmentEntry.COLUMN_APARTMENT_NUMBER)));
                mResidentVehicle.add(residentvehicle);
            }
            c.close();
            residentsvehiclesAdapter = new ResidentVehicleAdapterSearchList(getActivity(),mResidentVehicle);
        }


        rootView = inflater.inflate(R.layout.fragment_list_search_residentsvehicles, container, false);

        viewResidentsVehicles = (ListView) rootView.findViewById(R.id.listViewResidentsVehicles);
        viewResidentsVehicles.setAdapter(residentsvehiclesAdapter);

        searchResidentsVehicles = (SearchView) rootView.findViewById(R.id.searchResidentsVehicles);
        searchResidentsVehicles.setIconified(true);

        searchResidentsVehicles.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                residentsvehiclesAdapter.getFilter().filter(newText);
                return true;
            }
        });

        viewResidentsVehicles.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Utility.hideKeyboard(v,getContext());
                return false;
            }
        });

        return rootView;
    }
}
