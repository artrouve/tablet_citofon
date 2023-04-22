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
import com.handsriver.concierge.database.ConciergeContract.ParcelEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
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

public class ParcelsSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = ParcelsSyncAdapter.class.getSimpleName();
    public static final String PARCELS = "parcels";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    private Context mContext;
    ConciergeDbHelper dbHelper;
    SQLiteDatabase db;
    public static final String NOT_SYNC = "0";
    public static final String IS_SYNC = "1";
    public static final String NOT_UPDATE = "0";
    public static final String IS_UPDATE = "1";



    public ParcelsSyncAdapter(Context context, boolean autoInitialize) {
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
        String parcelsJsonStr;

        final String ID_PARCEL = "id_parcel";
        final String EXIT_FULL_NAME = "exit_full_name";
        final String IS_UPDATE = "is_update";
        final String UNIQUE_ID = "unique_id";



        try {
            SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            final String BASE_URL = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_URL_key),"");
            final int gatewayId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_gateway_key),"0"));
            final int buildingId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_building_key),"0"));
            final String API_KEY = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_apikey_key),"");

            if (BASE_URL.length()>0 && (BASE_URL.startsWith(HTTP) || BASE_URL.startsWith(HTTPS))){
                Uri buildUri = Uri.parse(BASE_URL).buildUpon().appendPath(PARCELS).build();
                URL url = new URL(buildUri.toString());

                JSONArray parcelsArrayJson = new JSONArray();

                String[] projection = {
                        ParcelEntry._ID,
                        ParcelEntry.COLUMN_APARTMENT_ID,
                        ParcelEntry.COLUMN_OBSERVATIONS,
                        ParcelEntry.COLUMN_PARCELTYPE_ID,
                        ParcelEntry.COLUMN_FULL_NAME,
                        ParcelEntry.COLUMN_ENTRY_PARCEL,
                        ParcelEntry.COLUMN_ENTRY_PARCEL_PORTER_ID,
                        ParcelEntry.COLUMN_EXIT_PARCEL,
                        ParcelEntry.COLUMN_EXIT_PARCEL_PORTER_ID,
                        ParcelEntry.COLUMN_EXIT_DOCUMENT_NUMBER,
                        ParcelEntry.COLUMN_EXIT_FULLNAME,
                        ParcelEntry.COLUMN_UNIQUE_ID

                };

                String selection = ParcelEntry.COLUMN_IS_SYNC + " = ? AND " + ParcelEntry.COLUMN_GATEWAY_ID + " = ? AND " + ParcelEntry.COLUMN_IS_UPDATE + " = ? ";
                String [] selectionArgs = {NOT_SYNC,String.valueOf(gatewayId),NOT_UPDATE};

                Cursor parcels = db.query(ParcelEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,null,null);

                if (parcels != null && parcels.getCount()>0){
                    while (parcels.moveToNext()){
                        JSONObject parcelJson = new JSONObject();
                        parcelJson.put(ID_PARCEL,parcels.getLong(parcels.getColumnIndex(ParcelEntry._ID)));
                        parcelJson.put(IS_UPDATE,false);
                        parcelJson.put(ParcelEntry.COLUMN_APARTMENT_ID,parcels.getLong(parcels.getColumnIndex(ParcelEntry.COLUMN_APARTMENT_ID)));
                        parcelJson.put(ParcelEntry.COLUMN_PARCELTYPE_ID,parcels.getLong(parcels.getColumnIndex(ParcelEntry.COLUMN_PARCELTYPE_ID)));
                        parcelJson.put(ParcelEntry.COLUMN_OBSERVATIONS,(parcels.isNull(parcels.getColumnIndex(ParcelEntry.COLUMN_OBSERVATIONS))) ? JSONObject.NULL : parcels.getString(parcels.getColumnIndex(ParcelEntry.COLUMN_OBSERVATIONS)));
                        parcelJson.put(ParcelEntry.COLUMN_FULL_NAME,parcels.getString(parcels.getColumnIndex(ParcelEntry.COLUMN_FULL_NAME)));
                        parcelJson.put(ParcelEntry.COLUMN_ENTRY_PARCEL,parcels.getString(parcels.getColumnIndex(ParcelEntry.COLUMN_ENTRY_PARCEL)));
                        parcelJson.put(ParcelEntry.COLUMN_ENTRY_PARCEL_PORTER_ID,parcels.getLong(parcels.getColumnIndex(ParcelEntry.COLUMN_ENTRY_PARCEL_PORTER_ID)));
                        parcelJson.put(ParcelEntry.COLUMN_EXIT_PARCEL,(parcels.isNull(parcels.getColumnIndex(ParcelEntry.COLUMN_EXIT_PARCEL))) ? JSONObject.NULL : parcels.getString(parcels.getColumnIndex(ParcelEntry.COLUMN_EXIT_PARCEL)));
                        parcelJson.put(ParcelEntry.COLUMN_EXIT_PARCEL_PORTER_ID,(parcels.isNull(parcels.getColumnIndex(ParcelEntry.COLUMN_EXIT_PARCEL_PORTER_ID))) ? JSONObject.NULL : parcels.getLong(parcels.getColumnIndex(ParcelEntry.COLUMN_EXIT_PARCEL_PORTER_ID)));
                        parcelJson.put(ParcelEntry.COLUMN_EXIT_FULLNAME,(parcels.isNull(parcels.getColumnIndex(ParcelEntry.COLUMN_EXIT_FULLNAME))) ? JSONObject.NULL : parcels.getString(parcels.getColumnIndex(ParcelEntry.COLUMN_EXIT_FULLNAME)));
                        parcelJson.put(ParcelEntry.COLUMN_EXIT_DOCUMENT_NUMBER,(parcels.isNull(parcels.getColumnIndex(ParcelEntry.COLUMN_EXIT_DOCUMENT_NUMBER))) ? JSONObject.NULL : parcels.getString(parcels.getColumnIndex(ParcelEntry.COLUMN_EXIT_DOCUMENT_NUMBER)));
                        parcelJson.put(UNIQUE_ID,(parcels.isNull(parcels.getColumnIndex(ParcelEntry.COLUMN_UNIQUE_ID))) ? JSONObject.NULL : parcels.getString(parcels.getColumnIndex(ParcelEntry.COLUMN_UNIQUE_ID)));

                        parcelsArrayJson.put(parcelJson);
                    }
                    parcels.close();
                }

                String[] projection_u = {
                        ParcelEntry._ID,
                        ParcelEntry.COLUMN_APARTMENT_ID,
                        ParcelEntry.COLUMN_ENTRY_PARCEL,
                        ParcelEntry.COLUMN_EXIT_PARCEL,
                        ParcelEntry.COLUMN_EXIT_PARCEL_PORTER_ID,
                        ParcelEntry.COLUMN_EXIT_DOCUMENT_NUMBER,
                        ParcelEntry.COLUMN_EXIT_FULLNAME,
                        ParcelEntry.COLUMN_PARCEL_ID_SERVER
                };


                String selection_u = ParcelEntry.COLUMN_IS_SYNC + " = ? AND " + ParcelEntry.COLUMN_GATEWAY_ID + " = ? AND " + ParcelEntry.COLUMN_IS_UPDATE + " = ? AND " + ParcelEntry.COLUMN_EXIT_PARCEL + " IS NOT NULL ";
                String [] selectionArgs_u = {IS_SYNC,String.valueOf(gatewayId),NOT_UPDATE};
                Cursor parcels_update = db.query(ParcelEntry.TABLE_NAME,projection_u,selection_u,selectionArgs_u,null,null,null,null);

                if (parcels_update != null && parcels_update.getCount()>0){
                    while (parcels_update.moveToNext()){
                        JSONObject parcelUpdateJson = new JSONObject();
                        parcelUpdateJson.put(ID_PARCEL,parcels_update.getLong(parcels_update.getColumnIndex(ParcelEntry._ID)));
                        parcelUpdateJson.put(IS_UPDATE,true);
                        parcelUpdateJson.put(ParcelEntry.COLUMN_APARTMENT_ID,parcels_update.getLong(parcels_update.getColumnIndex(ParcelEntry.COLUMN_APARTMENT_ID)));
                        parcelUpdateJson.put(ParcelEntry.COLUMN_ENTRY_PARCEL,parcels_update.getString(parcels_update.getColumnIndex(ParcelEntry.COLUMN_ENTRY_PARCEL)));
                        parcelUpdateJson.put(ParcelEntry.COLUMN_EXIT_PARCEL,(parcels_update.isNull(parcels_update.getColumnIndex(ParcelEntry.COLUMN_EXIT_PARCEL))) ? JSONObject.NULL : parcels_update.getString(parcels_update.getColumnIndex(ParcelEntry.COLUMN_EXIT_PARCEL)));
                        parcelUpdateJson.put(ParcelEntry.COLUMN_EXIT_PARCEL_PORTER_ID,(parcels_update.isNull(parcels_update.getColumnIndex(ParcelEntry.COLUMN_EXIT_PARCEL_PORTER_ID))) ? JSONObject.NULL : parcels_update.getLong(parcels_update.getColumnIndex(ParcelEntry.COLUMN_EXIT_PARCEL_PORTER_ID)));
                        parcelUpdateJson.put(ParcelEntry.COLUMN_EXIT_DOCUMENT_NUMBER,(parcels_update.isNull(parcels_update.getColumnIndex(ParcelEntry.COLUMN_EXIT_DOCUMENT_NUMBER))) ? JSONObject.NULL : parcels_update.getString(parcels_update.getColumnIndex(ParcelEntry.COLUMN_EXIT_DOCUMENT_NUMBER)));
                        parcelUpdateJson.put(ParcelEntry.COLUMN_EXIT_FULLNAME,(parcels_update.isNull(parcels_update.getColumnIndex(ParcelEntry.COLUMN_EXIT_FULLNAME))) ? JSONObject.NULL : parcels_update.getString(parcels_update.getColumnIndex(ParcelEntry.COLUMN_EXIT_FULLNAME)));
                        parcelUpdateJson.put(ParcelEntry.COLUMN_PARCEL_ID_SERVER,parcels_update.getLong(parcels_update.getColumnIndex(ParcelEntry.COLUMN_PARCEL_ID_SERVER)));

                        parcelsArrayJson.put(parcelUpdateJson);
                    }
                    parcels_update.close();
                }

                if ((parcels != null && parcels.getCount()>0) || (parcels_update != null && parcels_update.getCount()>0)){
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.accumulate("building_id",buildingId);
                    jsonObject.accumulate("gateway_id",gatewayId);
                    if (parcelsArrayJson.length() != 0){
                        jsonObject.accumulate("parcels",parcelsArrayJson);
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
                        parcelsJsonStr = buffer.toString();
                        getParcelsDataFromJson(parcelsJsonStr);
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
                        parcelsJsonStr = buffer.toString();
                        getParcelsDataFromJson(parcelsJsonStr);
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

    private void getParcelsDataFromJson(String parcelsJsonStr)
            throws JSONException {


        final String PARCELS_RETURN = "parcel_return";
        final String PARCELS_UPDATE_RETURN = "parcel_update_return";
        final String ID_PARCEL = "id_parcel";
        final String INSERT_DATA = "insert_data";
        final String ID_SERVER = "id_server";
        final String UPDATE_DATA = "update_data";

        try {
            JSONObject returnJson = new JSONObject(parcelsJsonStr);
            JSONArray parcelsReturn = (returnJson.isNull(PARCELS_RETURN)) ? null : returnJson.getJSONArray(PARCELS_RETURN);
            JSONArray parcelsUpdateReturn = (returnJson.isNull(PARCELS_UPDATE_RETURN)) ? null : returnJson.getJSONArray(PARCELS_UPDATE_RETURN);

            Vector<ContentValues> vectorParcelsReturn;
            if (parcelsReturn != null){
                vectorParcelsReturn = new Vector<ContentValues>(parcelsReturn.length());

                for(int i = 0; i < parcelsReturn.length(); i++) {

                    long id_parcel;
                    boolean insert_data;
                    long id_server;

                    JSONObject returnParcelJson = parcelsReturn.getJSONObject(i);

                    id_parcel = returnParcelJson.getLong(ID_PARCEL);
                    insert_data = returnParcelJson.getBoolean(INSERT_DATA);

                    ContentValues parcelsValues = new ContentValues();
                    if (insert_data){
                        parcelsValues.put(ParcelEntry.COLUMN_IS_SYNC, IS_SYNC);
                        id_server = returnParcelJson.getLong(ID_SERVER);
                        parcelsValues.put(ParcelEntry.COLUMN_PARCEL_ID_SERVER,id_server);
                    }
                    else{
                        parcelsValues.put(ParcelEntry.COLUMN_IS_SYNC, NOT_SYNC);
                    }
                    parcelsValues.put(ParcelEntry._ID, id_parcel);

                    vectorParcelsReturn.add(parcelsValues);
                }
            }else{
                vectorParcelsReturn = null;
            }

            Vector<ContentValues> vectorParcelsUpdateReturn;
            if (parcelsUpdateReturn != null){
                vectorParcelsUpdateReturn = new Vector<ContentValues>(parcelsUpdateReturn.length());

                for(int i = 0; i < parcelsUpdateReturn.length(); i++) {

                    long id_parcel;
                    boolean update_data;

                    JSONObject returnParcelUpdateJson = parcelsUpdateReturn.getJSONObject(i);

                    id_parcel = returnParcelUpdateJson.getLong(ID_PARCEL);
                    update_data = returnParcelUpdateJson.getBoolean(UPDATE_DATA);

                    ContentValues parcelsUpdateValues = new ContentValues();
                    if (update_data){
                        parcelsUpdateValues.put(ParcelEntry.COLUMN_IS_UPDATE, IS_UPDATE);
                    }
                    else{
                        parcelsUpdateValues.put(ParcelEntry.COLUMN_IS_UPDATE, NOT_UPDATE);
                    }
                    parcelsUpdateValues.put(ParcelEntry._ID, id_parcel);

                    vectorParcelsUpdateReturn.add(parcelsUpdateValues);
                }
            }
            else{
                vectorParcelsUpdateReturn = null;
            }

            UpdateSyncParcels.run(vectorParcelsReturn,vectorParcelsUpdateReturn,mContext);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

}
