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
import com.handsriver.concierge.database.ConciergeContract;
import com.handsriver.concierge.database.ConciergeContract.ResidentVehicleEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.updatesTables.UpdateSyncResidentsVehicles;

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
import java.net.URL;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Created by alain_r._trouve_silva after 27-04-17.
 */

public class ResidentsVehiclesTabletSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = ResidentsVehiclesTabletSyncAdapter.class.getSimpleName();
    public static final String RESIDENTSVEHICLES = "residentsvehicles_store";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    private Context mContext;
    ConciergeDbHelper dbHelper;
    SQLiteDatabase db;
    public static final String NOT_SYNC = "0";
    public static final String IS_SYNC = "1";
    public static final String NOT_UPDATE = "0";
    public static final String IS_UPDATE = "1";



    public ResidentsVehiclesTabletSyncAdapter(Context context, boolean autoInitialize) {
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
        String residentsvehiclesJsonStr;

        final String ID_RESIDENT = "id_residentvehicle";
        final String IS_UPDATE_COL = "is_update";


        try {
            SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            final String BASE_URL = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_URL_key),"");
            final int buildingId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_building_key),"0"));
            final String API_KEY = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_apikey_key),"");

            if (BASE_URL.length()>0 && (BASE_URL.startsWith(HTTP) || BASE_URL.startsWith(HTTPS))){
                Uri buildUri = Uri.parse(BASE_URL).buildUpon().appendPath(RESIDENTSVEHICLES).build();
                URL url = new URL(buildUri.toString());

                JSONArray residentsvehiclesArrayJson = new JSONArray();

                String[] projection = {
                        ResidentVehicleEntry._ID,
                        ResidentVehicleEntry.COLUMN_APARTMENT_ID,
                        ResidentVehicleEntry.COLUMN_PLATE,
                        ResidentVehicleEntry.COLUMN_ACTIVE,

                };

                String selection = ResidentVehicleEntry.COLUMN_IS_SYNC + " = ?";
                String [] selectionArgs = {NOT_SYNC};

                Cursor residentsvehicles = db.query(ResidentVehicleEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,null,null);

                if (residentsvehicles != null && residentsvehicles.getCount()>0){
                    while (residentsvehicles.moveToNext()){
                        JSONObject residentvehicleJson = new JSONObject();
                        residentvehicleJson.put(ID_RESIDENT,residentsvehicles.getLong(residentsvehicles.getColumnIndex(ResidentVehicleEntry._ID)));
                        residentvehicleJson.put(IS_UPDATE_COL,false);
                        residentvehicleJson.put(ResidentVehicleEntry.COLUMN_PLATE,residentsvehicles.getString(residentsvehicles.getColumnIndex(ResidentVehicleEntry.COLUMN_PLATE)));
                        residentvehicleJson.put(ResidentVehicleEntry.COLUMN_ACTIVE,residentsvehicles.getString(residentsvehicles.getColumnIndex(ResidentVehicleEntry.COLUMN_ACTIVE)));

                        residentvehicleJson.put(ResidentVehicleEntry.COLUMN_APARTMENT_ID,residentsvehicles.getLong(residentsvehicles.getColumnIndex(ResidentVehicleEntry.COLUMN_APARTMENT_ID)));

                        residentsvehiclesArrayJson.put(residentvehicleJson);
                    }
                    residentsvehicles.close();
                }

                String[] projection_u = {
                        ResidentVehicleEntry._ID,
                        ResidentVehicleEntry.COLUMN_PLATE,
                        ResidentVehicleEntry.COLUMN_ACTIVE,
                        ResidentVehicleEntry.COLUMN_RESIDENTVEHICLE_ID_SERVER

                };


                String selection_u = ResidentVehicleEntry.COLUMN_IS_SYNC + " = ? AND " + ResidentVehicleEntry.COLUMN_IS_UPDATE + " = ?";
                String [] selectionArgs_u = {IS_SYNC,IS_UPDATE};

                Cursor residentsvehicles_update = db.query(ResidentVehicleEntry.TABLE_NAME,projection_u,selection_u,selectionArgs_u,null,null,null,null);

                if (residentsvehicles_update != null && residentsvehicles_update.getCount()>0){
                    while (residentsvehicles_update.moveToNext()){
                        JSONObject residentvehicleUpdateJson = new JSONObject();
                        residentvehicleUpdateJson.put(ID_RESIDENT,residentsvehicles_update.getLong(residentsvehicles_update.getColumnIndex(ResidentVehicleEntry._ID)));
                        residentvehicleUpdateJson.put(IS_UPDATE_COL,true);
                        residentvehicleUpdateJson.put(ResidentVehicleEntry.COLUMN_PLATE,(residentsvehicles_update.isNull(residentsvehicles_update.getColumnIndex(ResidentVehicleEntry.COLUMN_PLATE))) ? JSONObject.NULL : residentsvehicles_update.getString(residentsvehicles_update.getColumnIndex(ResidentVehicleEntry.COLUMN_PLATE)));
                        residentvehicleUpdateJson.put(ResidentVehicleEntry.COLUMN_ACTIVE,(residentsvehicles_update.isNull(residentsvehicles_update.getColumnIndex(ResidentVehicleEntry.COLUMN_ACTIVE))) ? JSONObject.NULL : residentsvehicles_update.getString(residentsvehicles_update.getColumnIndex(ResidentVehicleEntry.COLUMN_ACTIVE)));


                        residentvehicleUpdateJson.put(ResidentVehicleEntry.COLUMN_RESIDENTVEHICLE_ID_SERVER,residentsvehicles_update.getLong(residentsvehicles_update.getColumnIndex(ResidentVehicleEntry.COLUMN_RESIDENTVEHICLE_ID_SERVER)));

                        residentsvehiclesArrayJson.put(residentvehicleUpdateJson);
                    }
                    residentsvehicles_update.close();
                }

                if ((residentsvehicles != null && residentsvehicles.getCount()>0) || (residentsvehicles_update != null && residentsvehicles_update.getCount()>0)){
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.accumulate("building_id",buildingId);
                    if (residentsvehiclesArrayJson.length() != 0){
                        jsonObject.accumulate("residentsvehicles",residentsvehiclesArrayJson);
                    }

                    if (BASE_URL.startsWith(HTTPS)){
                        urlConnectionHttps = (HttpsURLConnection) url.openConnection();
                        urlConnectionHttps.setRequestMethod("POST");
                        urlConnectionHttps.setRequestProperty("Content-Type","application/json");
                        urlConnectionHttps.setRequestProperty("api-key",API_KEY);
                        urlConnectionHttps.setDoOutput(true);
                        urlConnectionHttps.setDoInput(true);
                        urlConnectionHttps.setConnectTimeout(5000);
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
                        residentsvehiclesJsonStr = buffer.toString();
                        getResidentsVehiclesDataFromJson(residentsvehiclesJsonStr);
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
                        residentsvehiclesJsonStr = buffer.toString();
                        getResidentsVehiclesDataFromJson(residentsvehiclesJsonStr);
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

    private void getResidentsVehiclesDataFromJson(String residentsvehiclesJsonStr)
            throws JSONException {


        final String RESIDENTVEHICLE_RETURN = "residentvehicle_return";
        final String RESIDENTVEHICLE_UPDATE_RETURN = "residentvehicle_update_return";
        final String ID_RESIDENTVEHICLE = "id_residentvehicle";
        final String INSERT_DATA = "insert_data";
        final String ID_SERVER = "id_server";
        final String UPDATE_DATA = "update_data";

        try {
            JSONObject returnJson = new JSONObject(residentsvehiclesJsonStr);
            JSONArray residentsvehiclesReturn = (returnJson.isNull(RESIDENTVEHICLE_RETURN)) ? null : returnJson.getJSONArray(RESIDENTVEHICLE_RETURN);
            JSONArray residentsvehiclesUpdateReturn = (returnJson.isNull(RESIDENTVEHICLE_UPDATE_RETURN)) ? null : returnJson.getJSONArray(RESIDENTVEHICLE_UPDATE_RETURN);

            Vector<ContentValues> vectorResidentsVehiclesReturn;
            if (residentsvehiclesReturn != null){
                vectorResidentsVehiclesReturn = new Vector<ContentValues>(residentsvehiclesReturn.length());

                for(int i = 0; i < residentsvehiclesReturn.length(); i++) {

                    long id_residentvehicle;
                    boolean insert_data;
                    long id_server;

                    JSONObject returnResidentVehicleJson = residentsvehiclesReturn.getJSONObject(i);

                    id_residentvehicle = returnResidentVehicleJson.getLong(ID_RESIDENTVEHICLE);
                    insert_data = returnResidentVehicleJson.getBoolean(INSERT_DATA);

                    ContentValues parcelsValues = new ContentValues();
                    if (insert_data){
                        parcelsValues.put(ResidentVehicleEntry.COLUMN_IS_SYNC, IS_SYNC);
                        id_server = returnResidentVehicleJson.getLong(ID_SERVER);
                        parcelsValues.put(ResidentVehicleEntry.COLUMN_RESIDENTVEHICLE_ID_SERVER,id_server);
                    }
                    else{
                        parcelsValues.put(ResidentVehicleEntry.COLUMN_IS_SYNC, NOT_SYNC);
                    }
                    parcelsValues.put(ResidentVehicleEntry._ID, id_residentvehicle);

                    vectorResidentsVehiclesReturn.add(parcelsValues);
                }
            }else{
                vectorResidentsVehiclesReturn = null;
            }

            Vector<ContentValues> vectorResidentsVehiclesUpdateReturn;
            if (residentsvehiclesUpdateReturn != null){
                vectorResidentsVehiclesUpdateReturn = new Vector<ContentValues>(residentsvehiclesUpdateReturn.length());

                for(int i = 0; i < residentsvehiclesUpdateReturn.length(); i++) {

                    long id_residentvehicle;
                    boolean update_data;

                    JSONObject returnResidentVehicleUpdateJson = residentsvehiclesUpdateReturn.getJSONObject(i);

                    id_residentvehicle = returnResidentVehicleUpdateJson.getLong(ID_RESIDENTVEHICLE);
                    update_data = returnResidentVehicleUpdateJson.getBoolean(UPDATE_DATA);

                    ContentValues residentsvehiclesUpdateValues = new ContentValues();
                    if (update_data){
                        residentsvehiclesUpdateValues.put(ResidentVehicleEntry.COLUMN_IS_UPDATE, NOT_UPDATE);
                    }
                    else{
                        residentsvehiclesUpdateValues.put(ResidentVehicleEntry.COLUMN_IS_UPDATE, IS_UPDATE);
                    }
                    residentsvehiclesUpdateValues.put(ResidentVehicleEntry._ID, id_residentvehicle);

                    vectorResidentsVehiclesUpdateReturn.add(residentsvehiclesUpdateValues);
                }
            }
            else{
                vectorResidentsVehiclesUpdateReturn = null;
            }

            UpdateSyncResidentsVehicles.run(vectorResidentsVehiclesReturn,vectorResidentsVehiclesUpdateReturn);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

}
