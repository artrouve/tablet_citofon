package com.handsriver.concierge.commonspaces;

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

import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeContract;
import com.handsriver.concierge.database.ConciergeContract.CommonspaceEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.SelectToDB;
import com.handsriver.concierge.database.insertsTables.InsertCommonspacevisits;

import com.handsriver.concierge.home.HomeFragment;
import com.handsriver.concierge.visits.Visit;
import com.handsriver.concierge.visits.VisitsAdapterDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Created by alain_r._trouve_silva after 16-03-17.
 */

public class DialogCommonspacesvisitsRegister extends DialogFragment {
    ArrayList<Visit> arrayVisit;
    ListView visitList;
    TextView textViewCommonspace;
    TextView textViewLicensePlate;
    TableRow rowLicensePlate;
    TextView textViewApartment;

    String apartment;
    String commonspace;
    String licensePlate;
    VisitsAdapterDialog visitsAdapter;

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
        View content = inflater.inflate(R.layout.dialog_register_commonspacesvisits,null);

        builder.setTitle(R.string.confirmInfo);

        builder.setMessage(R.string.messageRegisterCommonspace);

        builder.setView(content);

        visitList = (ListView) content.findViewById(R.id.visitListDialog);
        textViewCommonspace = (TextView) content.findViewById(R.id.textViewCommonspaceDialog);
        textViewApartment = (TextView) content.findViewById(R.id.textViewApartmentDialog);
        textViewLicensePlate = (TextView) content.findViewById(R.id.textViewLicensePlateDialog);
        rowLicensePlate = (TableRow) content.findViewById(R.id.rowLicensePlate);

        arrayVisit = getArguments().getParcelableArrayList("visitList");
        visitsAdapter = new VisitsAdapterDialog(getContext(),arrayVisit);
        commonspace = getArguments().getString("commonspace");
        licensePlate = getArguments().getString("licensePlate");
        apartment = getArguments().getString("apartment");

        textViewCommonspace.setText(commonspace);
        textViewApartment.setText(apartment);
        if (licensePlate.length() > 0) {
            rowLicensePlate.setVisibility(View.VISIBLE);
            textViewLicensePlate.setText(licensePlate);
        }
        textViewLicensePlate.setText(licensePlate);
        visitList.setAdapter(visitsAdapter);


        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int porterIdServer;
                int gatewayId;
                int commonspaceId = 0;
                int apartmentId = 0;
                String parkingNumber = "1";

                SharedPreferences porterPrefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                porterIdServer = porterPrefs.getInt(getString(R.string.porterIdServerVar),DEF_VALUE);

                SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                gatewayId = Integer.parseInt(settingsPrefs.getString(getString(R.string.pref_id_gateway_key),"0"));

                Calendar calendar = Calendar.getInstance();

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                String entry = df.format(calendar.getTime());

                String licensePlate = textViewLicensePlate.getText().toString();


                String commonspace = textViewCommonspace.getText().toString();
                String[] projection = {
                        CommonspaceEntry.COLUMN_COMMONSPACE_ID_SERVER
                };
                String selection = CommonspaceEntry.COLUMN_NAME_COMMONSPACE + " = ?";
                String [] selectionArgs = {commonspace};

                Cursor c;
                try {
                    SelectToDB selectCommonspace = new SelectToDB(CommonspaceEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,null,null);
                    c = selectCommonspace.execute().get();
                }catch (Exception e){
                    c = null;
                }

                if ( c != null){
                    if(c.moveToFirst())
                    {
                        commonspaceId = c.getInt(0);

                    }
                    c.close();
                }


                String apartmentNumber = textViewApartment.getText().toString();
                String[] projection_apartmentnumber = {
                        ConciergeContract.ApartmentEntry.COLUMN_APARTMENT_ID_SERVER
                };
                String selection_apartmentnumber = ConciergeContract.ApartmentEntry.COLUMN_APARTMENT_NUMBER + " = ?";
                String [] selectionArgsApartmentnumber = {apartmentNumber};

                Cursor c_apartmentnumber;
                try {
                    SelectToDB selectApartment = new SelectToDB(ConciergeContract.ApartmentEntry.TABLE_NAME,projection_apartmentnumber,selection_apartmentnumber,selectionArgsApartmentnumber,null,null,null,null);
                    c_apartmentnumber = selectApartment.execute().get();
                }catch (Exception e){
                    c_apartmentnumber = null;
                }

                if (c_apartmentnumber != null) {
                    if (c_apartmentnumber.moveToFirst()) {
                        apartmentId = c_apartmentnumber.getInt(0);
                    }
                }

                if(apartmentId != 0 && commonspaceId!= 0 ){

                    InsertCommonspacevisits visits = new InsertCommonspacevisits(null,arrayVisit,entry,gatewayId,porterIdServer,licensePlate,commonspaceId,apartmentId,getContext());
                    visits.execute();
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
