package com.handsriver.concierge.parcels;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.handsriver.concierge.R;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.utilities.Utility;
import com.handsriver.concierge.database.ConciergeContract.ApartmentEntry;
import com.handsriver.concierge.database.ConciergeContract.ResidentEntry;
import com.handsriver.concierge.database.ConciergeContract.ParcelTypeEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.SelectToDB;
import com.handsriver.concierge.database.SelectToDBRaw;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

/**
 * Created by Created by alain_r._trouve_silva after 10-03-17.
 */

public class ParcelsRegisterFragment extends Fragment {

    AutoCompleteTextView autoCompleteApartment;
    TextInputEditText textObservations;
    ArrayList<String> autoCompleteApartmentContent;
    ArrayAdapter<String> autoCompleteApartmentAdapter;
    SimpleCursorAdapter residentsAdapter;
    SimpleCursorAdapter parceltypeAdapter;
    Spinner residentsSpinner;
    Spinner parceltypeSpinner;
    StringBuilder ocr;
    View rootView;
    LinearLayout linearParcel;

    public static final String IS_ACTIVE = "1";

    String[] fromColumns = {
            ResidentEntry.COLUMN_FULL_NAME
    };

    String[] fromColumnsParcelType = {
            ParcelTypeEntry.COLUMN_PARCELTYPE_TYPE
    };


