package com.handsriver.concierge.suppliers;

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
import android.widget.TableRow;
import android.widget.TextView;

import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.home.HomeFragment;
import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeContract.*;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.SelectToDB;
import com.handsriver.concierge.database.insertsTables.InsertSuppliers;
import com.handsriver.concierge.visits.Visit;
import com.handsriver.concierge.visits.VisitsAdapterDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/**
 * Created by Created by alain_r._trouve_silva after 16-03-17.
 */

public class DialogSuppliersRegister extends DialogFragment {
    ArrayList<Visit> arrayVisit;
    ListView visitList;
    TextView textViewSupplier;
    TextView textViewLicensePlate;
    TableRow rowLicensePlate;
    String supplier;
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
        View content = inflater.inflate(R.layout.dialog_register_suppliers,null);

        builder.setTitle(R.string.confirmInfo);

        builder.setMessage(R.string.messageRegisterSupplier);

        builder.setView(content);

        visitList = (ListView) content.findViewById(R.id.visitListDialog);
        textViewSupplier = (TextView) content.findViewById(R.id.textViewSupplierDialog);
        textViewLicensePlate = (TextView) content.findViewById(R.id.textViewLicensePlateDialog);
        rowLicensePlate = (TableRow) content.findViewById(R.id.rowLicensePlate);

        arrayVisit = getArguments().getParcelableArrayList("visitList");
        visitsAdapter = new VisitsAdapterDialog(getContext(),arrayVisit);
        supplier = getArguments().getString("supplier");
        licensePlate = getArguments().getString("licensePlate");

        textViewSupplier.setText(supplier);
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
                String parkingNumber = "1";

                SharedPreferences porterPrefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                porterIdServer = porterPrefs.getInt(getString(R.string.porterIdServerVar),DEF_VALUE);

                SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                gatewayId = Integer.parseInt(settingsPrefs.getString(getString(R.string.pref_id_gateway_key),"0"));

                Calendar calendar = Calendar.getInstance();

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                String entry = df.format(calendar.getTime());

                String licensePlate = textViewLicensePlate.getText().toString();

                String supplier = textViewSupplier.getText().toString();
                String[] projection = {
                        SupplierEntry.COLUMN_SUPPLIER_ID_SERVER
                };
                String selection = SupplierEntry.COLUMN_NAME_SUPPLIER + " = ?";
                String [] selectionArgs = {supplier};

                Cursor c;
                try {
                    SelectToDB selectSupplier = new SelectToDB(SupplierEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,null,null);
                    c = selectSupplier.execute().get();
                }catch (Exception e){
                    c = null;
                }

                if ( c != null){
                    if(c.moveToFirst())
                    {
                        int supplierId = c.getInt(0);

                        InsertSuppliers visits = new InsertSuppliers(null,arrayVisit,entry,gatewayId,porterIdServer,licensePlate,supplierId,getContext());
                        visits.execute();
                    }
                    c.close();
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
