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
import com.handsriver.concierge.database.ConciergeContract.WhitelistEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.InsertUpdateTables.IUWhitelist;
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

public class WhitelistSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = WhitelistSyncAdapter.class.getSimpleName();
    public static final String WHITELIST = "whitelist";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    private Context mContext;

    public WhitelistSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        this.mContext= context;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");

        HttpURLConnection urlConnection = null;
        HttpsURLConnection urlConnectionHttps = null;

        BufferedReader reader = null;

        String whitelistJsonStr;

        try {
            SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            final String BASE_URL = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_URL_key),"");
            final int buildingId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_building_key),"0"));
            final String API_KEY = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_apikey_key),"");
            final String actual_date = Utility.getHourForServerOnlyDate();

            if (BASE_URL.length()>0 && (BASE_URL.startsWith(HTTP) || BASE_URL.startsWith(HTTPS))){
                Uri buildUri = Uri.parse(BASE_URL).buildUpon().appendPath(WHITELIST).build();
                URL url = new URL(buildUri.toString());

                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("building_id",buildingId);
                jsonObject.accumulate("actual_date",actual_date);

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
                    whitelistJsonStr = buffer.toString();
                    getWhitelistDataFromJson(whitelistJsonStr);
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
                    whitelistJsonStr = buffer.toString();
                    getWhitelistDataFromJson(whitelistJsonStr);
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

    private void getWhitelistDataFromJson(String whitelistJsonStr)
            throws JSONException {

        final String ID_WHITELIST = "id_whitelist";


        try {
            JSONArray whitelistArray = new JSONArray(whitelistJsonStr);

            Vector<ContentValues> cVVector = new Vector<ContentValues>(whitelistArray.length());

            for(int i = 0; i < whitelistArray.length(); i++) {

                long apartment_id;
                long id_whitelist;
                String full_name;
                String document_number;

                JSONObject whitelistJson = whitelistArray.getJSONObject(i);

                apartment_id = whitelistJson.getLong(WhitelistEntry.COLUMN_APARTMENT_ID);
                id_whitelist = whitelistJson.getLong(ID_WHITELIST);
                full_name = (whitelistJson.isNull(WhitelistEntry.COLUMN_FULL_NAME)) ? null : whitelistJson.getString(WhitelistEntry.COLUMN_FULL_NAME);
                document_number = whitelistJson.getString(WhitelistEntry.COLUMN_DOCUMENT_NUMBER);

                ContentValues whitelistValues = new ContentValues();

                whitelistValues.put(WhitelistEntry.COLUMN_FULL_NAME, full_name);
                whitelistValues.put(WhitelistEntry.COLUMN_DOCUMENT_NUMBER, document_number);
                whitelistValues.put(WhitelistEntry.COLUMN_APARTMENT_ID, apartment_id);
                whitelistValues.put(WhitelistEntry.COLUMN_WHITELIST_ID_SERVER, id_whitelist);

                cVVector.add(whitelistValues);
            }

            IUWhitelist.run(cVVector);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

}
