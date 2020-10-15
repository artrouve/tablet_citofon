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
import com.handsriver.concierge.database.ConciergeContract.ParkingEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.InsertUpdateTables.IUApartments;
import com.handsriver.concierge.database.InsertUpdateTables.IUParkings;

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

public class ParkingsSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = ParkingsSyncAdapter.class.getSimpleName();
    public static final String PARKINGS = "parkings";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    private Context mContext;

    public ParkingsSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        this.mContext= context;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");

        HttpURLConnection urlConnection = null;
        HttpsURLConnection urlConnectionHttps = null;

        BufferedReader reader = null;

        String parkingsJsonStr;

        try {
            SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            final String BASE_URL = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_URL_key),"");
            final int buildingId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_building_key),"0"));
            final String API_KEY = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_apikey_key),"");

            if (BASE_URL.length()>0 && (BASE_URL.startsWith(HTTP) || BASE_URL.startsWith(HTTPS))){
                Uri buildUri = Uri.parse(BASE_URL).buildUpon().appendPath(PARKINGS).build();
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
                    parkingsJsonStr = buffer.toString();
                    getParkingsDataFromJson(parkingsJsonStr);
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
                    parkingsJsonStr = buffer.toString();
                    getParkingsDataFromJson(parkingsJsonStr);
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
        //return;

    }

    private void getParkingsDataFromJson(String parkingsJsonStr)
            throws JSONException {

        final String PARKING_NUMBER = "parking_number";
        final String ID_PARKING = "id_parking";
        final String APARTMENT_ID = "apartment_id";


        try {
            JSONArray parkingsArray = new JSONArray(parkingsJsonStr);

            Vector<ContentValues> cVVector = new Vector<ContentValues>(parkingsArray.length());

            for(int i = 0; i < parkingsArray.length(); i++) {

                long parking_id;
                long apartment_id;
                String parking_number;

                JSONObject parkingJson = parkingsArray.getJSONObject(i);

                parking_id = parkingJson.getLong(ID_PARKING);
                parking_number = parkingJson.getString(PARKING_NUMBER);
                apartment_id = parkingJson.getLong(APARTMENT_ID);

                ContentValues parkingValues = new ContentValues();

                parkingValues.put(ParkingEntry.COLUMN_PARKING_NUMBER, parking_number);
                parkingValues.put(ParkingEntry.COLUMN_PARKING_ID_SERVER, parking_id);
                parkingValues.put(ParkingEntry.COLUMN_APARTMENT_ID,apartment_id);

                cVVector.add(parkingValues);
            }

            IUParkings.run(cVVector);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

}
