package com.handsriver.concierge.vehicles;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeContract.VehicleEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.SelectToDBRaw;
import com.handsriver.concierge.utilities.GetPlateDetection;
import com.handsriver.concierge.utilities.Utility;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/**
 * Created by Created by alain_r._trouve_silva after 10-03-17.
 */

public class DetailSearchVehiclesListFragment extends Fragment {

    View rootView;
    TextView textViewLicensePlate;
    TextView textViewParkingNumber;
    TextView textViewExitDate;
    TextView textViewFineDate;
    TextView textViewEntry;
    TextView textViewApartmentNumber;
    String id;
    String licensePlate;
    String parkingNumber;
    String entry;
    String entryDialog;
    String exitDate;
    String fineDate;
    String apartmentNumber;
    Button buttonExitVehicle;
    Button buttonFineVehicle;
    Boolean isAutomaticFineExit;
    Boolean isMarkExit;
    Boolean isFined;
    Boolean isAutomaticFine;
    int hours;

    VehiclePlateAdapter vehiclePlateAdapter;
    ListView platesLists;
    VehiclePlateDetectedOper DetectedPlate;
    VehiclePlateDetectedOper DetectedPlateAux;
    ProgressBar indeterminatePlatesBar;

    public static final String NO_AVAILABLE = "Sin Información";
    public static final String FINE = "fine";
    public static final String EXIT = "exit";

    public static final int DIALOG_FRAGMENT = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConciergeDbHelper helper= new ConciergeDbHelper(getContext());
        DatabaseManager.initializeInstance(helper);


        //SE BUSCAN LAS PATENTES DETECTADAS GUARDADAS EN PREFERENCIAS Y SE MARCA LA SALIDA EN EL CASO QUE
        //SEA POSIBLE MARCARLA
        DetectedPlate = new VehiclePlateDetectedOper(getActivity());
        DetectedPlate.ExitVehicles();
        vehiclePlateAdapter = new VehiclePlateAdapter(getContext(),DetectedPlate.getItemsPlateDetected());



    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();

        id = args.getString("id");
        licensePlate = args.getString("licensePlate",NO_AVAILABLE);
        parkingNumber = args.getString("parkingNumber",NO_AVAILABLE);

        entry = Utility.changeDateFormat(args.getString("entry"),"ENTRY");
        entryDialog = args.getString("entry");
        apartmentNumber = args.getString("apartmentNumber",NO_AVAILABLE);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        String exit = df.format(calendar.getTime());

        SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        isAutomaticFineExit = settingsPrefs.getBoolean(getString(R.string.pref_id_automatic_fine_key),true);
        hours = Integer.parseInt(settingsPrefs.getString(getString(R.string.pref_id_max_time_parking_key),"0"));
        isMarkExit = settingsPrefs.getBoolean(getString(R.string.pref_id_mark_exit_key),true);
        isAutomaticFine = settingsPrefs.getBoolean(getString(R.string.pref_automatic_fine_key),false);

        getExitDate();

        isFined = Utility.differenceDateHours(exit,entryDialog,hours);

        rootView = inflater.inflate(R.layout.detail_vehicles, container, false);
        LinearLayout platesListsDetected = (LinearLayout) rootView.findViewById(R.id.platesListsDetected);




        buttonExitVehicle = (Button) rootView.findViewById(R.id.buttonExitVehicle);
        buttonFineVehicle = (Button) rootView.findViewById(R.id.buttonFineVehicle);

        boolean buttonExitVehicleVisible = false;
        if (exitDate.equals(NO_AVAILABLE) && isAutomaticFineExit) {
            buttonExitVehicle.setVisibility(View.VISIBLE);
            buttonExitVehicleVisible = true;
        } else if (exitDate.equals(NO_AVAILABLE) && !isAutomaticFineExit && isMarkExit ) {
            buttonExitVehicle.setVisibility(View.VISIBLE);
            buttonExitVehicleVisible = true;
            if(isFined && fineDate.equals(NO_AVAILABLE) && !isAutomaticFine){
                buttonFineVehicle.setVisibility(View.VISIBLE);
            }
        } else if (exitDate.equals(NO_AVAILABLE) && !isAutomaticFineExit && !isMarkExit){
            if(isFined && fineDate.equals(NO_AVAILABLE) && !isAutomaticFine){
                buttonFineVehicle.setVisibility(View.VISIBLE);
            }
        }
        if(buttonExitVehicleVisible){
            indeterminatePlatesBar = (ProgressBar)rootView.findViewById(R.id.indeterminatePlatesBar);

            platesLists = (ListView) rootView.findViewById(R.id.platesLists);
            platesLists.setAdapter(vehiclePlateAdapter);

            //SE BUSCAN LAS PATENTES DETECTADAS GUARDADAS EN PREFERENCIAS Y SE MARCA LA SALIDA EN EL CASO QUE
            //SEA POSIBLE MARCARLA

            DetectedPlate = new VehiclePlateDetectedOper(getActivity());
            DetectedPlate.getDetectedVehicles();
            DetectedPlate.ExitVehicles();

            DetectedPlateAux = new VehiclePlateDetectedOper(getActivity());
            DetectedPlateAux.getDetectedVehicles();


            vehiclePlateAdapter = new VehiclePlateAdapter(getContext(),DetectedPlate.getItemsPlateDetected());
            platesLists.setAdapter(vehiclePlateAdapter);

            String gatewayId = settingsPrefs.getString(getContext().getResources().getString(R.string.pref_id_gateway_key),"0");
            GetPlateDetection getPlate = new GetPlateDetection(indeterminatePlatesBar,DetectedPlateAux,vehiclePlateAdapter, getContext(),platesLists);
            getPlate.execute(gatewayId);


        }
        else{
            platesListsDetected.setVisibility(View.INVISIBLE);
        }

        buttonExitVehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString("id",id);
                args.putString("entry",entryDialog);
                args.putString("button",EXIT);
                args.putString("apartmentNumber",apartmentNumber);
                args.putString("licensePlate",licensePlate);
                args.putSerializable("platesDetected",DetectedPlate);


                DialogExitAndFineVehicleRegister dialog = new DialogExitAndFineVehicleRegister();
                dialog.setArguments(args);
                dialog.setTargetFragment(DetailSearchVehiclesListFragment.this,0);
                dialog.show(getFragmentManager(), "dialog");
            }
        });

        buttonFineVehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString("id",id);
                args.putString("entry",entryDialog);
                args.putString("button",FINE);
                args.putString("apartmentNumber",apartmentNumber);
                args.putString("licensePlate",licensePlate);

                DialogExitAndFineVehicleRegister dialog = new DialogExitAndFineVehicleRegister();
                dialog.setArguments(args);
                dialog.setTargetFragment(DetailSearchVehiclesListFragment.this,0);
                dialog.show(getFragmentManager(), "dialog");
            }
        });

        if(buttonExitVehicleVisible) {
            platesLists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    int i = 0;
                    for (i = 0; i < vehiclePlateAdapter.getCount(); i++) {
                        vehiclePlateAdapter.getItem(i).setSelected(false);

                    }
                    //SE MARCA
                    vehiclePlateAdapter.setFocusedPosition(position);
                    ((VehiclePlateDetected) parent.getItemAtPosition(position)).setSelected(true);
                    VehiclePlateDetected vehiclePlateDetected = (VehiclePlateDetected) parent.getItemAtPosition(position);


                }
            });
        }


        textViewLicensePlate = (TextView) rootView.findViewById(R.id.textViewDetailLicensePlate);
        textViewLicensePlate.setText(licensePlate);
        textViewParkingNumber = (TextView) rootView.findViewById(R.id.textViewDetailParkingNumber);
        textViewParkingNumber.setText(parkingNumber);
        textViewEntry = (TextView) rootView.findViewById(R.id.textViewDetailEntry);
        textViewEntry.setText(entry);
        textViewExitDate = (TextView) rootView.findViewById(R.id.textViewDetailExitDate);
        textViewExitDate.setText(exitDate);
        textViewFineDate = (TextView) rootView.findViewById(R.id.textViewDetailFineDate);
        textViewFineDate.setText(fineDate);
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
                    textViewExitDate.setText(exitDate);
                    textViewFineDate.setText(fineDate);

                    if (!exitDate.equals(NO_AVAILABLE))
                    {
                        buttonExitVehicle.setVisibility(View.GONE);
                        buttonFineVehicle.setVisibility(View.GONE);
                    }
                    if (!fineDate.equals(NO_AVAILABLE)){
                        buttonFineVehicle.setVisibility(View.GONE);
                    }
                }
                break;
        }
    }

    public void getExitDate (){

        final String query1 = "SELECT " + VehicleEntry.COLUMN_EXIT_DATE + "," + VehicleEntry.COLUMN_FINE_DATE +
                " FROM " + VehicleEntry.TABLE_NAME +
                " WHERE " + VehicleEntry._ID + " = ?";

        String [] args2 = new String[]{id};

        Cursor c1;

        try {
            SelectToDBRaw selectExitVehicle = new SelectToDBRaw(query1,args2);
            c1 = selectExitVehicle.execute().get();
        }catch (Exception e){
            c1 = null;
        }

        if (c1 != null){
            if(c1.moveToFirst()){
                if (c1.getString(0) == null || c1.getString(0).isEmpty())
                {
                    exitDate = NO_AVAILABLE;
                }
                else
                {
                    exitDate = Utility.changeDateFormat(c1.getString(0),"ENTRY");
                }

                if (c1.getString(1) == null || c1.getString(1).isEmpty())
                {
                    fineDate = NO_AVAILABLE;
                }
                else
                {
                    fineDate = Utility.changeDateFormat(c1.getString(1),"ENTRY");
                }
            }
        }
    }
}
