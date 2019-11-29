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
import com.handsriver.concierge.database.updatesTables.UpdateSyncTimekeepingExitOtherGateways;
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

public class TimekeepingExitOthersGatewaysSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = TimekeepingExitOthersGatewaysSyncAdapter.class.getSimpleName();
    public static final String TIMEKEEPING = "timekeeping_exit";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    private Context mContext;
    ConciergeDbHelper dbHelper;
    SQLiteDatabase db;

    public static final String NOT_UPDATE = "0";


    public TimekeepingExitOthersGatewaysSyncAdapter(Context context, boolean autoInitialize) {
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

        String timekeepingExitOtherGatewaysJsonStr;

        final String ID_TIMEKEEPING = "id_timekeeping";


        try {
            SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            final String BASE_URL = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_URL_key),"");
            final int gatewayId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_gateway_key),"0"));
            final int buildingId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_building_key),"0"));
            final String API_KEY = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_apikey_key),"");

            if (BASE_URL.length()>0 && (BASE_URL.startsWith(HTTP) || BASE_URL.startsWith(HTTPS))){
                String[] projection = {
                        TimekeepingEntry._ID,
                        TimekeepingEntry.COLUMN_PORTER_ID,
                        TimekeepingEntry.COLUMN_ENTRY_PORTER,
                        TimekeepingEntry.COLUMN_TIMEKEEPING_ID_SERVER
                };

                String selection = TimekeepingEntry.COLUMN_EXIT_PORTER + " IS NULL AND " + TimekeepingEntry.COLUMN_IS_UPDATE + " = ? ";
                String [] selectionArgs = {NOT_UPDATE};

                Cursor timekeeping;

                timekeeping = db.query(TimekeepingEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,null,null);

                JSONArray timekeepingExitArrayJson = new JSONArray();

                if (timekeeping != null && timekeeping.getCount()>0){
                    while (timekeeping.moveToNext()){
                        JSONObject timekeepingExitJson = new JSONObject();
                        timekeepingExitJson.put(ID_TIMEKEEPING,timekeeping.getLong(timekeeping.getColumnIndex(TimekeepingEntry._ID)));
                        timekeepingExitJson.put(TimekeepingEntry.COLUMN_PORTER_ID,timekeeping.getLong(timekeeping.getColumnIndex(TimekeepingEntry.COLUMN_PORTER_ID)));
                        timekeepingExitJson.put(TimekeepingEntry.COLUMN_ENTRY_PORTER,timekeeping.getString(timekeeping.getColumnIndex(TimekeepingEntry.COLUMN_ENTRY_PORTER)));
                        timekeepingExitJson.put(TimekeepingEntry.COLUMN_TIMEKEEPING_ID_SERVER,(timekeeping.isNull(timekeeping.getColumnIndex(TimekeepingEntry.COLUMN_TIMEKEEPING_ID_SERVER))) ? JSONObject.NULL : timekeeping.getLong(timekeeping.getColumnIndex(TimekeepingEntry.COLUMN_TIMEKEEPING_ID_SERVER)));
                        timekeepingExitArrayJson.put(timekeepingExitJson);
                    }
                    timekeeping.close();
                }

                if ((timekeeping != null && timekeeping.getCount()>0)){
                    Uri buildUri = Uri.parse(BASE_URL).buildUpon().appendPath(TIMEKEEPING).build();
                    URL url = new URL(buildUri.toString());

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.accumulate("building_id",buildingId);
                    jsonObject.accumulate("timekeeping_check",timekeepingExitArrayJson);

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
                        timekeepingExitOtherGatewaysJsonStr = buffer.toString();
                        getTimekeepingExitOthersGatewaysDataFromJson(timekeepingExitOtherGatewaysJsonStr);
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
                        timekeepingExitOtherGatewaysJsonStr = buffer.toString();
                        getTimekeepingExitOthersGatewaysDataFromJson(timekeepingExitOtherGatewaysJsonStr);
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

    private void getTimekeepingExitOthersGatewaysDataFromJson(String timekeepingExitOtherGatewaysJsonStr)
            throws JSONException {

        final String ID_TIMEKEEPING = "id_timekeeping";
        final String EXIT_PORTER = "exit_porter";
        final String EXIT_PORTER_ID = "exit_porter_id";
        final String TIMEKEEPING_EXIT_RETURN = "timekeeping_exit_return";


        try {
            JSONObject returnJson = new JSONObject(timekeepingExitOtherGatewaysJsonStr);
            JSONArray timekeepingExitOthersGatewaysArray = (returnJson.isNull(TIMEKEEPING_EXIT_RETURN)) ? null : returnJson.getJSONArray(TIMEKEEPING_EXIT_RETURN);

            Vector<ContentValues> cVVector;
            if (timekeepingExitOthersGatewaysArray != null){
                cVVector = new Vector<ContentValues>(timekeepingExitOthersGatewaysArray.length());

                for(int i = 0; i < timekeepingExitOthersGatewaysArray.length(); i++) {

                    long id_timekeeping;
                    String exit_porter;
                    long exit_porter_id;

                    JSONObject timekeepingExitJson = timekeepingExitOthersGatewaysArray.getJSONObject(i);

                    id_timekeeping = timekeepingExitJson.getLong(ID_TIMEKEEPING);
                    exit_porter = (timekeepingExitJson.isNull(EXIT_PORTER)) ? null : timekeepingExitJson.getString(EXIT_PORTER);
                    ContentValues timekeepingExitValues = new ContentValues();

                    timekeepingExitValues.put(TimekeepingEntry._ID, id_timekeeping);
                    if(!timekeepingExitJson.isNull(EXIT_PORTER_ID)){
                        exit_porter_id = timekeepingExitJson.getLong(EXIT_PORTER_ID);
                        timekeepingExitValues.put(TimekeepingEntry.COLUMN_EXIT_PORTER_ID, exit_porter_id);
                    }
                    timekeepingExitValues.put(TimekeepingEntry.COLUMN_EXIT_PORTER, exit_porter);

                    cVVector.add(timekeepingExitValues);
                }
            }
            else{
                cVVector = null;
            }

            UpdateSyncTimekeepingExitOtherGateways.run(cVVector);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

}
