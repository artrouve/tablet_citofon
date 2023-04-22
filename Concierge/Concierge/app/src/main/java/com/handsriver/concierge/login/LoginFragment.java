package com.handsriver.concierge.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;



import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.SelectToDBRaw;
import com.handsriver.concierge.database.updatesTables.UpdateExitVehicle;
import com.handsriver.concierge.home.MainActivity;
import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeContract.*;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.SelectToDB;
import com.handsriver.concierge.utilities.Utility;
import com.handsriver.concierge.vehicles.Vehicle;

import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.*;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;


import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
/*
import com.hcnetsdk.jna.HCNetSDKByJNA.*;
import com.hcnetsdk.jna.HCNetSDKByJNA;
import com.hcnetsdk.jna.HCNetSDKJNAInstance;
*/

/**
 * Created by Created by alain_r._trouve_silva after 25-01-17.
 */



public class LoginFragment extends Fragment {


    Button btnSignIn;
    View rootView;
    LinearLayout linearLogin;
    public static final String PREFS_NAME = "PorterPrefs";
    public static final String PREFS_PLATE_DETECTION_NAME = "PlateDetectionPrefs";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConciergeDbHelper helper= new ConciergeDbHelper(getContext());
        DatabaseManager.initializeInstance(helper);

    }




    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_login, container, false);

        btnSignIn = (Button)rootView.findViewById(R.id.button_login);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogLogIn dialog = new DialogLogIn();
                dialog.show(getFragmentManager(), "dialog");
            }
        });


        linearLogin = (LinearLayout) rootView.findViewById(R.id.activity_login);
        linearLogin.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Utility.hideKeyboard(v,getContext());
                return false;
            }
        });

        return rootView;
    }
}


/*



                                if(plate == "noPlate"){
                                    //SE TRATA DE UNA PATENTE NO DETECTADA POR ENDE EL TRATO ES DIFERENTE

                                }
                                else{
                                    //SE BUSCA EN EL REGISTRO PARA MARCAR LA SALIDA DEL VEHICULO SI ESTE YA ESTA ADENTRO
                                    //UNA VEZ SE ENCUENTRA EL VEHICULO ENTONCES SE MARCA LA SALIDA
                                    String query = "SELECT " + VehicleEntry.TABLE_NAME + "." + VehicleEntry._ID + "," +
                                            VehicleEntry.COLUMN_LICENSE_PLATE + "," +
                                            VehicleEntry.COLUMN_ENTRY + "," +
                                            VehicleEntry.COLUMN_EXIT_DATE + "," +
                                            VehicleEntry.COLUMN_FINE_DATE + "," +
                                            VehicleEntry.COLUMN_PARKING_NUMBER + "," +
                                            VehicleEntry.COLUMN_PORTER_ID + "," +
                                            ApartmentEntry.TABLE_NAME + "." + ApartmentEntry.COLUMN_APARTMENT_NUMBER +
                                            " FROM " + VehicleEntry.TABLE_NAME + "," + ApartmentEntry.TABLE_NAME +
                                            " WHERE " + ApartmentEntry.TABLE_NAME + "." + ApartmentEntry.COLUMN_APARTMENT_ID_SERVER + " = " + VehicleEntry.TABLE_NAME + "." + VehicleEntry.COLUMN_APARTMENT_ID +
                                            " AND " + VehicleEntry.TABLE_NAME + "." + VehicleEntry.COLUMN_LICENSE_PLATE + " = '" + plate + "'" +
                                            //" AND " + VehicleEntry.TABLE_NAME + "." + VehicleEntry.COLUMN_EXIT_DATE + " = null " +
                                            " ORDER BY " + VehicleEntry.COLUMN_ENTRY + " DESC";

                                    Cursor c;
                                    try {
                                        SelectToDBRaw selectVehicles = new SelectToDBRaw(query,null);
                                        c = selectVehicles.execute().get();
                                    }catch (Exception e){

                                        Exception ex = e;
                                        Log.e("Error", e.toString());
                                        c = null;
                                    }
                                    Vehicle vehicle = new Vehicle();
                                    Integer DEF_PORTER_ID = 0;
                                    if (c != null) {
                                        while (c.moveToNext()) {

                                            vehicle.setId(c.getString(c.getColumnIndex(VehicleEntry._ID)));
                                            vehicle.setLicensePlate(c.getString(c.getColumnIndex(VehicleEntry.COLUMN_LICENSE_PLATE)));
                                            vehicle.setEntry(c.getString(c.getColumnIndex(VehicleEntry.COLUMN_ENTRY)));
                                            vehicle.setExitDate(c.getString(c.getColumnIndex(VehicleEntry.COLUMN_EXIT_DATE)));
                                            vehicle.setFineDate(c.getString(c.getColumnIndex(VehicleEntry.COLUMN_FINE_DATE)));
                                            vehicle.setParkingNumber(c.getString(c.getColumnIndex(VehicleEntry.COLUMN_PARKING_NUMBER)));
                                            vehicle.setApartmentNumber(c.getString(c.getColumnIndex(ApartmentEntry.COLUMN_APARTMENT_NUMBER)));
                                            DEF_PORTER_ID = c.getInt(c.getColumnIndex(VehicleEntry.COLUMN_PORTER_ID));
                                            break;
                                        }
                                        String exit = datetime;

                                        int porterIdServer;
                                        int hours;
                                        boolean isAutomatic;
                                        boolean isFined;

                                        SharedPreferences porterPrefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                                        porterIdServer = porterPrefs.getInt(getString(R.string.porterIdServerVar), DEF_PORTER_ID);

                                        hours = Integer.parseInt(settingsPrefs.getString(getString(R.string.pref_id_max_time_parking_key),"0"));
                                        isAutomatic = settingsPrefs.getBoolean(getString(R.string.pref_id_automatic_fine_key),true);

                                        isFined = Utility.differenceDateHours(exit,vehicle.getEntry(),hours);

                                        UpdateExitVehicle updateExitVehicle = new UpdateExitVehicle(exit,porterIdServer,vehicle.getId(),isAutomatic,isFined,"exit",getContext(),vehicle.getApartmentNumber(),vehicle.getLicensePlate(),vehicle.getEntry());
                                        updateExitVehicle.execute();


                                    }
                                    else{
                                        //ES UN POTENCIAL REGISTRO DE INGRESO, PUESTO QUE NO TIENE SALIDA
                                    }
                                }

 */