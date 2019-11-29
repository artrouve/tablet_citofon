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
import com.handsriver.concierge.database.ConciergeContract.PaymentEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.updatesTables.UpdateSyncParcels;
import com.handsriver.concierge.database.updatesTables.UpdateSyncPayments;
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

public class PaymentsSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = PaymentsSyncAdapter.class.getSimpleName();
    public static final String PAYMENTS = "payments";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    private Context mContext;
    ConciergeDbHelper dbHelper;
    SQLiteDatabase db;
    public static final String NOT_SYNC = "0";
    public static final String IS_SYNC = "1";
    public static final String NOT_UPDATE = "0";
    public static final String IS_UPDATE = "1";



    public PaymentsSyncAdapter(Context context, boolean autoInitialize) {
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
        String paymentsJsonStr;

        final String ID_PAYMENT = "id_payment";


        try {
            SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            final String BASE_URL = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_URL_key),"");
            final int gatewayId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_gateway_key),"0"));
            final int buildingId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_building_key),"0"));
            final String API_KEY = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_apikey_key),"");

            if (BASE_URL.length()>0 && (BASE_URL.startsWith(HTTP) || BASE_URL.startsWith(HTTPS))){
                Uri buildUri = Uri.parse(BASE_URL).buildUpon().appendPath(PAYMENTS).build();
                URL url = new URL(buildUri.toString());

                JSONArray paymentsArrayJson = new JSONArray();

                String[] projection = {
                        PaymentEntry._ID,
                        PaymentEntry.COLUMN_PAYMENT_TYPE,
                        PaymentEntry.COLUMN_APARTMENT_ID,
                        PaymentEntry.COLUMN_NUMBER_TRX,
                        PaymentEntry.COLUMN_NUMBER_RECEIPT,
                        PaymentEntry.COLUMN_DATE_REGISTER,
                        PaymentEntry.COLUMN_PORTER_ID,
                        PaymentEntry.COLUMN_AMOUNT
                };

                String selection = PaymentEntry.COLUMN_IS_SYNC + " = ? AND " + PaymentEntry.COLUMN_GATEWAY_ID + " = ? ";
                String [] selectionArgs = {NOT_SYNC,String.valueOf(gatewayId)};

                Cursor payments = db.query(PaymentEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,null,null);

                if (payments != null && payments.getCount()>0){
                    while (payments.moveToNext()){
                        JSONObject paymentsJson = new JSONObject();
                        paymentsJson.put(ID_PAYMENT,payments.getLong(payments.getColumnIndex(PaymentEntry._ID)));
                        paymentsJson.put(PaymentEntry.COLUMN_PAYMENT_TYPE,payments.getInt(payments.getColumnIndex(PaymentEntry.COLUMN_PAYMENT_TYPE)));
                        paymentsJson.put(PaymentEntry.COLUMN_APARTMENT_ID,payments.getLong(payments.getColumnIndex(PaymentEntry.COLUMN_APARTMENT_ID)));
                        paymentsJson.put(PaymentEntry.COLUMN_NUMBER_TRX,(payments.isNull(payments.getColumnIndex(PaymentEntry.COLUMN_NUMBER_TRX))) ? JSONObject.NULL : payments.getString(payments.getColumnIndex(PaymentEntry.COLUMN_NUMBER_TRX)));
                        paymentsJson.put(PaymentEntry.COLUMN_NUMBER_RECEIPT,(payments.isNull(payments.getColumnIndex(PaymentEntry.COLUMN_NUMBER_RECEIPT))) ? JSONObject.NULL : payments.getString(payments.getColumnIndex(PaymentEntry.COLUMN_NUMBER_RECEIPT)));
                        paymentsJson.put(PaymentEntry.COLUMN_DATE_REGISTER,payments.getString(payments.getColumnIndex(PaymentEntry.COLUMN_DATE_REGISTER)));
                        paymentsJson.put(PaymentEntry.COLUMN_PORTER_ID,payments.getLong(payments.getColumnIndex(PaymentEntry.COLUMN_PORTER_ID)));
                        paymentsJson.put(PaymentEntry.COLUMN_AMOUNT,payments.getLong(payments.getColumnIndex(PaymentEntry.COLUMN_AMOUNT)));

                        paymentsArrayJson.put(paymentsJson);
                    }
                    payments.close();
                }

                if ((payments != null && payments.getCount()>0)){
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.accumulate("building_id",buildingId);
                    jsonObject.accumulate("gateway_id",gatewayId);
                    if (paymentsArrayJson.length() != 0){
                        jsonObject.accumulate("payments",paymentsArrayJson);
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
                        paymentsJsonStr = buffer.toString();
                        getPaymentsDataFromJson(paymentsJsonStr);
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
                        paymentsJsonStr = buffer.toString();
                        getPaymentsDataFromJson(paymentsJsonStr);
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

    private void getPaymentsDataFromJson(String paymentsJsonStr)
            throws JSONException {


        final String PAYMENTS_RETURN = "payment_return";
        final String ID_PAYMENT = "id_payment";
        final String INSERT_DATA = "insert_data";
        final String ID_SERVER = "id_server";

        try {
            JSONObject returnJson = new JSONObject(paymentsJsonStr);
            JSONArray paymentsReturn = (returnJson.isNull(PAYMENTS_RETURN)) ? null : returnJson.getJSONArray(PAYMENTS_RETURN);

            Vector<ContentValues> vectorPaymentsReturn;
            if (paymentsReturn != null){
                vectorPaymentsReturn = new Vector<ContentValues>(paymentsReturn.length());

                for(int i = 0; i < paymentsReturn.length(); i++) {

                    long id_payment;
                    boolean insert_data;
                    long id_server;

                    JSONObject returnPaymentJson = paymentsReturn.getJSONObject(i);

                    id_payment = returnPaymentJson.getLong(ID_PAYMENT);
                    insert_data = returnPaymentJson.getBoolean(INSERT_DATA);

                    ContentValues paymentsValues = new ContentValues();
                    if (insert_data){
                        paymentsValues.put(PaymentEntry.COLUMN_IS_SYNC, IS_SYNC);
                        id_server = returnPaymentJson.getLong(ID_SERVER);
                        paymentsValues.put(PaymentEntry.COLUMN_PAYMENT_ID_SERVER,id_server);
                    }
                    else{
                        paymentsValues.put(PaymentEntry.COLUMN_IS_SYNC, NOT_SYNC);
                    }
                    paymentsValues.put(PaymentEntry._ID, id_payment);

                    vectorPaymentsReturn.add(paymentsValues);
                }
            }else{
                vectorPaymentsReturn = null;
            }

            UpdateSyncPayments.run(vectorPaymentsReturn);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

}
