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
import com.handsriver.concierge.database.ConciergeContract.ParcelEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.updatesTables.UpdateSyncParcelsExitOtherGateways;

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

public class ParcelsExitOthersGatewaysSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = ParcelsExitOthersGatewaysSyncAdapter.class.getSimpleName();
    public static final String PARCELS = "parcels_exit";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    private Context mContext;
    ConciergeDbHelper dbHelper;
    SQLiteDatabase db;

    public static final String NOT_UPDATE = "0";


    public ParcelsExitOthersGatewaysSyncAdapter(Context context, boolean autoInitialize) {
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

        String parcelsExitOtherGatewaysJsonStr;

        final String ID_PARCEL = "id_parcel";


        try {
            SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            final String BASE_URL = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_URL_key),"");
            final int gatewayId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_gateway_key),"0"));
            final int buildingId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_building_key),"0"));
            final String API_KEY = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_apikey_key),"");

            if (BASE_URL.length()>0 && (BASE_URL.startsWith(HTTP) || BASE_URL.startsWith(HTTPS))){
                String[] projection = {
                        ParcelEntry._ID,
                        ParcelEntry.COLUMN_ENTRY_PARCEL,
                        ParcelEntry.COLUMN_FULL_NAME,
                        ParcelEntry.COLUMN_APARTMENT_ID,
                        ParcelEntry.COLUMN_PARCEL_ID_SERVER
                };

                String selection = ParcelEntry.COLUMN_EXIT_PARCEL + " IS NULL AND " + ParcelEntry.COLUMN_IS_UPDATE + " = ? ";
                String [] selectionArgs = {NOT_UPDATE};

                Cursor parcels;

                parcels = db.query(ParcelEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,null,null);

                JSONArray parcelExitArrayJson = new JSONArray();

                if (parcels != null && parcels.getCount()>0){
                    while (parcels.moveToNext()){
                        JSONObject parcelExitJson = new JSONObject();
                        parcelExitJson.put(ID_PARCEL,parcels.getLong(parcels.getColumnIndex(ParcelEntry._ID)));
                        parcelExitJson.put(ParcelEntry.COLUMN_ENTRY_PARCEL,parcels.getString(parcels.getColumnIndex(ParcelEntry.COLUMN_ENTRY_PARCEL)));
                        parcelExitJson.put(ParcelEntry.COLUMN_APARTMENT_ID,parcels.getString(parcels.getColumnIndex(ParcelEntry.COLUMN_APARTMENT_ID)));
                        parcelExitJson.put(ParcelEntry.COLUMN_FULL_NAME,(parcels.isNull(parcels.getColumnIndex(ParcelEntry.COLUMN_FULL_NAME))) ? JSONObject.NULL : parcels.getString(parcels.getColumnIndex(ParcelEntry.COLUMN_FULL_NAME)));
                        parcelExitJson.put(ParcelEntry.COLUMN_PARCEL_ID_SERVER,(parcels.isNull(parcels.getColumnIndex(ParcelEntry.COLUMN_PARCEL_ID_SERVER))) ? JSONObject.NULL : parcels.getLong(parcels.getColumnIndex(ParcelEntry.COLUMN_PARCEL_ID_SERVER)));
                        parcelExitArrayJson.put(parcelExitJson);
                    }
                    parcels.close();
                }

                if ((parcels != null && parcels.getCount()>0)){
                    Uri buildUri = Uri.parse(BASE_URL).buildUpon().appendPath(PARCELS).build();
                    URL url = new URL(buildUri.toString());

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.accumulate("building_id",buildingId);
                    jsonObject.accumulate("parcels_check",parcelExitArrayJson);

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
                        parcelsExitOtherGatewaysJsonStr = buffer.toString();
                        getParcelsExitOthersGatewaysDataFromJson(parcelsExitOtherGatewaysJsonStr);
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
                        parcelsExitOtherGatewaysJsonStr = buffer.toString();
                        getParcelsExitOthersGatewaysDataFromJson(parcelsExitOtherGatewaysJsonStr);
                    }

                }
            }

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
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

    private void getParcelsExitOthersGatewaysDataFromJson(String parcelsExitOtherGatewaysJsonStr)
            throws JSONException {

        final String ID_PARCEL = "id_parcel";
        final String PARCELS_EXIT_RETURN = "parcel_exit_return";

        try {
            JSONObject returnJson = new JSONObject(parcelsExitOtherGatewaysJsonStr);
            JSONArray parcelsExitOthersGatewaysArray = (returnJson.isNull(PARCELS_EXIT_RETURN)) ? null : returnJson.getJSONArray(PARCELS_EXIT_RETURN);

            Vector<ContentValues> cVVector;

            if (parcelsExitOthersGatewaysArray != null){
                cVVector = new Vector<ContentValues>(parcelsExitOthersGatewaysArray.length());

                for(int i = 0; i < parcelsExitOthersGatewaysArray.length(); i++) {

                    long id_parcel;
                    String exit_parcel;
                    String full_name_exit_parcel;
                    String document_number_exit_parcel;
                    long exit_parcel_porter_id;

                    JSONObject parcelsExitJson = parcelsExitOthersGatewaysArray.getJSONObject(i);

                    id_parcel = parcelsExitJson.getLong(ID_PARCEL);
                    exit_parcel = (parcelsExitJson.isNull(ParcelEntry.COLUMN_EXIT_PARCEL)) ? null : parcelsExitJson.getString(ParcelEntry.COLUMN_EXIT_PARCEL);

                    ContentValues parcelsExitValues = new ContentValues();

                    parcelsExitValues.put(ParcelEntry._ID, id_parcel);
                    parcelsExitValues.put(ParcelEntry.COLUMN_EXIT_PARCEL, exit_parcel);
                    if(!parcelsExitJson.isNull(ParcelEntry.COLUMN_EXIT_PARCEL_PORTER_ID)){
                        exit_parcel_porter_id = parcelsExitJson.getLong(ParcelEntry.COLUMN_EXIT_PARCEL_PORTER_ID);
                        parcelsExitValues.put(ParcelEntry.COLUMN_EXIT_PARCEL_PORTER_ID, exit_parcel_porter_id);

                        full_name_exit_parcel = parcelsExitJson.getString(ParcelEntry.COLUMN_EXIT_FULLNAME);
                        parcelsExitValues.put(ParcelEntry.COLUMN_EXIT_FULLNAME, full_name_exit_parcel);

                        document_number_exit_parcel = parcelsExitJson.getString(ParcelEntry.COLUMN_EXIT_DOCUMENT_NUMBER);
                        parcelsExitValues.put(ParcelEntry.COLUMN_EXIT_DOCUMENT_NUMBER, document_number_exit_parcel);


                    }

                    cVVector.add(parcelsExitValues);
                }
            }
            else{
                cVVector = null;
            }

            UpdateSyncParcelsExitOtherGateways.run(cVVector);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

}