    int[] toViews = {
            R.id.itemSpinner
    };


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConciergeDbHelper helper= new ConciergeDbHelper(getContext());
        DatabaseManager.initializeInstance(helper);
        autoCompleteApartmentContent = new ArrayList<String>();
        autoCompleteApartmentAdapter = new ArrayAdapter<String>(getActivity(),R.layout.spinner_item,autoCompleteApartmentContent);
        ocr = new StringBuilder();
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_register_parcels, container, false);


        String[] projection = {
                ApartmentEntry.COLUMN_APARTMENT_ID_SERVER,
                ApartmentEntry.COLUMN_APARTMENT_NUMBER
        };

        String selection = ApartmentEntry.COLUMN_ACTIVE + " = ?";
        String [] selectionArgs = {IS_ACTIVE};
        String sortOrder = ApartmentEntry.COLUMN_APARTMENT_NUMBER + " ASC";

        Cursor c;
        try {
            SelectToDB selectApartment = new SelectToDB(ApartmentEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder,null);
            c = selectApartment.execute().get();
        }catch (Exception e){
            c = null;
        }

        if (c != null){
            while (c.moveToNext()){
                autoCompleteApartmentContent.add(c.getString(c.getColumnIndex(ApartmentEntry.COLUMN_APARTMENT_NUMBER)));
            }
            c.close();
        }

        textObservations = (TextInputEditText) rootView.findViewById(R.id.observationsInput);

        residentsSpinner = (Spinner) rootView.findViewById(R.id.residentsSpinner);

        parceltypeSpinner = (Spinner) rootView.findViewById(R.id.parceltypeSpinner);
        spinnerParcelTypeLoad();

        residentsSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                spinnerResidentLoad(autoCompleteApartment.getText().toString());
                autoCompleteApartment.clearFocus();
                Utility.hide_keyboard(getActivity(),autoCompleteApartment.getWindowToken());
                return false;
            }
        });

        autoCompleteApartment = (AutoCompleteTextView) rootView.findViewById(R.id.autoApartment);
        autoCompleteApartment.setThreshold(1);
        autoCompleteApartment.setAdapter(autoCompleteApartmentAdapter);

        autoCompleteApartment.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                spinnerResidentLoad(autoCompleteApartment.getText().toString());
                autoCompleteApartment.clearFocus();
                Utility.hide_keyboard(getActivity(),autoCompleteApartment.getWindowToken());
            }
        });

        autoCompleteApartment.setValidator(new AutoCompleteTextView.Validator(){
            @Override
            public boolean isValid(CharSequence text) {
                Collections.sort(autoCompleteApartmentContent);
                if (Collections.binarySearch(autoCompleteApartmentContent, text.toString()) >= 0) {

                    spinnerResidentLoad(text.toString());
                    return true;
                }
                autoCompleteApartment.setError("Número de Departamento Inválido");
                return false;
            }

            @Override
            public CharSequence fixText(CharSequence invalidText) {
                return null;
            }
        });

        autoCompleteApartment.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    v.clearFocus();
                    Utility.hide_keyboard(getActivity(),autoCompleteApartment.getWindowToken());
                }
                return false;
            }
        });

        autoCompleteApartment.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return Utility.isScannerOCR(v,keyCode,event);
            }
        });

        textObservations.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return Utility.isScannerOCR(v,keyCode,event);
            }
        });

        MatrixCursor others = new MatrixCursor(new String[] {ResidentEntry._ID,ResidentEntry.COLUMN_FULL_NAME});
        others.addRow(new Object[] {0,"Otro"});

        residentsAdapter = new SimpleCursorAdapter(getActivity(),R.layout.spinner_item,others,fromColumns,toViews,0);

        residentsAdapter.setDropDownViewResource(R.layout.spinner_item);

        residentsSpinner.setAdapter(residentsAdapter);

        linearParcel = (LinearLayout) rootView.findViewById(R.id.linearParcel);
        linearParcel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Utility.hideKeyboard(v,getContext());
                return false;
            }
        });

        return rootView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.app_bar_menu_parcels, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.registerParcelsSave) {
            validateInfo();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void validateInfo(){
        autoCompleteApartment.clearFocus();
        String apartment = autoCompleteApartment.getText().toString();
        Cursor c = (Cursor)residentsSpinner.getSelectedItem();
        String resident = c.getString(c.getColumnIndex("full_name"));

        Cursor d = (Cursor)parceltypeSpinner.getSelectedItem();
        String typeParcel = d.getString(d.getColumnIndex("type"));
        String typeParcelId = d.getString(d.getColumnIndex("_id"));

        String observations = textObservations.getText().toString();

        if (apartment.length()>0)
        {
            Bundle args = new Bundle();
            args.putString("apartment",apartment);
            args.putString("resident",resident);
            args.putString("typeParcel",typeParcel);
            args.putString("typeParcelId",typeParcelId);
            args.putString("observations",observations);

            DialogParcelsRegister dialog = new DialogParcelsRegister();
            dialog.setArguments(args);
            dialog.show(getFragmentManager(), "dialog");
        }
        else{
            if (apartment.length()==0)
            {
                Toast.makeText(getActivity().getApplicationContext(), "Falta ingresar el número de departamento", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void spinnerResidentLoad(String text){

        MatrixCursor others = new MatrixCursor(new String[] {ResidentEntry._ID,ResidentEntry.COLUMN_FULL_NAME});
        others.addRow(new Object[] {0,"Otro"});

        final String query = "SELECT " + ResidentEntry.TABLE_NAME + "." + ResidentEntry._ID + "," + ResidentEntry.COLUMN_FULL_NAME +
                " FROM " + ResidentEntry.TABLE_NAME + "," + ApartmentEntry.TABLE_NAME +
                " WHERE " + ApartmentEntry.TABLE_NAME + "." + ApartmentEntry.COLUMN_APARTMENT_NUMBER + " = ?" +
                " AND " + ApartmentEntry.TABLE_NAME + "." + ApartmentEntry.COLUMN_APARTMENT_ID_SERVER + " = " + ResidentEntry.TABLE_NAME + "." + ResidentEntry.COLUMN_APARTMENT_ID +
                " ORDER BY " + ResidentEntry.COLUMN_FULL_NAME + " ASC";

        String [] args = new String[]{text};


        Cursor c;
        try {
            SelectToDBRaw selectResidents = new SelectToDBRaw(query,args);
            c = selectResidents.execute().get();
        }catch (Exception e){
            c = null;
        }

        MergeCursor mergeCursor = new MergeCursor(new Cursor[]{c,others});

        residentsAdapter = new SimpleCursorAdapter(getActivity(),R.layout.spinner_item,mergeCursor,fromColumns,toViews,0);

        residentsAdapter.setDropDownViewResource(R.layout.spinner_item);

        residentsSpinner.setAdapter(residentsAdapter);
    }

    private void spinnerParcelTypeLoad(){

        MatrixCursor others = new MatrixCursor(new String[] {ParcelTypeEntry.COLUMN_PARCELTYPE_ID_SERVER,ParcelTypeEntry.COLUMN_PARCELTYPE_TYPE});

        final String query = "SELECT " + ParcelTypeEntry.TABLE_NAME + "." + ParcelTypeEntry.COLUMN_PARCELTYPE_ID_SERVER + " AS _id," + ParcelTypeEntry.COLUMN_PARCELTYPE_TYPE +
                " FROM " + ParcelTypeEntry.TABLE_NAME +
                " WHERE active = ? "+
                " ORDER BY " + ParcelTypeEntry.COLUMN_PARCELTYPE_ID_SERVER + " ASC";

        String [] args = new String[]{"1"};


        Cursor c;
        try {
            SelectToDBRaw selectParcelType = new SelectToDBRaw(query,args);
            c = selectParcelType.execute().get();
        }catch (Exception e){
            c = null;
        }

        MergeCursor mergeCursor = new MergeCursor(new Cursor[]{c,others});

        parceltypeAdapter = new SimpleCursorAdapter(getActivity(),R.layout.spinner_item,mergeCursor,fromColumnsParcelType,toViews,0);

        parceltypeAdapter.setDropDownViewResource(R.layout.spinner_item);

        parceltypeSpinner.setAdapter(parceltypeAdapter);
    }


}
