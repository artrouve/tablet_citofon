package com.handsriver.concierge.parcels;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeContract.ParcelEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.SelectToDBRaw;
import com.handsriver.concierge.suppliers.DetailSearchSuppliersListFragment;
import com.handsriver.concierge.suppliers.DialogExitSuppliersRegister;
import com.handsriver.concierge.utilities.Utility;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by Created by alain_r._trouve_silva after 14-03-17.
 */

public class DetailSearchParcelsListFragment extends Fragment {
    View rootView;
    TextView textViewObservations;
    TextView textViewFullName;
    TextView textViewExitParcel;
    TextView textViewEntryParcel;
    TextView textViewApartmentNumber;
    TextView textViewUniqueIdParcel;
    TextView textViewFullNameExitParcel;
    TextView textViewDocumentNumberExitParcel;
    String id;
    String uniqueId;
    String observations;
    String fullName;
    String exitParcel;
    String entryParcel;
    String apartmentNumber;
    String fullNameExitParcel;
    String documentNumberExitParcel;
    Button buttonExitParcel;

    public static final String NO_AVAILABLE = "Sin Información";
    public static final int DIALOG_FRAGMENT = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConciergeDbHelper helper= new ConciergeDbHelper(getContext());
        DatabaseManager.initializeInstance(helper);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();

        id = args.getString("id");
        observations = args.getString("observations",NO_AVAILABLE);
        fullName = args.getString("fullName",NO_AVAILABLE);
        entryParcel = Utility.changeDateFormat(args.getString("entryParcel"),"ENTRY");
        apartmentNumber = args.getString("apartmentNumber",NO_AVAILABLE);
        uniqueId = args.getString("uniqueId");

        getExitDate();

        rootView = inflater.inflate(R.layout.detail_parcels, container, false);

        buttonExitParcel = (Button) rootView.findViewById(R.id.buttonExitParcel);

        if (exitParcel.equals(NO_AVAILABLE))
        {
            buttonExitParcel.setVisibility(View.VISIBLE);
        }

        buttonExitParcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString("id",id);

                DialogExitParcelsRegister dialog = new DialogExitParcelsRegister();
                dialog.setArguments(args);
                dialog.setTargetFragment(DetailSearchParcelsListFragment.this,0);
                dialog.show(getFragmentManager(), "dialog");
            }
        });


        textViewObservations = (TextView) rootView.findViewById(R.id.textViewDetailObservationsParcel);
        textViewObservations.setText(observations);
        textViewFullName = (TextView) rootView.findViewById(R.id.textViewDetailFullNameParcel);
        textViewFullName.setText(fullName);
        textViewEntryParcel = (TextView) rootView.findViewById(R.id.textViewDetailEntryParcel);
        textViewEntryParcel.setText(entryParcel);
        textViewExitParcel = (TextView) rootView.findViewById(R.id.textViewDetailExitDateParcel);
        textViewExitParcel.setText(exitParcel);
        textViewApartmentNumber = (TextView) rootView.findViewById(R.id.textViewDetailApartmentNumber);
        textViewApartmentNumber.setText(apartmentNumber);
        textViewUniqueIdParcel = (TextView) rootView.findViewById(R.id.textViewIdUniqueParcel);
        textViewUniqueIdParcel.setText(uniqueId);
        textViewDocumentNumberExitParcel = (TextView) rootView.findViewById(R.id.textViewDetailDocumentNumberExitParcel);
        textViewDocumentNumberExitParcel.setText(documentNumberExitParcel);
        textViewFullNameExitParcel = (TextView) rootView.findViewById(R.id.textViewDetailFullNameExitParcel);
        textViewFullNameExitParcel.setText(fullNameExitParcel);

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case DIALOG_FRAGMENT:
                if (resultCode == Activity.RESULT_OK){
                    getExitDate();
                    textViewExitParcel.setText(exitParcel);
                    textViewFullNameExitParcel.setText(fullNameExitParcel);
                    textViewDocumentNumberExitParcel.setText(documentNumberExitParcel);

                    if (!exitParcel.equals(NO_AVAILABLE))
                    {
                        buttonExitParcel.setVisibility(View.INVISIBLE);
                    }
                }
                break;
        }
    }

    public void getExitDate (){

        final String query1 = "SELECT " + ParcelEntry.COLUMN_EXIT_PARCEL + "," + ParcelEntry.COLUMN_EXIT_FULLNAME + "," + ParcelEntry.COLUMN_EXIT_DOCUMENT_NUMBER +
                " FROM " + ParcelEntry.TABLE_NAME +
                " WHERE " + ParcelEntry._ID + " = ?";

        String [] args2 = new String[]{id};

        Cursor c1;

        try {
            SelectToDBRaw selectExitParcel = new SelectToDBRaw(query1,args2);
            c1 = selectExitParcel.execute().get();
        }catch (Exception e){
            c1 = null;
        }

        if (c1 != null){
            if(c1.moveToFirst()){
                if (c1.getString(0) == null || c1.getString(0).isEmpty())
                {
                    exitParcel = NO_AVAILABLE;
                }
                else
                {
                    exitParcel = Utility.changeDateFormat(c1.getString(0),"ENTRY");
                }

                if (c1.getString(1) == null || c1.getString(1).isEmpty())
                {
                    fullNameExitParcel = NO_AVAILABLE;
                }
                else
                {
                    fullNameExitParcel = c1.getString(1);
                }

                if (c1.getString(2) == null || c1.getString(2).isEmpty())
                {
                    documentNumberExitParcel = NO_AVAILABLE;
                }
                else
                {
                    documentNumberExitParcel = c1.getString(2);
                }
            }
        }
    }
}
