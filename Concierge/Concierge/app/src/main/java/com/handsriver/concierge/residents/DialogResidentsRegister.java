package com.handsriver.concierge.residents;

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
import com.handsriver.concierge.database.insertsTables.InsertParcels;
import com.handsriver.concierge.database.insertsTables.InsertResidents;
import com.handsriver.concierge.database.updatesTables.UpdateUniqueIdParcel;
import com.handsriver.concierge.home.HomeFragment;
import com.handsriver.concierge.parcels.DialogReturnIdParcelRegister;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/**
 * Created by Created by alain_r._trouve_silva after 14-03-17.
 */

public class DialogResidentsRegister extends DialogFragment{

    TextView textViewApartment;
    TextView textViewFullName;
    TextView textViewEmail;

    TextView textViewPhone;
    TextView textViewMobile;
    TextView textViewRut;


    TableRow rowEmail;
    String apartment;
    String fullName;
    String email;
    String mobile;
    String phone;
    String rut;

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
        View content = inflater.inflate(R.layout.dialog_register_residents,null);

        builder.setTitle(R.string.confirmInfo);

        builder.setMessage(R.string.messageResident);

        builder.setView(content);

        textViewApartment = (TextView) content.findViewById(R.id.textViewApartmentDialog);
        textViewFullName = (TextView) content.findViewById(R.id.textViewFullNameDialog);
        textViewEmail = (TextView) content.findViewById(R.id.textViewEmailDialog);

        textViewPhone = (TextView) content.findViewById(R.id.textViewPhoneDialog);
        textViewMobile = (TextView) content.findViewById(R.id.textViewMobileDialog);
        textViewRut = (TextView) content.findViewById(R.id.textViewRutDialog);


        rowEmail = (TableRow) content.findViewById(R.id.emailRow);

        apartment = getArguments().getString("apartment");
        fullName = getArguments().getString("fullName");
        email = getArguments().getString("email");
        mobile = getArguments().getString("mobile");
        phone = getArguments().getString("phone");
        rut = getArguments().getString("rut");

        textViewApartment.setText(apartment);
        textViewFullName.setText(fullName);

        textViewMobile.setText(mobile);
        textViewPhone.setText(phone);
        textViewRut.setText(rut);


        if (email.length() > 0) {
            rowEmail.setVisibility(View.VISIBLE);
            textViewEmail.setText(email);
        }

        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String fullName = textViewFullName.getText().toString();
                String apartment = textViewApartment.getText().toString();
                String email = textViewEmail.getText().toString();
                String mobile = textViewMobile.getText().toString();
                String phone = textViewPhone.getText().toString();
                String rut = textViewRut.getText().toString();


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

                        try {
                            InsertResidents residents = new InsertResidents(null,email,fullName, mobile, phone, rut,apartmentId,getContext());
                            residents.execute().get();

                        }catch (Exception e){

                        }
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
