package com.handsriver.concierge.commonspaces;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
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
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeContract;
import com.handsriver.concierge.database.ConciergeContract.CommonspaceEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.SelectToDB;
import com.handsriver.concierge.database.SelectToDBRaw;
import com.handsriver.concierge.suppliers.DialogSuppliersRegister;
import com.handsriver.concierge.suppliers.SupplierAdapterSearchList;
import com.handsriver.concierge.suppliers.SupplierVisit;
import com.handsriver.concierge.utilities.FormatICAO9303;
import com.handsriver.concierge.utilities.RutFormat;
import com.handsriver.concierge.utilities.Utility;
import com.handsriver.concierge.visits.Visit;
import com.handsriver.concierge.visits.VisitsAdapter;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Created by alain_r._trouve_silva after 14-03-17.
 */

public class CommonspacesvisitRegisterFragment extends Fragment {
    AutoCompleteTextView autoCompleteApartment;
    Spinner commonspaces;

    ArrayAdapter<String> spinneradapter;
    ArrayList<String> spinnerContent;

    SimpleCursorAdapter commonspacesAdapter;
    TextInputEditText textRut;
    TextInputEditText textFullName;
    TextInputEditText textLicensePlate;
    TextView textCommonspacesvisitsAforo;

    Button addVisitList;
    ListView visitList;
    ArrayList<Visit> arrayOfVisit;
    VisitsAdapter visitsAdapter;
    StringBuilder ocr;
    View rootView;
    LinearLayout linearCommonspacesvisits;

