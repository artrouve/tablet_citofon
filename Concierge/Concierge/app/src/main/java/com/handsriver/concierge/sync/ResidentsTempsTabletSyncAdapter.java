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
import com.handsriver.concierge.database.ConciergeContract.ResidentTempEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.updatesTables.UpdateSyncResidentsTemps;

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

public class ResidentsTempsTabletSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = ResidentsTempsTabletSyncAdapter.class.getSimpleName();
    public static final String RESIDENTSTEMPS = "residentstemps_store";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    private Context mContext;
    ConciergeDbHelper dbHelper;
    SQLiteDatabase db;
    public static final String NOT_SYNC = "0";
    public static final String IS_SYNC = "1";
    public static final String NOT_UPDATE = "0";
    public static final String IS_UPDATE = "1";



    public ResidentsTempsTabletSyncAdapter(Context context, boolean autoInitialize) {
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
        String residentstempsJsonStr;

        final String ID_RESIDENT_TEMP = "id_residenttemp";
        final String IS_UPDATE_COL = "is_update";


        try {
            SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            final String BASE_URL = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_URL_key),"");
            final int buildingId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_building_key),"0"));
            final String API_KEY = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_apikey_key),"");

            if (BASE_URL.length()>0 && (BASE_URL.startsWith(HTTP) || BASE_URL.startsWith(HTTPS))){
                Uri buildUri = Uri.parse(BASE_URL).buildUpon().appendPath(RESIDENTSTEMPS).build();
                URL url = new URL(buildUri.toString());

                JSONArray residentstempsArrayJson = new JSONArray();

                String[] projection = {
                        ResidentTempEntry._ID,
                        ResidentTempEntry.COLUMN_APARTMENT_ID,
                        ResidentTempEntry.COLUMN_FULL_NAME,
                        ResidentTempEntry.COLUMN_EMAIL,
                        ResidentTempEntry.COLUMN_PHONE,
                        ResidentTempEntry.COLUMN_RUT,
                        ResidentTempEntry.COLUMN_START_DATE,
                        ResidentTempEntry.COLUMN_END_DATE,


                };

                String selection = ResidentTempEntry.COLUMN_IS_SYNC + " = ?";
                String [] selectionArgs = {NOT_SYNC};

                Cursor residentstemps = db.query(ResidentTempEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,null,null);

                if (residentstemps != null && residentstemps.getCount()>0){
                    while (residentstemps.moveToNext()){
                        JSONObject residenttempJson = new JSONObject();
                        residenttempJson.put(ID_RESIDENT_TEMP,residentstemps.getLong(residentstemps.getColumnIndex(ResidentTempEntry._ID)));
                        residenttempJson.put(IS_UPDATE_COL,false);

                        residenttempJson.put(ResidentTempEntry.COLUMN_FULL_NAME,residentstemps.getString(residentstemps.getColumnIndex(ResidentTempEntry.COLUMN_FULL_NAME)));
                        residenttempJson.put(ResidentTempEntry.COLUMN_EMAIL,(residentstemps.isNull(residentstemps.getColumnIndex(ResidentTempEntry.COLUMN_EMAIL))) ? JSONObject.NULL : residentstemps.getString(residentstemps.getColumnIndex(ResidentTempEntry.COLUMN_EMAIL)));

                        residenttempJson.put(ResidentTempEntry.COLUMN_PHONE,(residentstemps.isNull(residentstemps.getColumnIndex(ResidentTempEntry.COLUMN_EMAIL))) ? JSONObject.NULL : residentstemps.getString(residentstemps.getColumnIndex(ResidentTempEntry.COLUMN_PHONE)));
                        residenttempJson.put(ResidentTempEntry.COLUMN_RUT,(residentstemps.isNull(residentstemps.getColumnIndex(ResidentTempEntry.COLUMN_EMAIL))) ? JSONObject.NULL : residentstemps.getString(residentstemps.getColumnIndex(ResidentTempEntry.COLUMN_RUT)));

                        residenttempJson.put(ResidentTempEntry.COLUMN_START_DATE,(residentstemps.isNull(residentstemps.getColumnIndex(ResidentTempEntry.COLUMN_START_DATE))) ? JSONObject.NULL : residentstemps.getString(residentstemps.getColumnIndex(ResidentTempEntry.COLUMN_START_DATE)));
                        residenttempJson.put(ResidentTempEntry.COLUMN_END_DATE,(residentstemps.isNull(residentstemps.getColumnIndex(ResidentTempEntry.COLUMN_END_DATE))) ? JSONObject.NULL : residentstemps.getString(residentstemps.getColumnIndex(ResidentTempEntry.COLUMN_END_DATE)));

                        residenttempJson.put(ResidentTempEntry.COLUMN_APARTMENT_ID,residentstemps.getLong(residentstemps.getColumnIndex(ResidentTempEntry.COLUMN_APARTMENT_ID)));

                        residentstempsArrayJson.put(residenttempJson);
                    }
                    residentstemps.close();
                }

                String[] projection_u = {
                        ResidentTempEntry._ID,
                        ResidentTempEntry.COLUMN_FULL_NAME,
                        ResidentTempEntry.COLUMN_EMAIL,
                        ResidentTempEntry.COLUMN_PHONE,
                        ResidentTempEntry.COLUMN_RUT,
                        ResidentTempEntry.COLUMN_START_DATE,
                        ResidentTempEntry.COLUMN_END_DATE,
                        ResidentTempEntry.COLUMN_RESIDENTTEMP_ID_SERVER
                };


                String selection_u = ResidentTempEntry.COLUMN_IS_SYNC + " = ? AND " + ResidentTempEntry.COLUMN_IS_UPDATE + " = ?";
                String [] selectionArgs_u = {IS_SYNC,IS_UPDATE};

                Cursor residentstemps_update = db.query(ResidentTempEntry.TABLE_NAME,projection_u,selection_u,selectionArgs_u,null,null,null,null);

                if (residentstemps_update != null && residentstemps_update.getCount()>0){
                    while (residentstemps_update.moveToNext()){
                        JSONObject residenttempUpdateJson = new JSONObject();
                        residenttempUpdateJson.put(ID_RESIDENT_TEMP,residentstemps_update.getLong(residentstemps_update.getColumnIndex(ResidentTempEntry._ID)));
                        residenttempUpdateJson.put(IS_UPDATE_COL,true);

                        residenttempUpdateJson.put(ResidentTempEntry.COLUMN_FULL_NAME,(residentstemps_update.isNull(residentstemps_update.getColumnIndex(ResidentTempEntry.COLUMN_FULL_NAME))) ? JSONObject.NULL : residentstemps_update.getString(residentstemps_update.getColumnIndex(ResidentTempEntry.COLUMN_FULL_NAME)));
                        residenttempUpdateJson.put(ResidentTempEntry.COLUMN_EMAIL,(residentstemps_update.isNull(residentstemps_update.getColumnIndex(ResidentTempEntry.COLUMN_EMAIL))) ? JSONObject.NULL : residentstemps_update.getString(residentstemps_update.getColumnIndex(ResidentTempEntry.COLUMN_EMAIL)));

                        residenttempUpdateJson.put(ResidentTempEntry.COLUMN_PHONE,(residentstemps_update.isNull(residentstemps_update.getColumnIndex(ResidentTempEntry.COLUMN_PHONE))) ? JSONObject.NULL : residentstemps_update.getString(residentstemps_update.getColumnIndex(ResidentTempEntry.COLUMN_PHONE)));
                        residenttempUpdateJson.put(ResidentTempEntry.COLUMN_RUT,(residentstemps_update.isNull(residentstemps_update.getColumnIndex(ResidentTempEntry.COLUMN_RUT))) ? JSONObject.NULL : residentstemps_update.getString(residentstemps_update.getColumnIndex(ResidentTempEntry.COLUMN_RUT)));

                        residenttempUpdateJson.put(ResidentTempEntry.COLUMN_START_DATE,(residentstemps_update.isNull(residentstemps_update.getColumnIndex(ResidentTempEntry.COLUMN_START_DATE))) ? JSONObject.NULL : residentstemps_update.getString(residentstemps_update.getColumnIndex(ResidentTempEntry.COLUMN_START_DATE)));
                        residenttempUpdateJson.put(ResidentTempEntry.COLUMN_END_DATE,(residentstemps_update.isNull(residentstemps_update.getColumnIndex(ResidentTempEntry.COLUMN_END_DATE))) ? JSONObject.NULL : residentstemps_update.getString(residentstemps_update.getColumnIndex(ResidentTempEntry.COLUMN_END_DATE)));

                        residenttempUpdateJson.put(ResidentTempEntry.COLUMN_RESIDENTTEMP_ID_SERVER,residentstemps_update.getLong(residentstemps_update.getColumnIndex(ResidentTempEntry.COLUMN_RESIDENTTEMP_ID_SERVER)));

                        residentstempsArrayJson.put(residenttempUpdateJson);
                    }
                    residentstemps_update.close();
                }


                if ((residentstemps != null && residentstemps.getCount()>0) || (residentstemps_update != null && residentstemps_update.getCount()>0)){
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.accumulate("building_id",buildingId);
                    if (residentstempsArrayJson.length() != 0){
                        jsonObject.accumulate("residents",residentstempsArrayJson);
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
                        residentstempsJsonStr = buffer.toString();
                        getResidentsTempsDataFromJson(residentstempsJsonStr);
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
                        residentstempsJsonStr = buffer.toString();
                        getResidentsTempsDataFromJson(residentstempsJsonStr);
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

    private void getResidentsTempsDataFromJson(String residentstempsJsonStr)
            throws JSONException {


        final String RESIDENTTEMP_RETURN = "residenttemps_return";
        final String RESIDENTTEMP_UPDATE_RETURN = "residenttemp_update_return";
        final String ID_RESIDENTTEMP = "id_resident_temp";
        final String INSERT_DATA = "insert_data";
        final String ID_SERVER = "id_server";
        final String UPDATE_DATA = "update_data";

        try {
            JSONObject returnJson = new JSONObject(residentstempsJsonStr);
            JSONArray residentstempsReturn = (returnJson.isNull(RESIDENTTEMP_RETURN)) ? null : returnJson.getJSONArray(RESIDENTTEMP_RETURN);
            JSONArray residentstempsUpdateReturn = (returnJson.isNull(RESIDENTTEMP_UPDATE_RETURN)) ? null : returnJson.getJSONArray(RESIDENTTEMP_UPDATE_RETURN);

            Vector<ContentValues> vectorResidentsTempsReturn;
            if (residentstempsReturn != null){
                vectorResidentsTempsReturn = new Vector<ContentValues>(residentstempsReturn.length());

                for(int i = 0; i < residentstempsReturn.length(); i++) {

                    long id_resident_temp;
                    boolean insert_data;
                    long id_server;

                    JSONObject returnResidentTempJson = residentstempsReturn.getJSONObject(i);

                    id_resident_temp = returnResidentTempJson.getLong(ID_RESIDENTTEMP);
                    insert_data = returnResidentTempJson.getBoolean(INSERT_DATA);

                    ContentValues residentstempsValues = new ContentValues();
                    if (insert_data){
                        residentstempsValues.put(ResidentTempEntry.COLUMN_IS_SYNC, IS_SYNC);
                        id_server = returnResidentTempJson.getLong(ID_SERVER);
                        residentstempsValues.put(ResidentTempEntry.COLUMN_RESIDENTTEMP_ID_SERVER,id_server);
                    }
                    else{
                        residentstempsValues.put(ResidentTempEntry.COLUMN_IS_SYNC, NOT_SYNC);
                    }
                    residentstempsValues.put(ResidentTempEntry._ID, id_resident_temp);

                    vectorResidentsTempsReturn.add(residentstempsValues);
                }
            }else{
                vectorResidentsTempsReturn = null;
            }

            Vector<ContentValues> vectorResidentsTempsUpdateReturn;
            if (residentstempsUpdateReturn != null){
                vectorResidentsTempsUpdateReturn = new Vector<ContentValues>(residentstempsUpdateReturn.length());

                for(int i = 0; i < residentstempsUpdateReturn.length(); i++) {

                    long id_resident_temp;
                    boolean update_data;

                    JSONObject returnResidentTempUpdateJson = residentstempsUpdateReturn.getJSONObject(i);

                    id_resident_temp = returnResidentTempUpdateJson.getLong(ID_RESIDENTTEMP);
                    update_data = returnResidentTempUpdateJson.getBoolean(UPDATE_DATA);

                    ContentValues residentstempsUpdateValues = new ContentValues();
                    if (update_data){
                        residentstempsUpdateValues.put(ResidentTempEntry.COLUMN_IS_UPDATE, NOT_UPDATE);
                    }
                    else{
                        residentstempsUpdateValues.put(ResidentTempEntry.COLUMN_IS_UPDATE, IS_UPDATE);
                    }
                    residentstempsUpdateValues.put(ResidentTempEntry._ID, id_resident_temp);

                    vectorResidentsTempsUpdateReturn.add(residentstempsUpdateValues);
                }
            }
            else{
                vectorResidentsTempsUpdateReturn = null;
            }

            UpdateSyncResidentsTemps.run(vectorResidentsTempsReturn,vectorResidentsTempsUpdateReturn);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

}
