package com.handsriver.concierge.residents;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
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
import android.widget.TextView;
import android.widget.Toast;

import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeContract.ApartmentEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.SelectToDB;
import com.handsriver.concierge.utilities.Utility;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Created by alain_r._trouve_silva after 07-06-17.
 */

public class ResidentVehicleRegisterFragment extends Fragment {
    AutoCompleteTextView autoCompleteApartment;
    TextInputEditText textPlate;

    ArrayList<String> autoCompleteApartmentContent;
    ArrayAdapter<String> autoCompleteApartmentAdapter;
    StringBuilder ocr;
    View rootView;
    LinearLayout linearResidentVehicle;

    public static final String IS_ACTIVE = "1";

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
        rootView = inflater.inflate(R.layout.fragment_register_residentsvehicles, container, false);


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

        if (c != null) {
            while (c.moveToNext()){
                autoCompleteApartmentContent.add(c.getString(c.getColumnIndex(ApartmentEntry.COLUMN_APARTMENT_NUMBER)));
            }
            c.close();
        }

        textPlate = (TextInputEditText) rootView.findViewById(R.id.plateResidentVehicleInput);

        autoCompleteApartment = (AutoCompleteTextView) rootView.findViewById(R.id.autoApartment);
        autoCompleteApartment.setThreshold(1);
        autoCompleteApartment.setAdapter(autoCompleteApartmentAdapter);

        autoCompleteApartment.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                autoCompleteApartment.clearFocus();
                Utility.hide_keyboard(getActivity(),autoCompleteApartment.getWindowToken());
            }
        });

        autoCompleteApartment.setValidator(new AutoCompleteTextView.Validator(){
            @Override
            public boolean isValid(CharSequence text) {
                Collections.sort(autoCompleteApartmentContent);
                if (Collections.binarySearch(autoCompleteApartmentContent, text.toString()) >= 0) {
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

        textPlate.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return Utility.isScannerOCR(v,keyCode,event);
            }
        });



        linearResidentVehicle = (LinearLayout) rootView.findViewById(R.id.linearResidentVehicle);
        linearResidentVehicle.setOnTouchListener(new View.OnTouchListener() {
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
        inflater.inflate(R.menu.app_bar_menu_residentsvehicles, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.registerResidentsVehiclesSave) {
            validateInfo();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void validateInfo(){
        autoCompleteApartment.clearFocus();
        String apartment = autoCompleteApartment.getText().toString();
        String plate = textPlate.getText().toString();

        if (apartment.length()>0 && plate.length() > 0 )
        {
            Bundle args = new Bundle();
            args.putString("apartment",apartment);
            args.putString("plate",plate);
            args.putString("active",IS_ACTIVE);

            DialogResidentsVehiclesRegister dialog = new DialogResidentsVehiclesRegister();
            dialog.setArguments(args);
            dialog.show(getFragmentManager(), "dialog");
        }
        else{
            if (apartment.length()>0 && plate.length() == 0 ) {
                Toast.makeText(getActivity().getApplicationContext(), "Falta ingresar la patente", Toast.LENGTH_LONG).show();
            }else if (apartment.length()==0 && plate.length() > 0){
                Toast.makeText(getActivity().getApplicationContext(), "Falta ingresar el número del departamento", Toast.LENGTH_LONG).show();
            }
            else if (apartment.length()==0 && plate.length() == 0){
                Toast.makeText(getActivity().getApplicationContext(), "Falta ingresar el número del departamento y patente", Toast.LENGTH_LONG).show();
            }
        }
    }

}