    public static final String IS_ACTIVE = "1";


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConciergeDbHelper helper= new ConciergeDbHelper(getContext());
        DatabaseManager.initializeInstance(helper);
        spinnerContent = new ArrayList<String>();
        spinneradapter = new ArrayAdapter<String>(getActivity(),R.layout.spinner_item,spinnerContent);
        arrayOfVisit = new ArrayList<Visit>();
        visitsAdapter = new VisitsAdapter(getContext(),arrayOfVisit);
        ocr = new StringBuilder();
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_register_commonspacesvisits, container, false);
        rootView.setFocusableInTouchMode(true);
        rootView.requestFocus();
        rootView.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return isScannerOCR(v,keyCode,event);
            }

        });

        String[] projectionSpinner = {
                CommonspaceEntry._ID,
                CommonspaceEntry.COLUMN_COMMONSPACE_ID_SERVER,
                CommonspaceEntry.COLUMN_NAME_COMMONSPACE,
                CommonspaceEntry.COLUMN_AFORO,

        };

        String selection = CommonspaceEntry.COLUMN_ACTIVE + " = ?";
        String [] selectionArgs = {IS_ACTIVE};

        String sortOrderSpinner = CommonspaceEntry.COLUMN_NAME_COMMONSPACE + " ASC";

        Cursor c;
        try {
            SelectToDB selectCommonspaces = new SelectToDB(CommonspaceEntry.TABLE_NAME,projectionSpinner,selection,selectionArgs,null,null,sortOrderSpinner,null);
            c = selectCommonspaces.execute().get();
        }catch (Exception e){
            c = null;
        }


        String[] projectionApartment = {
                ConciergeContract.ApartmentEntry.COLUMN_APARTMENT_ID_SERVER,
                ConciergeContract.ApartmentEntry.COLUMN_APARTMENT_NUMBER
        };
        String selectionApartment = ConciergeContract.ApartmentEntry.COLUMN_ACTIVE + " = ?";
        String [] selectionArgsApartment = {IS_ACTIVE};

        String sortOrder = ConciergeContract.ApartmentEntry.COLUMN_APARTMENT_NUMBER + " ASC";

        Cursor c_apartment;
        try {
            SelectToDB selectApartment = new SelectToDB(ConciergeContract.ApartmentEntry.TABLE_NAME,projectionApartment,selectionApartment,selectionArgsApartment,null,null,sortOrder,null);
            c_apartment = selectApartment.execute().get();
        }catch (Exception e){
            c_apartment = null;
        }

        if (c_apartment != null){
            while (c_apartment.moveToNext()){
                spinnerContent.add(c_apartment.getString(c_apartment.getColumnIndex(ConciergeContract.ApartmentEntry.COLUMN_APARTMENT_NUMBER)));
            }
            c_apartment.close();
        }



        textRut = (TextInputEditText)rootView.findViewById(R.id.rutInput);
        textFullName = (TextInputEditText) rootView.findViewById(R.id.fullnameInput);
        textLicensePlate = (TextInputEditText) rootView.findViewById(R.id.vehicleInput);
        autoCompleteApartment = (AutoCompleteTextView) rootView.findViewById(R.id.autoApartment);
        commonspaces = (Spinner) rootView.findViewById(R.id.commonspacesvisitsSpinner);
        textCommonspacesvisitsAforo = (TextView) rootView.findViewById(R.id.commonspacesvisitsAforo);



        String[] fromColumns = {
                CommonspaceEntry.COLUMN_NAME_COMMONSPACE
        };

        int[] toViews = {
                R.id.itemSpinner
        };

        commonspacesAdapter = new SimpleCursorAdapter(getActivity(),R.layout.spinner_item,c,fromColumns,toViews,0);

        commonspacesAdapter.setDropDownViewResource(R.layout.spinner_item);

        commonspaces.setAdapter(commonspacesAdapter);

        textFullName.addTextChangedListener(inputAutomaticDocument);

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
        commonspaces.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keycode, KeyEvent event) {
                return isScannerOCR(v,keycode,event);
            }
        });


        commonspaces.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // Get the spinner selected item text
                Cursor selectedItemText = (Cursor) adapterView.getItemAtPosition(i);
                Integer commonspaceId = (Integer) selectedItemText.getInt(selectedItemText.getColumnIndex(CommonspaceEntry._ID));
                Integer VisitsWithoutExit = 0;
                String txtaforo = (String) selectedItemText.getString(selectedItemText.getColumnIndex(CommonspaceEntry.COLUMN_AFORO));

                String query = "SELECT count(*) FROM " + ConciergeContract.CommonspaceVisitsEntry.TABLE_NAME +
                " WHERE " + ConciergeContract.CommonspaceVisitsEntry.COLUMN_EXIT_VISIT + " IS NULL "+
                " AND " + ConciergeContract.CommonspaceVisitsEntry.COLUMN_COMMONSPACE_ID + " = " + commonspaceId;

                Cursor c;
                try {
                    SelectToDBRaw countWithoutExit = new SelectToDBRaw(query,null);
                    c = countWithoutExit.execute().get();
                }catch (Exception e){
                    c = null;
                }

                if (c != null) {
                    while (c.moveToNext()){

                        VisitsWithoutExit = c.getInt(0);

                    }
                    c.close();

                }

                // Display the selected item into the TextView

                textCommonspacesvisitsAforo.setText("Aforo : " + VisitsWithoutExit.toString() + " de " + txtaforo);

                if(VisitsWithoutExit>= Integer.parseInt(txtaforo)){
                    textCommonspacesvisitsAforo.setTextColor(Color.RED);
                }
                else{
                    textCommonspacesvisitsAforo.setTextColor(Color.GREEN);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

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

        linearCommonspacesvisits = (LinearLayout) rootView.findViewById(R.id.linearCommonspacesvisits);

        linearCommonspacesvisits.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Utility.hideKeyboard(v,getContext());
                return false;
            }
        });

        autoCompleteApartment.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keycode, KeyEvent event) {
                return isScannerOCR(v,keycode,event);
            }
        });


        autoCompleteApartment.setThreshold(1);
        autoCompleteApartment.setAdapter(spinneradapter);

        autoCompleteApartment.setValidator(new AutoCompleteTextView.Validator(){
            @Override
            public boolean isValid(CharSequence text) {
                Collections.sort(spinnerContent);
                if (Collections.binarySearch(spinnerContent, text.toString()) >= 0) {
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

        autoCompleteApartment.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Utility.hide_keyboard(getActivity(),autoCompleteApartment.getWindowToken());
            }
        });


        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.app_bar_menu_commonspacesvisits, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.registerCommonspacesvisitsSave) {
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
                if(ini == '_' || string_scan.contains("<")){

                    if(ini == '_'){
                        string_scan = string_scan.substring(1,string_scan.length());
                    }

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
        autoCompleteApartment.clearFocus();
        String apartment = autoCompleteApartment.getText().toString();


        Cursor c = (Cursor)commonspaces.getSelectedItem();
        String commonspace = c.getString(c.getColumnIndex("name_commonspace"));
        String licensePlate = textLicensePlate.getText().toString();

        if (apartment.length()>0 && commonspace.length()>0 && arrayOfVisit.size()>0)
        {
            Bundle args = new Bundle();
            args.putParcelableArrayList("visitList",arrayOfVisit);
            args.putString("apartment",apartment);
            args.putString("commonspace",commonspace);
            args.putString("licensePlate",licensePlate);


            DialogCommonspacesvisitsRegister dialog = new DialogCommonspacesvisitsRegister();
            dialog.setArguments(args);
            dialog.show(getFragmentManager(), "dialog");
        }
        else{
            if (apartment.length()>0 && commonspace.length()>0 && arrayOfVisit.isEmpty())
            {
                Toast.makeText(getActivity().getApplicationContext(), "Falta ingresar por lo menos una visita", Toast.LENGTH_LONG).show();
            }
            else if(apartment.length()==0){
                Toast.makeText(getActivity().getApplicationContext(), "Falta ingresar un departamento", Toast.LENGTH_LONG).show();
            }
            else if (commonspace.length()==0 && !arrayOfVisit.isEmpty())
            {
                Toast.makeText(getActivity().getApplicationContext(), "Falta ingresar un Espacio Común", Toast.LENGTH_LONG).show();
            }
            else if (commonspace.length()==0 && arrayOfVisit.isEmpty())
            {
                Toast.makeText(getActivity().getApplicationContext(), "Falta ingresar por lo menos una visita y un Espacio Común", Toast.LENGTH_LONG).show();
            }
        }
    }
}
