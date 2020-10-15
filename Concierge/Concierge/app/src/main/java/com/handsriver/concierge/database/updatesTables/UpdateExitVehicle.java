package com.handsriver.concierge.database.updatesTables;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeContract.VehicleEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.email_notifications.SendEmailNotifications;
import com.handsriver.concierge.sync.ConfigureSyncAccount;

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

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Created by alain_r._trouve_silva after 23-03-17.
 */

public class UpdateExitVehicle extends AsyncTask<Void,Void,Integer> {
    private SQLiteDatabase db;
    private String exit;
    private String entry;
    private String licensePlate;
    private String apartmentNumber;
    private int porterId;
    private String id;
    private String button;
    private boolean isAutomatic;
    private boolean isFined;
    private Context mContext;

    private static final String TAG = "UpdateExitVehicle";
    public static final String FINE = "fine";
    public static final String EXIT = "exit";

    public static final String LOG_TAG = UpdateExitVehicle.class.getSimpleName();
    public static final String EMAIL_SERVICE = "email_service";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    public static final String SEND_FINE = "fine";


    public UpdateExitVehicle(String exit, int porterId, String id, Boolean isAutomatic, Boolean isFined, String button, Context mContext, String apartmentNumber, String licensePlate, String entry){
        this.exit = exit;
        this.porterId = porterId;
        this.id = id;
        this.isAutomatic = isAutomatic;
        this.isFined = isFined;
        this.button = button;
        this.mContext = mContext;
        this.apartmentNumber = apartmentNumber;
        this.licensePlate = licensePlate;
        this.entry = entry;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        String tableName = VehicleEntry.TABLE_NAME;
        SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean isfineNotifications = settingsPrefs.getBoolean(mContext.getResources().getString(R.string.pref_send_email_fine_key),false);

        int count = 0;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            ContentValues values = new ContentValues();
            if (button.equals(EXIT)){
                if (isAutomatic && isFined){
                    values.put(VehicleEntry.COLUMN_EXIT_DATE,exit);
                    values.put(VehicleEntry.COLUMN_EXIT_PORTER_ID, porterId);
                    values.put(VehicleEntry.COLUMN_FINE_DATE,exit);
                    values.put(VehicleEntry.COLUMN_FINE_PORTER_ID,porterId);

                    String whereClause = VehicleEntry._ID + " = ?";
                    String [] whereArgs = {id};

                    count = db.update(tableName,values,whereClause,whereArgs);

                    if(isfineNotifications){
                        sendEmailFineNotification(apartmentNumber,licensePlate,entry,exit);
                    }
                }
                else if ((isAutomatic && !isFined) || (!isAutomatic)){
                    values.put(VehicleEntry.COLUMN_EXIT_DATE,exit);
                    values.put(VehicleEntry.COLUMN_EXIT_PORTER_ID, porterId);

                    String whereClause = VehicleEntry._ID + " = ?";
                    String [] whereArgs = {id};

                    count = db.update(tableName,values,whereClause,whereArgs);
                }
            }else if (button.equals(FINE)){
                if (!isAutomatic){
                    values.put(VehicleEntry.COLUMN_FINE_DATE,exit);
                    values.put(VehicleEntry.COLUMN_FINE_PORTER_ID,porterId);

                    String whereClause = VehicleEntry._ID + " = ?";
                    String [] whereArgs = {id};

                    count = db.update(tableName,values,whereClause,whereArgs);

                    if(isfineNotifications){
                        sendEmailFineNotification(apartmentNumber,licensePlate,entry,exit);
                    }
                }
            }
            ConfigureSyncAccount.syncImmediatelyVisitsAndVehicles(mContext);
        } catch (SQLException e){
            Log.e(TAG, "SQLiteException:" + e.getMessage());
            DatabaseManager.getInstance().closeDatabase();
        }
        return count;
    }

    @Override
    protected void onPostExecute(Integer aInt) {
        DatabaseManager.getInstance().closeDatabase();
        super.onPostExecute(aInt);
    }

    private void sendEmailFineNotification(String apartment, String licensePlate,String entry, String dateFine){
        Log.d(LOG_TAG, "Starting Send Email");

        HttpURLConnection urlConnection = null;
        HttpsURLConnection urlConnectionHttps = null;

        try {
            SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            final String BASE_URL = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_URL_key),"");
            final int buildingId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_building_key),"0"));
            final int gatewayId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_gateway_key),"0"));
            final String API_KEY = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_apikey_key),"");
            final String emails_cc = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_send_email_field_key),"");

            if (BASE_URL.length()>0 && (BASE_URL.startsWith(HTTP) || BASE_URL.startsWith(HTTPS))){
                Uri buildUri = Uri.parse(BASE_URL).buildUpon().appendPath(EMAIL_SERVICE).build();
                URL url = new URL(buildUri.toString());

                if (!licensePlate.isEmpty()){
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.accumulate("building_id",buildingId);
                    jsonObject.accumulate("gateway_id",gatewayId);
                    jsonObject.accumulate("vehicle",licensePlate);
                    jsonObject.accumulate("apartment",apartment);
                    jsonObject.accumulate("entry",entry);
                    jsonObject.accumulate("fine_date",dateFine);
                    jsonObject.accumulate("emails_cc",emails_cc);
                    jsonObject.accumulate("type",SEND_FINE);

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
        }
    }
}
