package com.handsriver.concierge.residents;

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
import com.handsriver.concierge.database.ConciergeContract.ApartmentEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.SelectToDB;
import com.handsriver.concierge.database.SelectToDBRaw;
import com.handsriver.concierge.parcels.DialogParcelsRegister;
import com.handsriver.concierge.utilities.Utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

/**
 * Created by Created by alain_r._trouve_silva after 07-06-17.
 */

public class ResidentRegisterFragment extends Fragment {
    AutoCompleteTextView autoCompleteApartment;
    TextInputEditText textFullname;
    TextInputEditText textEmail;
    ArrayList<String> autoCompleteApartmentContent;
    ArrayAdapter<String> autoCompleteApartmentAdapter;
    StringBuilder ocr;
    View rootView;
    LinearLayout linearResident;

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
        rootView = inflater.inflate(R.layout.fragment_register_residents, container, false);


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

        textFullname = (TextInputEditText) rootView.findViewById(R.id.fullnameResidentInput);
        textEmail = (TextInputEditText) rootView.findViewById(R.id.emailResidentInput);

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

        textFullname.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return Utility.isScannerOCR(v,keyCode,event);
            }
        });

        textEmail.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return Utility.isScannerOCR(v,keyCode,event);
            }
        });

        linearResident = (LinearLayout) rootView.findViewById(R.id.linearResident);
        linearResident.setOnTouchListener(new View.OnTouchListener() {
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
        inflater.inflate(R.menu.app_bar_menu_residents, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.registerResidentsSave) {
            validateInfo();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void validateInfo(){
        autoCompleteApartment.clearFocus();
        String apartment = autoCompleteApartment.getText().toString();
        String fullName = textFullname.getText().toString();
        String email = textEmail.getText().toString();

        if (apartment.length()>0 && fullName.length() > 0 && Utility.isValidEmail(email))
        {
            Bundle args = new Bundle();
            args.putString("apartment",apartment);
            args.putString("fullName",fullName);
            args.putString("email",email);

            DialogResidentsRegister dialog = new DialogResidentsRegister();
            dialog.setArguments(args);
            dialog.show(getFragmentManager(), "dialog");
        }
        else{
            if (apartment.length()>0 && fullName.length() == 0 && Utility.isValidEmail(email)) {
                Toast.makeText(getActivity().getApplicationContext(), "Falta ingresar el número de residente", Toast.LENGTH_LONG).show();
            }else if (apartment.length()==0 && fullName.length() > 0 && Utility.isValidEmail(email)){
                Toast.makeText(getActivity().getApplicationContext(), "Falta ingresar el nombre del departamento", Toast.LENGTH_LONG).show();
            }else if (apartment.length()>0 && fullName.length() > 0 && !Utility.isValidEmail(email)){
                Toast.makeText(getActivity().getApplicationContext(), "Ingrese un correo válido", Toast.LENGTH_LONG).show();
            }else if (apartment.length()==0 && fullName.length() == 0 && !Utility.isValidEmail(email)){
                Toast.makeText(getActivity().getApplicationContext(), "Falta ingresar el nombre del residente, el número del departamento y un correo válido", Toast.LENGTH_LONG).show();
            }else if (apartment.length()==0 && fullName.length() == 0 && Utility.isValidEmail(email)){
                Toast.makeText(getActivity().getApplicationContext(), "Falta ingresar el nombre del residente y el número del departamento", Toast.LENGTH_LONG).show();
            }else if (apartment.length()==0 && fullName.length() > 0 && !Utility.isValidEmail(email)){
                Toast.makeText(getActivity().getApplicationContext(), "Falta ingresar el número del departamento y un correo válido ", Toast.LENGTH_LONG).show();
            }else if (apartment.length()>0 && fullName.length() == 0 && !Utility.isValidEmail(email)){
                Toast.makeText(getActivity().getApplicationContext(), "Falta ingresar el nombre del residente y un correo válido ", Toast.LENGTH_LONG).show();
            }
        }
    }

}
