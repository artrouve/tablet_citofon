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
import com.handsriver.concierge.database.ConciergeContract.ResidentEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.updatesTables.UpdateSyncCodeAuth;
import com.handsriver.concierge.database.updatesTables.UpdateSyncParcels;

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

public class CodeAuthSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = CodeAuthSyncAdapter.class.getSimpleName();
    public static final String CODE_AUTH = "code_request";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    private Context mContext;
    ConciergeDbHelper dbHelper;
    SQLiteDatabase db;
    public static final String NOT_SYNC = "0";
    public static final String IS_SYNC = "1";
    public static final String NOT_UPDATE = "0";
    public static final String IS_UPDATE = "1";
    public static final String IS_REQUEST_CODE = "1";
    public static final String NOT_REQUEST_CODE = "0";

    public CodeAuthSyncAdapter(Context context, boolean autoInitialize) {
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
        String residentsJsonStr;

        final String ID_PARCEL = "id_resident";
        final String EXIT_FULL_NAME = "exit_full_name";
        final String IS_UPDATE = "is_update";


        try {
            SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            final String BASE_URL = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_URL_key),"");
            final int gatewayId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_gateway_key),"0"));
            final int buildingId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_building_key),"0"));
            final String API_KEY = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_apikey_key),"");

            if (BASE_URL.length()>0 && (BASE_URL.startsWith(HTTP) || BASE_URL.startsWith(HTTPS))){
                Uri buildUri = Uri.parse(BASE_URL).buildUpon().appendPath(CODE_AUTH).build();
                URL url = new URL(buildUri.toString());

                JSONArray residentsArrayJson = new JSONArray();

                String[] projection = {
                        ResidentEntry._ID,
                        ResidentEntry.COLUMN_RESIDENT_ID_SERVER

                };

                String selection = ResidentEntry.COLUMN_REQUEST_CODE + " = ? AND " + ResidentEntry.COLUMN_IS_UPDATE + " = ? AND " + ResidentEntry.COLUMN_IS_SYNC + " = ?";
                String [] selectionArgs = {IS_REQUEST_CODE,NOT_UPDATE,IS_SYNC};

                Cursor residents = db.query(ResidentEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,null,null);

                if (residents != null && residents.getCount()>0){
                    while (residents.moveToNext()){
                        JSONObject residentJson = new JSONObject();
                        residentJson.put(ResidentEntry.COLUMN_RESIDENT_ID_SERVER,residents.getLong(residents.getColumnIndex(ResidentEntry.COLUMN_RESIDENT_ID_SERVER)));
                        residentsArrayJson.put(residentJson);
                    }
                    residents.close();
                }

                if ((residents != null && residents.getCount()>0)){
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.accumulate("building_id",buildingId);
                    jsonObject.accumulate("gateway_id",gatewayId);
                    if (residentsArrayJson.length() != 0){
                        jsonObject.accumulate("residents",residentsArrayJson);
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
                        residentsJsonStr = buffer.toString();
                        getResidentsDataFromJson(residentsJsonStr);
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
                        residentsJsonStr = buffer.toString();
                        getResidentsDataFromJson(residentsJsonStr);
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

    private void getResidentsDataFromJson(String residentsJsonStr)
            throws JSONException {


        final String CODE_REQUEST_RETURN = "code_request_return";
        final String RESIDENT_ID_SERVER = "resident_id_server";
        final String CODE_GENERATE = "code_generate";
        try {
            JSONObject returnJson = new JSONObject(residentsJsonStr);
            JSONArray residentReturn = returnJson.getJSONArray(CODE_REQUEST_RETURN);

            Vector<ContentValues> vectorResidentsReturn;
            if (residentReturn != null){
                vectorResidentsReturn = new Vector<ContentValues>(residentReturn.length());

                for(int i = 0; i < residentReturn.length(); i++) {

                    long resident_id_server;
                    boolean code_generate;

                    JSONObject returnResidentJson = residentReturn.getJSONObject(i);

                    resident_id_server = returnResidentJson.getLong(RESIDENT_ID_SERVER);
                    code_generate = returnResidentJson.getBoolean(CODE_GENERATE);

                    ContentValues residentsValues = new ContentValues();
                    if (code_generate){
                        residentsValues.put(ResidentEntry.COLUMN_REQUEST_CODE, NOT_REQUEST_CODE);
                    }
                    else{
                        residentsValues.put(ResidentEntry.COLUMN_REQUEST_CODE, IS_REQUEST_CODE);
                    }

                    residentsValues.put(ResidentEntry.COLUMN_RESIDENT_ID_SERVER, resident_id_server);

                    vectorResidentsReturn.add(residentsValues);
                }
            }else{
                vectorResidentsReturn = null;
            }

            UpdateSyncCodeAuth.run(vectorResidentsReturn);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

}
