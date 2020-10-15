package com.handsriver.concierge.visits;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.handsriver.concierge.database.ConciergeContract.ApartmentEntry;
import com.handsriver.concierge.database.ConciergeContract.WhitelistEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.SelectToDBRaw;

import java.util.concurrent.ExecutionException;

/**
 * Created by Created by alain_r._trouve_silva after 19-04-17.
 */

public class DialogVisitsWhitelistAddList extends DialogFragment {

    Visit visit;
    TextView textViewFullName;
    TextView textViewDocumentNumber;
    TextView textViewApartmentNumber;
    String fullName;
    String documentNumber;
    String apartmentNumber;
    String type;
    String list = "white";

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
        View content = inflater.inflate(R.layout.dialog_register_whitelist,null);

        type = getArguments().getString("type");

        builder.setView(content);

        builder.setMessage(R.string.textWhitelist);

        builder.setIcon(R.drawable.ic_check);

        builder.setTitle(R.string.whitelist);

        final String query = "SELECT " + WhitelistEntry.COLUMN_DOCUMENT_NUMBER + "," +
                WhitelistEntry.COLUMN_FULL_NAME + "," +
                ApartmentEntry.TABLE_NAME + "." + ApartmentEntry.COLUMN_APARTMENT_NUMBER +
                " FROM " + WhitelistEntry.TABLE_NAME + "," + ApartmentEntry.TABLE_NAME +
                " WHERE " + ApartmentEntry.TABLE_NAME + "." + ApartmentEntry.COLUMN_APARTMENT_ID_SERVER + " = " + WhitelistEntry.TABLE_NAME + "." + WhitelistEntry.COLUMN_APARTMENT_ID +
                " ORDER BY " + WhitelistEntry.COLUMN_WHITELIST_ID_SERVER + " DESC";

        Cursor c;
        try {
            SelectToDBRaw selectwhitelist = new SelectToDBRaw(query,null);
            c = selectwhitelist.execute().get();
        }catch (Exception e){
            c = null;
        }

        if (c != null) {
            boolean flag = false;
            while (c.moveToNext()){
                if (!flag &&c.getString(c.getColumnIndex(WhitelistEntry.COLUMN_DOCUMENT_NUMBER)).replace(".","").replace("-","").equals(visit.getDocumentNumber().replace(".","").replace("-",""))){
                    documentNumber = c.getString(c.getColumnIndex(WhitelistEntry.COLUMN_DOCUMENT_NUMBER));
                    fullName = c.getString(c.getColumnIndex(WhitelistEntry.COLUMN_FULL_NAME));
                    apartmentNumber = c.getString(c.getColumnIndex(ApartmentEntry.COLUMN_APARTMENT_NUMBER));
                    flag = true;
                }
            }
            c.close();
        }

        textViewApartmentNumber = (TextView) content.findViewById(R.id.textViewApartmentDialog);
        textViewDocumentNumber = (TextView) content.findViewById(R.id.textViewDocumentNumberDialog);
        textViewFullName = (TextView) content.findViewById(R.id.textViewFullNameDialog);
        textViewApartmentNumber.setText(apartmentNumber);
        textViewDocumentNumber.setText(documentNumber);
        textViewFullName.setText(fullName);

        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent();
                        intent.putExtra("visit",visit);
                        intent.putExtra("type",type);
                        intent.putExtra("list",list);

                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                    }
                });

        return builder.create();    }

    public void setVisit(Visit visit){
        this.visit = visit;
    }
}
