package com.handsriver.concierge.visits;


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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;

import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.email_notifications.SendEmailNotifications;
import com.handsriver.concierge.home.HomeFragment;
import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeContract.*;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.SelectToDB;
import com.handsriver.concierge.database.insertsTables.InsertVehicle;
import com.handsriver.concierge.database.insertsTables.InsertVisits;
import com.handsriver.concierge.sync.ConfigureSyncAccount;
import com.handsriver.concierge.utilities.LicensePlateCheck;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/**
 * Created by Created by alain_r._trouve_silva after 27-02-17.
 */

public class DialogVisitsRegister extends DialogFragment {

    ArrayList<Visit> arrayVisit;
    ListView visitList;
    TextView textViewApartment;
    TextView textViewLicensePlate;
    TextView textViewParking;
    TextView textViewAlertDialog;
    ProgressBar indeterminateBar;
    TableRow rowParkingLots;
    TableRow rowLicensePlate;
    String apartment;
    String licensePlate;
    String parkingLotsSelected;
    Boolean notificationVisits;

    VisitsAdapterDialog visitsAdapter;

    public static final String PREFS_NAME = "PorterPrefs";
    public static final int DEF_VALUE = 0;
    public static final String SEND_VISITS = "visits";


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
        View content = inflater.inflate(R.layout.dialog_register_visits,null);

        builder.setTitle(R.string.confirmInfo);

        builder.setMessage(R.string.messageRegister);

        builder.setView(content);

        visitList = (ListView) content.findViewById(R.id.visitListDialog);
        textViewApartment = (TextView) content.findViewById(R.id.textViewApartmentDialog);
        textViewLicensePlate = (TextView) content.findViewById(R.id.textViewLicensePlateDialog);
        textViewParking = (TextView) content.findViewById(R.id.textViewParkingDialog);
        textViewAlertDialog = (TextView) content.findViewById(R.id.textViewAlertDialog);
        rowParkingLots = (TableRow) content.findViewById(R.id.rowParking);
        rowLicensePlate = (TableRow) content.findViewById(R.id.rowLicensePlate);
        indeterminateBar = (ProgressBar) content.findViewById(R.id.indeterminateBar);

        arrayVisit = getArguments().getParcelableArrayList("visitList");
        visitsAdapter = new VisitsAdapterDialog(getContext(),arrayVisit);
        apartment = getArguments().getString("apartment");
        licensePlate = getArguments().getString("licensePlate");
        parkingLotsSelected = getArguments().getString("spinnerParkingSelected");

        LicensePlateCheck check = new LicensePlateCheck(textViewAlertDialog,textViewLicensePlate,indeterminateBar);
        check.execute(licensePlate);

        textViewApartment.setText(apartment);
        if (licensePlate.length() > 0) {
            rowLicensePlate.setVisibility(View.VISIBLE);
            textViewLicensePlate.setText(licensePlate);
            if (parkingLotsSelected != null){
                rowParkingLots.setVisibility(View.VISIBLE);
                textViewParking.setText(parkingLotsSelected);
            }
        }

        visitList.setAdapter(visitsAdapter);


        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int porterIdServer;
                int gatewayId;

                SharedPreferences porterPrefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                porterIdServer = porterPrefs.getInt(getString(R.string.porterIdServerVar),DEF_VALUE);

                SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                gatewayId = Integer.parseInt(settingsPrefs.getString(getString(R.string.pref_id_gateway_key),"0"));
                notificationVisits = settingsPrefs.getBoolean(getString(R.string.pref_send_email_visits_key),false);

                Calendar calendar = Calendar.getInstance();

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                String entry = df.format(calendar.getTime());

                String licensePlate = textViewLicensePlate.getText().toString();

                String apartmentNumber = textViewApartment.getText().toString();
                String[] projection = {
                        ApartmentEntry.COLUMN_APARTMENT_ID_SERVER
                };
                String selection = ApartmentEntry.COLUMN_APARTMENT_NUMBER + " = ?";
                String [] selectionArgs = {apartmentNumber};

                Cursor c;
                try {
                    SelectToDB selectApartment = new SelectToDB(ApartmentEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,null,null);
                    c = selectApartment.execute().get();
                }catch (Exception e){
                    c = null;
                }

                if (c != null) {
                    if(c.moveToFirst())
                    {
                        int apartmentId = c.getInt(0);

                        InsertVisits visits = new InsertVisits(null,arrayVisit,entry,apartmentId,gatewayId,porterIdServer);
                        visits.execute();

                        if (!licensePlate.equals(""))
                        {
                            InsertVehicle vehicle = new InsertVehicle(null,licensePlate,entry,apartmentId,porterIdServer,gatewayId,parkingLotsSelected);
                            vehicle.execute();
                        }
                        ConfigureSyncAccount.syncImmediatelyVisitsAndVehicles(getContext());
                    }
                    c.close();
                }

                if(notificationVisits){
                    SendEmailNotifications notifications = new SendEmailNotifications(arrayVisit,apartmentNumber,licensePlate,entry,getContext());
                    notifications.execute();
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
