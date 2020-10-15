package com.handsriver.concierge.suppliers;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeContract.SupplierVisitsEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.SelectToDBRaw;
import com.handsriver.concierge.utilities.FormatICAO9303;
import com.handsriver.concierge.utilities.Utility;
import com.handsriver.concierge.visits.Visit;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by Created by alain_r._trouve_silva after 17-03-17.
 */

public class DetailSearchSuppliersListFragment extends Fragment {
    View rootView;
    TextView textViewNameSupplier;
    TextView textViewEntry;
    TextView textViewExitSupplier;
    TextView textViewObsExitSupplier;
    TextView textViewLicensePlateSupplier;
    TextInputEditText textInputObsExitSupplier;
    ListView viewVisitsSupplier;
    String gateway_id;
    String supplier_id;
    String nameSupplier;
    String entry;
    String exitSupplier;
    String obsExitSupplier;
    String licensePlate;
    SimpleCursorAdapter visitsSuppliersAdapter;
    Button buttonExitSupplier;
    ArrayList<String> visitsSupplierIds;


    public static final String NO_AVAILABLE = "Sin Información";
    public static final int DIALOG_FRAGMENT = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConciergeDbHelper helper= new ConciergeDbHelper(getContext());
        DatabaseManager.initializeInstance(helper);
        visitsSupplierIds = new ArrayList<String>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();

        gateway_id = args.getString("gatewayId");
        supplier_id = args.getString("supplierId");
        nameSupplier = args.getString("nameSupplier",NO_AVAILABLE);
        obsExitSupplier = args.getString("obsExitSupplier");

        entry = Utility.changeDateFormat(args.getString("entry"),"ENTRY");

        String [] args1 = new String[]{gateway_id,supplier_id,Utility.changeDateFormatDatabase(entry)};

        final String query = "SELECT " + SupplierVisitsEntry._ID + "," +
                SupplierVisitsEntry.COLUMN_FULL_NAME + "," +
                SupplierVisitsEntry.COLUMN_DOCUMENT_NUMBER + "," +
                SupplierVisitsEntry.COLUMN_NATIONALITY + "," +
                SupplierVisitsEntry.COLUMN_BIRTHDATE +
                " FROM " + SupplierVisitsEntry.TABLE_NAME +
                " WHERE " + SupplierVisitsEntry.COLUMN_GATEWAY_ID + " = ? AND " + SupplierVisitsEntry.COLUMN_SUPPLIER_ID + " = ? AND " + SupplierVisitsEntry.COLUMN_ENTRY + " = ?" +
                " ORDER BY " + SupplierVisitsEntry.COLUMN_FULL_NAME + " ASC";

        Cursor c;
        try {
            SelectToDBRaw selectSuppliers = new SelectToDBRaw(query,args1);
            c = selectSuppliers.execute().get();
        }catch (Exception e){
            c = null;
        }

        String[] fromColumns = {
                SupplierVisitsEntry.COLUMN_FULL_NAME,
                SupplierVisitsEntry.COLUMN_DOCUMENT_NUMBER,
                SupplierVisitsEntry.COLUMN_NATIONALITY,
                SupplierVisitsEntry.COLUMN_BIRTHDATE
        };

        int[] toViews = {
                R.id.textViewFullNameSupplier,
                R.id.textViewDocumentNumberSupplier,
                R.id.textViewNationalitySupplier,
                R.id.textViewBirthdateSupplier
        };

        visitsSuppliersAdapter = new SimpleCursorAdapter(getActivity(), R.layout.item_visits_supplier_search,c,fromColumns,toViews,0);

        visitsSuppliersAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (columnIndex == 1 || columnIndex == 2 || columnIndex == 3 || columnIndex == 4){
                    String string = cursor.getString(columnIndex);
                    if (string != null && string.length() > 0){
                        TextView textView = (TextView) view;
                        textView.setText(string);
                    }
                    else{
                        TextView textView = (TextView) view;
                        textView.setText(NO_AVAILABLE);
                    }
                    return true;
                }
                return false;
            }
        });

        rootView = inflater.inflate(R.layout.detail_suppliers, container, false);

        textInputObsExitSupplier = (TextInputEditText) rootView.findViewById(R.id.exitObsSupplierInput);

        viewVisitsSupplier = (ListView) rootView.findViewById(R.id.listViewVisitsSuppliers);
        viewVisitsSupplier.setAdapter(visitsSuppliersAdapter);



        buttonExitSupplier = (Button) rootView.findViewById(R.id.buttonExitSupplier);

        if (c != null){
            while (c.moveToNext()){
                visitsSupplierIds.add(c.getString(0));
            }
        }


        buttonExitSupplier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString("obsExitSupplier",textInputObsExitSupplier.getText().toString());
                args.putStringArrayList("visitsIdsList",visitsSupplierIds);

                DialogExitSuppliersRegister dialog = new DialogExitSuppliersRegister();
                dialog.setArguments(args);
                dialog.setTargetFragment(DetailSearchSuppliersListFragment.this,0);
                dialog.show(getFragmentManager(), "dialog");
            }
        });

        getExitDateAndLicensePlate();

        if (exitSupplier.equals(NO_AVAILABLE))
        {
            textInputObsExitSupplier.setVisibility(View.VISIBLE);
            buttonExitSupplier.setVisibility(View.VISIBLE);
        }

        textViewEntry = (TextView) rootView.findViewById(R.id.textViewDetailEntrySupplier);
        textViewEntry.setText(entry);
        textViewNameSupplier = (TextView) rootView.findViewById(R.id.textViewDetailNameSupplier);
        textViewNameSupplier.setText(nameSupplier);
        textViewExitSupplier = (TextView) rootView.findViewById(R.id.textViewDetailExitSupplier);
        textViewExitSupplier.setText(exitSupplier);

        textViewObsExitSupplier = (TextView) rootView.findViewById(R.id.textViewDetailExitObsSupplier);
        textViewObsExitSupplier.setText(obsExitSupplier);


        textViewLicensePlateSupplier = (TextView) rootView.findViewById(R.id.textViewLicensePlateSupplier);
        textViewLicensePlateSupplier.setText(licensePlate);

        viewVisitsSupplier.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Utility.hideKeyboard(v,getContext());
                return false;
            }
        });

        textInputObsExitSupplier.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keycode, KeyEvent event) {
                return isScannerOCR(v,keycode,event);
            }
        });

        return rootView;
    }

    private boolean isScannerOCR(View v, int keycode, KeyEvent event){

        if (event.getDeviceId() == KeyCharacterMap.VIRTUAL_KEYBOARD) {
            return false;
        }
        return true;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case DIALOG_FRAGMENT:
                if (resultCode == Activity.RESULT_OK){
                    getExitDateAndLicensePlate();
                    textViewExitSupplier.setText(exitSupplier);
                    textViewObsExitSupplier.setText(obsExitSupplier);

                    if (!exitSupplier.equals(NO_AVAILABLE))
                    {
                        textInputObsExitSupplier.setVisibility(View.GONE);
                        buttonExitSupplier.setVisibility(View.GONE);
                    }
                }
                break;
        }
    }

    public void getExitDateAndLicensePlate (){

        final String query1 = "SELECT " + SupplierVisitsEntry.COLUMN_EXIT_SUPPLIER + "," + SupplierVisitsEntry.COLUMN_LICENSE_PLATE + "," + SupplierVisitsEntry.COLUMN_EXIT_OBS +
                " FROM " + SupplierVisitsEntry.TABLE_NAME +
                " WHERE " + SupplierVisitsEntry.COLUMN_GATEWAY_ID + " = ? AND " + SupplierVisitsEntry.COLUMN_SUPPLIER_ID + " = ? AND " + SupplierVisitsEntry.COLUMN_ENTRY + " = ?" +
                " GROUP BY " + SupplierVisitsEntry.COLUMN_ENTRY + "," + SupplierVisitsEntry.COLUMN_SUPPLIER_ID + "," + SupplierVisitsEntry.COLUMN_GATEWAY_ID  + "," + SupplierVisitsEntry.COLUMN_LICENSE_PLATE;

        String [] args2 = new String[]{gateway_id,supplier_id,Utility.changeDateFormatDatabase(entry)};

        Cursor c1;

        try {
            SelectToDBRaw selectExitSupplier = new SelectToDBRaw(query1,args2);
            c1 = selectExitSupplier.execute().get();
        }catch (Exception e){
            c1 = null;
        }

        if (c1 != null){
            if(c1.moveToFirst()){
                if (c1.getString(0) == null || c1.getString(0).isEmpty())
                {
                    exitSupplier = NO_AVAILABLE;
                }
                else
                {
                    exitSupplier = Utility.changeDateFormat(c1.getString(0),"ENTRY");
                }
                if (c1.getString(1) == null || c1.getString(1).isEmpty())
                {
                    licensePlate = NO_AVAILABLE;
                }
                else
                {
                    licensePlate = c1.getString(1);
                }

                if (c1.getString(2) == null || c1.getString(2).isEmpty())
                {
                    obsExitSupplier = NO_AVAILABLE;
                }
                else
                {
                    obsExitSupplier = c1.getString(2);
                }


            }
        }
    }
}
