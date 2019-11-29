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
import com.handsriver.concierge.database.ConciergeContract.SupplierVisitsEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.InsertUpdateTables.IUSuppliersVisitsOthersGateways;
import com.handsriver.concierge.database.InsertUpdateTables.IUTimekeepingOthersGateways;
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

public class VisitsSuppliersOthersGatewaysSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = VisitsSuppliersOthersGatewaysSyncAdapter.class.getSimpleName();
    public static final String SUPPLIERS_VISITS = "supplier_visits_list";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    private Context mContext;
    ConciergeDbHelper dbHelper;
    SQLiteDatabase db;



    public VisitsSuppliersOthersGatewaysSyncAdapter(Context context, boolean autoInitialize) {
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

        String suppliersVisitsOtherGatewaysJsonStr;

        try {
            SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            final String BASE_URL = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_URL_key),"");
            final int gatewayId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_gateway_key),"0"));
            final int buildingId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_building_key),"0"));
            final String API_KEY = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_apikey_key),"");
            String search_date;

            if (BASE_URL.length()>0 && (BASE_URL.startsWith(HTTP) || BASE_URL.startsWith(HTTPS))){
                String[] projection = {
                        SupplierVisitsEntry._ID
                };

                String selection = SupplierVisitsEntry.COLUMN_GATEWAY_ID + " != ?";
                String [] selectionArgs = {String.valueOf(gatewayId)};

                Cursor  suppliersVisitsOthersGateways;

                suppliersVisitsOthersGateways = db.query(SupplierVisitsEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,null,null);

                if (suppliersVisitsOthersGateways != null && suppliersVisitsOthersGateways.getCount()>0){
                    search_date = Utility.getDateSimpleForServer1Days();
                    suppliersVisitsOthersGateways.close();
                }
                else{
                    search_date = Utility.getDateSimpleForServer7Days();
                }

                Uri buildUri = Uri.parse(BASE_URL).buildUpon().appendPath(SUPPLIERS_VISITS).build();
                URL url = new URL(buildUri.toString());

                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("building_id",buildingId);
                jsonObject.accumulate("gateway_id",gatewayId);
                jsonObject.accumulate("search_date",search_date);

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
                    suppliersVisitsOtherGatewaysJsonStr = buffer.toString();
                    getSuppliersVisitsOthersGatewaysDataFromJson(suppliersVisitsOtherGatewaysJsonStr);
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
                    suppliersVisitsOtherGatewaysJsonStr = buffer.toString();
                    getSuppliersVisitsOthersGatewaysDataFromJson(suppliersVisitsOtherGatewaysJsonStr);
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

    private void getSuppliersVisitsOthersGatewaysDataFromJson(String suppliersVisitsOtherGatewaysJsonStr)
            throws JSONException {

        final String ID_SUPPLIER_VISIT = "id_supplier_visit";
        final int IS_SYNC = 1;
        final int NOT_UPDATE = 0;
        final int IS_UPDATE = 1;



        try {
            JSONArray suppliersVisitsOthersGatewaysArray = new JSONArray(suppliersVisitsOtherGatewaysJsonStr);

            Vector<ContentValues> cVVector = new Vector<ContentValues>(suppliersVisitsOthersGatewaysArray.length());

            for(int i = 0; i < suppliersVisitsOthersGatewaysArray.length(); i++) {

                long id_supplier_visit;
                String document_number;
                String full_name;
                String nationality;
                String gender;
                String birthdate;
                String license_plate;
                String entry;
                String exit_supplier;
                long entry_porter_id;
                long exit_porter_id;
                long gateway_id;
                long supplier_id;

                JSONObject suppliersVisitsOthersGatewaysJson = suppliersVisitsOthersGatewaysArray.getJSONObject(i);

                id_supplier_visit = suppliersVisitsOthersGatewaysJson.getLong(ID_SUPPLIER_VISIT);
                document_number = (suppliersVisitsOthersGatewaysJson.isNull(SupplierVisitsEntry.COLUMN_DOCUMENT_NUMBER)) ? null : suppliersVisitsOthersGatewaysJson.getString(SupplierVisitsEntry.COLUMN_DOCUMENT_NUMBER);
                full_name = (suppliersVisitsOthersGatewaysJson.isNull(SupplierVisitsEntry.COLUMN_FULL_NAME)) ? null : suppliersVisitsOthersGatewaysJson.getString(SupplierVisitsEntry.COLUMN_FULL_NAME);
                nationality = (suppliersVisitsOthersGatewaysJson.isNull(SupplierVisitsEntry.COLUMN_NATIONALITY)) ? null : suppliersVisitsOthersGatewaysJson.getString(SupplierVisitsEntry.COLUMN_NATIONALITY);
                gender = (suppliersVisitsOthersGatewaysJson.isNull(SupplierVisitsEntry.COLUMN_GENDER)) ? null : suppliersVisitsOthersGatewaysJson.getString(SupplierVisitsEntry.COLUMN_GENDER);
                birthdate = (suppliersVisitsOthersGatewaysJson.isNull(SupplierVisitsEntry.COLUMN_BIRTHDATE)) ? null : suppliersVisitsOthersGatewaysJson.getString(SupplierVisitsEntry.COLUMN_BIRTHDATE);
                license_plate = (suppliersVisitsOthersGatewaysJson.isNull(SupplierVisitsEntry.COLUMN_LICENSE_PLATE)) ? null : suppliersVisitsOthersGatewaysJson.getString(SupplierVisitsEntry.COLUMN_LICENSE_PLATE);
                entry = suppliersVisitsOthersGatewaysJson.getString(SupplierVisitsEntry.COLUMN_ENTRY);
                exit_supplier = (suppliersVisitsOthersGatewaysJson.isNull(SupplierVisitsEntry.COLUMN_EXIT_SUPPLIER)) ? null : suppliersVisitsOthersGatewaysJson.getString(SupplierVisitsEntry.COLUMN_EXIT_SUPPLIER);
                gateway_id = suppliersVisitsOthersGatewaysJson.getLong(SupplierVisitsEntry.COLUMN_GATEWAY_ID);
                supplier_id = suppliersVisitsOthersGatewaysJson.getLong(SupplierVisitsEntry.COLUMN_SUPPLIER_ID);
                entry_porter_id = suppliersVisitsOthersGatewaysJson.getLong(SupplierVisitsEntry.COLUMN_ENTRY_PORTER_ID);


                ContentValues supplierVisitValues = new ContentValues();

                supplierVisitValues.put(SupplierVisitsEntry.COLUMN_SUPPLIER_VISIT_ID_SERVER, id_supplier_visit);
                supplierVisitValues.put(SupplierVisitsEntry.COLUMN_DOCUMENT_NUMBER, document_number);
                supplierVisitValues.put(SupplierVisitsEntry.COLUMN_FULL_NAME, full_name);
                supplierVisitValues.put(SupplierVisitsEntry.COLUMN_NATIONALITY, nationality);
                supplierVisitValues.put(SupplierVisitsEntry.COLUMN_GENDER, gender);
                supplierVisitValues.put(SupplierVisitsEntry.COLUMN_BIRTHDATE, birthdate);
                supplierVisitValues.put(SupplierVisitsEntry.COLUMN_LICENSE_PLATE, license_plate);
                supplierVisitValues.put(SupplierVisitsEntry.COLUMN_ENTRY, entry);
                supplierVisitValues.put(SupplierVisitsEntry.COLUMN_EXIT_SUPPLIER, exit_supplier);
                supplierVisitValues.put(SupplierVisitsEntry.COLUMN_GATEWAY_ID, gateway_id);
                supplierVisitValues.put(SupplierVisitsEntry.COLUMN_SUPPLIER_ID, supplier_id);
                supplierVisitValues.put(SupplierVisitsEntry.COLUMN_ENTRY_PORTER_ID, entry_porter_id);
                if (!suppliersVisitsOthersGatewaysJson.isNull(SupplierVisitsEntry.COLUMN_EXIT_PORTER_ID)){
                    exit_porter_id = suppliersVisitsOthersGatewaysJson.getLong(SupplierVisitsEntry.COLUMN_EXIT_PORTER_ID);
                    supplierVisitValues.put(SupplierVisitsEntry.COLUMN_EXIT_PORTER_ID, exit_porter_id);
                }
                supplierVisitValues.put(SupplierVisitsEntry.COLUMN_IS_SYNC,IS_SYNC);

                if(exit_supplier == null){
                    supplierVisitValues.put(SupplierVisitsEntry.COLUMN_IS_UPDATE,NOT_UPDATE);
                }
                else{
                    supplierVisitValues.put(SupplierVisitsEntry.COLUMN_IS_UPDATE,IS_UPDATE);

                }

                cVVector.add(supplierVisitValues);
            }


            IUSuppliersVisitsOthersGateways.run(cVVector);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

}
