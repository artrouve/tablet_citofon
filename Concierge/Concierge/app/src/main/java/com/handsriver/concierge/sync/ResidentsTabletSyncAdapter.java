package com.handsriver.concierge.sync;

import android.accounts.Account;
import android.annotation.SuppressLint;
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
import com.handsriver.concierge.database.updatesTables.UpdateSyncParcels;
import com.handsriver.concierge.database.updatesTables.UpdateSyncResidents;


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

public class ResidentsTabletSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = ResidentsTabletSyncAdapter.class.getSimpleName();
    public static final String RESIDENTS = "residents_store";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    private Context mContext;
    ConciergeDbHelper dbHelper;
    SQLiteDatabase db;
    public static final String NOT_SYNC = "0";
    public static final String IS_SYNC = "1";
    public static final String NOT_UPDATE = "0";
    public static final String IS_UPDATE = "1";
    public static final String IS_DELETE = "1";


    public ResidentsTabletSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        this.mContext= context;
    }

    @SuppressLint("Range")
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");

        HttpURLConnection urlConnection = null;
        HttpsURLConnection urlConnectionHttps = null;

        BufferedReader reader = null;
        dbHelper = new ConciergeDbHelper(mContext);
        db = DatabaseManager.getInstance().openDatabase();
        String residentsJsonStr;

        final String ID_RESIDENT = "id_resident";
        final String IS_UPDATE_COL = "is_update";
        final String IS_DELETE_COL = "is_deleted";

        try {
            SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            final String BASE_URL = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_URL_key),"");
            final int buildingId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_building_key),"0"));
            final String API_KEY = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_apikey_key),"");

            if (BASE_URL.length()>0 && (BASE_URL.startsWith(HTTP) || BASE_URL.startsWith(HTTPS))){
                Uri buildUri = Uri.parse(BASE_URL).buildUpon().appendPath(RESIDENTS).build();
                URL url = new URL(buildUri.toString());

                JSONArray residentsArrayJson = new JSONArray();

                String[] projection = {
                        ResidentEntry._ID,
                        ResidentEntry.COLUMN_APARTMENT_ID,
                        ResidentEntry.COLUMN_FULL_NAME,
                        ResidentEntry.COLUMN_EMAIL,
                        ResidentEntry.COLUMN_MOBILE,
                        ResidentEntry.COLUMN_PHONE,
                        ResidentEntry.COLUMN_RUT,


                };

                String selection = ResidentEntry.COLUMN_IS_SYNC + " = ?";
                String [] selectionArgs = {NOT_SYNC};

                Cursor residents = db.query(ResidentEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,null,null);

                if (residents != null && residents.getCount()>0){
                    while (residents.moveToNext()){
                        JSONObject residentJson = new JSONObject();
                        residentJson.put(ID_RESIDENT,residents.getLong(residents.getColumnIndex(ResidentEntry._ID)));
                        residentJson.put(IS_UPDATE_COL,false);
                        residentJson.put(ResidentEntry.COLUMN_FULL_NAME,residents.getString(residents.getColumnIndex(ResidentEntry.COLUMN_FULL_NAME)));
                        residentJson.put(ResidentEntry.COLUMN_EMAIL,(residents.isNull(residents.getColumnIndex(ResidentEntry.COLUMN_EMAIL))) ? JSONObject.NULL : residents.getString(residents.getColumnIndex(ResidentEntry.COLUMN_EMAIL)));

                        residentJson.put(ResidentEntry.COLUMN_MOBILE,(residents.isNull(residents.getColumnIndex(ResidentEntry.COLUMN_EMAIL))) ? JSONObject.NULL : residents.getString(residents.getColumnIndex(ResidentEntry.COLUMN_MOBILE)));
                        residentJson.put(ResidentEntry.COLUMN_PHONE,(residents.isNull(residents.getColumnIndex(ResidentEntry.COLUMN_EMAIL))) ? JSONObject.NULL : residents.getString(residents.getColumnIndex(ResidentEntry.COLUMN_PHONE)));
                        residentJson.put(ResidentEntry.COLUMN_RUT,(residents.isNull(residents.getColumnIndex(ResidentEntry.COLUMN_EMAIL))) ? JSONObject.NULL : residents.getString(residents.getColumnIndex(ResidentEntry.COLUMN_RUT)));

                        residentJson.put(ResidentEntry.COLUMN_APARTMENT_ID,residents.getLong(residents.getColumnIndex(ResidentEntry.COLUMN_APARTMENT_ID)));

                        residentsArrayJson.put(residentJson);
                    }
                    residents.close();
                }

                String[] projection_u = {
                        ResidentEntry._ID,
                        ResidentEntry.COLUMN_EMAIL,
                        ResidentEntry.COLUMN_MOBILE,
                        ResidentEntry.COLUMN_PHONE,
                        ResidentEntry.COLUMN_RUT,
                        ResidentEntry.COLUMN_RESIDENT_ID_SERVER
                };


                String selection_u = ResidentEntry.COLUMN_IS_SYNC + " = ? AND " + ResidentEntry.COLUMN_IS_UPDATE + " = ?";
                String [] selectionArgs_u = {IS_SYNC,IS_UPDATE};

                Cursor residents_update = db.query(ResidentEntry.TABLE_NAME,projection_u,selection_u,selectionArgs_u,null,null,null,null);

                if (residents_update != null && residents_update.getCount()>0){
                    while (residents_update.moveToNext()){
                        JSONObject residentUpdateJson = new JSONObject();
                        residentUpdateJson.put(ID_RESIDENT,residents_update.getLong(residents_update.getColumnIndex(ResidentEntry._ID)));
                        residentUpdateJson.put(IS_UPDATE_COL,true);
                        residentUpdateJson.put(IS_DELETE_COL,false);
                        residentUpdateJson.put(ResidentEntry.COLUMN_EMAIL,(residents_update.isNull(residents_update.getColumnIndex(ResidentEntry.COLUMN_EMAIL))) ? JSONObject.NULL : residents_update.getString(residents_update.getColumnIndex(ResidentEntry.COLUMN_EMAIL)));

                        residentUpdateJson.put(ResidentEntry.COLUMN_MOBILE,(residents_update.isNull(residents_update.getColumnIndex(ResidentEntry.COLUMN_EMAIL))) ? JSONObject.NULL : residents_update.getString(residents_update.getColumnIndex(ResidentEntry.COLUMN_MOBILE)));
                        residentUpdateJson.put(ResidentEntry.COLUMN_PHONE,(residents_update.isNull(residents_update.getColumnIndex(ResidentEntry.COLUMN_EMAIL))) ? JSONObject.NULL : residents_update.getString(residents_update.getColumnIndex(ResidentEntry.COLUMN_PHONE)));
                        residentUpdateJson.put(ResidentEntry.COLUMN_RUT,(residents_update.isNull(residents_update.getColumnIndex(ResidentEntry.COLUMN_EMAIL))) ? JSONObject.NULL : residents_update.getString(residents_update.getColumnIndex(ResidentEntry.COLUMN_RUT)));

                        residentUpdateJson.put(ResidentEntry.COLUMN_RESIDENT_ID_SERVER,residents_update.getLong(residents_update.getColumnIndex(ResidentEntry.COLUMN_RESIDENT_ID_SERVER)));

                        residentsArrayJson.put(residentUpdateJson);
                    }
                    residents_update.close();
                }

                String[] projection_d = {
                        ResidentEntry._ID,
                        ResidentEntry.COLUMN_RESIDENT_ID_SERVER,
                };


                String selection_d = ResidentEntry.COLUMN_IS_DELETED + " = ? ";
                String [] selectionArgs_d = {IS_DELETE};

                Cursor residents_delete = db.query(ResidentEntry.TABLE_NAME,projection_d,selection_d,selectionArgs_d,null,null,null,null);

                if (residents_delete != null && residents_delete.getCount()>0){
                    while (residents_delete.moveToNext()){
                        JSONObject residentDeleteJson = new JSONObject();
                        residentDeleteJson.put(ID_RESIDENT,residents_delete.getLong(residents_delete.getColumnIndex(ResidentEntry._ID)));
                        residentDeleteJson.put(IS_UPDATE_COL,true);
                        residentDeleteJson.put(IS_DELETE_COL,true);
                        residentDeleteJson.put(ResidentEntry.COLUMN_RESIDENT_ID_SERVER,residents_delete.getLong(residents_delete.getColumnIndex(ResidentEntry.COLUMN_RESIDENT_ID_SERVER)));
                        residentsArrayJson.put(residentDeleteJson);
                    }
                    residents_delete.close();
                }


                if ((residents != null && residents.getCount()>0) || (residents_update != null && residents_update.getCount()>0)  || (residents_delete != null && residents_delete.getCount()>0)){
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.accumulate("building_id",buildingId);
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


        final String RESIDENT_RETURN = "resident_return";
        final String RESIDENT_UPDATE_RETURN = "resident_update_return";
        final String RESIDENT_DELETE_RETURN = "resident_delete_return";
        final String ID_RESIDENT = "id_resident";
        final String INSERT_DATA = "insert_data";
        final String ID_SERVER = "id_server";
        final String UPDATE_DATA = "update_data";
        final String DELETE_DATA = "delete_data";

        try {
            JSONObject returnJson = new JSONObject(residentsJsonStr);
            JSONArray residentsReturn = (returnJson.isNull(RESIDENT_RETURN)) ? null : returnJson.getJSONArray(RESIDENT_RETURN);
            JSONArray residentsUpdateReturn = (returnJson.isNull(RESIDENT_UPDATE_RETURN)) ? null : returnJson.getJSONArray(RESIDENT_UPDATE_RETURN);
            JSONArray residentsDeleteReturn = (returnJson.isNull(RESIDENT_DELETE_RETURN)) ? null : returnJson.getJSONArray(RESIDENT_DELETE_RETURN);

            Vector<ContentValues> vectorResidentsReturn;
            if (residentsReturn != null){
                vectorResidentsReturn = new Vector<ContentValues>(residentsReturn.length());

                for(int i = 0; i < residentsReturn.length(); i++) {

                    long id_resident;
                    boolean insert_data;
                    long id_server;

                    JSONObject returnResidentJson = residentsReturn.getJSONObject(i);

                    id_resident = returnResidentJson.getLong(ID_RESIDENT);
                    insert_data = returnResidentJson.getBoolean(INSERT_DATA);

                    ContentValues parcelsValues = new ContentValues();
                    if (insert_data){
                        parcelsValues.put(ResidentEntry.COLUMN_IS_SYNC, IS_SYNC);
                        id_server = returnResidentJson.getLong(ID_SERVER);
                        parcelsValues.put(ResidentEntry.COLUMN_RESIDENT_ID_SERVER,id_server);
                    }
                    else{
                        parcelsValues.put(ResidentEntry.COLUMN_IS_SYNC, NOT_SYNC);
                    }
                    parcelsValues.put(ResidentEntry._ID, id_resident);

                    vectorResidentsReturn.add(parcelsValues);
                }
            }else{
                vectorResidentsReturn = null;
            }

            Vector<ContentValues> vectorResidentsUpdateReturn;
            if (residentsUpdateReturn != null){
                vectorResidentsUpdateReturn = new Vector<ContentValues>(residentsUpdateReturn.length());

                for(int i = 0; i < residentsUpdateReturn.length(); i++) {

                    long id_resident;
                    boolean update_data;

                    JSONObject returnResidentUpdateJson = residentsUpdateReturn.getJSONObject(i);

                    id_resident = returnResidentUpdateJson.getLong(ID_RESIDENT);
                    update_data = returnResidentUpdateJson.getBoolean(UPDATE_DATA);

                    ContentValues residentsUpdateValues = new ContentValues();
                    if (update_data){
                        residentsUpdateValues.put(ResidentEntry.COLUMN_IS_UPDATE, NOT_UPDATE);
                    }
                    else{
                        residentsUpdateValues.put(ResidentEntry.COLUMN_IS_UPDATE, IS_UPDATE);
                    }
                    residentsUpdateValues.put(ResidentEntry._ID, id_resident);

                    vectorResidentsUpdateReturn.add(residentsUpdateValues);
                }
            }
            else{
                vectorResidentsUpdateReturn = null;
            }

            Vector<ContentValues> vectorResidentsDeleteReturn;
            if (residentsDeleteReturn != null){
                vectorResidentsDeleteReturn = new Vector<ContentValues>(residentsDeleteReturn.length());

                for(int i = 0; i < residentsDeleteReturn.length(); i++) {

                    long id_resident;
                    boolean delete_data;

                    JSONObject returnResidentDeleteJson = residentsDeleteReturn.getJSONObject(i);

                    id_resident = returnResidentDeleteJson.getLong(ID_RESIDENT);
                    delete_data = returnResidentDeleteJson.getBoolean(DELETE_DATA);

                    ContentValues residentsDeleteValues = new ContentValues();
                    if (delete_data){
                        residentsDeleteValues.put(ResidentEntry.COLUMN_IS_UPDATE, NOT_UPDATE);
                    }
                    else{
                        residentsDeleteValues.put(ResidentEntry.COLUMN_IS_UPDATE, IS_UPDATE);
                    }
                    residentsDeleteValues.put(ResidentEntry._ID, id_resident);

                    vectorResidentsDeleteReturn.add(residentsDeleteValues);
                }
            }
            else{
                vectorResidentsDeleteReturn = null;
            }



            UpdateSyncResidents.run(vectorResidentsReturn,vectorResidentsUpdateReturn,vectorResidentsDeleteReturn);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

}
