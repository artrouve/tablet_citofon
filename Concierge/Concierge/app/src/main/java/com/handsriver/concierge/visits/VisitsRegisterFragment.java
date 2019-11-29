package com.handsriver.concierge.visits;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.handsriver.concierge.database.ConciergeContract.BlacklistEntry;
import com.handsriver.concierge.database.ConciergeContract.WhitelistEntry;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.SelectToDBRaw;
import com.handsriver.concierge.utilities.FormatICAO9303;
import com.handsriver.concierge.R;
import com.handsriver.concierge.utilities.LicensePlateCheck;
import com.handsriver.concierge.utilities.RutFormat;
import com.handsriver.concierge.database.ConciergeContract.ApartmentEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.SelectToDB;
import com.handsriver.concierge.utilities.Utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Created by alain_r._trouve_silva after 27-01-17.
 */

public class VisitsRegisterFragment extends Fragment{

    AutoCompleteTextView autoCompleteApartment;
    TextInputEditText textRut;
    TextInputEditText textFullName;
    TextInputEditText textLicensePlate;
    TextView textViewParkingLots;
    Button addVisitList;
    ListView visitList;
    String parkingLots;
    ArrayList<Visit> arrayOfVisit;
    ArrayList<String> spinnerContent;
    ArrayAdapter<String> spinneradapter;
    ArrayAdapter<String> parkingAdapter;
    VisitsAdapter visitsAdapter;
    StringBuilder ocr;
    View rootView;
    Spinner spinnerParkingLots;
    List<String> items;
    LinearLayout linearLayout;


    public static final String MANUAL_ONLY_RUT = "MANUAL_ONLY_RUT";
    public static final String MANUAL_ALL = "MANUAL_ALL";
    public static final String AUTOMATIC = "AUTOMATIC";
    public static final String IS_ACTIVE = "1";


    public static final int DIALOG_FRAGMENT = 0;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConciergeDbHelper helper= new ConciergeDbHelper(getContext());
        DatabaseManager.initializeInstance(helper);
        arrayOfVisit = new ArrayList<Visit>();
        spinnerContent = new ArrayList<String>();
        visitsAdapter = new VisitsAdapter(getContext(),arrayOfVisit);
        spinneradapter = new ArrayAdapter<String>(getActivity(),R.layout.spinner_item,spinnerContent);

        ocr = new StringBuilder();
        setHasOptionsMenu(true);

        SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        parkingLots = settingsPrefs.getString(getString(R.string.pref_id_parking_key),"");

