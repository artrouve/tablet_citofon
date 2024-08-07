package com.handsriver.concierge.parcels;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.updatesTables.UpdateExitParcel;
import com.handsriver.concierge.database.updatesTables.UpdateExitSuppliersVisit;
import com.handsriver.concierge.utilities.FormatICAO9303;
import com.handsriver.concierge.utilities.RutFormat;
import com.handsriver.concierge.utilities.Utility;
import com.handsriver.concierge.visits.Visit;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Created by alain_r._trouve_silva after 17-04-17.
 */

public class DialogExitParcelsRegister extends DialogFragment {
    String id_parcel;
    StringBuilder ocr;
    TextInputEditText textInputEditTextRut;
    TextInputEditText textInputEditTextFullName;
    TextView textViewDocumentNumber;
    TextView textViewFullName;
    Button buttonRegisterInfoExit;
    Boolean isMarkInfoExitParcel;
    Button buttonPositive;
    LinearLayout linearExitParcel;

    public static final String PREFS_NAME = "PorterPrefs";
    public static final int DEF_VALUE = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConciergeDbHelper helper= new ConciergeDbHelper(getContext());
        DatabaseManager.initializeInstance(helper);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ocr = new StringBuilder();

        SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        isMarkInfoExitParcel = settingsPrefs.getBoolean(getString(R.string.pref_id_info_exit_parcel_key),true);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(R.string.markExitParcelQuestion);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View content = inflater.inflate(R.layout.dialog_register_parcels_exit,null);

        builder.setView(content);

        id_parcel = getArguments().getString("id");

