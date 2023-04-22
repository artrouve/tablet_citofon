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
import com.handsriver.concierge.database.ConciergeContract.ResidentTempEntry;
import com.handsriver.concierge.database.InsertUpdateTables.IUResidentsTemps;

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

public class ResidentsTempsSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = ResidentsTempsSyncAdapter.class.getSimpleName();
    public static final String RESIDENTSTEMPS = "residentstemps";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    private Context mContext;

    public ResidentsTempsSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        this.mContext= context;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");

        HttpURLConnection urlConnection = null;
        HttpsURLConnection urlConnectionHttps = null;

        BufferedReader reader = null;

        String residentstempsJsonStr;

        try {
            SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            final String BASE_URL = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_URL_key),"");
            final int buildingId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_building_key),"0"));
            final String API_KEY = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_apikey_key),"");

            if (BASE_URL.length()>0 && (BASE_URL.startsWith(HTTP) || BASE_URL.startsWith(HTTPS))){
                Uri buildUri = Uri.parse(BASE_URL).buildUpon().appendPath(RESIDENTSTEMPS).build();
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
                    residentstempsJsonStr = buffer.toString();

                    getResidentsTempsDataFromJson(residentstempsJsonStr);
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
                    residentstempsJsonStr = buffer.toString();
                    getResidentsTempsDataFromJson(residentstempsJsonStr);
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

    private void getResidentsTempsDataFromJson(String residenttempJsonStr)
            throws JSONException {

        final String FULL_NAME = "full_name";
        final String ID_RESIDENT = "id_resident_temp";
        final String APARTMENT_ID = "apartment_id";
        final String EMAIL = "email";

        final String PHONE = "phone";
        final String STARTDATE = "start_date";
        final String ENDDATE = "end_date";
        final String RUT = "document_number";


        try {
            JSONArray residentstempsArray = new JSONArray(residenttempJsonStr);

            Vector<ContentValues> cVVector = new Vector<ContentValues>(residentstempsArray.length());

            for(int i = 0; i < residentstempsArray.length(); i++) {

                long apartment_id;
                long id_resident;
                String full_name;
                String email;
                String phone;
                String start_date;
                String end_date;
                String rut;


                JSONObject residenttempJson = residentstempsArray.getJSONObject(i);

                apartment_id = residenttempJson.getLong(APARTMENT_ID);
                id_resident = residenttempJson.getLong(ID_RESIDENT);
                full_name = residenttempJson.getString(FULL_NAME);
                email = (residenttempJson.isNull(EMAIL)) ? null : residenttempJson.getString(EMAIL);
                phone = (residenttempJson.isNull(PHONE)) ? "" : residenttempJson.getString(PHONE);
                rut = (residenttempJson.isNull(RUT)) ? "" : residenttempJson.getString(RUT);
                start_date = (residenttempJson.isNull(STARTDATE)) ? "" : residenttempJson.getString(STARTDATE);
                end_date = (residenttempJson.isNull(ENDDATE)) ? "" : residenttempJson.getString(ENDDATE);

                ContentValues residenttempValues = new ContentValues();

                residenttempValues.put(ResidentTempEntry.COLUMN_FULL_NAME, full_name);
                residenttempValues.put(ResidentTempEntry.COLUMN_EMAIL, email);


                residenttempValues.put(ResidentTempEntry.COLUMN_PHONE, phone);
                residenttempValues.put(ResidentTempEntry.COLUMN_RUT, rut);

                residenttempValues.put(ResidentTempEntry.COLUMN_START_DATE, start_date);
                residenttempValues.put(ResidentTempEntry.COLUMN_END_DATE, end_date);

                residenttempValues.put(ResidentTempEntry.COLUMN_APARTMENT_ID, apartment_id);
                residenttempValues.put(ResidentTempEntry.COLUMN_RESIDENTTEMP_ID_SERVER, id_resident);


                cVVector.add(residenttempValues);
            }

            IUResidentsTemps.run(cVVector);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

}
