package com.handsriver.concierge.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeContract.VisitEntry;
import com.handsriver.concierge.database.ConciergeContract.VehicleEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.InsertUpdateTables.IUSuppliers;
import com.handsriver.concierge.database.SelectToDB;
import com.handsriver.concierge.database.updatesTables.UpdateSyncVisitsVehicles;
import com.handsriver.concierge.utilities.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Created by alain_r._trouve_silva after 27-04-17.
 */

public class VisitsVehiclesSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = VisitsVehiclesSyncAdapter.class.getSimpleName();
    public static final String VISITS_VEHICLES = "service";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    private Context mContext;
    ConciergeDbHelper dbHelper;
    SQLiteDatabase db;
    public static final String NOT_SYNC = "0";
    public static final String IS_SYNC = "1";
    public static final String NOT_UPDATE = "0";
    public static final String IS_UPDATE = "1";



    public VisitsVehiclesSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        this.mContext= context;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");

        HttpURLConnection urlConnection = null;
        HttpsURLConnection urlConnectionHttps = null;

        BufferedReader reader = null;
        dbHelper = new ConciergeDbHelper(mContext);
        db = DatabaseManager.getInstance().openDatabase();
        String visitsVehiclesJsonStr;

        final String ID_VISIT = "id_visit";
        final String ID_VEHICLE = "id_vehicle";
        final String IS_UPDATE = "is_update";


        try {
            SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            final String BASE_URL = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_URL_key),"");
            final int gatewayId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_gateway_key),"0"));
            final int buildingId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_building_key),"0"));
            final String API_KEY = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_apikey_key),"");
            String dateForServer = Utility.getHourForServer();
            final boolean isMarkExit = settingsPrefs.getBoolean(mContext.getResources().getString(R.string.pref_id_mark_exit_key),true);
            final int hours = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_max_time_parking_key),"0"));

            if (BASE_URL.length()>0 && (BASE_URL.startsWith(HTTP) || BASE_URL.startsWith(HTTPS))){
                Uri buildUri = Uri.parse(BASE_URL).buildUpon().appendPath(VISITS_VEHICLES).build();
                URL url = new URL(buildUri.toString());

                String[] projection = {
                        VisitEntry._ID,
                        VisitEntry.COLUMN_APARTMENT_ID,
                        VisitEntry.COLUMN_PORTER_ID,
                        VisitEntry.COLUMN_ENTRY,
                        VisitEntry.COLUMN_DOCUMENT_NUMBER,
                        VisitEntry.COLUMN_FULL_NAME,
                        VisitEntry.COLUMN_GENDER,
                        VisitEntry.COLUMN_BIRTHDATE,
                        VisitEntry.COLUMN_OPTIONAL,
                        VisitEntry.COLUMN_NATIONALITY,
                };

                String selection = VisitEntry.COLUMN_IS_SYNC + " = ? AND " + VisitEntry.COLUMN_GATEWAY_ID + " = ?";
                String [] selectionArgs = {NOT_SYNC,String.valueOf(gatewayId)};

                Cursor visits;

                visits = db.query(VisitEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,null,null);

                JSONArray visitsArrayJson = new JSONArray();

                if (visits != null && visits.getCount()>0){
                    while (visits.moveToNext()){
                        JSONObject visitsJson = new JSONObject();
                        visitsJson.put(ID_VISIT,visits.getLong(visits.getColumnIndex(VisitEntry._ID)));
                        visitsJson.put(VisitEntry.COLUMN_APARTMENT_ID,visits.getLong(visits.getColumnIndex(VisitEntry.COLUMN_APARTMENT_ID)));
                        visitsJson.put(VisitEntry.COLUMN_PORTER_ID,visits.getLong(visits.getColumnIndex(VisitEntry.COLUMN_PORTER_ID)));
                        visitsJson.put(VisitEntry.COLUMN_ENTRY,visits.getString(visits.getColumnIndex(VisitEntry.COLUMN_ENTRY)));
                        visitsJson.put(VisitEntry.COLUMN_DOCUMENT_NUMBER, (visits.isNull(visits.getColumnIndex(VisitEntry.COLUMN_DOCUMENT_NUMBER))) ? JSONObject.NULL : visits.getString(visits.getColumnIndex(VisitEntry.COLUMN_DOCUMENT_NUMBER)));
                        visitsJson.put(VisitEntry.COLUMN_FULL_NAME,(visits.isNull(visits.getColumnIndex(VisitEntry.COLUMN_FULL_NAME))) ? JSONObject.NULL : visits.getString(visits.getColumnIndex(VisitEntry.COLUMN_FULL_NAME)));
                        visitsJson.put(VisitEntry.COLUMN_GENDER,(visits.isNull(visits.getColumnIndex(VisitEntry.COLUMN_GENDER))) ? JSONObject.NULL : visits.getString(visits.getColumnIndex(VisitEntry.COLUMN_GENDER)));
                        visitsJson.put(VisitEntry.COLUMN_BIRTHDATE,(visits.isNull(visits.getColumnIndex(VisitEntry.COLUMN_BIRTHDATE))) ? JSONObject.NULL : visits.getString(visits.getColumnIndex(VisitEntry.COLUMN_BIRTHDATE)));
                        visitsJson.put(VisitEntry.COLUMN_OPTIONAL,(visits.isNull(visits.getColumnIndex(VisitEntry.COLUMN_OPTIONAL))) ? JSONObject.NULL : visits.getString(visits.getColumnIndex(VisitEntry.COLUMN_OPTIONAL)));
                        visitsJson.put(VisitEntry.COLUMN_NATIONALITY,(visits.isNull(visits.getColumnIndex(VisitEntry.COLUMN_NATIONALITY))) ? JSONObject.NULL : visits.getString(visits.getColumnIndex(VisitEntry.COLUMN_NATIONALITY)));
                        visitsArrayJson.put(visitsJson);
                    }
                    visits.close();
                }



                String[] projection_vi_u = {
                        VisitEntry._ID,
                        VisitEntry.COLUMN_VISIT_ID_SERVER,
                        VisitEntry.COLUMN_EXIT_PORTER_ID,
                        VisitEntry.COLUMN_EXIT_DATE,

                };

                String selection_vi_u = VisitEntry.COLUMN_IS_SYNC + " = ? AND " + VisitEntry.COLUMN_GATEWAY_ID + " = ? AND " + VisitEntry.COLUMN_IS_UPDATE + " = ? AND " + VisitEntry.COLUMN_EXIT_DATE + " IS NOT NULL";
                String [] selectionArgs_vi_u = {IS_SYNC,String.valueOf(gatewayId),NOT_UPDATE};

                Cursor visit_update = null;

                visit_update = db.query(VisitEntry.TABLE_NAME,projection_vi_u,selection_vi_u,selectionArgs_vi_u,null,null,null,null);


                if (visit_update != null && visit_update.getCount()>0){
                    while (visit_update.moveToNext()){
                        JSONObject visitJson = new JSONObject();
                        visitJson.put(ID_VISIT,visit_update.getLong(visit_update.getColumnIndex(VisitEntry._ID)));
                        visitJson.put(IS_UPDATE,true);
                        visitJson.put(VisitEntry.COLUMN_EXIT_DATE,(visit_update.isNull(visit_update.getColumnIndex(VisitEntry.COLUMN_EXIT_DATE))) ? JSONObject.NULL : visit_update.getString(visit_update.getColumnIndex(VisitEntry.COLUMN_EXIT_DATE)));
                        visitJson.put(VisitEntry.COLUMN_EXIT_PORTER_ID,(visit_update.isNull(visit_update.getColumnIndex(VisitEntry.COLUMN_EXIT_PORTER_ID))) ? JSONObject.NULL : visit_update.getLong(visit_update.getColumnIndex(VisitEntry.COLUMN_EXIT_PORTER_ID)));
                        visitJson.put(VisitEntry.COLUMN_VISIT_ID_SERVER,visit_update.getLong(visit_update.getColumnIndex(VisitEntry.COLUMN_VISIT_ID_SERVER)));

                        visitsArrayJson.put(visitJson);
                    }
                    visit_update.close();
                }


                JSONArray vehiclesArrayJson = new JSONArray();

                String[] projection_v = {
                        VehicleEntry._ID,
                        VehicleEntry.COLUMN_APARTMENT_ID,
                        VehicleEntry.COLUMN_PORTER_ID,
                        VehicleEntry.COLUMN_ENTRY,
                        VehicleEntry.COLUMN_LICENSE_PLATE,
                        VehicleEntry.COLUMN_FINE_DATE,
                        VehicleEntry.COLUMN_FINE_PORTER_ID,
                        VehicleEntry.COLUMN_EXIT_DATE,
                        VehicleEntry.COLUMN_PARKING_NUMBER,
                        VehicleEntry.COLUMN_EXIT_PORTER_ID
                };

                String selection_v = VehicleEntry.COLUMN_IS_SYNC + " = ? AND " + VehicleEntry.COLUMN_GATEWAY_ID + " = ? AND " + VehicleEntry.COLUMN_IS_UPDATE + " = ? ";
                String [] selectionArgs_v = {NOT_SYNC,String.valueOf(gatewayId),NOT_UPDATE};

                Cursor vehicles;
                vehicles = db.query(VehicleEntry.TABLE_NAME,projection_v,selection_v,selectionArgs_v,null,null,null,null);

                if (vehicles != null && vehicles.getCount()>0){
                    while (vehicles.moveToNext()){
                        JSONObject vehicleJson = new JSONObject();
                        vehicleJson.put(ID_VEHICLE,vehicles.getLong(vehicles.getColumnIndex(VehicleEntry._ID)));
                        vehicleJson.put(IS_UPDATE,false);
                        vehicleJson.put(VehicleEntry.COLUMN_APARTMENT_ID,vehicles.getLong(vehicles.getColumnIndex(VehicleEntry.COLUMN_APARTMENT_ID)));
                        vehicleJson.put(VehicleEntry.COLUMN_PORTER_ID,vehicles.getLong(vehicles.getColumnIndex(VehicleEntry.COLUMN_PORTER_ID)));
                        vehicleJson.put(VehicleEntry.COLUMN_ENTRY,vehicles.getString(vehicles.getColumnIndex(VehicleEntry.COLUMN_ENTRY)));
                        vehicleJson.put(VehicleEntry.COLUMN_LICENSE_PLATE,vehicles.getString(vehicles.getColumnIndex(VehicleEntry.COLUMN_LICENSE_PLATE)));
                        vehicleJson.put(VehicleEntry.COLUMN_FINE_DATE,(vehicles.isNull(vehicles.getColumnIndex(VehicleEntry.COLUMN_FINE_DATE))) ? JSONObject.NULL : vehicles.getString(vehicles.getColumnIndex(VehicleEntry.COLUMN_FINE_DATE)));
                        vehicleJson.put(VehicleEntry.COLUMN_FINE_PORTER_ID,(vehicles.isNull(vehicles.getColumnIndex(VehicleEntry.COLUMN_FINE_PORTER_ID))) ? JSONObject.NULL : vehicles.getLong(vehicles.getColumnIndex(VehicleEntry.COLUMN_FINE_PORTER_ID)));
                        vehicleJson.put(VehicleEntry.COLUMN_EXIT_DATE,(vehicles.isNull(vehicles.getColumnIndex(VehicleEntry.COLUMN_EXIT_DATE))) ? JSONObject.NULL : vehicles.getString(vehicles.getColumnIndex(VehicleEntry.COLUMN_EXIT_DATE)));
                        vehicleJson.put(VehicleEntry.COLUMN_PARKING_NUMBER,(vehicles.isNull(vehicles.getColumnIndex(VehicleEntry.COLUMN_PARKING_NUMBER))) ? JSONObject.NULL : vehicles.getString(vehicles.getColumnIndex(VehicleEntry.COLUMN_PARKING_NUMBER)));
                        vehicleJson.put(VehicleEntry.COLUMN_EXIT_PORTER_ID,(vehicles.isNull(vehicles.getColumnIndex(VehicleEntry.COLUMN_EXIT_PORTER_ID))) ? JSONObject.NULL : vehicles.getLong(vehicles.getColumnIndex(VehicleEntry.COLUMN_EXIT_PORTER_ID)));

                        vehiclesArrayJson.put(vehicleJson);
                    }
                    vehicles.close();
                }

                String[] projection_v_u = {
                        VehicleEntry._ID,
                        VehicleEntry.COLUMN_ENTRY,
                        VehicleEntry.COLUMN_LICENSE_PLATE,
                        VehicleEntry.COLUMN_FINE_DATE,
                        VehicleEntry.COLUMN_FINE_PORTER_ID,
                        VehicleEntry.COLUMN_EXIT_DATE,
                        VehicleEntry.COLUMN_EXIT_PORTER_ID,
                        VehicleEntry.COLUMN_VEHICLE_ID_SERVER
                };

                Cursor vehicle_update = null;

                if (isMarkExit && hours == 0){
                    String selection_v_u = VehicleEntry.COLUMN_IS_SYNC + " = ? AND " + VehicleEntry.COLUMN_GATEWAY_ID + " = ? AND " + VehicleEntry.COLUMN_IS_UPDATE + " = ? AND " + VehicleEntry.COLUMN_EXIT_DATE + " IS NOT NULL";
                    String [] selectionArgs_v_u = {IS_SYNC,String.valueOf(gatewayId),NOT_UPDATE};
                    vehicle_update = db.query(VehicleEntry.TABLE_NAME,projection_v_u,selection_v_u,selectionArgs_v_u,null,null,null,null);

                } else if (isMarkExit && hours > 0){
                    String selection_v_u = VehicleEntry.COLUMN_IS_SYNC + " = ? AND " + VehicleEntry.COLUMN_GATEWAY_ID + " = ? AND " + VehicleEntry.COLUMN_IS_UPDATE + " = ? AND (" + VehicleEntry.COLUMN_EXIT_DATE + " IS NOT NULL OR (" + VehicleEntry.COLUMN_IS_UPDATE_FINE + " = ? AND " + VehicleEntry.COLUMN_FINE_DATE + " IS NOT NULL))";
                    String [] selectionArgs_v_u = {IS_SYNC,String.valueOf(gatewayId),NOT_UPDATE,NOT_UPDATE};
                    vehicle_update = db.query(VehicleEntry.TABLE_NAME,projection_v_u,selection_v_u,selectionArgs_v_u,null,null,null,null);

                }else if (!isMarkExit && hours > 0){
                    String selection_v_u = VehicleEntry.COLUMN_IS_SYNC + " = ? AND " + VehicleEntry.COLUMN_GATEWAY_ID + " = ? AND " + VehicleEntry.COLUMN_IS_UPDATE + " = ? AND " + VehicleEntry.COLUMN_FINE_DATE + " IS NOT NULL";
                    String [] selectionArgs_v_u = {IS_SYNC,String.valueOf(gatewayId),NOT_UPDATE};
                    vehicle_update = db.query(VehicleEntry.TABLE_NAME,projection_v_u,selection_v_u,selectionArgs_v_u,null,null,null,null);

                }

                if (vehicle_update != null && vehicle_update.getCount()>0){
                    while (vehicle_update.moveToNext()){
                        JSONObject vehicleJson = new JSONObject();
                        vehicleJson.put(ID_VEHICLE,vehicle_update.getLong(vehicle_update.getColumnIndex(VehicleEntry._ID)));
                        vehicleJson.put(IS_UPDATE,true);
                        vehicleJson.put(VehicleEntry.COLUMN_ENTRY,vehicle_update.getString(vehicle_update.getColumnIndex(VehicleEntry.COLUMN_ENTRY)));
                        vehicleJson.put(VehicleEntry.COLUMN_LICENSE_PLATE,vehicle_update.getString(vehicle_update.getColumnIndex(VehicleEntry.COLUMN_LICENSE_PLATE)));
                        vehicleJson.put(VehicleEntry.COLUMN_FINE_DATE,(vehicle_update.isNull(vehicle_update.getColumnIndex(VehicleEntry.COLUMN_FINE_DATE))) ? JSONObject.NULL : vehicle_update.getString(vehicle_update.getColumnIndex(VehicleEntry.COLUMN_FINE_DATE)));
                        vehicleJson.put(VehicleEntry.COLUMN_FINE_PORTER_ID,(vehicle_update.isNull(vehicle_update.getColumnIndex(VehicleEntry.COLUMN_FINE_PORTER_ID))) ? JSONObject.NULL : vehicle_update.getLong(vehicle_update.getColumnIndex(VehicleEntry.COLUMN_FINE_PORTER_ID)));
                        vehicleJson.put(VehicleEntry.COLUMN_EXIT_DATE,(vehicle_update.isNull(vehicle_update.getColumnIndex(VehicleEntry.COLUMN_EXIT_DATE))) ? JSONObject.NULL : vehicle_update.getString(vehicle_update.getColumnIndex(VehicleEntry.COLUMN_EXIT_DATE)));
                        vehicleJson.put(VehicleEntry.COLUMN_EXIT_PORTER_ID,(vehicle_update.isNull(vehicle_update.getColumnIndex(VehicleEntry.COLUMN_EXIT_PORTER_ID))) ? JSONObject.NULL : vehicle_update.getLong(vehicle_update.getColumnIndex(VehicleEntry.COLUMN_EXIT_PORTER_ID)));
                        vehicleJson.put(VehicleEntry.COLUMN_VEHICLE_ID_SERVER,vehicle_update.getLong(vehicle_update.getColumnIndex(VehicleEntry.COLUMN_VEHICLE_ID_SERVER)));

                        vehiclesArrayJson.put(vehicleJson);
                    }
                    vehicle_update.close();
                }

                if ((visits != null && visits.getCount()>0) || (vehicles != null && vehicles.getCount()>0) || (vehicle_update != null && vehicle_update.getCount()>0)){
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.accumulate("building_id",buildingId);
                    jsonObject.accumulate("last_update",dateForServer);
                    jsonObject.accumulate("gateway_id",gatewayId);
                    if (visitsArrayJson.length() != 0){
                        jsonObject.accumulate("visits",visitsArrayJson);
                    }
                    if (vehiclesArrayJson.length() != 0){
                        jsonObject.accumulate("vehicles",vehiclesArrayJson);
                    }

                    if (BASE_URL.startsWith(HTTPS)){
                        urlConnectionHttps = (HttpsURLConnection) url.openConnection();
                        urlConnectionHttps.setRequestMethod("POST");
                        urlConnectionHttps.setRequestProperty("Content-Type","application/json");
                        urlConnectionHttps.setRequestProperty("api-key",API_KEY);
                        urlConnectionHttps.setDoOutput(true);
                        urlConnectionHttps.setDoInput(true);
                        urlConnectionHttps.setConnectTimeout(15000);
                        urlConnectionHttps.connect();

                        OutputStream dataOutputStream = new BufferedOutputStream(urlConnectionHttps.getOutputStream());
                        OutputStreamWriter osw = new OutputStreamWriter(dataOutputStream, "UTF-8");
                        osw.write(jsonObject.toString());
                        osw.flush();
                        osw.close();

                        InputStreamReader inputStream = new InputStreamReader(urlConnectionHttps.getInputStream(),"UTF-8");

                        StringBuffer buffer = new StringBuffer();
                        if (inputStream == null) {
                            return;
                        }
                        reader = new BufferedReader(inputStream);

                        String line;
                        while ((line = reader.readLine()) != null) {

                            buffer.append(line + "\n");
                        }

                        if (buffer.length() == 0) {
                            return;
                        }
                        visitsVehiclesJsonStr = buffer.toString();
                        getVisitsVehiclesDataFromJson(visitsVehiclesJsonStr,isMarkExit,hours);
                    }
                    else{
                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setRequestMethod("POST");
                        urlConnection.setRequestProperty("Content-Type","application/json");
                        urlConnection.setRequestProperty("api-key",API_KEY);
                        urlConnection.setDoOutput(true);
                        urlConnection.setDoInput(true);
                        urlConnection.setConnectTimeout(5000);
                        urlConnection.connect();

                        OutputStream dataOutputStream = new BufferedOutputStream(urlConnection.getOutputStream());
                        OutputStreamWriter osw = new OutputStreamWriter(dataOutputStream, "UTF-8");
                        osw.write(jsonObject.toString());
                        osw.flush();
                        osw.close();

                        InputStreamReader inputStream = new InputStreamReader(urlConnection.getInputStream(),"UTF-8");

                        StringBuffer buffer = new StringBuffer();
                        if (inputStream == null) {
                            return;
                        }
                        reader = new BufferedReader(inputStream);

                        String line;
                        while ((line = reader.readLine()) != null) {

                            buffer.append(line + "\n");
                        }

                        if (buffer.length() == 0) {
                            return;
                        }
                        visitsVehiclesJsonStr = buffer.toString();
                        getVisitsVehiclesDataFromJson(visitsVehiclesJsonStr,isMarkExit,hours);
                    }

                }
            }


        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        } finally {
            DatabaseManager.getInstance().closeDatabase();
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (urlConnectionHttps != null) {
                urlConnectionHttps.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    private void getVisitsVehiclesDataFromJson(String visitsVehiclesJsonStr, boolean isMarkExit, int hours)
            throws JSONException {


        final String VISITS_RETURN = "visits_return";
        final String VISITS_UPDATE_RETURN = "visit_update_return";
        final String VEHICLE_RETURN = "vehicle_return";
        final String VEHICLE_UPDATE_RETURN = "vehicle_update_return";

        final String ID_VISIT = "id_visit";
        final String INSERT_DATA = "insert_data";
        final String ID_VEHICLE = "id_vehicle";
        final String ID_SERVER = "id_server";
        final String UPDATE_DATA = "update_data";

        try {
            JSONObject returnJson = new JSONObject(visitsVehiclesJsonStr);
            JSONArray visitsReturn = (returnJson.isNull(VISITS_RETURN)) ? null : returnJson.getJSONArray(VISITS_RETURN);
            JSONArray visitsUpdateReturn = (returnJson.isNull(VISITS_UPDATE_RETURN)) ? null : returnJson.getJSONArray(VISITS_UPDATE_RETURN);
            JSONArray vehicleReturn = (returnJson.isNull(VEHICLE_RETURN)) ? null : returnJson.getJSONArray(VEHICLE_RETURN);
            JSONArray vehiclesUpdateReturn = (returnJson.isNull(VEHICLE_UPDATE_RETURN)) ? null : returnJson.getJSONArray(VEHICLE_UPDATE_RETURN);



            Vector<ContentValues> vectorVisitsReturn;
            if (visitsReturn != null){
                vectorVisitsReturn  = new Vector<ContentValues>(visitsReturn.length());

                for(int i = 0; i < visitsReturn.length(); i++) {

                    long id_visit;
                    boolean insert_data;
                    long id_server;

                    JSONObject returnVisitJson = visitsReturn.getJSONObject(i);

                    id_visit = returnVisitJson.getLong(ID_VISIT);
                    insert_data = returnVisitJson.getBoolean(INSERT_DATA);

                    ContentValues visitValues = new ContentValues();
                    if (insert_data){
                        visitValues.put(VisitEntry.COLUMN_IS_SYNC, IS_SYNC);
                        id_server = returnVisitJson.getLong(ID_SERVER);
                        visitValues.put(VisitEntry.COLUMN_VISIT_ID_SERVER,id_server);
                    }
                    else{
                        visitValues.put(VisitEntry.COLUMN_IS_SYNC, NOT_SYNC);

                    }
                    visitValues.put(VisitEntry._ID, id_visit);

                    vectorVisitsReturn.add(visitValues);
                }
            }
            else{
                vectorVisitsReturn = null;
            }

            Vector<ContentValues> vectorVehiclesReturn;
            if (vehicleReturn != null){
                vectorVehiclesReturn = new Vector<ContentValues>(vehicleReturn.length());

                for(int i = 0; i < vehicleReturn.length(); i++) {

                    long id_vehicle;
                    boolean insert_data;
                    long id_server;

                    JSONObject returnVehicleJson = vehicleReturn.getJSONObject(i);

                    id_vehicle = returnVehicleJson.getLong(ID_VEHICLE);
                    insert_data = returnVehicleJson.getBoolean(INSERT_DATA);

                    ContentValues vehiclesValues = new ContentValues();
                    if (insert_data){
                        vehiclesValues.put(VehicleEntry.COLUMN_IS_SYNC, IS_SYNC);
                        id_server = returnVehicleJson.getLong(ID_SERVER);
                        vehiclesValues.put(VehicleEntry.COLUMN_VEHICLE_ID_SERVER,id_server);
                    }
                    else{
                        vehiclesValues.put(VehicleEntry.COLUMN_IS_SYNC, NOT_SYNC);
                    }
                    vehiclesValues.put(VehicleEntry._ID, id_vehicle);

                    vectorVehiclesReturn.add(vehiclesValues);
                }
            }else{
                vectorVehiclesReturn = null;
            }

            Vector<ContentValues> vectorVehiclesUpdateReturn;
            if (vehiclesUpdateReturn != null){
                vectorVehiclesUpdateReturn = new Vector<ContentValues>(vehiclesUpdateReturn.length());

                for(int i = 0; i < vehiclesUpdateReturn.length(); i++) {

                    long id_vehicle;
                    boolean update_data;

                    JSONObject returnVehicleUpdateJson = vehiclesUpdateReturn.getJSONObject(i);

                    id_vehicle = returnVehicleUpdateJson.getLong(ID_VEHICLE);
                    update_data = returnVehicleUpdateJson.getBoolean(UPDATE_DATA);

                    ContentValues vehiclesUpdateValues = new ContentValues();
                    if (update_data){
                        vehiclesUpdateValues.put(VehicleEntry.COLUMN_IS_UPDATE, IS_UPDATE);
                    }
                    else{
                        vehiclesUpdateValues.put(VehicleEntry.COLUMN_IS_UPDATE, NOT_UPDATE);
                    }
                    vehiclesUpdateValues.put(VehicleEntry._ID, id_vehicle);

                    vectorVehiclesUpdateReturn.add(vehiclesUpdateValues);
                }
            }
            else{
                vectorVehiclesUpdateReturn = null;
            }


            Vector<ContentValues> vectorVisitsUpdateReturn;
            if (visitsUpdateReturn != null){
                vectorVisitsUpdateReturn = new Vector<ContentValues>(visitsUpdateReturn.length());

                for(int i = 0; i < visitsUpdateReturn.length(); i++) {

                    long id_visit;
                    boolean update_data;

                    JSONObject returnVisitUpdateJson = visitsUpdateReturn.getJSONObject(i);

                    id_visit = returnVisitUpdateJson.getLong(ID_VISIT);
                    update_data = returnVisitUpdateJson.getBoolean(UPDATE_DATA);

                    ContentValues visitsUpdateValues = new ContentValues();
                    if (update_data){
                        visitsUpdateValues.put(VisitEntry.COLUMN_IS_UPDATE, IS_UPDATE);
                    }
                    else{
                        visitsUpdateValues.put(VisitEntry.COLUMN_IS_UPDATE, NOT_UPDATE);
                    }
                    visitsUpdateValues.put(VisitEntry._ID, id_visit);

                    vectorVisitsUpdateReturn.add(visitsUpdateValues);
                }
            }
            else{
                vectorVisitsUpdateReturn = null;
            }

            UpdateSyncVisitsVehicles.run(vectorVisitsReturn,vectorVehiclesReturn,vectorVehiclesUpdateReturn,vectorVisitsUpdateReturn,isMarkExit,hours);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

}
