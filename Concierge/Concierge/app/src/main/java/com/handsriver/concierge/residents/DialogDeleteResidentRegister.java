package com.handsriver.concierge.residents;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;

import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.deleteTables.DeleteResident;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Created by alain_r._trouve_silva after 20-04-17.
 */

public class DialogDeleteResidentRegister extends DialogFragment {
    String id_resident;

    public static final String PREFS_NAME = "PorterPrefs";
    public static final String DELETE = "delete";
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
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        id_resident = String.valueOf(getArguments().getLong("id"));

        builder.setTitle(R.string.confirmInfo);
        builder.setMessage(R.string.markDeleteResidentQuestion);

        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    DeleteResident deleteResident = new DeleteResident(id_resident,getContext());
                    deleteResident.execute();

                    SearchResidentsFragment fragmentSearchResidents = new SearchResidentsFragment();
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame_main,fragmentSearchResidents).commit();
                    NavigationView navigationView = (NavigationView)getActivity().findViewById(R.id.nav_view);
                    navigationView.setCheckedItem(R.id.searchResidents);
                    Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
                    toolbar.setTitle(getString(R.string.searchResident));

                }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {}
        });

        return builder.create();
    }
}
