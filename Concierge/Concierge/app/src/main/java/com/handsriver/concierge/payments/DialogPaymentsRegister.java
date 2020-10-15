package com.handsriver.concierge.payments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;

import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeContract.ApartmentEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.SelectToDB;
import com.handsriver.concierge.database.insertsTables.InsertPayments;
import com.handsriver.concierge.home.HomeFragment;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/**
 * Created by Created by alain_r._trouve_silva after 08-04-17.
 */

public class DialogPaymentsRegister extends DialogFragment {

    TextView textViewApartment;
    TextView textViewPaymentType;
    TextView textViewDocumentNumber;
    TextView textViewDocumentNumberBuilding;
    TextView textViewAmount;
    TableRow rowDocNumber;
    TableRow rowBuildingPayment;
    String apartment;
    String paymentType;
    String documentNumber;
    String documentNumberBuilding;
    String amount;
    String paymentTypeIndex;

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

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View content = inflater.inflate(R.layout.dialog_register_payments,null);

        builder.setTitle(R.string.confirmInfo);

        builder.setMessage(R.string.messagePayment);

        builder.setView(content);

        textViewApartment = (TextView) content.findViewById(R.id.textViewApartmentDialog);
        textViewPaymentType = (TextView) content.findViewById(R.id.textViewPaymentTypeDialog);
        textViewDocumentNumber = (TextView) content.findViewById(R.id.textViewDocumentNumberPaymentDialog);
        textViewDocumentNumberBuilding = (TextView) content.findViewById(R.id.textViewDocumentNumberPaymentBuildingDialog);
        textViewAmount = (TextView) content.findViewById(R.id.textViewAmountDialog);
        rowDocNumber = (TableRow) content.findViewById(R.id.rowDocNumber);
        rowBuildingPayment = (TableRow) content.findViewById(R.id.rowBuildingPayment);

        apartment = getArguments().getString("apartment");
        paymentType = getArguments().getString("paymentType");
        paymentTypeIndex = getArguments().getString("paymentTypeIndex");
        documentNumber = getArguments().getString("documentNumber");
        documentNumberBuilding = getArguments().getString("documentNumberBuilding");
        amount = getArguments().getString("amount");

        if (amount != null && amount.length()>0){
            amount = amount.replaceAll("[$,.]", "");
        }
        else{
            amount = "";
        }

        DecimalFormat formatter = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        formatter.setDecimalFormatSymbols(symbols);
        formatter.setMaximumFractionDigits(0);

        textViewApartment.setText(apartment);
        textViewPaymentType.setText(paymentType);

        if (documentNumber.length() > 0){
            rowDocNumber.setVisibility(View.VISIBLE);
            textViewDocumentNumber.setText(documentNumber);
        }

        if (documentNumberBuilding.length() > 0){
            rowBuildingPayment.setVisibility(View.VISIBLE);
            textViewDocumentNumberBuilding.setText(documentNumberBuilding);
        }

        textViewAmount.setText(formatter.format(Integer.parseInt(amount)));

        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int porterIdServer;
                int gatewayId;

                SharedPreferences porterPrefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                porterIdServer = porterPrefs.getInt(getString(R.string.porterIdServerVar),DEF_VALUE);

                SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                gatewayId = Integer.parseInt(settingsPrefs.getString(getString(R.string.pref_id_gateway_key),"0"));

                Calendar calendar = Calendar.getInstance();

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                String entry = df.format(calendar.getTime());

                String apartment = textViewApartment.getText().toString();

                String[] projection = {
                        ApartmentEntry.COLUMN_APARTMENT_ID_SERVER
                };
                String selection = ApartmentEntry.COLUMN_APARTMENT_NUMBER + " = ?";
                String [] selectionArgs = {apartment};

                Cursor c;
                try {
                    SelectToDB selectApartment = new SelectToDB(ApartmentEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,null,null);
                    c = selectApartment.execute().get();
                }catch (Exception e){
                    c = null;
                }

                if (c != null){
                    if(c.moveToFirst())
                    {
                        int apartmentId = c.getInt(0);

                        InsertPayments parcel = new InsertPayments(null,documentNumber,documentNumberBuilding,paymentTypeIndex,amount,entry,apartmentId,gatewayId,porterIdServer,getContext());
                        parcel.execute();

                    }
                    c.close();
                }


                HomeFragment fragmentHome = new HomeFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame_main,fragmentHome).commit();
                NavigationView navigationView = (NavigationView)getActivity().findViewById(R.id.nav_view);
                navigationView.setCheckedItem(R.id.home);
                Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
                toolbar.setTitle(getString(R.string.app_name));


            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        return builder.create();
    }
}