        builder.setTitle(R.string.confirmInfo);

        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (isMarkInfoExitParcel && textViewDocumentNumber.length() == 0 && textViewFullName.length() == 0){
                    Toast.makeText(getActivity().getApplicationContext(), "Ingrese al menos el RUT o Nombre Completo", Toast.LENGTH_LONG).show();
                    getDialog().dismiss();
                }
                else{
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                    String exit = df.format(calendar.getTime());

                    int porterIdServer;
                    SharedPreferences porterPrefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    porterIdServer = porterPrefs.getInt(getString(R.string.porterIdServerVar), DEF_VALUE);

                    String fullnameArg = textViewFullName.getText().toString();
                    String documentNumberArg = textViewDocumentNumber.getText().toString();

                    UpdateExitParcel updateExitParcel = new UpdateExitParcel(exit, porterIdServer, id_parcel, fullnameArg,documentNumberArg,getContext());
                    updateExitParcel.execute();

                    getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, getActivity().getIntent());
                }
            }
        });


        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
        if (isMarkInfoExitParcel){
            buttonPositive= alert.getButton(AlertDialog.BUTTON_POSITIVE);
            buttonPositive.setEnabled(false);
        }

        content.setFocusableInTouchMode(true);
        content.requestFocus();
        content.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return isScannerOCR(v,keyCode,event);
            }

        });
        linearExitParcel = (LinearLayout) content.findViewById(R.id.linearExitParcel);
        textInputEditTextRut = (TextInputEditText) content.findViewById(R.id.rutInput);
        textInputEditTextFullName = (TextInputEditText) content.findViewById(R.id.fullnameInput);
        textViewDocumentNumber = (TextView) content.findViewById(R.id.textViewfullName);
        textViewFullName = (TextView) content.findViewById(R.id.textViewDocumentNumber);
        buttonRegisterInfoExit = (Button) content.findViewById(R.id.buttonRegisterInfoExitParcel);

        textInputEditTextFullName.addTextChangedListener(inputAutomaticDocument);
        textInputEditTextRut.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keycode, KeyEvent event) {
                return isScannerOCR(v,keycode,event);
            }
        });


        textInputEditTextFullName.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keycode, KeyEvent event) {
                return isScannerOCR(v,keycode,event);
            }
        });


        textInputEditTextRut.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                textInputEditTextRut.removeTextChangedListener(this);
                RutFormat.rutFormatRealTime(s);
                textInputEditTextRut.addTextChangedListener(this);
            }
        });

        buttonRegisterInfoExit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                addItemManual();

            }
        });

        linearExitParcel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Utility.hideKeyboard(v,getContext());
                return false;
            }
        });

        return alert;
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

                    Visit newVisit = FormatICAO9303.formatDocument(string_scan);

                    if (newVisit == null)
                    {
                        Toast.makeText(getActivity().getApplicationContext(), "Lectura Errónea, Favor Escanear Nuevamente", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        textViewDocumentNumber.setText(newVisit.getDocumentNumber());
                        textViewFullName.setText(newVisit.getFullName());
                        if (isMarkInfoExitParcel){
                            buttonPositive.setEnabled(true);
                        }
                        textInputEditTextFullName.setText("");


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
                        Toast.makeText(getActivity().getApplicationContext(), "Lectura Errónea, Favor Escanear Nuevamente", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        textViewDocumentNumber.setText(newVisit.getDocumentNumber());
                        textViewFullName.setText(newVisit.getFullName());
                        if (isMarkInfoExitParcel){
                            buttonPositive.setEnabled(true);
                        }
                        ocr.setLength(0);
                    }
                }
            }
            return true;
        }

    }

    private void addItemManual (){

        if (textInputEditTextRut.length() == 0 && textInputEditTextFullName.length() == 0 && isMarkInfoExitParcel) {
            Toast.makeText(getActivity().getApplicationContext(), "Ingrese al menos el RUT o Nombre Completo", Toast.LENGTH_LONG).show();
            textViewDocumentNumber.setText("");
            textViewFullName.setText("");
            if (isMarkInfoExitParcel){
                buttonPositive.setEnabled(false);
            }
        }
        else
        {
            if (textInputEditTextFullName.length()!= 0 && textInputEditTextRut.length() == 0) {
                textViewDocumentNumber.setText("");
                textViewFullName.setText("");
                textViewFullName.setText(textInputEditTextFullName.getText().toString());
                textInputEditTextRut.getText().clear();
                textInputEditTextFullName.getText().clear();
                textInputEditTextRut.clearFocus();
                textInputEditTextFullName.clearFocus();
                if (isMarkInfoExitParcel){
                    buttonPositive.setEnabled(true);
                }
            }
            else if (textInputEditTextFullName.length() == 0 && textInputEditTextRut.length()>2) {
                if (RutFormat.checkRutDv(textInputEditTextRut.getText().toString())) {
                    textViewDocumentNumber.setText("");
                    textViewFullName.setText("");
                    textViewDocumentNumber.setText(textInputEditTextRut.getText().toString());
                    textInputEditTextRut.getText().clear();
                    textInputEditTextFullName.getText().clear();
                    textInputEditTextRut.clearFocus();
                    textInputEditTextFullName.clearFocus();
                    if (isMarkInfoExitParcel){
                        buttonPositive.setEnabled(true);

                    }
                }
                else{
                    Toast.makeText(getActivity().getApplicationContext(), "Rut incorrecto, verifique con el Receptor", Toast.LENGTH_LONG).show();
                    if (isMarkInfoExitParcel){
                        buttonPositive.setEnabled(false);
                    }
                }
            }else if (textInputEditTextFullName.length() == 0 && textInputEditTextRut.length()<=2) {
                Toast.makeText(getActivity().getApplicationContext(), "Rut demasiado corto, verifique con el Receptor", Toast.LENGTH_LONG).show();
                if (isMarkInfoExitParcel){
                    buttonPositive.setEnabled(false);
                }

            }else if (textInputEditTextFullName.length() != 0 && textInputEditTextRut.length()>2){
                if (RutFormat.checkRutDv(textInputEditTextRut.getText().toString())) {
                    textViewDocumentNumber.setText("");
                    textViewFullName.setText("");
                    textViewFullName.setText(textInputEditTextFullName.getText().toString());
                    textViewDocumentNumber.setText(textInputEditTextRut.getText().toString());
                    textInputEditTextRut.getText().clear();
                    textInputEditTextFullName.getText().clear();
                    textInputEditTextRut.clearFocus();
                    textInputEditTextFullName.clearFocus();
                    if (isMarkInfoExitParcel){
                        buttonPositive.setEnabled(true);
                    }

                }
                else{
                    Toast.makeText(getActivity().getApplicationContext(), "Rut incorrecto, verifique con el Receptor", Toast.LENGTH_LONG).show();
                    if (isMarkInfoExitParcel){
                        buttonPositive.setEnabled(false);

                    }
                }
            }
        }
    }
}
