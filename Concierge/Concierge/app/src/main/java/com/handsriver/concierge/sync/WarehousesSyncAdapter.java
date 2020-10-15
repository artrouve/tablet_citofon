package com.handsriver.concierge.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeContract.WarehouseEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.InsertUpdateTables.IUParkings;
import com.handsriver.concierge.database.InsertUpdateTables.IUWarehouses;

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
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Created by alain_r._trouve_silva after 27-04-17.
 */

public class WarehousesSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = WarehousesSyncAdapter.class.getSimpleName();
    public static final String WAREHOUSES = "warehouses";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    private Context mContext;

    public WarehousesSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        this.mContext= context;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");

        HttpURLConnection urlConnection = null;
        HttpsURLConnection urlConnectionHttps = null;

        BufferedReader reader = null;

        String warehousesJsonStr;

        try {
            SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            final String BASE_URL = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_URL_key),"");
            final int buildingId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_building_key),"0"));
            final String API_KEY = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_apikey_key),"");

            if (BASE_URL.length()>0 && (BASE_URL.startsWith(HTTP) || BASE_URL.startsWith(HTTPS))){
                Uri buildUri = Uri.parse(BASE_URL).buildUpon().appendPath(WAREHOUSES).build();
                URL url = new URL(buildUri.toString());

                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("building_id",buildingId);

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
                        // Nothing to do.
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
                    warehousesJsonStr = buffer.toString();
                    getWarehousesDataFromJson(warehousesJsonStr);
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
                        // Nothing to do.
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
                    warehousesJsonStr = buffer.toString();
                    getWarehousesDataFromJson(warehousesJsonStr);
                }

            }

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        } finally {
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

    private void getWarehousesDataFromJson(String warehousesJsonStr)
            throws JSONException {

        final String WAREHOUSE_NUMBER = "warehouse_number";
        final String ID_WAREHOUSE = "id_warehouse";
        final String APARTMENT_ID = "apartment_id";

        try {
            JSONArray warehousesArray = new JSONArray(warehousesJsonStr);

            Vector<ContentValues> cVVector = new Vector<ContentValues>(warehousesArray.length());

            for(int i = 0; i < warehousesArray.length(); i++) {

                long warehouses_id;
                long apartment_id;
                String warehouses_number;

                JSONObject warehouseJson = warehousesArray.getJSONObject(i);

                warehouses_id = warehouseJson.getLong(ID_WAREHOUSE);
                warehouses_number = warehouseJson.getString(WAREHOUSE_NUMBER);
                apartment_id = warehouseJson.getLong(APARTMENT_ID);

                ContentValues warehouseValues = new ContentValues();

                warehouseValues.put(WarehouseEntry.COLUMN_WAREHOUSE_NUMBER, warehouses_number);
                warehouseValues.put(WarehouseEntry.COLUMN_WAREHOUSE_ID_SERVER, warehouses_id);
                warehouseValues.put(WarehouseEntry.COLUMN_APARTMENT_ID,apartment_id);

                cVVector.add(warehouseValues);
            }

            IUWarehouses.run(cVVector);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

}
