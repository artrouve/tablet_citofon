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
import com.handsriver.concierge.database.ConciergeContract.TimekeepingEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.InsertUpdateTables.IUTimekeepingOthersGateways;
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

public class TimekeepingOthersGatewaysSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = TimekeepingOthersGatewaysSyncAdapter.class.getSimpleName();
    public static final String TIMEKEEPING = "timekeeping_list";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    private Context mContext;
    ConciergeDbHelper dbHelper;
    SQLiteDatabase db;



    public TimekeepingOthersGatewaysSyncAdapter(Context context, boolean autoInitialize) {
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

        String timekeepingOtherGatewaysJsonStr;

        try {
            SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            final String BASE_URL = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_URL_key),"");
            final int gatewayId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_gateway_key),"0"));
            final int buildingId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_building_key),"0"));
            final String API_KEY = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_apikey_key),"");
            String search_date;

            if (BASE_URL.length()>0 && (BASE_URL.startsWith(HTTP) || BASE_URL.startsWith(HTTPS))){
                String[] projection = {
                        TimekeepingEntry._ID
                };

                String selection = TimekeepingEntry.COLUMN_GATEWAY_ID + " != ?";
                String [] selectionArgs = {String.valueOf(gatewayId)};

                Cursor timekeeping;

                timekeeping = db.query(TimekeepingEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,null,null);

                if (timekeeping != null && timekeeping.getCount()>0){
                    search_date = Utility.getDateSimpleForServer1Days();
                    timekeeping.close();
                }
                else{
                    search_date = Utility.getDateSimpleForServer7Days();
                }

                Uri buildUri = Uri.parse(BASE_URL).buildUpon().appendPath(TIMEKEEPING).build();
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
                    timekeepingOtherGatewaysJsonStr = buffer.toString();
                    getTimekeepingOthersGatewaysDataFromJson(timekeepingOtherGatewaysJsonStr);
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
                    timekeepingOtherGatewaysJsonStr = buffer.toString();
                    getTimekeepingOthersGatewaysDataFromJson(timekeepingOtherGatewaysJsonStr);
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

    private void getTimekeepingOthersGatewaysDataFromJson(String timekeepingOtherGatewaysJsonStr)
            throws JSONException {

        final String ID_TIMEKEEPING = "id_timekeeping";
        final String ENTRY_PORTER = "entry_porter";
        final String EXIT_PORTER = "exit_porter";
        final String GATEWAY_ID = "gateway_id";
        final String ENTRY_PORTER_ID = "entry_porter_id";
        final String EXIT_PORTER_ID = "exit_porter_id";
        final String PORTER_ID = "porter_id";
        final int IS_SYNC = 1;
        final int NOT_UPDATE = 0;
        final int IS_UPDATE = 1;


        try {
            JSONArray timekeepingOthersGatewaysArray = new JSONArray(timekeepingOtherGatewaysJsonStr);

            Vector<ContentValues> cVVector = new Vector<ContentValues>(timekeepingOthersGatewaysArray.length());

            for(int i = 0; i < timekeepingOthersGatewaysArray.length(); i++) {

                long id_timekeeping;
                String entry_porter;
                String exit_porter;
                long entry_porter_id;
                long exit_porter_id;
                long gateway_id;
                long porter_id;

                JSONObject timekeepingJson = timekeepingOthersGatewaysArray.getJSONObject(i);

                id_timekeeping = timekeepingJson.getLong(ID_TIMEKEEPING);
                entry_porter = timekeepingJson.getString(ENTRY_PORTER);
                exit_porter = (timekeepingJson.isNull(EXIT_PORTER)) ? null : timekeepingJson.getString(EXIT_PORTER);
                gateway_id = timekeepingJson.getLong(GATEWAY_ID);
                entry_porter_id = timekeepingJson.getLong(ENTRY_PORTER_ID);
                porter_id = timekeepingJson.getLong(PORTER_ID);

                ContentValues timekeepingValues = new ContentValues();

                timekeepingValues.put(TimekeepingEntry.COLUMN_TIMEKEEPING_ID_SERVER, id_timekeeping);
                timekeepingValues.put(TimekeepingEntry.COLUMN_ENTRY_PORTER, entry_porter);
                timekeepingValues.put(TimekeepingEntry.COLUMN_EXIT_PORTER, exit_porter);
                timekeepingValues.put(TimekeepingEntry.COLUMN_GATEWAY_ID, gateway_id);
                timekeepingValues.put(TimekeepingEntry.COLUMN_ENTRY_PORTER_ID, entry_porter_id);
                if(!timekeepingJson.isNull(EXIT_PORTER_ID)){
                    exit_porter_id = timekeepingJson.getLong(EXIT_PORTER_ID);
                    timekeepingValues.put(TimekeepingEntry.COLUMN_EXIT_PORTER_ID, exit_porter_id);
                }
                timekeepingValues.put(TimekeepingEntry.COLUMN_PORTER_ID, porter_id);
                timekeepingValues.put(TimekeepingEntry.COLUMN_IS_SYNC,IS_SYNC);

                if (exit_porter == null){
                    timekeepingValues.put(TimekeepingEntry.COLUMN_IS_UPDATE,NOT_UPDATE);
                }
                else {
                    timekeepingValues.put(TimekeepingEntry.COLUMN_IS_UPDATE,IS_UPDATE);
                }

                cVVector.add(timekeepingValues);
            }

            IUTimekeepingOthersGateways.run(cVVector);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

}
