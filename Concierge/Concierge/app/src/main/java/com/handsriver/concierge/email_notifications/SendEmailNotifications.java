package com.handsriver.concierge.email_notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeContract.VisitEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.sync.VisitsVehiclesSyncAdapter;
import com.handsriver.concierge.utilities.Utility;
import com.handsriver.concierge.visits.Visit;

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
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class SendEmailNotifications extends AsyncTask<Void,Void,Void> {

    public static final String LOG_TAG = SendEmailNotifications.class.getSimpleName();
    public static final String EMAIL_SERVICE = "email_service";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    public static final String SEND_VISITS = "visits";

    private ArrayList<Visit> arrayVisit;
    private String apartment;
    private String licensePlate;
    private String entry;
    private Context mContext;

    public SendEmailNotifications(ArrayList<Visit> arrayVisit, String apartment, String licensePlate, String entry, Context mContext) {
        this.arrayVisit = arrayVisit;
        this.apartment = apartment;
        this.licensePlate = licensePlate;
        this.entry = entry;
        this.mContext = mContext;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        sendEmailVisitsVehicleNotification(arrayVisit,apartment,licensePlate,entry,mContext);
        return null;
    }



    private void sendEmailVisitsVehicleNotification(ArrayList<Visit> arrayVisit, String apartment, String licensePlate, String entry, Context mContext){

        Log.d(LOG_TAG, "Starting Send Email");

        HttpURLConnection urlConnection = null;
        HttpsURLConnection urlConnectionHttps = null;

        BufferedReader reader = null;

        try {
            SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            final String BASE_URL = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_URL_key),"");
            final int gatewayId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_gateway_key),"0"));
            final int buildingId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_building_key),"0"));
            final String API_KEY = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_apikey_key),"");

            if (BASE_URL.length()>0 && (BASE_URL.startsWith(HTTP) || BASE_URL.startsWith(HTTPS))){
                Uri buildUri = Uri.parse(BASE_URL).buildUpon().appendPath(EMAIL_SERVICE).build();
                URL url = new URL(buildUri.toString());

                JSONArray visitsArrayJson = new JSONArray();

                for (Visit visits : arrayVisit ){
                    JSONObject visitsJson = new JSONObject();
                    visitsJson.put(VisitEntry.COLUMN_DOCUMENT_NUMBER, visits.getDocumentNumber() == null ? JSONObject.NULL : visits.getDocumentNumber());
                    visitsJson.put(VisitEntry.COLUMN_FULL_NAME, visits.getFullName() == null ? JSONObject.NULL : visits.getFullName());
                    visitsArrayJson.put(visitsJson);
                }


                if (!arrayVisit.isEmpty()){
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.accumulate("building_id",buildingId);
                    jsonObject.accumulate("gateway_id",gatewayId);
                    jsonObject.accumulate("vehicle",licensePlate);
                    jsonObject.accumulate("apartment",apartment);
                    jsonObject.accumulate("entry",entry);
                    jsonObject.accumulate("type",SEND_VISITS);

                    if (visitsArrayJson.length() != 0){
                        jsonObject.accumulate("visits",visitsArrayJson);
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
                        inputStream.close();

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
                        inputStream.close();
                    }

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
}
