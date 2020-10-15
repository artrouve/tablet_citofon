package com.handsriver.concierge.visits;

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
import com.handsriver.concierge.database.updatesTables.UpdateExitVisit;
import com.handsriver.concierge.utilities.Utility;
import com.handsriver.concierge.vehicles.VehiclePlateDetectedOper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Created by alain_r._trouve_silva after 20-04-17.
 */

public class DialogExitVisitRegister extends DialogFragment {
    String id_visit;

    public static final String PREFS_NAME = "PorterPrefs";
    public static final String EXIT = "exit";
    public static final int DEF_VALUE = 0;
    String exit;
    String button;

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

        id_visit = getArguments().getString("id");
        exit = getArguments().getString("exit");
        button = getArguments().getString("button");

        builder.setTitle(R.string.confirmInfo);

        builder.setMessage(R.string.markExitVisitQuestion);

        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                    String exit = df.format(calendar.getTime());

                    int porterIdServer;

                    SharedPreferences porterPrefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    porterIdServer = porterPrefs.getInt(getString(R.string.porterIdServerVar), DEF_VALUE);

                    UpdateExitVisit updateExitVisit = new UpdateExitVisit(exit,porterIdServer,id_visit,button,getContext());
                    updateExitVisit.execute();

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
