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
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.InsertUpdateTables.IUResidents;
import com.handsriver.concierge.database.InsertUpdateTables.IUVisitsOthersGateways;
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

public class VisitsOthersGatewaysSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = VisitsOthersGatewaysSyncAdapter.class.getSimpleName();
    public static final String VISITS = "visits";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    private Context mContext;
    ConciergeDbHelper dbHelper;
    SQLiteDatabase db;



    public VisitsOthersGatewaysSyncAdapter(Context context, boolean autoInitialize) {
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

        String visitsOtherGatewaysJsonStr;

        try {
            SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            final String BASE_URL = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_URL_key),"");
            final int gatewayId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_gateway_key),"0"));
            final int buildingId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_building_key),"0"));
            final String API_KEY = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_apikey_key),"");
            String search_date;

            if (BASE_URL.length()>0 && (BASE_URL.startsWith(HTTP) || BASE_URL.startsWith(HTTPS))){
                String[] projection = {
                        VisitEntry._ID
                };

                String selection = VisitEntry.COLUMN_GATEWAY_ID + " != ?";
                String [] selectionArgs = {String.valueOf(gatewayId)};

                Cursor visits;

                visits = db.query(VisitEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,null,null);

                if (visits != null && visits.getCount()>0){
                    search_date = Utility.getDateSimpleForServer1Days();
                    visits.close();
                }
                else{
                    search_date = Utility.getDateSimpleForServer7Days();
                }

                Uri buildUri = Uri.parse(BASE_URL).buildUpon().appendPath(VISITS).build();
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
                    visitsOtherGatewaysJsonStr = buffer.toString();
                    getVisitsOthersGatewaysDataFromJson(visitsOtherGatewaysJsonStr);
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
                    visitsOtherGatewaysJsonStr = buffer.toString();
                    getVisitsOthersGatewaysDataFromJson(visitsOtherGatewaysJsonStr);
                }

            }

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        } finally {
            DatabaseManager.getInstance().closeDatabase();
            if (urlConnectionHttps != null) {
                urlConnectionHttps.disconnect();
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
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

    private void getVisitsOthersGatewaysDataFromJson(String visitsOtherGatewaysJsonStr)
            throws JSONException {

        final String ID_VISIT = "id_visit";
        final String FULL_NAME = "full_name";
        final String APARTMENT_ID = "apartment_id";
        final String DOCUMENT_NUMBER = "document_number";
        final String NATIONALITY = "nationality";
        final String GENDER = "gender";
        final String BIRTHDATE = "birthdate";
        final String ENTRY = "entry";
        final String GATEWAY_ID = "gateway_id";
        final String PORTER_ID = "porter_id";
        final int IS_SYNC = 1;


        try {
            JSONArray visitsOthersGatewaysArray = new JSONArray(visitsOtherGatewaysJsonStr);

            Vector<ContentValues> cVVector = new Vector<ContentValues>(visitsOthersGatewaysArray.length());

            for(int i = 0; i < visitsOthersGatewaysArray.length(); i++) {

                long id_visit;
                String full_name;
                long apartment_id;
                String document_number;
                String nationality;
                String gender;
                String birthdate;
                String entry;
                long gateway_id;
                long porter_id;

                JSONObject visitJson = visitsOthersGatewaysArray.getJSONObject(i);

                id_visit = visitJson.getLong(ID_VISIT);
                full_name = (visitJson.isNull(FULL_NAME)) ? null : visitJson.getString(FULL_NAME);
                apartment_id = visitJson.getLong(APARTMENT_ID);
                document_number = (visitJson.isNull(DOCUMENT_NUMBER)) ? null : visitJson.getString(DOCUMENT_NUMBER);
                nationality = (visitJson.isNull(NATIONALITY)) ? null : visitJson.getString(NATIONALITY);
                gender = (visitJson.isNull(GENDER)) ? null : visitJson.getString(GENDER);
                birthdate = (visitJson.isNull(BIRTHDATE)) ? null : visitJson.getString(BIRTHDATE);
                entry = visitJson.getString(ENTRY);
                gateway_id = visitJson.getLong(GATEWAY_ID);
                porter_id = visitJson.getLong(PORTER_ID);

                ContentValues visitValues = new ContentValues();

                visitValues.put(VisitEntry.COLUMN_VISIT_ID_SERVER, id_visit);
                visitValues.put(VisitEntry.COLUMN_FULL_NAME, full_name);
                visitValues.put(VisitEntry.COLUMN_APARTMENT_ID, apartment_id);
                visitValues.put(VisitEntry.COLUMN_DOCUMENT_NUMBER, document_number);
                visitValues.put(VisitEntry.COLUMN_NATIONALITY, nationality);
                visitValues.put(VisitEntry.COLUMN_GENDER, gender);
                visitValues.put(VisitEntry.COLUMN_BIRTHDATE, birthdate);
                visitValues.put(VisitEntry.COLUMN_ENTRY, entry);
                visitValues.put(VisitEntry.COLUMN_GATEWAY_ID, gateway_id);
                visitValues.put(VisitEntry.COLUMN_PORTER_ID, porter_id);
                visitValues.put(VisitEntry.COLUMN_IS_SYNC,IS_SYNC);

                cVVector.add(visitValues);
            }

            IUVisitsOthersGateways.run(cVVector);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

}
