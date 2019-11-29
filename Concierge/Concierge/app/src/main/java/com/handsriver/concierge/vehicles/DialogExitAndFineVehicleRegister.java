package com.handsriver.concierge.vehicles;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.updatesTables.UpdateExitVehicle;
import com.handsriver.concierge.utilities.Utility;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Created by alain_r._trouve_silva after 20-04-17.
 */

public class DialogExitAndFineVehicleRegister extends DialogFragment {
    String id_vehicle;

    public static final String PREFS_NAME = "PorterPrefs";
    public static final String FINE = "fine";
    public static final String EXIT = "exit";
    public static final int DEF_VALUE = 0;
    String entry;
    String button;
    String apartmentNumber;
    String licensePlate;
    boolean isFined;

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

        id_vehicle = getArguments().getString("id");
        entry = getArguments().getString("entry");
        button = getArguments().getString("button");
        apartmentNumber = getArguments().getString("apartmentNumber");
        licensePlate = getArguments().getString("licensePlate");

        builder.setTitle(R.string.confirmInfo);

        if (button.equals(EXIT)){
            builder.setMessage(R.string.markExitVehicleQuestion);
        }else if (button.equals(FINE)){
            builder.setMessage(R.string.markFineVehicleQuestion);
        }

        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                    String exit = df.format(calendar.getTime());

                    int porterIdServer;
                    int hours;
                    boolean isAutomatic;

                    SharedPreferences porterPrefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    porterIdServer = porterPrefs.getInt(getString(R.string.porterIdServerVar), DEF_VALUE);

                    SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    hours = Integer.parseInt(settingsPrefs.getString(getString(R.string.pref_id_max_time_parking_key),"0"));
                    isAutomatic = settingsPrefs.getBoolean(getString(R.string.pref_id_automatic_fine_key),true);

                    isFined = Utility.differenceDateHours(exit,entry,hours);

                    UpdateExitVehicle updateExitVehicle = new UpdateExitVehicle(exit,porterIdServer,id_vehicle,isAutomatic,isFined,button,getContext(),apartmentNumber,licensePlate,entry);
                    updateExitVehicle.execute();

                    Intent intent = new Intent();
                    intent.putExtra("button",button);

                    getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);

                }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {}
        });


        return builder.create();
    }
}
