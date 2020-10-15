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
import com.handsriver.concierge.database.ConciergeContract.BlacklistEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.InsertUpdateTables.IUBlacklist;
import com.handsriver.concierge.database.InsertUpdateTables.IUResidents;

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

public class BlacklistSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = BlacklistSyncAdapter.class.getSimpleName();
    public static final String BLACKLIST = "blacklist";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    private Context mContext;

    public BlacklistSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        this.mContext= context;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");

        HttpURLConnection urlConnection = null;
        HttpsURLConnection urlConnectionHttps = null;

        BufferedReader reader = null;

        String blacklistJsonStr;

        try {
            SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            final String BASE_URL = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_URL_key),"");
            final int buildingId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_building_key),"0"));
            final String API_KEY = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_apikey_key),"");

            if (BASE_URL.length()>0 && (BASE_URL.startsWith(HTTP) || BASE_URL.startsWith(HTTPS))){
                Uri buildUri = Uri.parse(BASE_URL).buildUpon().appendPath(BLACKLIST).build();
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
                    blacklistJsonStr = buffer.toString();
                    getBlacklistDataFromJson(blacklistJsonStr);
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
                    blacklistJsonStr = buffer.toString();
                    getBlacklistDataFromJson(blacklistJsonStr);
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

    private void getBlacklistDataFromJson(String blacklistJsonStr)
            throws JSONException {

        final String OBSERVATIONS = "observations";
        final String ID_BLACKLIST = "id_blacklist";
        final String APARTMENT_ID = "apartment_id";
        final String DOCUMENT_NUMBER = "rut";
        final String IDENTIFICATION = "identification";

        try {
            JSONArray blacklistArray = new JSONArray(blacklistJsonStr);

            Vector<ContentValues> cVVector = new Vector<ContentValues>(blacklistArray.length());

            for(int i = 0; i < blacklistArray.length(); i++) {

                long apartment_id;
                long id_blacklist;
                String observations;
                String documentNumber;
                String identification;

                JSONObject blacklistJson = blacklistArray.getJSONObject(i);

                apartment_id = blacklistJson.getLong(APARTMENT_ID);
                id_blacklist = blacklistJson.getLong(ID_BLACKLIST);
                observations = blacklistJson.getString(OBSERVATIONS);
                documentNumber = blacklistJson.getString(DOCUMENT_NUMBER);
                identification = blacklistJson.getString(IDENTIFICATION);

                ContentValues residentValues = new ContentValues();

                residentValues.put(BlacklistEntry.COLUMN_APARTMENT_ID, apartment_id);
                residentValues.put(BlacklistEntry.COLUMN_BLACKLIST_ID_SERVER, id_blacklist);
                residentValues.put(BlacklistEntry.COLUMN_OBSERVATIONS, observations);
                residentValues.put(BlacklistEntry.COLUMN_DOCUMENT_NUMBER, documentNumber);
                residentValues.put(BlacklistEntry.COLUMN_IDENTIFICATION, identification);

                cVVector.add(residentValues);
            }

            IUBlacklist.run(cVVector);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

}
