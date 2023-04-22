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
import com.handsriver.concierge.database.InsertUpdateTables.IUParcelsOthersGateways;
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
import java.net.URL;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Created by alain_r._trouve_silva after 27-04-17.
 */

public class ParcelsOthersGatewaysSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = ParcelsOthersGatewaysSyncAdapter.class.getSimpleName();
    public static final String PARCELS = "parcels_list";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    private Context mContext;
    ConciergeDbHelper dbHelper;
    SQLiteDatabase db;



    public ParcelsOthersGatewaysSyncAdapter(Context context, boolean autoInitialize) {
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

        String parcelsOtherGatewaysJsonStr;

        try {
            SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            final String BASE_URL = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_URL_key),"");
            final int gatewayId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_gateway_key),"0"));
            final int buildingId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_building_key),"0"));
            final String API_KEY = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_apikey_key),"");
            String search_date;

            if (BASE_URL.length()>0 && (BASE_URL.startsWith(HTTP) || BASE_URL.startsWith(HTTPS))){
                String[] projection = {
                        ParcelEntry._ID
                };

                String selection = ParcelEntry.COLUMN_GATEWAY_ID + " != ?";
                String [] selectionArgs = {String.valueOf(gatewayId)};

                Cursor  parcelsOthersGateways;

                parcelsOthersGateways = db.query(ParcelEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,null,null);

                if (parcelsOthersGateways != null && parcelsOthersGateways.getCount()>0){
                    search_date = Utility.getDateSimpleForServer1Days();
                    parcelsOthersGateways.close();
                }
                else{
                    search_date = Utility.getDateSimpleForServer7Days();
                }

                Uri buildUri = Uri.parse(BASE_URL).buildUpon().appendPath(PARCELS).build();
                URL url = new URL(buildUri.toString());

                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("building_id",buildingId);
                jsonObject.accumulate("gateway_id",gatewayId);
                jsonObject.accumulate("search_date",search_date);

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
                    parcelsOtherGatewaysJsonStr = buffer.toString();
                    getParcelsOthersGatewaysDataFromJson(parcelsOtherGatewaysJsonStr);
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
                    parcelsOtherGatewaysJsonStr = buffer.toString();
                    getParcelsOthersGatewaysDataFromJson(parcelsOtherGatewaysJsonStr);
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

    private void getParcelsOthersGatewaysDataFromJson(String commonspacesVisitsOtherGatewaysJsonStr)
            throws JSONException {

        final String ID_PARCEL = "id_parcel";
        final String UNIQUIE_ID = "unique_id";
        final int IS_SYNC = 1;
        final int NOT_UPDATE = 0;
        final int IS_UPDATE = 1;



        try {
            JSONArray parcelsOthersGatewaysArray = new JSONArray(commonspacesVisitsOtherGatewaysJsonStr);

            Vector<ContentValues> cVVector = new Vector<ContentValues>(parcelsOthersGatewaysArray.length());

            for(int i = 0; i < parcelsOthersGatewaysArray.length(); i++) {


                /*
                 * COLUMNS TABLE PARCELS
                 * */

                long id_parcel;
                String full_name;
                String unique_id_parcel;
                String observations;
                String entry_parcel;
                String exit_parcel;
                String exit_full_name;
                String exit_document_number;
                long gateway_id;
                long appartment_id;
                long entry_parcel_porter_id;


                long exit_parcel_porter_id;
                long parceltype_id;
                long parcel_id_server;



                JSONObject parcelsOthersGatewaysJson = parcelsOthersGatewaysArray.getJSONObject(i);

                parcel_id_server = parcelsOthersGatewaysJson.getLong(ID_PARCEL);
                full_name = (parcelsOthersGatewaysJson.isNull(ParcelEntry.COLUMN_FULL_NAME)) ? null : parcelsOthersGatewaysJson.getString(ParcelEntry.COLUMN_FULL_NAME);
                unique_id_parcel = (parcelsOthersGatewaysJson.isNull(UNIQUIE_ID)) ? null : parcelsOthersGatewaysJson.getString(UNIQUIE_ID);
                observations = (parcelsOthersGatewaysJson.isNull(ParcelEntry.COLUMN_OBSERVATIONS)) ? null : parcelsOthersGatewaysJson.getString(ParcelEntry.COLUMN_OBSERVATIONS);
                entry_parcel = (parcelsOthersGatewaysJson.isNull(ParcelEntry.COLUMN_ENTRY_PARCEL)) ? null : parcelsOthersGatewaysJson.getString(ParcelEntry.COLUMN_ENTRY_PARCEL);
                exit_parcel = (parcelsOthersGatewaysJson.isNull(ParcelEntry.COLUMN_EXIT_PARCEL)) ? null : parcelsOthersGatewaysJson.getString(ParcelEntry.COLUMN_EXIT_PARCEL);


                exit_full_name = (parcelsOthersGatewaysJson.isNull(ParcelEntry.COLUMN_EXIT_FULLNAME)) ? null : parcelsOthersGatewaysJson.getString(ParcelEntry.COLUMN_EXIT_FULLNAME);
                exit_document_number = (parcelsOthersGatewaysJson.isNull(ParcelEntry.COLUMN_EXIT_DOCUMENT_NUMBER)) ? null : parcelsOthersGatewaysJson.getString(ParcelEntry.COLUMN_EXIT_DOCUMENT_NUMBER);

                gateway_id = parcelsOthersGatewaysJson.getLong(ParcelEntry.COLUMN_GATEWAY_ID);
                appartment_id = parcelsOthersGatewaysJson.getLong(ParcelEntry.COLUMN_APARTMENT_ID);
                parceltype_id = parcelsOthersGatewaysJson.getLong(ParcelEntry.COLUMN_PARCELTYPE_ID);
                entry_parcel_porter_id = parcelsOthersGatewaysJson.getLong(ParcelEntry.COLUMN_ENTRY_PARCEL_PORTER_ID);



                ContentValues parcelValues = new ContentValues();


                parcelValues.put(ParcelEntry.COLUMN_FULL_NAME, full_name);
                parcelValues.put(ParcelEntry.COLUMN_UNIQUE_ID, unique_id_parcel);
                parcelValues.put(ParcelEntry.COLUMN_APARTMENT_ID, appartment_id);
                parcelValues.put(ParcelEntry.COLUMN_PARCELTYPE_ID, parceltype_id);
                parcelValues.put(ParcelEntry.COLUMN_OBSERVATIONS, observations);
                parcelValues.put(ParcelEntry.COLUMN_ENTRY_PARCEL, entry_parcel);
                parcelValues.put(ParcelEntry.COLUMN_EXIT_PARCEL, exit_parcel);
                parcelValues.put(ParcelEntry.COLUMN_EXIT_FULLNAME, exit_full_name);
                parcelValues.put(ParcelEntry.COLUMN_EXIT_DOCUMENT_NUMBER, exit_document_number);
                if(!parcelsOthersGatewaysJson.isNull(ParcelEntry.COLUMN_EXIT_PARCEL_PORTER_ID)){
                    exit_parcel_porter_id = parcelsOthersGatewaysJson.getLong(ParcelEntry.COLUMN_EXIT_PARCEL_PORTER_ID);
                    parcelValues.put(ParcelEntry.COLUMN_EXIT_PARCEL_PORTER_ID, exit_parcel_porter_id);
                }
                parcelValues.put(ParcelEntry.COLUMN_GATEWAY_ID, gateway_id);
                parcelValues.put(ParcelEntry.COLUMN_ENTRY_PARCEL_PORTER_ID, entry_parcel_porter_id);
                parcelValues.put(ParcelEntry.COLUMN_PARCEL_ID_SERVER, parcel_id_server);

                parcelValues.put(ParcelEntry.COLUMN_IS_SYNC,IS_SYNC);
                if(exit_parcel == null){
                    parcelValues.put(ParcelEntry.COLUMN_IS_UPDATE,NOT_UPDATE);
                }
                else{
                    parcelValues.put(ParcelEntry.COLUMN_IS_UPDATE,IS_UPDATE);
                }
                cVVector.add(parcelValues);
            }

            IUParcelsOthersGateways.run(cVVector);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

}

