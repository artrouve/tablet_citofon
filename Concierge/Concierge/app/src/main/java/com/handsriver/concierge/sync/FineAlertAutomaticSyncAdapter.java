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
import com.handsriver.concierge.database.ConciergeContract.VehicleEntry;
import com.handsriver.concierge.database.ConciergeContract.VisitEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.updatesTables.UpdateSyncVehiclesAlertFine;
import com.handsriver.concierge.database.updatesTables.UpdateSyncVisitsVehicles;
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

public class FineAlertAutomaticSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = FineAlertAutomaticSyncAdapter.class.getSimpleName();
    public static final String EMAIL_SERVICE = "email_service";
    public static final String SEND_AUTOMATIC = "automatic_fine_alert";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    private Context mContext;
    ConciergeDbHelper dbHelper;
    SQLiteDatabase db;
    public static final String NOT_SYNC = "0";
    public static final String IS_SYNC = "1";
    public static final String NOT_UPDATE = "0";
    public static final String IS_UPDATE = "1";
    public static final String PREFS_NAME = "PorterPrefs";
    public static final int DEF_VALUE = 0;

    public FineAlertAutomaticSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        this.mContext= context;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean isAutomaticFine = settingsPrefs.getBoolean(mContext.getResources().getString(R.string.pref_automatic_fine_key), false);
        boolean isNotifications = settingsPrefs.getBoolean(mContext.getResources().getString(R.string.pref_send_email_fine_key), false);
        boolean isAlert = settingsPrefs.getBoolean(mContext.getResources().getString(R.string.pref_send_email_alert_key), false);

        if (isAutomaticFine) {
            Log.d(LOG_TAG, "Starting sync");

            HttpURLConnection urlConnection = null;
            HttpsURLConnection urlConnectionHttps = null;

            dbHelper = new ConciergeDbHelper(mContext);
            db = DatabaseManager.getInstance().openDatabase();

            try {
                final String BASE_URL = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_URL_key),"");
                final int gatewayId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_gateway_key),"0"));
                final int buildingId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_building_key),"0"));
                final String API_KEY = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_apikey_key),"");
                String dateForServer = Utility.getHourForServer();
                final int hours = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_max_time_parking_key),"0"));
                final int minutes = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_time_delay_parking_key),"0"));
                final String emails_cc = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_send_email_field_key),"");

                int porterIdServer;
                boolean timeExpire;

                SharedPreferences porterPrefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                porterIdServer = porterPrefs.getInt(getContext().getResources().getString(R.string.porterIdServerVar),DEF_VALUE);

                if (BASE_URL.length()>0 && (BASE_URL.startsWith(HTTP) || BASE_URL.startsWith(HTTPS))){
                    Uri buildUri = Uri.parse(BASE_URL).buildUpon().appendPath(EMAIL_SERVICE).build();
                    URL url = new URL(buildUri.toString());

                    JSONArray vehiclesFineNotificationArrayJson = new JSONArray();

                    String[] projection_fine = {
                            VehicleEntry._ID,
                            VehicleEntry.COLUMN_APARTMENT_ID,
                            VehicleEntry.COLUMN_ENTRY,
                            VehicleEntry.COLUMN_LICENSE_PLATE,
                            VehicleEntry.COLUMN_EXIT_DATE,
                    };

                    String selection_fine = VehicleEntry.COLUMN_GATEWAY_ID + " = ? AND " + VehicleEntry.COLUMN_FINE_DATE + " IS NULL AND " + VehicleEntry.COLUMN_EXIT_DATE + " IS NULL ";
                    String [] selectionArgs_fine = {String.valueOf(gatewayId)};

                    Cursor vehicles_fine;
                    vehicles_fine = db.query(VehicleEntry.TABLE_NAME,projection_fine,selection_fine,selectionArgs_fine,null,null,null,null);

                    if (vehicles_fine != null && vehicles_fine.getCount()>0){
                        while (vehicles_fine.moveToNext()){
                            if(isAlert){
                                timeExpire = Utility.differenceDateHoursMinutesExtra(dateForServer,vehicles_fine.getString(vehicles_fine.getColumnIndex(VehicleEntry.COLUMN_ENTRY)),hours,minutes);
                            }
                            else{
                                timeExpire = Utility.differenceDateHours(dateForServer,vehicles_fine.getString(vehicles_fine.getColumnIndex(VehicleEntry.COLUMN_ENTRY)),hours);
                            }
                            if(timeExpire){
                                JSONObject vehicleJson = new JSONObject();
                                vehicleJson.put(VehicleEntry._ID,vehicles_fine.getLong(vehicles_fine.getColumnIndex(VehicleEntry._ID)));
                                vehicleJson.put(VehicleEntry.COLUMN_APARTMENT_ID,vehicles_fine.getLong(vehicles_fine.getColumnIndex(VehicleEntry.COLUMN_APARTMENT_ID)));
                                vehicleJson.put(VehicleEntry.COLUMN_ENTRY,vehicles_fine.getString(vehicles_fine.getColumnIndex(VehicleEntry.COLUMN_ENTRY)));
                                vehicleJson.put(VehicleEntry.COLUMN_LICENSE_PLATE,vehicles_fine.getString(vehicles_fine.getColumnIndex(VehicleEntry.COLUMN_LICENSE_PLATE)));
                                vehicleJson.put(VehicleEntry.COLUMN_FINE_DATE,dateForServer);
                                vehicleJson.put(VehicleEntry.COLUMN_FINE_PORTER_ID,porterIdServer);
                                vehiclesFineNotificationArrayJson.put(vehicleJson);
                            }
                        }
                        vehicles_fine.close();
                    }


                    if (vehiclesFineNotificationArrayJson.length()>0){
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.accumulate("building_id",buildingId);
                        jsonObject.accumulate("gateway_id",gatewayId);
                        jsonObject.accumulate("type",SEND_AUTOMATIC);
                        jsonObject.accumulate("emails_cc",emails_cc);

                        if (vehiclesFineNotificationArrayJson.length() != 0){
                            jsonObject.accumulate("vehicles_alert_fine",vehiclesFineNotificationArrayJson);
                        }

                        if (BASE_URL.startsWith(HTTPS) && isNotifications){
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
                            if(isNotifications){
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
                        prepareUpdateAlertFine(jsonObject.toString());
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
            }
        }

    }

    private void prepareUpdateAlertFine(String visitsVehiclesJsonStr) {

        final String VEHICLE_ALERT_FINE = "vehicles_alert_fine";

        try {
            JSONObject returnJson = new JSONObject(visitsVehiclesJsonStr);
            JSONArray vehicle_alert_fine = (returnJson.isNull(VEHICLE_ALERT_FINE)) ? null : returnJson.getJSONArray(VEHICLE_ALERT_FINE);

            Vector<ContentValues> vectorVehiclesAlertFine;
            if (vehicle_alert_fine != null){
                vectorVehiclesAlertFine = new Vector<ContentValues>(vehicle_alert_fine.length());

                for(int i = 0; i < vehicle_alert_fine.length(); i++) {

                    JSONObject returnVehicleJson = vehicle_alert_fine.getJSONObject(i);

                    ContentValues vehiclesValues = new ContentValues();
                    vehiclesValues.put(VehicleEntry._ID,returnVehicleJson.getLong(VehicleEntry._ID));
                    vehiclesValues.put(VehicleEntry.COLUMN_FINE_DATE, returnVehicleJson.getString(VehicleEntry.COLUMN_FINE_DATE));
                    vehiclesValues.put(VehicleEntry.COLUMN_FINE_PORTER_ID, returnVehicleJson.getLong(VehicleEntry.COLUMN_FINE_PORTER_ID));

                    vectorVehiclesAlertFine.add(vehiclesValues);
                }

            }else{
                vectorVehiclesAlertFine = null;
            }

            UpdateSyncVehiclesAlertFine.run(vectorVehiclesAlertFine,getContext());

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }
}