        if (parkingLots.length() > 0){
            items = Arrays.asList(parkingLots.split("\\s*,\\s*"));
            items = new ArrayList<String>(items);
            parkingAdapter = new ArrayAdapter<String>(getActivity(),R.layout.spinner_item,items);

        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        rootView = inflater.inflate(R.layout.fragment_register_visits, container, false);
        rootView.setFocusableInTouchMode(true);
        rootView.requestFocus();
        rootView.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return isScannerOCR(v,keyCode,event);
            }

        });


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
                spinnerContent.add(c.getString(c.getColumnIndex(ApartmentEntry.COLUMN_APARTMENT_NUMBER)));
            }
            c.close();
        }


        textRut = (TextInputEditText)rootView.findViewById(R.id.rutInput);
        textFullName = (TextInputEditText) rootView.findViewById(R.id.fullnameInput);
        textLicensePlate = (TextInputEditText) rootView.findViewById(R.id.vehicleInput);
        autoCompleteApartment = (AutoCompleteTextView) rootView.findViewById(R.id.autoApartment);
        spinnerParkingLots = (Spinner) rootView.findViewById(R.id.spinnerParkingLots);
        textViewParkingLots = (TextView) rootView.findViewById(R.id.textViewParkingLots);
        spinnerParkingLots.setAdapter(parkingAdapter);

        if (parkingLots.length() > 0){
            spinnerParkingLots.setVisibility(View.VISIBLE);
            textViewParkingLots.setVisibility(View.VISIBLE);
        }

        if (args != null){
            autoCompleteApartment.setText(args.getString("apartmentNumber"));
        }

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
        autoCompleteApartment.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keycode, KeyEvent event) {
                return isScannerOCR(v,keycode,event);
            }
        });

        addVisitList = (Button) rootView.findViewById(R.id.addVisitList);
        visitList = (ListView) rootView.findViewById(R.id.visitList);

        visitList.setAdapter(visitsAdapter);

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

                addItemManual();
                Utility.hideKeyboard(view,getContext());

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


        linearLayout = (LinearLayout) rootView.findViewById(R.id.linearVisit);

        linearLayout.setOnTouchListener(new View.OnTouchListener() {
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
        inflater.inflate(R.menu.app_bar_menu_visits, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.registerVisitsSave) {
            validateInfo();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

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
                        Toast.makeText(getActivity().getApplicationContext(), "Lectura Errónea, Favor Escanear Nuevamente", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        if(whitelistVerified(newVisit.getDocumentNumber())){
                            dialogWhitelist(newVisit,AUTOMATIC);
                        }else{
                            if(blacklistVerified(newVisit.getDocumentNumber())){
                                dialogBlacklist(newVisit,AUTOMATIC);
                            }
                            else{
                                visitsAdapter.add(newVisit);
                                ocr.setLength(0);
                            }
                        }

                    }
                }
            }
            return true;
        }

    }

    private void addItemManual (){
        Visit newVisit = new Visit();

        if (textRut.length() == 0 && textFullName.length() == 0) {
            Toast.makeText(getActivity().getApplicationContext(), "Ingrese al menos el RUT o Nombre Completo", Toast.LENGTH_LONG).show();
        }
        else
        {
            if (textFullName.length()!= 0 && textRut.length() == 0) {
                newVisit.setFullName(textFullName.getText().toString());
                visitsAdapter.add(newVisit);
                textRut.getText().clear();
                textFullName.getText().clear();
            }
            else if (textFullName.length() == 0 && textRut.length()>2) {
                if (RutFormat.checkRutDv(textRut.getText().toString())) {
                    newVisit.setDocumentNumber(textRut.getText().toString());
                    if(whitelistVerified(newVisit.getDocumentNumber())){
                        dialogWhitelist(newVisit,MANUAL_ONLY_RUT);
                    }
                    else{
                        if (blacklistVerified(newVisit.getDocumentNumber())){
                            dialogBlacklist(newVisit,MANUAL_ONLY_RUT);
                        }
                        else{
                            visitsAdapter.add(newVisit);
                            textRut.getText().clear();
                            textFullName.getText().clear();
                        }
                    }
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

                    if(whitelistVerified(newVisit.getDocumentNumber())){
                        dialogWhitelist(newVisit,MANUAL_ALL);
                    }
                    else{
                        if (blacklistVerified(newVisit.getDocumentNumber())){
                            dialogBlacklist(newVisit,MANUAL_ALL);
                        }
                        else{
                            visitsAdapter.add(newVisit);
                            textRut.getText().clear();
                            textFullName.getText().clear();
                        }
                    }
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
        String licensePlate = textLicensePlate.getText().toString();

        if (apartment.length()>0 && arrayOfVisit.size()>0)
        {
            Bundle args = new Bundle();
            args.putParcelableArrayList("visitList",arrayOfVisit);

            args.putString("apartment",apartment);
            args.putString("licensePlate",licensePlate);

            if (parkingLots.equals("")){
                args.putString("spinnerParkingSelected",null);
            }
            else{
                args.putString("spinnerParkingSelected",spinnerParkingLots.getSelectedItem().toString());
            }

            DialogVisitsRegister dialog = new DialogVisitsRegister();
            dialog.setArguments(args);
            dialog.show(getFragmentManager(), "dialog");
        }
        else{
            if (apartment.length()>0 && arrayOfVisit.isEmpty())
            {
                Toast.makeText(getActivity().getApplicationContext(), "Falta ingresar por lo menos una visita", Toast.LENGTH_LONG).show();
            }
            else if (apartment.length()==0 && !arrayOfVisit.isEmpty())
            {
                Toast.makeText(getActivity().getApplicationContext(), "Falta ingresar el número de departamento", Toast.LENGTH_LONG).show();
            }
            else if (apartment.length()==0 && arrayOfVisit.isEmpty())
            {
                Toast.makeText(getActivity().getApplicationContext(), "Falta ingresar por lo menos una visita y el número de departamento", Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean blacklistVerified(String documentNumber){
        final String query = "SELECT " + BlacklistEntry.COLUMN_DOCUMENT_NUMBER +
                " FROM " + BlacklistEntry.TABLE_NAME;

        Cursor c = null;
        try {
            SelectToDBRaw selectBlacklist = new SelectToDBRaw(query,null);
            c = selectBlacklist.execute().get();
        }catch (Exception e) {
            return false;
        }

        if (c != null) {
            if (c.moveToNext()){
                if (c.getString(c.getColumnIndex(BlacklistEntry.COLUMN_DOCUMENT_NUMBER)).replace(".","").replace("-","").equals(documentNumber.replace(".","").replace("-",""))){
                    return true;
                }
            }
            c.close();
        }
        return false;
    }

    private boolean whitelistVerified(String documentNumber){
        final String query = "SELECT " + WhitelistEntry.COLUMN_DOCUMENT_NUMBER +
                " FROM " + WhitelistEntry.TABLE_NAME;

        Cursor c;
        try {
            SelectToDBRaw selectWhitelist = new SelectToDBRaw(query,null);
            c = selectWhitelist.execute().get();
        } catch (Exception e){
            return false;
        }

        if (c != null) {
            if (c.moveToNext()){
                if (c.getString(c.getColumnIndex(WhitelistEntry.COLUMN_DOCUMENT_NUMBER)).replace(".","").replace("-","").equals(documentNumber.replace(".","").replace("-",""))){
                    return true;
                }
            }
            c.close();
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String white = "white";
        String black = "black";

        switch (requestCode){
            case DIALOG_FRAGMENT:
                if (data.getStringExtra("list").equals(black)){
                    if (resultCode == Activity.RESULT_OK){
                        Visit newVisit = data.getParcelableExtra("visit");
                        String type = data.getStringExtra("type");
                        if (type.equals(AUTOMATIC)){
                            visitsAdapter.add(newVisit);
                            ocr.setLength(0);
                        } else if (type.equals(MANUAL_ONLY_RUT) || type.equals(MANUAL_ALL)){
                            visitsAdapter.add(newVisit);
                            textRut.getText().clear();
                            textFullName.getText().clear();
                        }
                    }
                    if (resultCode == Activity.RESULT_CANCELED){
                        String type = data.getStringExtra("type");
                        if (type.equals(AUTOMATIC)){
                            ocr.setLength(0);
                        }
                        else if (type.equals(MANUAL_ONLY_RUT) || type.equals(MANUAL_ALL)){
                            textRut.getText().clear();
                            textFullName.getText().clear();
                        }
                    }
                }
                else if (data.getStringExtra("list").equals(white)){
                    if (resultCode == Activity.RESULT_OK){
                        Visit newVisit = data.getParcelableExtra("visit");
                        String type = data.getStringExtra("type");
                        if (type.equals(AUTOMATIC)){
                            visitsAdapter.add(newVisit);
                            ocr.setLength(0);
                        } else if (type.equals(MANUAL_ONLY_RUT) || type.equals(MANUAL_ALL)){
                            visitsAdapter.add(newVisit);
                            textRut.getText().clear();
                            textFullName.getText().clear();
                        }
                    }
                }
                break;
        }
    }

    private void dialogBlacklist(Visit newVisit, String type){
        Bundle args = new Bundle();
        args.putString("type",type);
        DialogVisitsBlacklistAddList dialog = new DialogVisitsBlacklistAddList();
        dialog.setCancelable(false);
        dialog.setVisit(newVisit);
        dialog.setArguments(args);
        dialog.setTargetFragment(VisitsRegisterFragment.this,0);
        dialog.show(getFragmentManager(), "dialog");
    }

    private void dialogWhitelist(Visit newVisit, String type){
        Bundle args = new Bundle();
        args.putString("type",type);
        DialogVisitsWhitelistAddList dialog = new DialogVisitsWhitelistAddList();
        dialog.setCancelable(false);
        dialog.setVisit(newVisit);
        dialog.setArguments(args);
        dialog.setTargetFragment(VisitsRegisterFragment.this,0);
        dialog.show(getFragmentManager(), "dialog");
    }
}
