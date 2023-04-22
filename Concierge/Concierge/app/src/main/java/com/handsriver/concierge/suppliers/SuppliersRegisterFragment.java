package com.handsriver.concierge.suppliers;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.utilities.FormatICAO9303;
import com.handsriver.concierge.R;
import com.handsriver.concierge.utilities.RutFormat;
import com.handsriver.concierge.database.ConciergeContract.*;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.SelectToDB;
import com.handsriver.concierge.utilities.Utility;
import com.handsriver.concierge.visits.Visit;
import com.handsriver.concierge.visits.VisitsAdapter;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by Created by alain_r._trouve_silva after 14-03-17.
 */

public class SuppliersRegisterFragment extends Fragment {

    Spinner suppliers;
    SimpleCursorAdapter suppliersAdapter;
    TextInputEditText textRut;
    TextInputEditText textFullName;
    TextInputEditText textLicensePlate;
    Button addVisitList;
    ListView visitList;
    ArrayList<Visit> arrayOfVisit;
    VisitsAdapter visitsAdapter;
    StringBuilder ocr;
    View rootView;
    LinearLayout linearSuppliers;

    public static final String IS_ACTIVE = "1";


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConciergeDbHelper helper= new ConciergeDbHelper(getContext());
        DatabaseManager.initializeInstance(helper);
        arrayOfVisit = new ArrayList<Visit>();
        visitsAdapter = new VisitsAdapter(getContext(),arrayOfVisit);
        ocr = new StringBuilder();
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_register_suppliers, container, false);
        rootView.setFocusableInTouchMode(true);
        rootView.requestFocus();
        rootView.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return isScannerOCR(v,keyCode,event);
            }

        });

        String[] projectionSpinner = {
                SupplierEntry._ID,
                SupplierEntry.COLUMN_SUPPLIER_ID_SERVER,
                SupplierEntry.COLUMN_NAME_SUPPLIER
        };

        String selection = SupplierEntry.COLUMN_ACTIVE + " = ?";
        String [] selectionArgs = {IS_ACTIVE};

        String sortOrderSpinner = SupplierEntry.COLUMN_NAME_SUPPLIER + " ASC";

        Cursor c;
        try {
            SelectToDB selectSuppliers = new SelectToDB(SupplierEntry.TABLE_NAME,projectionSpinner,selection,selectionArgs,null,null,sortOrderSpinner,null);
            c = selectSuppliers.execute().get();
        }catch (Exception e){
            c = null;
        }

        textRut = (TextInputEditText)rootView.findViewById(R.id.rutInput);
        textFullName = (TextInputEditText) rootView.findViewById(R.id.fullnameInput);
        textLicensePlate = (TextInputEditText) rootView.findViewById(R.id.vehicleInput);
        suppliers = (Spinner) rootView.findViewById(R.id.suppliersSpinner);

        String[] fromColumns = {
                SupplierEntry.COLUMN_NAME_SUPPLIER
        };

        int[] toViews = {
                R.id.itemSpinner
        };

        suppliersAdapter = new SimpleCursorAdapter(getActivity(),R.layout.spinner_item,c,fromColumns,toViews,0);

        suppliersAdapter.setDropDownViewResource(R.layout.spinner_item);

        suppliers.setAdapter(suppliersAdapter);

        textFullName.addTextChangedListener(inputAutomaticDocument);;
        textRut.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keycode, KeyEvent event) {
                return isScannerOCR(v,keycode,event);
            }
        });
        textFullName.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keycode, KeyEvent event) {
                return isScannerOCR(v,keycode,event);
            }
        });
        textLicensePlate.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keycode, KeyEvent event) {
                return isScannerOCR(v,keycode,event);
            }
        });
        suppliers.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keycode, KeyEvent event) {
                return isScannerOCR(v,keycode,event);
            }
        });

        addVisitList = (Button) rootView.findViewById(R.id.addVisitList);
        visitList = (ListView) rootView.findViewById(R.id.visitList);

        visitList.setAdapter(visitsAdapter);

        textRut.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                textRut.removeTextChangedListener(this);
                RutFormat.rutFormatRealTime(s);
                textRut.addTextChangedListener(this);
            }
        });

        addVisitList.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                Visit newVisit = new Visit();
                addItemManual(newVisit);

            }
        });

        linearSuppliers = (LinearLayout) rootView.findViewById(R.id.linearSuppliers);

        linearSuppliers.setOnTouchListener(new View.OnTouchListener() {
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
        inflater.inflate(R.menu.app_bar_menu_suppliers, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.registerSuppliersSave) {
            validateInfo();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    private TextWatcher inputAutomaticDocument = new TextWatcher() {
        public void afterTextChanged(Editable s) { }
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        { }
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            String string_scan = s.toString();
            if(!string_scan.equals("")){

                char ini = string_scan.charAt(0);
                if(ini == '_'){

                    string_scan = string_scan.substring(1,string_scan.length());
                    Visit newVisit = FormatICAO9303.formatDocument(string_scan.toString());

                    if (newVisit == null)
                    {
                        textFullName.setText("");
                        Toast.makeText(getActivity().getApplicationContext(), "Lectura Errónea, Favor Escanear Nuevamente", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        newVisit.setFullName(newVisit.getFullName());
                        newVisit.setDocumentNumber(newVisit.getDocumentNumber());
                        visitsAdapter.add(newVisit);
                        textFullName.setText("");
                    }
                }

            }

        }
    };

    private boolean isScannerOCR(View v, int keycode, KeyEvent event){

        if (event.getDeviceId() == KeyCharacterMap.VIRTUAL_KEYBOARD) {
            return false;
        }
        else{
            if (event.getAction() == KeyEvent.ACTION_DOWN && keycode != KeyEvent.KEYCODE_SHIFT_LEFT)
            {
                ocr.append((char)event.getUnicodeChar());
                if (keycode == KeyEvent.KEYCODE_ENTER)
                {
                    Utility.hideKeyboard(v,getContext());

                    Visit newVisit = FormatICAO9303.formatDocument(ocr.toString());

                    if (newVisit == null)
                    {
                        ocr.setLength(0);
                        Toast.makeText(getActivity().getApplicationContext(), "Lectura Errónea, Favor Escanear Nuevamente", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        newVisit.setFullName(newVisit.getFullName());
                        newVisit.setDocumentNumber(newVisit.getDocumentNumber());
                        visitsAdapter.add(newVisit);
                        ocr.setLength(0);
                    }
                }
            }
            return true;
        }

    }

    private void addItemManual (Visit newVisit){
        if (textRut.length() == 0 && textFullName.length() == 0) {
            Toast.makeText(getActivity().getApplicationContext(), "Ingrese al menos el RUT o Nombre Completo", Toast.LENGTH_LONG).show();
        }
        else
        {
            if (textFullName.length()!= 0 && textRut.length() == 0) {
                newVisit.setFullName(textFullName.getText().toString());
                visitsAdapter.add(newVisit);
                textFullName.getText().clear();
            }
            else if (textFullName.length() == 0 && textRut.length()>2) {
                if (RutFormat.checkRutDv(textRut.getText().toString())) {
                    newVisit.setDocumentNumber(textRut.getText().toString());
                    visitsAdapter.add(newVisit);
                    textRut.getText().clear();
                }
                else{
                    Toast.makeText(getActivity().getApplicationContext(), "Rut incorrecto, verifique con la Visita", Toast.LENGTH_LONG).show();
                }
            }else if (textFullName.length() == 0 && textRut.length()<=2) {
                Toast.makeText(getActivity().getApplicationContext(), "Rut demasiado corto, verifique con la Visita", Toast.LENGTH_LONG).show();
            }else if (textFullName.length() != 0 && textRut.length()>2){
                if (RutFormat.checkRutDv(textRut.getText().toString())) {
                    newVisit.setDocumentNumber(textRut.getText().toString());
                    newVisit.setFullName(textFullName.getText().toString());
                    visitsAdapter.add(newVisit);
                    textRut.getText().clear();
                    textFullName.getText().clear();
                }
                else{
                    Toast.makeText(getActivity().getApplicationContext(), "Rut incorrecto, verifique con la Visita", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void validateInfo(){
        Cursor c = (Cursor)suppliers.getSelectedItem();
        String supplier = c.getString(c.getColumnIndex("name_supplier"));
        String licensePlate = textLicensePlate.getText().toString();

        if (supplier.length()>0 && arrayOfVisit.size()>0)
        {
            Bundle args = new Bundle();
            args.putParcelableArrayList("visitList",arrayOfVisit);
            args.putString("supplier",supplier);
            args.putString("licensePlate",licensePlate);

            DialogSuppliersRegister dialog = new DialogSuppliersRegister();
            dialog.setArguments(args);
            dialog.show(getFragmentManager(), "dialog");
        }
        else{
            if (supplier.length()>0 && arrayOfVisit.isEmpty())
            {
                Toast.makeText(getActivity().getApplicationContext(), "Falta ingresar por lo menos una visita", Toast.LENGTH_LONG).show();
            }
            else if (supplier.length()==0 && !arrayOfVisit.isEmpty())
            {
                Toast.makeText(getActivity().getApplicationContext(), "Falta ingresar un proveedor", Toast.LENGTH_LONG).show();
            }
            else if (supplier.length()==0 && arrayOfVisit.isEmpty())
            {
                Toast.makeText(getActivity().getApplicationContext(), "Falta ingresar por lo menos una visita y un proveedor", Toast.LENGTH_LONG).show();
            }
        }
    }
}
