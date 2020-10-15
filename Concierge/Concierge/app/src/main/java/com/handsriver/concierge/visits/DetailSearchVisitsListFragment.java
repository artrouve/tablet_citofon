package com.handsriver.concierge.visits;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeContract;
import com.handsriver.concierge.database.SelectToDBRaw;
import com.handsriver.concierge.utilities.Utility;
import com.handsriver.concierge.vehicles.DetailSearchVehiclesListFragment;
import com.handsriver.concierge.vehicles.DialogExitAndFineVehicleRegister;

/**
 * Created by Created by alain_r._trouve_silva after 06-03-17.
 */

public class DetailSearchVisitsListFragment extends Fragment {

    View rootView;
    TextView textViewFullname;
    TextView textViewDocumentNumber;
    TextView textViewGender;
    TextView textViewBirthdate;
    TextView textViewNationality;
    TextView textViewEntry;
    TextView textViewExit;
    TextView textViewApartmentNumber;
    TextView textViewOptional;
    TextView textViewOptionalField;
    Button buttonExitVisit;
    String id;
    String fullname;
    String documentNumber;
    String gender;
    String birthdate;
    String nationality;
    String entry;
    String exit;
    String apartmentNumber;
    String optional;

    public static final String NO_AVAILABLE = "Sin Información";
    public static final String EXIT = "exit";
    public static final int DIALOG_FRAGMENT = 0;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        id = args.getString("id");
        fullname = args.getString("fullname",NO_AVAILABLE);
        documentNumber = args.getString("documentNumber",NO_AVAILABLE);
        nationality = args.getString("nationality",NO_AVAILABLE);
        gender = args.getString("gender",NO_AVAILABLE);
        entry = Utility.changeDateFormat(args.getString("entry"),"ENTRY");

        exit = args.getString("exitDate");
        birthdate = args.getString("birthdate");
        apartmentNumber = args.getString("apartmentNumber",NO_AVAILABLE);
        optional = args.getString("optional");

        getExitDate();



        if (birthdate == null || birthdate.isEmpty())
        {
            birthdate = NO_AVAILABLE;
        }
        else
        {
            birthdate = Utility.changeDateFormat(birthdate,"BIRTHDATE");
        }

        rootView = inflater.inflate(R.layout.detail_visits, container, false);
        buttonExitVisit = (Button) rootView.findViewById(R.id.buttonExitVisit);
        if(exit.equals(NO_AVAILABLE)){
            buttonExitVisit.setVisibility(View.VISIBLE);
            buttonExitVisit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle args = new Bundle();
                    args.putString("id",id);
                    args.putString("exit",exit);
                    args.putString("button",EXIT);

                    DialogExitVisitRegister dialog = new DialogExitVisitRegister();
                    dialog.setArguments(args);
                    dialog.setTargetFragment(DetailSearchVisitsListFragment.this,0);
                    dialog.show(getFragmentManager(), "dialog");
                }
            });

        }



        textViewEntry = (TextView) rootView.findViewById(R.id.textViewDetailEntry);
        textViewEntry.setText(entry);

        textViewExit = (TextView) rootView.findViewById(R.id.textViewDetailExit);
        textViewExit.setText(exit);


        textViewBirthdate = (TextView) rootView.findViewById(R.id.textViewDetailBirthdate);
        textViewBirthdate.setText(birthdate);

        SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String optionalField = settingsPrefs.getString(getString(R.string.pref_OPTIONAL_FILED_VISITS_key),"");

        textViewOptionalField = (TextView) rootView.findViewById(R.id.textViewDetailOptionalField);
        textViewOptionalField.setText(optionalField+":");

        textViewOptional = (TextView) rootView.findViewById(R.id.textViewDetailOptionalValue);
        if(optional==""){
            optional = NO_AVAILABLE;
        }
        textViewOptional.setText(optional);


        textViewDocumentNumber = (TextView) rootView.findViewById(R.id.textViewDetailDocumentNumber);
        textViewDocumentNumber.setText(documentNumber);
        textViewFullname = (TextView) rootView.findViewById(R.id.textViewDetailfullName);
        textViewFullname.setText(fullname);
        textViewNationality = (TextView) rootView.findViewById(R.id.textViewDetailNationality);
        textViewNationality.setText(nationality);
        textViewGender = (TextView) rootView.findViewById(R.id.textViewDetailGender);
        textViewGender.setText(gender);
        textViewApartmentNumber = (TextView) rootView.findViewById(R.id.textViewDetailApartmentNumber);
        textViewApartmentNumber.setText(apartmentNumber);

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case DIALOG_FRAGMENT:
                if (resultCode == Activity.RESULT_OK){
                    getExitDate();
                    textViewExit.setText(exit);

                    if (!exit.equals(NO_AVAILABLE))
                    {
                        buttonExitVisit.setVisibility(View.GONE);

                    }

                }
                break;
        }
    }

    public void getExitDate (){

        final String query1 = "SELECT " + ConciergeContract.VisitEntry.COLUMN_EXIT_DATE +
                " FROM " + ConciergeContract.VisitEntry.TABLE_NAME +
                " WHERE " + ConciergeContract.VisitEntry._ID + " = ?";

        String [] args2 = new String[]{id};

        Cursor c1;

        try {
            SelectToDBRaw selectExitVisit = new SelectToDBRaw(query1,args2);
            c1 = selectExitVisit.execute().get();
        }catch (Exception e){
            c1 = null;
        }

        if (c1 != null){
            if(c1.moveToFirst()){
                if (c1.getString(0) == null || c1.getString(0).isEmpty())
                {
                    exit = NO_AVAILABLE;
                }
                else
                {
                    exit = Utility.changeDateFormat(c1.getString(0),"ENTRY");
                }

            }
        }
    }

}
