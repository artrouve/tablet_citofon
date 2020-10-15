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
import com.handsriver.concierge.database.ConciergeContract.PorterEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.updatesTables.UpdateSyncPorters;

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

public class PortersTabletSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = PortersTabletSyncAdapter.class.getSimpleName();
    public static final String PORTERS_UPDATE = "porters_update";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    private Context mContext;
    ConciergeDbHelper dbHelper;
    SQLiteDatabase db;
    public static final String NOT_UPDATE = "0";
    public static final String IS_UPDATE = "1";



    public PortersTabletSyncAdapter(Context context, boolean autoInitialize) {
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
        String portersJsonStr;

        final String ID_PORTER = "id_porter";


        try {
            SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            final String BASE_URL = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_URL_key),"");
            final int buildingId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_building_key),"0"));
            final String API_KEY = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_apikey_key),"");

            if (BASE_URL.length()>0 && (BASE_URL.startsWith(HTTP) || BASE_URL.startsWith(HTTPS))){
                Uri buildUri = Uri.parse(BASE_URL).buildUpon().appendPath(PORTERS_UPDATE).build();
                URL url = new URL(buildUri.toString());

                JSONArray portersArrayJson = new JSONArray();

                String[] projection_u = {
                        PorterEntry.COLUMN_IS_UPDATE_PASSWORD,
                        PorterEntry.COLUMN_PASSWORD,
                        PorterEntry.COLUMN_PORTER_ID_SERVER
                };


                String selection_u = PorterEntry.COLUMN_IS_UPDATE_PASSWORD + " = ?";
                String [] selectionArgs_u = {IS_UPDATE};

                Cursor porters_update = db.query(PorterEntry.TABLE_NAME,projection_u,selection_u,selectionArgs_u,null,null,null,null);

                if (porters_update != null && porters_update.getCount()>0){
                    while (porters_update.moveToNext()){
                        JSONObject portersUpdateJson = new JSONObject();
                        portersUpdateJson.put(ID_PORTER,porters_update.getLong(porters_update.getColumnIndex(PorterEntry.COLUMN_PORTER_ID_SERVER)));
                        portersUpdateJson.put(PorterEntry.COLUMN_PASSWORD,porters_update.getString(porters_update.getColumnIndex(PorterEntry.COLUMN_PASSWORD)));

                        portersArrayJson.put(portersUpdateJson);
                    }
                    porters_update.close();
                }

                if (porters_update != null && porters_update.getCount()>0){
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.accumulate("building_id",buildingId);
                    if (portersArrayJson.length() != 0){
                        jsonObject.accumulate("porters",portersArrayJson);
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
                        portersJsonStr = buffer.toString();
                        getPortersDataFromJson(portersJsonStr);
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
                        portersJsonStr = buffer.toString();
                        getPortersDataFromJson(portersJsonStr);
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

    private void getPortersDataFromJson(String portersJsonStr)
            throws JSONException {


        final String PORTER_UPDATE_RETURN = "porter_update_return";
        final String ID_PORTER = "id_porter";
        final String UPDATE_DATA = "update_data";

        try {
            JSONObject returnJson = new JSONObject(portersJsonStr);
            JSONArray portersUpdateReturn = (returnJson.isNull(PORTER_UPDATE_RETURN)) ? null : returnJson.getJSONArray(PORTER_UPDATE_RETURN);

            Vector<ContentValues> vectorPortersUpdateReturn;
            if (portersUpdateReturn != null){
                vectorPortersUpdateReturn = new Vector<ContentValues>(portersUpdateReturn.length());

                for(int i = 0; i < portersUpdateReturn.length(); i++) {

                    long id_porter;
                    boolean update_data;

                    JSONObject returnPorterUpdateJson = portersUpdateReturn.getJSONObject(i);

                    id_porter = returnPorterUpdateJson.getLong(ID_PORTER);
                    update_data = returnPorterUpdateJson.getBoolean(UPDATE_DATA);

                    ContentValues portersUpdateValues = new ContentValues();
                    if (update_data){
                        portersUpdateValues.put(PorterEntry.COLUMN_IS_UPDATE_PASSWORD, NOT_UPDATE);
                    }
                    else{
                        portersUpdateValues.put(PorterEntry.COLUMN_IS_UPDATE_PASSWORD, IS_UPDATE);
                    }
                    portersUpdateValues.put(PorterEntry.COLUMN_PORTER_ID_SERVER, id_porter);

                    vectorPortersUpdateReturn.add(portersUpdateValues);
                }
            }
            else{
                vectorPortersUpdateReturn = null;
            }

            UpdateSyncPorters.run(vectorPortersUpdateReturn);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

}
