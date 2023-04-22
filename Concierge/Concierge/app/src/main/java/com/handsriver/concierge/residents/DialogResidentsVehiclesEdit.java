package com.handsriver.concierge.residents;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.updatesTables.UpdateResidentsVehicles;
import com.handsriver.concierge.sync.ConfigureSyncAccount;
import com.handsriver.concierge.utilities.Utility;

/**
 * Created by Created by alain_r._trouve_silva after 14-03-17.
 */

public class DialogResidentsVehiclesEdit extends DialogFragment{

    TextView textViewApartment;
    EditText textViewPlate;
    TextView textViewActive;

    String apartment;
    String plate;
    String active;

    long id;

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
        View content = inflater.inflate(R.layout.dialog_register_residentsvehicles_edit,null);

        builder.setTitle(R.string.editResidentVehicle);

        builder.setMessage(R.string.messageEditResidentVehicle);

        builder.setView(content);

        textViewApartment = (TextView) content.findViewById(R.id.textViewApartmentDialog);
        textViewPlate = (EditText) content.findViewById(R.id.textViewPlateDialog);
        textViewActive = (TextView) content.findViewById(R.id.textViewActiveDialog);



        apartment = getArguments().getString("apartment");
        plate = getArguments().getString("plate");
        active = getArguments().getString("active").equals("1")==true?"Si":"No";

        id = getArguments().getLong("id");

        textViewApartment.setText(apartment);
        textViewPlate.setText(plate);
        textViewActive.setText(active);

        builder.setPositiveButton(R.string.edit, null);

        builder.setNegativeButton(R.string.cancel, null);

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        Button button = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!textViewPlate.getText().toString().isEmpty()){

                    String plate = textViewPlate.getText().toString();
                    String active = textViewActive.getText().toString().equals("Si")?"1":"0";

                    UpdateResidentsVehicles residentsvehicles = new UpdateResidentsVehicles(plate,active,id,getContext());
                    residentsvehicles.execute();

                    ConfigureSyncAccount.syncImmediatelyResidentsVehiclesTablet(getContext());
                    alertDialog.dismiss();
                    SearchResidentsVehiclesFragment fragmentSearchResidentsVehicles = new SearchResidentsVehiclesFragment();
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame_main,fragmentSearchResidentsVehicles).commit();
                    NavigationView navigationView = (NavigationView)getActivity().findViewById(R.id.nav_view);
                    navigationView.setCheckedItem(R.id.searchResidentsVehicles);
                    Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
                    toolbar.setTitle(getString(R.string.searchResidentVehicle));
                }
                else{
                    Toast.makeText(getActivity().getApplicationContext(), "Ingrese una patente válida", Toast.LENGTH_LONG).show();
                }
            }
        });

        return alertDialog;
    }
}
