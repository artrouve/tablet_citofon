package com.handsriver.concierge.push_notifications;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.handsriver.concierge.database.ConciergeContract.ResidentEntry;
import com.handsriver.concierge.database.ConciergeContract.ParcelEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;

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

/**
 * Created by Created by alain_r._trouve_silva after 27-06-17.
 */

public class SendPushNotifications {

    public static String LOG_TAG = SendPushNotifications.class.getSimpleName();
    public static String TITLE = "Encomienda recibida";
    public static String BODY = "Tiene una encomienda para retiro";
    public static String DEFAULT = "default";
    //public static String AUTHORIZATION = "key=AAAAxi1rbFQ:APA91bFhYpJ33K5P95EkWiLfyyMrUbn1NS1nu89_UVaxXv4iwdelHUXo3qPKJZIAm1xu0RybZBJiLSQ5AvURcJuQhVpQX5jO1MdMDItTozrceTMjt9NHxwCZ_0MRTPdaCGUj9JwqECSR";
    public static String AUTHORIZATION = "key=AAAAGnxBHCk:APA91bGg1yFAt95j8oGx1BObyh13y2RNoal0doHZxUku3ZdE1JS5QFWC-sbMus8PpVIblWTxyi4L6vpHFKHx0vMTm6pAHvw6I2sKFFSG-dZMwdePe_njMsfSDXCvTUfGkSZ6lHEFwHyJ";

    public static String ENABLE_PUSH_NOTIFICATION = "1";



    public static void sendPushNotification(String id_parcel){
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        final String BASE_URL = "https://fcm.googleapis.com/fcm/send";
        SQLiteDatabase db;
        db = DatabaseManager.getInstance().openDatabase();
        JSONArray jsonArrayRegistrationIds = new JSONArray();
        String response;

        try {
            Uri buildUri = Uri.parse(BASE_URL).buildUpon().build();

            URL url = new URL(buildUri.toString());

            String[] projection = {
                    ParcelEntry._ID,
                    ParcelEntry.COLUMN_APARTMENT_ID

            };

            String selection = ParcelEntry._ID + " = ?";
            String [] selectionArgs = {id_parcel};
            Cursor parcel = db.query(ParcelEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,null,null);

            if (parcel != null && parcel.getCount()>0){
                if (parcel.moveToNext()){
                    String[] projection_resident = {
                            ResidentEntry._ID,
                            ResidentEntry.COLUMN_TOKEN

                    };
                    String selection_resident = ResidentEntry.COLUMN_APARTMENT_ID + " = ? AND " + ResidentEntry.COLUMN_PUSH_NOTIFICATIONS + " = ? AND " + ResidentEntry.COLUMN_TOKEN + " IS NOT NULL";
                    String [] selectionArgs_resident = {parcel.getString(parcel.getColumnIndex(ParcelEntry.COLUMN_APARTMENT_ID)),ENABLE_PUSH_NOTIFICATION};
                    Cursor residents = db.query(ResidentEntry.TABLE_NAME,projection_resident,selection_resident,selectionArgs_resident,null,null,null,null);

                    if (residents != null && residents.getCount()>0){
                        while (residents.moveToNext()){
                            jsonArrayRegistrationIds.put(residents.getString(residents.getColumnIndex(ResidentEntry.COLUMN_TOKEN)));
                        }
                        residents.close();
                    }
                }
                parcel.close();
            }

            if (jsonArrayRegistrationIds.length() > 0 ){

                JSONObject jsonSend = new JSONObject();
                JSONObject jsonNotification = new JSONObject();
                jsonNotification.accumulate("title",TITLE);
                jsonNotification.accumulate("body",BODY);
                jsonNotification.accumulate("sound",DEFAULT);
                jsonSend.accumulate("notification",jsonNotification);

                if (jsonArrayRegistrationIds.length() == 1){
                    jsonSend.accumulate("to",jsonArrayRegistrationIds.get(0));
                }
                else{
                    jsonSend.accumulate("registration_ids",jsonArrayRegistrationIds);
                }

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type","application/json");
                urlConnection.setRequestProperty("Authorization",AUTHORIZATION);
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setConnectTimeout(5000);
                urlConnection.connect();

                OutputStream dataOutputStream = new BufferedOutputStream(urlConnection.getOutputStream());
                OutputStreamWriter osw = new OutputStreamWriter(dataOutputStream, "UTF-8");
                osw.write(jsonSend.toString());
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
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);

        } finally {
            DatabaseManager.getInstance().closeDatabase();
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
