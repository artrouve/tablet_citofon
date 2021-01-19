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
import com.handsriver.concierge.database.ConciergeContract.CommonspaceVisitsEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.InsertUpdateTables.IUCommonspacesVisitsOthersGateways;
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

public class VisitsCommonspacesOthersGatewaysSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = VisitsCommonspacesOthersGatewaysSyncAdapter.class.getSimpleName();
    public static final String COMMONSPACES_VISITS = "commonspace_visits_list";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    private Context mContext;
    ConciergeDbHelper dbHelper;
    SQLiteDatabase db;



    public VisitsCommonspacesOthersGatewaysSyncAdapter(Context context, boolean autoInitialize) {
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

        String commonspacesVisitsOtherGatewaysJsonStr;

        try {
            SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            final String BASE_URL = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_URL_key),"");
            final int gatewayId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_gateway_key),"0"));
            final int buildingId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_building_key),"0"));
            final String API_KEY = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_apikey_key),"");
            String search_date;

            if (BASE_URL.length()>0 && (BASE_URL.startsWith(HTTP) || BASE_URL.startsWith(HTTPS))){
                String[] projection = {
                        CommonspaceVisitsEntry._ID
                };

                String selection = CommonspaceVisitsEntry.COLUMN_GATEWAY_ID + " != ?";
                String [] selectionArgs = {String.valueOf(gatewayId)};

                Cursor  commonspacesVisitsOthersGateways;

                commonspacesVisitsOthersGateways = db.query(CommonspaceVisitsEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,null,null);

                if (commonspacesVisitsOthersGateways != null && commonspacesVisitsOthersGateways.getCount()>0){
                    search_date = Utility.getDateSimpleForServer1Days();
                    commonspacesVisitsOthersGateways.close();
                }
                else{
                    search_date = Utility.getDateSimpleForServer7Days();
                }

                Uri buildUri = Uri.parse(BASE_URL).buildUpon().appendPath(COMMONSPACES_VISITS).build();
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
                    commonspacesVisitsOtherGatewaysJsonStr = buffer.toString();
                    getCommonspacesVisitsOthersGatewaysDataFromJson(commonspacesVisitsOtherGatewaysJsonStr);
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
                    commonspacesVisitsOtherGatewaysJsonStr = buffer.toString();
                    getCommonspacesVisitsOthersGatewaysDataFromJson(commonspacesVisitsOtherGatewaysJsonStr);
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

    private void getCommonspacesVisitsOthersGatewaysDataFromJson(String commonspacesVisitsOtherGatewaysJsonStr)
            throws JSONException {

        final String ID_COMMONSPACE_VISIT = "id_commonspace_visit";
        final int IS_SYNC = 1;
        final int NOT_UPDATE = 0;
        final int IS_UPDATE = 1;



        try {
            JSONArray commonspacesVisitsOthersGatewaysArray = new JSONArray(commonspacesVisitsOtherGatewaysJsonStr);

            Vector<ContentValues> cVVector = new Vector<ContentValues>(commonspacesVisitsOthersGatewaysArray.length());

            for(int i = 0; i < commonspacesVisitsOthersGatewaysArray.length(); i++) {

                long id_commonspace_visit;
                String document_number;
                String full_name;
                String nationality;
                String gender;
                String birthdate;
                String license_plate;
                String entry;
                String exit_visit;
                String obs_exit_visit;
                long entry_porter_id;
                long exit_porter_id;
                long gateway_id;
                long commonspace_id;
                long appartment_id;


                JSONObject commonspacesVisitsOthersGatewaysJson = commonspacesVisitsOthersGatewaysArray.getJSONObject(i);

                id_commonspace_visit = commonspacesVisitsOthersGatewaysJson.getLong(ID_COMMONSPACE_VISIT);
                document_number = (commonspacesVisitsOthersGatewaysJson.isNull(CommonspaceVisitsEntry.COLUMN_DOCUMENT_NUMBER)) ? null : commonspacesVisitsOthersGatewaysJson.getString(CommonspaceVisitsEntry.COLUMN_DOCUMENT_NUMBER);


                full_name = (commonspacesVisitsOthersGatewaysJson.isNull(CommonspaceVisitsEntry.COLUMN_FULL_NAME)) ? null : commonspacesVisitsOthersGatewaysJson.getString(CommonspaceVisitsEntry.COLUMN_FULL_NAME);
                nationality = (commonspacesVisitsOthersGatewaysJson.isNull(CommonspaceVisitsEntry.COLUMN_NATIONALITY)) ? null : commonspacesVisitsOthersGatewaysJson.getString(CommonspaceVisitsEntry.COLUMN_NATIONALITY);
                gender = (commonspacesVisitsOthersGatewaysJson.isNull(CommonspaceVisitsEntry.COLUMN_GENDER)) ? null : commonspacesVisitsOthersGatewaysJson.getString(CommonspaceVisitsEntry.COLUMN_GENDER);
                birthdate = (commonspacesVisitsOthersGatewaysJson.isNull(CommonspaceVisitsEntry.COLUMN_BIRTHDATE)) ? null : commonspacesVisitsOthersGatewaysJson.getString(CommonspaceVisitsEntry.COLUMN_BIRTHDATE);
                license_plate = (commonspacesVisitsOthersGatewaysJson.isNull(CommonspaceVisitsEntry.COLUMN_LICENSE_PLATE)) ? null : commonspacesVisitsOthersGatewaysJson.getString(CommonspaceVisitsEntry.COLUMN_LICENSE_PLATE);
                entry = commonspacesVisitsOthersGatewaysJson.getString(CommonspaceVisitsEntry.COLUMN_ENTRY);
                exit_visit = (commonspacesVisitsOthersGatewaysJson.isNull(CommonspaceVisitsEntry.COLUMN_EXIT_VISIT)) ? null : commonspacesVisitsOthersGatewaysJson.getString(CommonspaceVisitsEntry.COLUMN_EXIT_VISIT);
                //obs_exit_supplier = (commonspacesVisitsOthersGatewaysJson.isNull(SupplierVisitsEntry.COLUMN_EXIT_OBS)) ? null : suppliersVisitsOthersGatewaysJson.getString(SupplierVisitsEntry.COLUMN_EXIT_OBS);
                gateway_id = commonspacesVisitsOthersGatewaysJson.getLong(CommonspaceVisitsEntry.COLUMN_GATEWAY_ID);
                commonspace_id = commonspacesVisitsOthersGatewaysJson.getLong(CommonspaceVisitsEntry.COLUMN_COMMONSPACE_ID);
                appartment_id = commonspacesVisitsOthersGatewaysJson.getLong(CommonspaceVisitsEntry.COLUMN_APARTMENT_ID);

                entry_porter_id = commonspacesVisitsOthersGatewaysJson.getLong(CommonspaceVisitsEntry.COLUMN_ENTRY_PORTER_ID);


                ContentValues commonspaceVisitValues = new ContentValues();

                commonspaceVisitValues.put(CommonspaceVisitsEntry.COLUMN_COMMONSPACE_VISIT_ID_SERVER, id_commonspace_visit);
                commonspaceVisitValues.put(CommonspaceVisitsEntry.COLUMN_DOCUMENT_NUMBER, document_number);
                commonspaceVisitValues.put(CommonspaceVisitsEntry.COLUMN_FULL_NAME, full_name);
                commonspaceVisitValues.put(CommonspaceVisitsEntry.COLUMN_NATIONALITY, nationality);
                commonspaceVisitValues.put(CommonspaceVisitsEntry.COLUMN_GENDER, gender);
                commonspaceVisitValues.put(CommonspaceVisitsEntry.COLUMN_BIRTHDATE, birthdate);
                commonspaceVisitValues.put(CommonspaceVisitsEntry.COLUMN_LICENSE_PLATE, license_plate);
                commonspaceVisitValues.put(CommonspaceVisitsEntry.COLUMN_ENTRY, entry);
                commonspaceVisitValues.put(CommonspaceVisitsEntry.COLUMN_EXIT_VISIT, exit_visit);
                //commonspaceVisitValues.put(CommonspaceVisitsEntry.COLUMN_EXIT_OBS, obs_exit_supplier);
                commonspaceVisitValues.put(CommonspaceVisitsEntry.COLUMN_GATEWAY_ID, gateway_id);
                commonspaceVisitValues.put(CommonspaceVisitsEntry.COLUMN_COMMONSPACE_ID, commonspace_id);
                commonspaceVisitValues.put(CommonspaceVisitsEntry.COLUMN_APARTMENT_ID, appartment_id);
                commonspaceVisitValues.put(CommonspaceVisitsEntry.COLUMN_ENTRY_PORTER_ID, entry_porter_id);
                if (!commonspacesVisitsOthersGatewaysJson.isNull(CommonspaceVisitsEntry.COLUMN_EXIT_PORTER_ID)){
                    exit_porter_id = commonspacesVisitsOthersGatewaysJson.getLong(CommonspaceVisitsEntry.COLUMN_EXIT_PORTER_ID);
                    commonspaceVisitValues.put(CommonspaceVisitsEntry.COLUMN_EXIT_PORTER_ID, exit_porter_id);
                }
                commonspaceVisitValues.put(CommonspaceVisitsEntry.COLUMN_IS_SYNC,IS_SYNC);

                if(exit_visit == null){
                    commonspaceVisitValues.put(CommonspaceVisitsEntry.COLUMN_IS_UPDATE,NOT_UPDATE);
                }
                else{
                    commonspaceVisitValues.put(CommonspaceVisitsEntry.COLUMN_IS_UPDATE,IS_UPDATE);

                }

                cVVector.add(commonspaceVisitValues);
            }


            IUCommonspacesVisitsOthersGateways.run(cVVector);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

}

