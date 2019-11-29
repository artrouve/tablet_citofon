package com.handsriver.concierge.visits;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeContract.BlacklistEntry;
import com.handsriver.concierge.database.ConciergeContract.ApartmentEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.SelectToDBRaw;
import com.handsriver.concierge.database.updatesTables.UpdateExitParcel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/**
 * Created by Created by alain_r._trouve_silva after 19-04-17.
 */

public class DialogVisitsBlacklistAddList extends DialogFragment {

    Visit visit;
    TextView textViewFullName;
    TextView textViewDocumentNumber;
    TextView textViewApartmentNumber;
    TextView textViewObservations;
    String fullName;
    String documentNumber;
    String apartmentNumber;
    String observations;
    String type;
    String list = "black";


    public static final String NO_AVAILABLE = "Sin Información";

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
        View content = inflater.inflate(R.layout.dialog_register_blacklist,null);

        type = getArguments().getString("type");

        builder.setView(content);

        builder.setMessage(R.string.textBlacklist);

        builder.setIcon(R.drawable.ic_warning);

        builder.setTitle(R.string.warningBlacklist);

        final String query = "SELECT " + BlacklistEntry.COLUMN_DOCUMENT_NUMBER + "," +
                BlacklistEntry.COLUMN_IDENTIFICATION + "," +
                BlacklistEntry.COLUMN_OBSERVATIONS + "," +
                ApartmentEntry.TABLE_NAME + "." + ApartmentEntry.COLUMN_APARTMENT_NUMBER +
                " FROM " + BlacklistEntry.TABLE_NAME + "," + ApartmentEntry.TABLE_NAME +
                " WHERE " + ApartmentEntry.TABLE_NAME + "." + ApartmentEntry.COLUMN_APARTMENT_ID_SERVER + " = " + BlacklistEntry.TABLE_NAME + "." + BlacklistEntry.COLUMN_APARTMENT_ID +
                " ORDER BY " + BlacklistEntry.COLUMN_BLACKLIST_ID_SERVER + " DESC";

        Cursor c;
        try {
            SelectToDBRaw selectBlacklist = new SelectToDBRaw(query,null);
            c = selectBlacklist.execute().get();
        }catch (Exception e){
            c = null;
        }

        if (c != null) {
            boolean flag = false;
            while (c.moveToNext()){
                if (!flag && c.getString(0).replace(".","").replace("-","").equals(visit.getDocumentNumber().replace(".","").replace("-",""))){
                    documentNumber = c.getString(0);
                    fullName = c.getString(1);
                    observations = c.getString(2);
                    apartmentNumber = c.getString(3);
                    flag = true;
                }
            }
        }

        if (fullName == null || fullName.equals("")){
            fullName = NO_AVAILABLE;
        }
        if (observations == null || observations.equals("")){
            observations = NO_AVAILABLE;
        }

        textViewApartmentNumber = (TextView) content.findViewById(R.id.textViewApartmentDialog);
        textViewDocumentNumber = (TextView) content.findViewById(R.id.textViewDocumentNumberDialog);
        textViewFullName = (TextView) content.findViewById(R.id.textViewFullNameDialog);
        textViewObservations = (TextView) content.findViewById(R.id.textViewObservationsDialog);
        textViewApartmentNumber.setText(apartmentNumber);
        textViewDocumentNumber.setText(documentNumber);
        textViewFullName.setText(fullName);
        textViewObservations.setText(observations);

        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent();
                        intent.putExtra("visit",visit);
                        intent.putExtra("type",type);
                        intent.putExtra("list",list);

                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent();
                        intent.putExtra("type",type);
                        intent.putExtra("list",list);
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, intent);
                    }
                });

        return builder.create();    }

    public void setVisit(Visit visit){
        this.visit = visit;
    }
}
