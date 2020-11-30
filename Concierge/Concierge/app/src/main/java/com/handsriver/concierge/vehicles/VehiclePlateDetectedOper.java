package com.handsriver.concierge.vehicles;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeContract.VehicleEntry;
import com.handsriver.concierge.database.ConciergeContract.ApartmentEntry;
import com.handsriver.concierge.database.SelectToDBRaw;
import com.handsriver.concierge.database.updatesTables.UpdateExitVehicle;
import com.handsriver.concierge.utilities.Utility;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Created by alain_r._trouve_silva after 08-07-17.
 */

public class VehiclePlateDetectedOper implements Serializable {

    private ArrayList<VehiclePlateDetected> itemsPlateDetected;
    private Context context;
    public static final String PREFS_PLATE_DETECTION_NAME = "PlateDetectionPrefs";
    public static final String PREFS_NAME = "PorterPrefs";

    public ArrayList<VehiclePlateDetected> getItemsPlateDetected() {
        return itemsPlateDetected;
    }
    public void setItemsPlateDetected(ArrayList<VehiclePlateDetected> itemsPlateDetected) {
        this.itemsPlateDetected = itemsPlateDetected;
    }

    public VehiclePlateDetectedOper(Context context){
        this.context = context;
    }


    public void getDetectedVehicles(){

        this.itemsPlateDetected = new ArrayList<VehiclePlateDetected>();

        //OBTAIN PLATES DETECTED
        SharedPreferences plateDetectionPref = context.getSharedPreferences(PREFS_PLATE_DETECTION_NAME, Context.MODE_PRIVATE);
        String platesDetection = plateDetectionPref.getString(context.getString(R.string.platesDetectionReceived), "");

        if (platesDetection.length() > 0){
            List<String> plates_detected = Arrays.asList(platesDetection.split("\\s*;\\s*"));
            plates_detected = new ArrayList<String>(plates_detected);
            for (String elements_plate:plates_detected) {

                List<String> items_plate_detected =  Arrays.asList(elements_plate.split("\\s*@\\s*"));

                VehiclePlateDetected newplatedetected = new VehiclePlateDetected();

                newplatedetected.setLicensePlate(items_plate_detected.get(0));
                newplatedetected.setDate(items_plate_detected.get(1));
                newplatedetected.setUrlImage(items_plate_detected.get(2));

                this.itemsPlateDetected.add(newplatedetected);
            }
        }

    }

    public void ExitVehicles(){

        this.itemsPlateDetected = new ArrayList<VehiclePlateDetected>();

        //OBTAIN PLATES DETECTED
        SharedPreferences plateDetectionPref = context.getSharedPreferences(PREFS_PLATE_DETECTION_NAME, Context.MODE_PRIVATE);
        String platesDetection = plateDetectionPref.getString(context.getString(R.string.platesDetectionReceived), "");

        if (platesDetection.length() > 0){
            List<String> plates_detected = Arrays.asList(platesDetection.split("\\s*;\\s*"));
            plates_detected = new ArrayList<String>(plates_detected);
            for (String elements_plate:plates_detected) {

                List<String> items_plate_detected =  Arrays.asList(elements_plate.split("\\s*@\\s*"));

                VehiclePlateDetected newplatedetected = new VehiclePlateDetected();

                newplatedetected.setLicensePlate(items_plate_detected.get(0));
                newplatedetected.setDate(items_plate_detected.get(1));
                newplatedetected.setUrlImage(items_plate_detected.get(2));

                //PARA CADA UNO SE DEBE AVERIGUAR SI CORRESPONDE A UNA SALIDA
                //SI CORRESPONDE A UNA SALIDA ENTONCES SE DEBE ASOCIAR A LA PATENTE

                if(newplatedetected.getLicensePlate() == "noPlate"){
                    //SE TRATA DE UNA PATENTE NO DETECTADA POR ENDE EL TRATO ES DIFERENTE
                    //SE AGREGA AL LISTADO
                    this.itemsPlateDetected.add(newplatedetected);

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
                            " AND " + VehicleEntry.TABLE_NAME + "." + VehicleEntry.COLUMN_LICENSE_PLATE + " = '" + newplatedetected.getLicensePlate() + "'" +
                            " AND " + VehicleEntry.TABLE_NAME + "." + VehicleEntry.COLUMN_EXIT_DATE + " is null " +
                            " AND " + VehicleEntry.TABLE_NAME + "." + VehicleEntry.COLUMN_ENTRY + " < '" + newplatedetected.getDate() + "'" +
                            " ORDER BY " + VehicleEntry.COLUMN_ENTRY + " DESC limit 1";

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

                        if(vehicle.getEntry()!=null){

                            //SE TRATA DE UNA SALIDA
                            String exit = newplatedetected.getDate();

                            int porterIdServer;
                            int hours;
                            boolean isAutomatic;
                            boolean isFined;

                            SharedPreferences porterPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                            porterIdServer = porterPrefs.getInt(context.getString(R.string.porterIdServerVar), DEF_PORTER_ID);

                            SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(context);
                            hours = Integer.parseInt(settingsPrefs.getString(context.getString(R.string.pref_id_max_time_parking_key),"0"));
                            isAutomatic = settingsPrefs.getBoolean(context.getString(R.string.pref_id_automatic_fine_key),true);

                            isFined = Utility.differenceDateHours(exit,vehicle.getEntry(),hours);

                            UpdateExitVehicle updateExitVehicle = new UpdateExitVehicle(exit,porterIdServer,vehicle.getId(),isAutomatic,isFined,"exit",context,vehicle.getApartmentNumber(),vehicle.getLicensePlate(),vehicle.getEntry());
                            updateExitVehicle.execute();

                            //SE ELIMINA DE LAS PREFERENCIAS
                            DeleteFromSharedPreferences(newplatedetected);
                        }
                        else{
                            //ES UN POTENCIAL REGISTRO DE INGRESO, PUESTO QUE NO TIENE ENTRADA REGISTRADA SIN SALIDA
                            this.itemsPlateDetected.add(newplatedetected);
                        }



                    }
                    else{
                        //ES UN POTENCIAL REGISTRO DE INGRESO, PUESTO QUE NO TIENE ENTRADA REGISTRADA SIN SALIDA
                        this.itemsPlateDetected.add(newplatedetected);
                    }
                }
            }
        }

    }

    public VehiclePlateDetected getItemSelected(){
        for (VehiclePlateDetected itm_check : this.itemsPlateDetected) {
            if(itm_check.getSelected()){
                return itm_check;
            }

        }
        return null;
    }

    public void DeleteFromSharedPreferences(VehiclePlateDetected vehiclePlateDetectedToDelete){
        if(vehiclePlateDetectedToDelete!=null){

            String stringToDelete = vehiclePlateDetectedToDelete.getLicensePlate()+"@"+vehiclePlateDetectedToDelete.getDate()+"@"+vehiclePlateDetectedToDelete.getUrlImage()+";";

            SharedPreferences plateDetectionPrefs = context.getSharedPreferences(PREFS_PLATE_DETECTION_NAME, Context.MODE_PRIVATE);
            String platesDetection = plateDetectionPrefs.getString(context.getString(R.string.platesDetectionReceived), "");

            platesDetection = platesDetection.replace(stringToDelete,"");
            SharedPreferences.Editor editor = plateDetectionPrefs.edit();
            editor.putString(context.getString(R.string.platesDetectionReceived),platesDetection);
            editor.apply();
        }


    }




}
