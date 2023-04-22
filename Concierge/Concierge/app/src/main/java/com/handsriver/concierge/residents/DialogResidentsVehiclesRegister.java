package com.handsriver.concierge.residents;

import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;

import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeContract.ApartmentEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.SelectToDB;
import com.handsriver.concierge.database.insertsTables.InsertResidentsVehicles;
import com.handsriver.concierge.home.HomeFragment;
import com.handsriver.concierge.sync.ConfigureSyncAccount;

/**
 * Created by Created by alain_r._trouve_silva after 14-03-17.
 */

public class DialogResidentsVehiclesRegister extends DialogFragment{

    TextView textViewApartment;
    TextView textViewPlate;
    TextView textViewActive;

    String apartment;
    String plate;
    String active;

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
        View content = inflater.inflate(R.layout.dialog_register_residentsvehicles,null);

        builder.setTitle(R.string.confirmInfo);

        builder.setMessage(R.string.messageResident);

        builder.setView(content);

        textViewApartment = (TextView) content.findViewById(R.id.textViewApartmentDialog);
        textViewPlate = (TextView) content.findViewById(R.id.textViewPlateDialog);
        textViewActive = (TextView) content.findViewById(R.id.textViewPlateActiveDialog);

        apartment = getArguments().getString("apartment");
        plate = getArguments().getString("plate");
        active = getArguments().getString("active").equals("1")==true?"Si":"No";

        textViewApartment.setText(apartment);
        textViewPlate.setText(plate);
        textViewActive.setText(active);


        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String plate = textViewPlate.getText().toString();
                String apartment = textViewApartment.getText().toString();
                String active = textViewActive.getText().toString()=="Si"?"1":"0";



                String[] projection = {
                        ApartmentEntry.COLUMN_APARTMENT_ID_SERVER
                };
                String selection = ApartmentEntry.COLUMN_APARTMENT_NUMBER + " = ?";
                String [] selectionArgs = {apartment};

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
                            InsertResidentsVehicles residentsvehicles = new InsertResidentsVehicles(null,plate, Integer.parseInt(active),apartmentId,getContext());
                            residentsvehicles.execute().get();

                        }catch (Exception e){

                        }
                    }
                    c.close();
                }

                ConfigureSyncAccount.syncImmediatelyResidentsVehiclesTablet(getContext());

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
