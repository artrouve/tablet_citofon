package com.handsriver.concierge.timekeeping;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.TextView;
import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeContract.TimekeepingEntry;
import com.handsriver.concierge.database.ConciergeContract.PorterEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.SelectToDBRaw;
import com.handsriver.concierge.suppliers.SupplierVisit;
import com.handsriver.concierge.utilities.Utility;
import com.handsriver.concierge.visits.SearchVisitsListFragment;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;

/**
 * Created by Created by alain_r._trouve_silva after 25-03-17.
 */

public class SearchTimekeepingListFragment extends Fragment {
    View rootView;
    Callback mCallback;
    TimekeepingAdapterSearchList timekeepingAdapter;
    ListView viewTimekeeping;
    public SearchView searchTimekeeping;
    int porterIdServer;
    ArrayList<Timekeeping> mTimekeeping;


    public static final String PREFS_NAME = "PorterPrefs";
    public static final int DEF_VALUE = 0;
    public static final String NO_AVAILABLE = "Sin Información";

    public interface Callback{
        public void onItemSelectedTimekeeping (Timekeeping timekeeping);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConciergeDbHelper helper= new ConciergeDbHelper(getContext());
        DatabaseManager.initializeInstance(helper);
        mTimekeeping = new ArrayList<Timekeeping>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        SharedPreferences porterPrefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        porterIdServer = porterPrefs.getInt(getString(R.string.porterIdServerVar),DEF_VALUE);

        final String query = "SELECT " + TimekeepingEntry.TABLE_NAME + "." + TimekeepingEntry._ID + "," +
                PorterEntry.COLUMN_FIRST_NAME + "," +
                PorterEntry.COLUMN_LAST_NAME + "," +
                PorterEntry.COLUMN_RUT + "," +
                TimekeepingEntry.COLUMN_ENTRY_PORTER + "," +
                TimekeepingEntry.COLUMN_EXIT_PORTER +
                " FROM " + TimekeepingEntry.TABLE_NAME + "," + PorterEntry.TABLE_NAME +
                " WHERE " + PorterEntry.TABLE_NAME + "." + PorterEntry.COLUMN_PORTER_ID_SERVER + " = " + TimekeepingEntry.TABLE_NAME + "." + TimekeepingEntry.COLUMN_PORTER_ID +
                " AND " + TimekeepingEntry.COLUMN_PORTER_ID + " = ?" +
                " ORDER BY " + TimekeepingEntry.COLUMN_ENTRY_PORTER + " DESC";

        String[] selectionArgs = {String.valueOf(porterIdServer)};

        Cursor c;
        try {
            SelectToDBRaw selectTimekeeping = new SelectToDBRaw(query,selectionArgs);
            c = selectTimekeeping.execute().get();
        }catch (Exception e){
            c = null;
        }

        if (c != null){
            while (c.moveToNext()){
                Timekeeping timekeeping = new Timekeeping();
                timekeeping.setFirstName(c.getString(c.getColumnIndex(PorterEntry.COLUMN_FIRST_NAME)));
                timekeeping.setLastName(c.getString(c.getColumnIndex(PorterEntry.COLUMN_LAST_NAME)));
                timekeeping.setRut(c.getString(c.getColumnIndex(PorterEntry.COLUMN_RUT)));
                timekeeping.setEntryPorter(c.getString(c.getColumnIndex(TimekeepingEntry.COLUMN_ENTRY_PORTER)));
                timekeeping.setExitPorter(c.getString(c.getColumnIndex(TimekeepingEntry.COLUMN_EXIT_PORTER)));
                mTimekeeping.add(timekeeping);
            }
            c.close();
            timekeepingAdapter = new TimekeepingAdapterSearchList(getActivity(),mTimekeeping);
        }

        rootView = inflater.inflate(R.layout.fragment_list_search_timekeeping, container, false);

        viewTimekeeping = (ListView) rootView.findViewById(R.id.listViewTimekeeping);
        viewTimekeeping.setAdapter(timekeepingAdapter);

        searchTimekeeping = (SearchView) rootView.findViewById(R.id.searchTimekeeping);

        searchTimekeeping.setIconified(true);
        searchTimekeeping.setInputType(InputType.TYPE_NULL);

        searchTimekeeping.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){

                    DialogFragment newFragment = new DatePickerFragment();
                    newFragment.show(getFragmentManager(), "timePicker");
                    searchTimekeeping.clearFocus();

                }
            }
        });

        searchTimekeeping.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                timekeepingAdapter.getFilter().filter(newText);
                return true;
            }
        });

        viewTimekeeping.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Timekeeping timekeeping = (Timekeeping) parent.getItemAtPosition(position);

                if (timekeeping != null)
                {
                    mCallback.onItemSelectedTimekeeping(timekeeping);
                }

            }
        });

        viewTimekeeping.setOnTouchListener(new View.OnTouchListener() {
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
