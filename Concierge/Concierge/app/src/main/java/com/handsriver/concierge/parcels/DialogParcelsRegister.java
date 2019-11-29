package com.handsriver.concierge.parcels;

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
import android.widget.TextView;

import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.updatesTables.UpdateUniqueIdParcel;
import com.handsriver.concierge.home.HomeFragment;
import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeContract.*;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.SelectToDB;
import com.handsriver.concierge.database.insertsTables.InsertParcels;
import com.handsriver.concierge.sync.ConfigureSyncAccount;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/**
 * Created by Created by alain_r._trouve_silva after 14-03-17.
 */

public class DialogParcelsRegister extends DialogFragment{

    TextView textViewApartment;
    TextView textViewResident;
    TextView textViewObservations;
    String apartment;
    String resident;
    String observations;

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
        View content = inflater.inflate(R.layout.dialog_register_parcels,null);

        builder.setTitle(R.string.confirmInfo);

        builder.setMessage(R.string.messageParcel);

        builder.setView(content);

        textViewApartment = (TextView) content.findViewById(R.id.textViewApartmentDialog);
        textViewResident = (TextView) content.findViewById(R.id.textViewResidentDialog);
        textViewObservations = (TextView) content.findViewById(R.id.textViewObservationsDialog);

        apartment = getArguments().getString("apartment");
        resident = getArguments().getString("resident");
        observations = getArguments().getString("observations");

        textViewApartment.setText(apartment);
        textViewResident.setText(resident);
        textViewObservations.setText(observations);

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

                String resident = textViewResident.getText().toString();
                String apartment = textViewApartment.getText().toString();
                String observations = textViewObservations.getText().toString();

                String[] projection = {
                        ApartmentEntry.COLUMN_APARTMENT_ID_SERVER
                };
                String selection = ApartmentEntry.COLUMN_APARTMENT_NUMBER + " = ?";
                String [] selectionArgs = {apartment};

                String unique_id = "";

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
                            InsertParcels parcel = new InsertParcels(null,observations,entry,apartmentId,gatewayId,porterIdServer,resident);
                            long id = parcel.execute().get();
                            unique_id = String.valueOf(gatewayId)+"-"+id;

                            UpdateUniqueIdParcel updateParcel = new UpdateUniqueIdParcel(String.valueOf(id),unique_id);
                            updateParcel.execute();

                            ConfigureSyncAccount.syncImmediatelyParcels(getContext());
                        }catch (Exception e){

                        }


                    }
                    c.close();
                }


                Bundle args = new Bundle();
                args.putString("uniqueId",unique_id);
                DialogReturnIdParcelRegister dialogConfirm = new DialogReturnIdParcelRegister();
                dialogConfirm.setArguments(args);
                dialogConfirm.show(getFragmentManager(), "dialog");

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
