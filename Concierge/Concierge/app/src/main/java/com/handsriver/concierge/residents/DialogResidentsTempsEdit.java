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
import com.handsriver.concierge.database.updatesTables.UpdateResidentsTemps;
import com.handsriver.concierge.utilities.Utility;

/**
 * Created by Created by alain_r._trouve_silva after 14-03-17.
 */

public class DialogResidentsTempsEdit extends DialogFragment{

    TextView textViewApartment;
    TextView textViewFullName;
    EditText textViewEmail;
    EditText textViewRut;
    EditText textViewPhone;
    EditText textViewStartDate;
    EditText textViewEndDate;


    String apartment;
    String fullName;
    String email;
    String rut;
    String phone;
    String startDate;
    String endDate;

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
        View content = inflater.inflate(R.layout.dialog_register_residentstemps_edit,null);

        builder.setTitle(R.string.editResidentTemp);

        builder.setMessage(R.string.messageEditResident);

        builder.setView(content);

        textViewApartment = (TextView) content.findViewById(R.id.textViewApartmentDialog);
        textViewFullName = (TextView) content.findViewById(R.id.textViewFullNameDialog);
        textViewEmail = (EditText) content.findViewById(R.id.textViewEmailDialog);

        textViewPhone = (EditText) content.findViewById(R.id.textViewPhoneDialog);
        textViewRut = (EditText) content.findViewById(R.id.textViewRutDialog);
        textViewStartDate = (EditText) content.findViewById(R.id.textViewStartDateDialog);
        textViewEndDate = (EditText) content.findViewById(R.id.textViewEndDateDialog);


        apartment = getArguments().getString("apartment");
        fullName = getArguments().getString("fullName");
        email = getArguments().getString("email");
        rut = getArguments().getString("rut");
        phone = getArguments().getString("phone");
        startDate = getArguments().getString("startDate");
        endDate = getArguments().getString("endDate");


        id = getArguments().getLong("id");

        textViewApartment.setText(apartment);
        textViewFullName.setText(fullName);
        textViewEmail.setText(email);
        textViewRut.setText(rut);
        textViewPhone.setText(phone);
        textViewStartDate.setText(startDate);
        textViewEndDate.setText(endDate);

        builder.setPositiveButton(R.string.edit, null);

        builder.setNegativeButton(R.string.cancel, null);

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        Button button = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Utility.isValidEmail(textViewEmail.getText().toString()) || textViewEmail.getText().toString().isEmpty()){
                    //SE DEBE VALIDAR QUE LAS FECHAS SEAN LAS ADECUADAS
                    String email = textViewEmail.getText().toString();
                    String full_name = textViewFullName.getText().toString();

                    String rut = textViewRut.getText().toString();
                    String phone = textViewPhone.getText().toString();
                    String startDate = textViewStartDate.getText().toString();
                    String endDate = textViewEndDate.getText().toString();

                    UpdateResidentsTemps residents = new UpdateResidentsTemps(full_name,email,phone,rut,startDate,endDate,id,getContext());
                    residents.execute();

                    alertDialog.dismiss();
                    SearchResidentsTempsFragment fragmentSearchResidentsTemps = new SearchResidentsTempsFragment();
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame_main,fragmentSearchResidentsTemps).commit();
                    NavigationView navigationView = (NavigationView)getActivity().findViewById(R.id.nav_view);
                    navigationView.setCheckedItem(R.id.searchResidentsTemps);
                    Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
                    toolbar.setTitle(getString(R.string.searchResidenttemp));
                }
                else{
                    Toast.makeText(getActivity().getApplicationContext(), "Ingrese un correo válido", Toast.LENGTH_LONG).show();
                }
            }
        });

        return alertDialog;
    }
}
