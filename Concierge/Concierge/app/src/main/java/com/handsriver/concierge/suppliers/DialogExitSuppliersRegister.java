package com.handsriver.concierge.suppliers;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.updatesTables.UpdateExitSuppliersVisit;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Created by alain_r._trouve_silva after 15-04-17.
 */

public class DialogExitSuppliersRegister extends DialogFragment {
    ArrayList<String> arrayIdsvisits;
    String obsExitSupplier;
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

        arrayIdsvisits = getArguments().getStringArrayList("visitsIdsList");
        obsExitSupplier = getArguments().getString("obsExitSupplier");
        builder.setTitle(R.string.confirmInfo);
        builder.setMessage(R.string.markExitSuppleirQuestion)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                        String exit = df.format(calendar.getTime());

                        int porterIdServer;
                        SharedPreferences porterPrefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                        porterIdServer = porterPrefs.getInt(getString(R.string.porterIdServerVar),DEF_VALUE);

                        UpdateExitSuppliersVisit updateSupplierVisits = new UpdateExitSuppliersVisit(exit,obsExitSupplier,porterIdServer,arrayIdsvisits,getContext());
                        updateSupplierVisits.execute();

                        getTargetFragment().onActivityResult(getTargetRequestCode(),Activity.RESULT_OK,getActivity().getIntent());

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

        return builder.create();
    }


}
