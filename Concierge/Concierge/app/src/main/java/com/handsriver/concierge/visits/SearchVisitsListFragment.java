package com.handsriver.concierge.visits;

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
import com.handsriver.concierge.database.ConciergeContract.VisitEntry;
import com.handsriver.concierge.database.ConciergeContract.ApartmentEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.SelectToDBRaw;
import com.handsriver.concierge.utilities.Utility;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by Created by alain_r._trouve_silva after 04-03-17.
 */

public class SearchVisitsListFragment extends Fragment{

    View rootView;
    Callback mCallback;
    VisitAdapterSearchList visitsAdapter;
    ListView viewVisits;
    SearchView searchVisits;
    ArrayList<Visit> mVisits;

    public interface Callback{
        public void onItemSelected (Visit visit);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConciergeDbHelper helper= new ConciergeDbHelper(getContext());
        DatabaseManager.initializeInstance(helper);        mVisits = new ArrayList<Visit>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final String query = "SELECT " + VisitEntry.TABLE_NAME + "." + VisitEntry._ID + "," +
                VisitEntry.COLUMN_FULL_NAME + "," +
                VisitEntry.COLUMN_DOCUMENT_NUMBER + "," +
                VisitEntry.COLUMN_NATIONALITY + "," +
                VisitEntry.COLUMN_GENDER + "," +
                VisitEntry.COLUMN_BIRTHDATE + "," +
                VisitEntry.COLUMN_ENTRY + "," +
                ApartmentEntry.TABLE_NAME + "." + ApartmentEntry.COLUMN_APARTMENT_NUMBER +
                " FROM " + VisitEntry.TABLE_NAME + "," + ApartmentEntry.TABLE_NAME +
                " WHERE " + ApartmentEntry.TABLE_NAME + "." + ApartmentEntry.COLUMN_APARTMENT_ID_SERVER + " = " + VisitEntry.TABLE_NAME + "." + VisitEntry.COLUMN_APARTMENT_ID +
                " ORDER BY " + VisitEntry.COLUMN_ENTRY + " DESC";

        Cursor c;
        try {
            SelectToDBRaw selectVisits = new SelectToDBRaw(query,null);
            c = selectVisits.execute().get();
        }catch (Exception e){
            c = null;
        }

        if (c != null){

            while (c.moveToNext()){
                Visit visit = new Visit();
                visit.setFullName(c.getString(c.getColumnIndex(VisitEntry.COLUMN_FULL_NAME)));
                visit.setDocumentNumber(c.getString(c.getColumnIndex(VisitEntry.COLUMN_DOCUMENT_NUMBER)));
                visit.setNationality(c.getString(c.getColumnIndex(VisitEntry.COLUMN_NATIONALITY)));
                visit.setGender(c.getString(c.getColumnIndex(VisitEntry.COLUMN_GENDER)));
                visit.setBirthdate(c.getString(c.getColumnIndex(VisitEntry.COLUMN_BIRTHDATE)));
                visit.setEntry(c.getString(c.getColumnIndex(VisitEntry.COLUMN_ENTRY)));
                visit.setApartmentNumber(c.getString(c.getColumnIndex(ApartmentEntry.COLUMN_APARTMENT_NUMBER)));
                mVisits.add(visit);

            }
            c.close();
            visitsAdapter = new VisitAdapterSearchList(getContext(),mVisits);
        }

        rootView = inflater.inflate(R.layout.fragment_list_search_visits, container, false);

        viewVisits = (ListView) rootView.findViewById(R.id.listViewVisits);
        viewVisits.setAdapter(visitsAdapter);

        searchVisits = (SearchView) rootView.findViewById(R.id.searchVisits);
        searchVisits.setIconified(true);

        searchVisits.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                visitsAdapter.getFilter().filter(newText);
                return true;
            }
        });



        viewVisits.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Visit visit = (Visit) parent.getItemAtPosition(position);

                if (visit != null)
                {
                    mCallback.onItemSelected(visit);
                }

            }
        });

        viewVisits.setOnTouchListener(new View.OnTouchListener() {
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