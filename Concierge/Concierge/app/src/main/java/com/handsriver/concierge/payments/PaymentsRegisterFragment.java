package com.handsriver.concierge.payments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.handsriver.concierge.database.ConciergeContract;
import com.handsriver.concierge.database.ConciergeContract.ApartmentEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.SelectToDB;
import com.handsriver.concierge.database.SelectToDBRaw;
import com.handsriver.concierge.parcels.DialogParcelsRegister;
import com.handsriver.concierge.utilities.Utility;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/**
 * Created by Created by alain_r._trouve_silva after 08-04-17.
 */

public class PaymentsRegisterFragment extends Fragment {
    AutoCompleteTextView autoCompleteApartment;
    TextInputEditText editTextDocumentNumber;
    TextInputEditText editTextDocumentNumberBuilding;
    TextInputEditText editTextAmount;
    ArrayList<String> autoCompleteApartmentContent;
    ArrayAdapter<String> autoCompleteApartmentAdapter;
    Spinner paymentTypeSpinner;
    ArrayAdapter<String> arrayAdapterPaymentType;
    View rootView;
    LinearLayout linearPayments;

    public static final String IS_ACTIVE = "1";


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConciergeDbHelper helper= new ConciergeDbHelper(getContext());
        DatabaseManager.initializeInstance(helper);
        autoCompleteApartmentContent = new ArrayList<String>();
        autoCompleteApartmentAdapter = new ArrayAdapter<String>(getActivity(),R.layout.spinner_item,autoCompleteApartmentContent);
        arrayAdapterPaymentType = new ArrayAdapter<String>(getActivity(),R.layout.spinner_item,getResources().getStringArray(R.array.paymentType));
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_register_payments, container, false);


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


        editTextDocumentNumber = (TextInputEditText) rootView.findViewById(R.id.textInputPaymentNumDoc);
        editTextDocumentNumberBuilding = (TextInputEditText) rootView.findViewById(R.id.textInputPaymentNumDocBuilding);
        editTextAmount = (TextInputEditText) rootView.findViewById(R.id.textInputPaymentAmount);


        paymentTypeSpinner = (Spinner) rootView.findViewById(R.id.paymentTypeSpinner);
        paymentTypeSpinner.setAdapter(arrayAdapterPaymentType);

        autoCompleteApartment = (AutoCompleteTextView) rootView.findViewById(R.id.autoApartment);
        autoCompleteApartment.setThreshold(1);
        autoCompleteApartment.setAdapter(autoCompleteApartmentAdapter);

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

        editTextAmount.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().equals(current)){
                    editTextAmount.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[$,.]", "");

                    DecimalFormat formatted = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                    DecimalFormatSymbols symbols = formatted.getDecimalFormatSymbols();
                    symbols.setGroupingSeparator('.');
                    symbols.setDecimalSeparator(',');
                    formatted.setDecimalFormatSymbols(symbols);
                    formatted.setMaximumFractionDigits(0);

                    if (cleanString.length() > 0){
                        current = formatted.format(Long.parseLong(cleanString));
                    }
                    else {
                        current = "";
                    }
                    editTextAmount.setText(current);
                    editTextAmount.setSelection(current.length());

                    editTextAmount.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        linearPayments = (LinearLayout) rootView.findViewById(R.id.linearPayments);
        linearPayments.setOnTouchListener(new View.OnTouchListener() {
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
        inflater.inflate(R.menu.app_bar_menu_payments, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.registerPaymentSave) {
            validateInfo();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void validateInfo(){
        autoCompleteApartment.clearFocus();
        String apartment = autoCompleteApartment.getText().toString();
        String amount = editTextAmount.getText().toString();
        String documentNumber = editTextDocumentNumber.getText().toString();
        String documentNumberBuilding = editTextDocumentNumberBuilding.getText().toString();
        String paymentType = paymentTypeSpinner.getSelectedItem().toString();
        String paymentTypeIndex = String.valueOf(paymentTypeSpinner.getSelectedItemPosition());


        if (apartment.length()>0 && amount.length()>0)
        {

            //SE DEBE CONSULTAR A LA BASE DE DATOS SI EL NUMERO DE DOCUMENTO O EL NUMERO DE RECIBO YA FUE INGRESADO ENTONCES NO DEBE PODER DEJAR REGISTRO.
            final String query = "SELECT " +
                    ConciergeContract.PaymentEntry.COLUMN_NUMBER_TRX + "," +
                    ConciergeContract.PaymentEntry.COLUMN_NUMBER_RECEIPT +
                    " FROM " + ConciergeContract.PaymentEntry.TABLE_NAME +
                    " WHERE " + ConciergeContract.PaymentEntry.COLUMN_NUMBER_TRX + " = '" + documentNumber + "'" +
                    " OR " + ConciergeContract.PaymentEntry.COLUMN_NUMBER_RECEIPT + " = '" + documentNumberBuilding + "'";

            Cursor c;


            boolean register = true;
            try {
                SelectToDBRaw selectDocument = new SelectToDBRaw(query,null);
                c = selectDocument.execute().get();
            }catch (Exception e){
                c = null;
            }

            if (c != null) {
                while (c.moveToNext()){
                    register = false;
                }
                c.close();
            }


            if(register){

                Bundle args = new Bundle();
                args.putString("apartment",apartment);
                args.putString("amount",amount);
                args.putString("documentNumber",documentNumber);
                args.putString("documentNumberBuilding",documentNumberBuilding);
                args.putString("paymentType",paymentType);
                args.putString("paymentTypeIndex",paymentTypeIndex);

                DialogPaymentsRegister dialog = new DialogPaymentsRegister();
                dialog.setArguments(args);
                dialog.show(getFragmentManager(), "dialog");

            }
            else{

                Toast.makeText(getActivity().getApplicationContext(), "Número de transacción o recibo ya ingresado", Toast.LENGTH_LONG).show();

            }


        }
        else{
                Toast.makeText(getActivity().getApplicationContext(), "Falta ingresar el número de departamento y/o monto pagado", Toast.LENGTH_LONG).show();
        }
    }

}
