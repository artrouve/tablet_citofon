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
import com.handsriver.concierge.database.updatesTables.UpdateSyncSuppliersVisitsExitOtherGateways;
import com.handsriver.concierge.database.updatesTables.UpdateSyncTimekeepingExitOtherGateways;

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

public class VisitsSuppliersExitOthersGatewaysSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = VisitsSuppliersExitOthersGatewaysSyncAdapter.class.getSimpleName();
    public static final String SUPPLIERS_VISITS = "supplier_visits_exit";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    private Context mContext;
    ConciergeDbHelper dbHelper;
    SQLiteDatabase db;

    public static final String NOT_UPDATE = "0";


    public VisitsSuppliersExitOthersGatewaysSyncAdapter(Context context, boolean autoInitialize) {
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

        String suppliersVisitsExitOtherGatewaysJsonStr;

        final String ID_SUPPLIER_VISIT = "id_supplier_visit";


        try {
            SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            final String BASE_URL = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_URL_key),"");
            final int gatewayId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_gateway_key),"0"));
            final int buildingId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_building_key),"0"));
            final String API_KEY = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_apikey_key),"");

            if (BASE_URL.length()>0 && (BASE_URL.startsWith(HTTP) || BASE_URL.startsWith(HTTPS))){
                String[] projection = {
                        SupplierVisitsEntry._ID,
                        SupplierVisitsEntry.COLUMN_SUPPLIER_ID,
                        SupplierVisitsEntry.COLUMN_ENTRY,
                        SupplierVisitsEntry.COLUMN_DOCUMENT_NUMBER,
                        SupplierVisitsEntry.COLUMN_FULL_NAME,
                        SupplierVisitsEntry.COLUMN_SUPPLIER_VISIT_ID_SERVER
                };

                String selection = SupplierVisitsEntry.COLUMN_EXIT_SUPPLIER + " IS NULL AND " + SupplierVisitsEntry.COLUMN_IS_UPDATE + " = ? ";
                String [] selectionArgs = {NOT_UPDATE};

                Cursor suppliers_visits;

                suppliers_visits = db.query(SupplierVisitsEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,null,null);

                JSONArray suppliersVisitExitArrayJson = new JSONArray();

                if (suppliers_visits != null && suppliers_visits.getCount()>0){
                    while (suppliers_visits.moveToNext()){
                        JSONObject suppliersVisitExitJson = new JSONObject();
                        suppliersVisitExitJson.put(ID_SUPPLIER_VISIT,suppliers_visits.getLong(suppliers_visits.getColumnIndex(SupplierVisitsEntry._ID)));
                        suppliersVisitExitJson.put(SupplierVisitsEntry.COLUMN_SUPPLIER_ID,suppliers_visits.getLong(suppliers_visits.getColumnIndex(SupplierVisitsEntry.COLUMN_SUPPLIER_ID)));
                        suppliersVisitExitJson.put(SupplierVisitsEntry.COLUMN_ENTRY,suppliers_visits.getString(suppliers_visits.getColumnIndex(SupplierVisitsEntry.COLUMN_ENTRY)));
                        suppliersVisitExitJson.put(SupplierVisitsEntry.COLUMN_DOCUMENT_NUMBER,(suppliers_visits.isNull(suppliers_visits.getColumnIndex(SupplierVisitsEntry.COLUMN_DOCUMENT_NUMBER))) ? JSONObject.NULL : suppliers_visits.getString(suppliers_visits.getColumnIndex(SupplierVisitsEntry.COLUMN_DOCUMENT_NUMBER)));
                        suppliersVisitExitJson.put(SupplierVisitsEntry.COLUMN_FULL_NAME,(suppliers_visits.isNull(suppliers_visits.getColumnIndex(SupplierVisitsEntry.COLUMN_FULL_NAME))) ? JSONObject.NULL : suppliers_visits.getString(suppliers_visits.getColumnIndex(SupplierVisitsEntry.COLUMN_FULL_NAME)));
                        suppliersVisitExitJson.put(SupplierVisitsEntry.COLUMN_SUPPLIER_VISIT_ID_SERVER,(suppliers_visits.isNull(suppliers_visits.getColumnIndex(SupplierVisitsEntry.COLUMN_SUPPLIER_VISIT_ID_SERVER))) ? JSONObject.NULL : suppliers_visits.getLong(suppliers_visits.getColumnIndex(SupplierVisitsEntry.COLUMN_SUPPLIER_VISIT_ID_SERVER)));
                        suppliersVisitExitArrayJson.put(suppliersVisitExitJson);
                    }
                    suppliers_visits.close();
                }

                if ((suppliers_visits != null && suppliers_visits.getCount()>0)){
                    Uri buildUri = Uri.parse(BASE_URL).buildUpon().appendPath(SUPPLIERS_VISITS).build();
                    URL url = new URL(buildUri.toString());

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.accumulate("building_id",buildingId);
                    jsonObject.accumulate("suppliers_check",suppliersVisitExitArrayJson);

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
                        suppliersVisitsExitOtherGatewaysJsonStr = buffer.toString();
                        getSuppliersVisitsExitOthersGatewaysDataFromJson(suppliersVisitsExitOtherGatewaysJsonStr);
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
                        suppliersVisitsExitOtherGatewaysJsonStr = buffer.toString();
                        getSuppliersVisitsExitOthersGatewaysDataFromJson(suppliersVisitsExitOtherGatewaysJsonStr);
                    }

                }
            }

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
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

    private void getSuppliersVisitsExitOthersGatewaysDataFromJson(String supplierVisitsExitOtherGatewaysJsonStr)
            throws JSONException {

        final String ID_SUPPLIER_VISIT = "id_supplier_visit";
        final String SUPPLIERS_VISITS_EXIT_RETURN = "supplier_visit_exit_return";

        try {
            JSONObject returnJson = new JSONObject(supplierVisitsExitOtherGatewaysJsonStr);
            JSONArray suppliersVisitsExitOthersGatewaysArray = (returnJson.isNull(SUPPLIERS_VISITS_EXIT_RETURN)) ? null : returnJson.getJSONArray(SUPPLIERS_VISITS_EXIT_RETURN);

            Vector<ContentValues> cVVector;

            if (suppliersVisitsExitOthersGatewaysArray != null){
                cVVector = new Vector<ContentValues>(suppliersVisitsExitOthersGatewaysArray.length());

                for(int i = 0; i < suppliersVisitsExitOthersGatewaysArray.length(); i++) {

                    long id_supplier_visit;
                    String exit_supplier;
                    long exit_porter_id;

                    JSONObject suppliersVisitsExitJson = suppliersVisitsExitOthersGatewaysArray.getJSONObject(i);

                    id_supplier_visit = suppliersVisitsExitJson.getLong(ID_SUPPLIER_VISIT);
                    exit_supplier = (suppliersVisitsExitJson.isNull(SupplierVisitsEntry.COLUMN_EXIT_SUPPLIER)) ? null : suppliersVisitsExitJson.getString(SupplierVisitsEntry.COLUMN_EXIT_SUPPLIER);

                    ContentValues suppliersVisitsExitValues = new ContentValues();

                    suppliersVisitsExitValues.put(SupplierVisitsEntry._ID, id_supplier_visit);
                    suppliersVisitsExitValues.put(SupplierVisitsEntry.COLUMN_EXIT_SUPPLIER, exit_supplier);
                    if(!suppliersVisitsExitJson.isNull(SupplierVisitsEntry.COLUMN_EXIT_PORTER_ID)){
                        exit_porter_id = suppliersVisitsExitJson.getLong(SupplierVisitsEntry.COLUMN_EXIT_PORTER_ID);
                        suppliersVisitsExitValues.put(SupplierVisitsEntry.COLUMN_EXIT_PORTER_ID, exit_porter_id);

                    }

                    cVVector.add(suppliersVisitsExitValues);
                }
            }
            else{
                cVVector = null;
            }

            UpdateSyncSuppliersVisitsExitOtherGateways.run(cVVector);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

}
