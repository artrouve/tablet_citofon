package com.handsriver.concierge.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeContract.ResidentVehicleEntry;
import com.handsriver.concierge.database.InsertUpdateTables.IUResidentsVehicles;
import com.handsriver.concierge.isapi.alpr.ResidentsVehiclesSyncIsapi;

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

public class ResidentsVehiclesSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = ResidentsVehiclesSyncAdapter.class.getSimpleName();
    public static final String RESIDENTSVEHICLES = "residentsvehicles";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    private Context mContext;

    public ResidentsVehiclesSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        this.mContext= context;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");

        HttpURLConnection urlConnection = null;
        HttpsURLConnection urlConnectionHttps = null;

        BufferedReader reader = null;

        String residentsvehiclesJsonStr;

        try {
            SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            final String BASE_URL = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_URL_key),"");
            final int buildingId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_building_key),"0"));
            final String API_KEY = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_apikey_key),"");

            if (BASE_URL.length()>0 && (BASE_URL.startsWith(HTTP) || BASE_URL.startsWith(HTTPS))){
                Uri buildUri = Uri.parse(BASE_URL).buildUpon().appendPath(RESIDENTSVEHICLES).build();
                URL url = new URL(buildUri.toString());

                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("building_id",buildingId);


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

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        } finally {
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


        //SE VERIFICA SI EL SISTEMA CUENTA CON DETECCION DE PATENTES.
        SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean isAutomaticPlateDetection = settingsPrefs.getBoolean(mContext.getResources().getString(R.string.pref_automatic_plate_detection_key), false);
        if(isAutomaticPlateDetection){
            ResidentsVehiclesSyncIsapi syncIsapiPlates = new ResidentsVehiclesSyncIsapi(mContext);
            syncIsapiPlates.sync();
        }

    }

    private void getResidentsVehiclesDataFromJson(String residentvehicleJsonStr)
            throws JSONException {

        final String PLATE = "plate";
        final String ID_RESIDENTVEHICLE = "id_residentvehicle";
        final String APARTMENT_ID = "apartment_id";
        final String ACTIVE = "active";


        try {
            JSONArray residentsvehiclesArray = new JSONArray(residentvehicleJsonStr);

            Vector<ContentValues> cVVector = new Vector<ContentValues>(residentsvehiclesArray.length());

            for(int i = 0; i < residentsvehiclesArray.length(); i++) {

                long apartment_id;
                long id_residentvehicle;
                String plate;
                String active;


                JSONObject residentvehicleJson = residentsvehiclesArray.getJSONObject(i);

                apartment_id = residentvehicleJson.getLong(APARTMENT_ID);
                id_residentvehicle = residentvehicleJson.getLong(ID_RESIDENTVEHICLE);
                plate = residentvehicleJson.getString(PLATE);
                active = (residentvehicleJson.isNull(ACTIVE)) ? null : residentvehicleJson.getString(ACTIVE);

                ContentValues residentvehicleValues = new ContentValues();

                residentvehicleValues.put(ResidentVehicleEntry.COLUMN_PLATE, plate);
                residentvehicleValues.put(ResidentVehicleEntry.COLUMN_ACTIVE, active);


                residentvehicleValues.put(ResidentVehicleEntry.COLUMN_APARTMENT_ID, apartment_id);
                residentvehicleValues.put(ResidentVehicleEntry.COLUMN_RESIDENTVEHICLE_ID_SERVER, id_residentvehicle);


                cVVector.add(residentvehicleValues);
            }

            IUResidentsVehicles.run(cVVector);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

}
