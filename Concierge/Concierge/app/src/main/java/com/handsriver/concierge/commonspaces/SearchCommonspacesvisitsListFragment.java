package com.handsriver.concierge.commonspaces;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeContract;
import com.handsriver.concierge.database.ConciergeContract.CommonspaceEntry;
import com.handsriver.concierge.database.ConciergeContract.CommonspaceVisitsEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.SelectToDBRaw;
import com.handsriver.concierge.suppliers.SupplierAdapterSearchList;
import com.handsriver.concierge.suppliers.SupplierVisit;
import com.handsriver.concierge.utilities.Utility;
import com.handsriver.concierge.vehicles.Vehicle;
import com.handsriver.concierge.vehicles.VehicleAdapterSearchList;

import java.util.ArrayList;

/**
 * Created by Created by alain_r._trouve_silva after 16-03-17.
 */

public class SearchCommonspacesvisitsListFragment extends Fragment {
    View rootView;
    SearchCommonspacesvisitsListFragment.Callback mCallback;
    private CommonspacevisitAdapterSearchList commonspacevisitAdapter;
    private ListView viewCommonspacesvisits;
    SearchView searchCommonspacevisits;
    ArrayList<CommonspaceVisit> mVisit;


    public interface Callback{
        public void onItemSelectedCommonspacevisit(CommonspaceVisit visit);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConciergeDbHelper helper= new ConciergeDbHelper(getContext());
        DatabaseManager.initializeInstance(helper);
        mVisit = new ArrayList<CommonspaceVisit>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        final String query = "SELECT " + CommonspaceEntry.TABLE_NAME + "." + CommonspaceEntry._ID + "," +
                CommonspaceVisitsEntry.TABLE_NAME + "." + CommonspaceVisitsEntry._ID + "," +
                CommonspaceVisitsEntry.COLUMN_ENTRY  + "," +
                CommonspaceEntry.TABLE_NAME + "." + CommonspaceEntry.COLUMN_NAME_COMMONSPACE + "," +
                CommonspaceVisitsEntry.COLUMN_FULL_NAME  + "," +
                CommonspaceVisitsEntry.COLUMN_DOCUMENT_NUMBER  + "," +
                ConciergeContract.ApartmentEntry.TABLE_NAME + "." + ConciergeContract.ApartmentEntry.COLUMN_APARTMENT_NUMBER  + "," +
                CommonspaceVisitsEntry.COLUMN_GATEWAY_ID + "," +
                CommonspaceVisitsEntry.COLUMN_COMMONSPACE_ID + "," +
                CommonspaceVisitsEntry.COLUMN_EXIT_VISIT +

                " FROM " + CommonspaceVisitsEntry.TABLE_NAME + "," + CommonspaceEntry.TABLE_NAME + "," + ConciergeContract.ApartmentEntry.TABLE_NAME +
                " WHERE " + CommonspaceVisitsEntry.TABLE_NAME + "." + CommonspaceVisitsEntry.COLUMN_COMMONSPACE_ID + " = " + CommonspaceEntry.TABLE_NAME + "." + CommonspaceEntry.COLUMN_COMMONSPACE_ID_SERVER +
                " AND " + ConciergeContract.ApartmentEntry.TABLE_NAME + "." + ConciergeContract.ApartmentEntry.COLUMN_APARTMENT_ID_SERVER + " = " + CommonspaceVisitsEntry.TABLE_NAME + "." + CommonspaceVisitsEntry.COLUMN_APARTMENT_ID +
                " ORDER BY " + CommonspaceVisitsEntry.COLUMN_ENTRY + " DESC";

        Cursor c;
        try {
            SelectToDBRaw selectSuppliers = new SelectToDBRaw(query,null);
            c = selectSuppliers.execute().get();
        }catch (Exception e){
            c = null;
        }

        if (c != null) {
            while (c.moveToNext()){
                CommonspaceVisit visit = new CommonspaceVisit();
                visit.setId(c.getString(c.getColumnIndex(CommonspaceVisitsEntry.TABLE_NAME + "." + CommonspaceVisitsEntry._ID)));
                visit.setEntry(c.getString(c.getColumnIndex(CommonspaceVisitsEntry.COLUMN_ENTRY)));
                visit.setFullName(c.getString(c.getColumnIndex(CommonspaceVisitsEntry.COLUMN_FULL_NAME)));
                visit.setDocumentNumber(c.getString(c.getColumnIndex(CommonspaceVisitsEntry.COLUMN_DOCUMENT_NUMBER)));
                visit.setApartmentNumber(c.getString(c.getColumnIndex(ConciergeContract.ApartmentEntry.TABLE_NAME + "." + ConciergeContract.ApartmentEntry.COLUMN_APARTMENT_NUMBER)));
                visit.setCommonspaceName(c.getString(c.getColumnIndex(CommonspaceEntry.COLUMN_NAME_COMMONSPACE)));
                visit.setExitDate(c.getString(c.getColumnIndex(CommonspaceVisitsEntry.COLUMN_EXIT_VISIT)));
                mVisit.add(visit);
            }
            c.close();
            commonspacevisitAdapter = new CommonspacevisitAdapterSearchList(getActivity(),mVisit);
        }


        rootView = inflater.inflate(R.layout.fragment_list_search_commonspacesvisitis, container, false);

        viewCommonspacesvisits = (ListView) rootView.findViewById(R.id.listViewVisitsCommonspaces);
        viewCommonspacesvisits.setAdapter(commonspacevisitAdapter);

        searchCommonspacevisits = (SearchView) rootView.findViewById(R.id.searchCommonspacesvisits);
        searchCommonspacevisits.setIconified(true);

        searchCommonspacevisits.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                commonspacevisitAdapter.getFilter().filter(newText);
                return true;
            }
        });


        viewCommonspacesvisits.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CommonspaceVisit visit = (CommonspaceVisit) parent.getItemAtPosition(position);

                if (visit != null)
                {
                    mCallback.onItemSelectedCommonspacevisit(visit);
                }

            }
        });

        viewCommonspacesvisits.setOnTouchListener(new View.OnTouchListener() {
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
