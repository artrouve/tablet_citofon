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
import com.handsriver.concierge.database.updatesTables.UpdateSyncParcels;
import com.handsriver.concierge.database.updatesTables.UpdateSyncTimekeeping;

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

public class TimekeepingSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = TimekeepingSyncAdapter.class.getSimpleName();
    public static final String TIMEKEEPING = "storeTimekeeping";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    private Context mContext;
    ConciergeDbHelper dbHelper;
    SQLiteDatabase db;
    public static final String NOT_SYNC = "0";
    public static final String IS_SYNC = "1";
    public static final String NOT_UPDATE = "0";
    public static final String IS_UPDATE = "1";



    public TimekeepingSyncAdapter(Context context, boolean autoInitialize) {
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
        String timekeepingJsonStr;

        final String ID_TIMEKEEPING = "id_timekeeping";
        final String IS_UPDATE = "is_update";


        try {
            SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            final String BASE_URL = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_URL_key),"");
            final int gatewayId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_gateway_key),"0"));
            final int buildingId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_building_key),"0"));
            final String API_KEY = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_apikey_key),"");

            if (BASE_URL.length()>0 && (BASE_URL.startsWith(HTTP) || BASE_URL.startsWith(HTTPS))){
                Uri buildUri = Uri.parse(BASE_URL).buildUpon().appendPath(TIMEKEEPING).build();
                URL url = new URL(buildUri.toString());

                JSONArray timekeepingArrayJson = new JSONArray();

                String[] projection = {
                        TimekeepingEntry._ID,
                        TimekeepingEntry.COLUMN_ENTRY_PORTER,
                        TimekeepingEntry.COLUMN_ENTRY_HASH,
                        TimekeepingEntry.COLUMN_EXIT_PORTER,
                        TimekeepingEntry.COLUMN_EXIT_HASH,
                        TimekeepingEntry.COLUMN_PORTER_ID,
                        TimekeepingEntry.COLUMN_ENTRY_PORTER_ID,
                        TimekeepingEntry.COLUMN_EXIT_PORTER_ID
                };

                String selection = TimekeepingEntry.COLUMN_IS_SYNC + " = ? AND " + TimekeepingEntry.COLUMN_GATEWAY_ID + " = ? AND " + TimekeepingEntry.COLUMN_IS_UPDATE + " = ? ";
                String [] selectionArgs = {NOT_SYNC,String.valueOf(gatewayId),NOT_UPDATE};

                Cursor timekeeping = db.query(TimekeepingEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,null,null);

                if (timekeeping != null && timekeeping.getCount()>0){
                    while (timekeeping.moveToNext()){
                        JSONObject timekeepingJson = new JSONObject();
                        timekeepingJson.put(ID_TIMEKEEPING,timekeeping.getLong(timekeeping.getColumnIndex(TimekeepingEntry._ID)));
                        timekeepingJson.put(IS_UPDATE,false);
                        timekeepingJson.put(TimekeepingEntry.COLUMN_ENTRY_PORTER,timekeeping.getString(timekeeping.getColumnIndex(TimekeepingEntry.COLUMN_ENTRY_PORTER)));
                        timekeepingJson.put(TimekeepingEntry.COLUMN_ENTRY_HASH,timekeeping.getString(timekeeping.getColumnIndex(TimekeepingEntry.COLUMN_ENTRY_HASH)));
                        timekeepingJson.put(TimekeepingEntry.COLUMN_EXIT_PORTER,(timekeeping.isNull(timekeeping.getColumnIndex(TimekeepingEntry.COLUMN_EXIT_PORTER))) ? JSONObject.NULL : timekeeping.getString(timekeeping.getColumnIndex(TimekeepingEntry.COLUMN_EXIT_PORTER)));
                        timekeepingJson.put(TimekeepingEntry.COLUMN_EXIT_HASH,(timekeeping.isNull(timekeeping.getColumnIndex(TimekeepingEntry.COLUMN_EXIT_HASH))) ? JSONObject.NULL : timekeeping.getString(timekeeping.getColumnIndex(TimekeepingEntry.COLUMN_EXIT_HASH)));
                        timekeepingJson.put(TimekeepingEntry.COLUMN_PORTER_ID,timekeeping.getLong(timekeeping.getColumnIndex(TimekeepingEntry.COLUMN_PORTER_ID)));
                        timekeepingJson.put(TimekeepingEntry.COLUMN_ENTRY_PORTER_ID,timekeeping.getLong(timekeeping.getColumnIndex(TimekeepingEntry.COLUMN_ENTRY_PORTER_ID)));
                        timekeepingJson.put(TimekeepingEntry.COLUMN_EXIT_PORTER_ID,(timekeeping.isNull(timekeeping.getColumnIndex(TimekeepingEntry.COLUMN_EXIT_PORTER_ID))) ? JSONObject.NULL : timekeeping.getLong(timekeeping.getColumnIndex(TimekeepingEntry.COLUMN_EXIT_PORTER_ID)));

                        timekeepingArrayJson.put(timekeepingJson);
                    }
                    timekeeping.close();
                }

                String[] projection_u = {
                        TimekeepingEntry._ID,
                        TimekeepingEntry.COLUMN_PORTER_ID,
                        TimekeepingEntry.COLUMN_ENTRY_PORTER,
                        TimekeepingEntry.COLUMN_EXIT_PORTER,
                        TimekeepingEntry.COLUMN_EXIT_HASH,
                        TimekeepingEntry.COLUMN_EXIT_PORTER_ID,
                        TimekeepingEntry.COLUMN_TIMEKEEPING_ID_SERVER
                };

                String selection_u = TimekeepingEntry.COLUMN_IS_SYNC + " = ? AND " + TimekeepingEntry.COLUMN_IS_UPDATE + " = ? AND " + TimekeepingEntry.COLUMN_EXIT_PORTER + " IS NOT NULL";
                String [] selectionArgs_u = {IS_SYNC,NOT_UPDATE};
                Cursor timekeeping_update = db.query(TimekeepingEntry.TABLE_NAME,projection_u,selection_u,selectionArgs_u,null,null,null,null);

                if (timekeeping_update != null && timekeeping_update.getCount()>0){
                    while (timekeeping_update.moveToNext()){

                        JSONObject timekeepingUpdateJson = new JSONObject();
                        timekeepingUpdateJson.put(ID_TIMEKEEPING,timekeeping_update.getLong(timekeeping_update.getColumnIndex(TimekeepingEntry._ID)));
                        timekeepingUpdateJson.put(IS_UPDATE,true);
                        timekeepingUpdateJson.put(TimekeepingEntry.COLUMN_ENTRY_PORTER,timekeeping_update.getString(timekeeping_update.getColumnIndex(TimekeepingEntry.COLUMN_ENTRY_PORTER)));
                        timekeepingUpdateJson.put(TimekeepingEntry.COLUMN_EXIT_PORTER,(timekeeping_update.isNull(timekeeping_update.getColumnIndex(TimekeepingEntry.COLUMN_EXIT_PORTER))) ? JSONObject.NULL : timekeeping_update.getString(timekeeping_update.getColumnIndex(TimekeepingEntry.COLUMN_EXIT_PORTER)));
                        timekeepingUpdateJson.put(TimekeepingEntry.COLUMN_EXIT_HASH,(timekeeping_update.isNull(timekeeping_update.getColumnIndex(TimekeepingEntry.COLUMN_EXIT_HASH))) ? JSONObject.NULL : timekeeping_update.getString(timekeeping_update.getColumnIndex(TimekeepingEntry.COLUMN_EXIT_HASH)));
                        timekeepingUpdateJson.put(TimekeepingEntry.COLUMN_PORTER_ID,timekeeping_update.getLong(timekeeping_update.getColumnIndex(TimekeepingEntry.COLUMN_PORTER_ID)));
                        timekeepingUpdateJson.put(TimekeepingEntry.COLUMN_EXIT_PORTER_ID,(timekeeping_update.isNull(timekeeping_update.getColumnIndex(TimekeepingEntry.COLUMN_EXIT_PORTER_ID))) ? JSONObject.NULL : timekeeping_update.getLong(timekeeping_update.getColumnIndex(TimekeepingEntry.COLUMN_EXIT_PORTER_ID)));
                        timekeepingUpdateJson.put(TimekeepingEntry.COLUMN_TIMEKEEPING_ID_SERVER,(timekeeping_update.isNull(timekeeping_update.getColumnIndex(TimekeepingEntry.COLUMN_TIMEKEEPING_ID_SERVER))) ? JSONObject.NULL : timekeeping_update.getLong(timekeeping_update.getColumnIndex(TimekeepingEntry.COLUMN_TIMEKEEPING_ID_SERVER)));

                        timekeepingArrayJson.put(timekeepingUpdateJson);
                    }
                    timekeeping_update.close();
                }

                if ((timekeeping != null && timekeeping.getCount()>0) || (timekeeping_update != null && timekeeping_update.getCount()>0)){
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.accumulate("building_id",buildingId);
                    jsonObject.accumulate("gateway_id",gatewayId);
                    if (timekeepingArrayJson.length() != 0){
                        jsonObject.accumulate("timekeeping",timekeepingArrayJson);
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
                        timekeepingJsonStr = buffer.toString();
                        getTimekeepingDataFromJson(timekeepingJsonStr);
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
                        timekeepingJsonStr = buffer.toString();
                        getTimekeepingDataFromJson(timekeepingJsonStr);
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

    private void getTimekeepingDataFromJson(String timekeepingJsonStr)
            throws JSONException {


        final String TIMEKEEPING_RETURN = "timekeeping_return";
        final String TIMEKEEPING_UPDATE_RETURN = "timekeeping_update_return";
        final String ID_TIMEKEEPING = "id_timekeeping";
        final String INSERT_DATA = "insert_data";
        final String ID_SERVER = "id_server";
        final String UPDATE_DATA = "update_data";

        try {
            JSONObject returnJson = new JSONObject(timekeepingJsonStr);
            JSONArray timekeepingReturn = (returnJson.isNull(TIMEKEEPING_RETURN)) ? null : returnJson.getJSONArray(TIMEKEEPING_RETURN);
            JSONArray timekeepingUpdateReturn = (returnJson.isNull(TIMEKEEPING_UPDATE_RETURN)) ? null : returnJson.getJSONArray(TIMEKEEPING_UPDATE_RETURN);

            Vector<ContentValues> vectorTimekeepingReturn;
            if (timekeepingReturn != null){
                vectorTimekeepingReturn = new Vector<ContentValues>(timekeepingReturn.length());

                for(int i = 0; i < timekeepingReturn.length(); i++) {

                    long id_timekeeping;
                    boolean insert_data;
                    long id_server;

                    JSONObject returnTimekeepingJson = timekeepingReturn.getJSONObject(i);

                    id_timekeeping = returnTimekeepingJson.getLong(ID_TIMEKEEPING);
                    insert_data = returnTimekeepingJson.getBoolean(INSERT_DATA);

                    ContentValues timekeepingValues = new ContentValues();
                    if (insert_data){
                        timekeepingValues.put(TimekeepingEntry.COLUMN_IS_SYNC, IS_SYNC);
                        id_server = returnTimekeepingJson.getLong(ID_SERVER);
                        timekeepingValues.put(TimekeepingEntry.COLUMN_TIMEKEEPING_ID_SERVER,id_server);
                    }
                    else{
                        timekeepingValues.put(TimekeepingEntry.COLUMN_IS_SYNC, NOT_SYNC);
                    }
                    timekeepingValues.put(TimekeepingEntry._ID, id_timekeeping);

                    vectorTimekeepingReturn.add(timekeepingValues);
                }
            }else{
                vectorTimekeepingReturn = null;
            }

            Vector<ContentValues> vectorTimekeepingUpdateReturn;
            if (timekeepingUpdateReturn != null){
                vectorTimekeepingUpdateReturn = new Vector<ContentValues>(timekeepingUpdateReturn.length());

                for(int i = 0; i < timekeepingUpdateReturn.length(); i++) {

                    long id_timekeeping;
                    boolean update_data;

                    JSONObject returnTimekeepingUpdateJson = timekeepingUpdateReturn.getJSONObject(i);

                    id_timekeeping = returnTimekeepingUpdateJson.getLong(ID_TIMEKEEPING);
                    update_data = returnTimekeepingUpdateJson.getBoolean(UPDATE_DATA);

                    ContentValues timekeepingUpdateValues = new ContentValues();
                    if (update_data){
                        timekeepingUpdateValues.put(TimekeepingEntry.COLUMN_IS_UPDATE, IS_UPDATE);
                    }
                    else{
                        timekeepingUpdateValues.put(TimekeepingEntry.COLUMN_IS_UPDATE, NOT_UPDATE);
                    }
                    timekeepingUpdateValues.put(TimekeepingEntry._ID, id_timekeeping);

                    vectorTimekeepingUpdateReturn.add(timekeepingUpdateValues);
                }
            }
            else{
                vectorTimekeepingUpdateReturn = null;
            }

            UpdateSyncTimekeeping.run(vectorTimekeepingReturn,vectorTimekeepingUpdateReturn);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

}
