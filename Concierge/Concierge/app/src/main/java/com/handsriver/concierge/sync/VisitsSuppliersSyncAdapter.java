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
import com.handsriver.concierge.database.updatesTables.UpdateSyncParcels;
import com.handsriver.concierge.database.updatesTables.UpdateSyncSuppliersVisits;

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

public class VisitsSuppliersSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = VisitsSuppliersSyncAdapter.class.getSimpleName();
    public static final String VISITS_SUPPLIERS = "visits_suppliers";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    private Context mContext;
    ConciergeDbHelper dbHelper;
    SQLiteDatabase db;
    public static final String NOT_SYNC = "0";
    public static final String IS_SYNC = "1";
    public static final String NOT_UPDATE = "0";
    public static final String IS_UPDATE = "1";



    public VisitsSuppliersSyncAdapter(Context context, boolean autoInitialize) {
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
        String suppliersVisitsJsonStr;

        final String ID_SUPPLIER_VISIT = "id_supplier_visit";
        final String IS_UPDATE = "is_update";


        try {
            SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            final String BASE_URL = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_URL_key),"");
            final int gatewayId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_gateway_key),"0"));
            final int buildingId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_building_key),"0"));
            final String API_KEY = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_apikey_key),"");

            if (BASE_URL.length()>0 && (BASE_URL.startsWith(HTTP) || BASE_URL.startsWith(HTTPS))){
                Uri buildUri = Uri.parse(BASE_URL).buildUpon().appendPath(VISITS_SUPPLIERS).build();
                URL url = new URL(buildUri.toString());

                JSONArray suppliersVisitsArrayJson = new JSONArray();

                String[] projection = {
                        SupplierVisitsEntry._ID,
                        SupplierVisitsEntry.COLUMN_DOCUMENT_NUMBER,
                        SupplierVisitsEntry.COLUMN_FULL_NAME,
                        SupplierVisitsEntry.COLUMN_GENDER,
                        SupplierVisitsEntry.COLUMN_BIRTHDATE,
                        SupplierVisitsEntry.COLUMN_NATIONALITY,
                        SupplierVisitsEntry.COLUMN_ENTRY,
                        SupplierVisitsEntry.COLUMN_SUPPLIER_ID,
                        SupplierVisitsEntry.COLUMN_EXIT_SUPPLIER,
                        SupplierVisitsEntry.COLUMN_EXIT_OBS,
                        SupplierVisitsEntry.COLUMN_ENTRY_PORTER_ID,
                        SupplierVisitsEntry.COLUMN_EXIT_PORTER_ID,
                        SupplierVisitsEntry.COLUMN_LICENSE_PLATE

                };

                String selection = SupplierVisitsEntry.COLUMN_IS_SYNC + " = ? AND " + SupplierVisitsEntry.COLUMN_GATEWAY_ID + " = ? AND " + SupplierVisitsEntry.COLUMN_IS_UPDATE + " = ? ";
                String [] selectionArgs = {NOT_SYNC,String.valueOf(gatewayId),NOT_UPDATE};

                Cursor suppliers_visits = db.query(SupplierVisitsEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,null,null);

                if (suppliers_visits != null && suppliers_visits.getCount()>0){
                    while (suppliers_visits.moveToNext()){
                        JSONObject suppliersVisitsJson = new JSONObject();
                        suppliersVisitsJson.put(ID_SUPPLIER_VISIT,suppliers_visits.getLong(suppliers_visits.getColumnIndex(SupplierVisitsEntry._ID)));
                        suppliersVisitsJson.put(IS_UPDATE,false);
                        suppliersVisitsJson.put(SupplierVisitsEntry.COLUMN_DOCUMENT_NUMBER,(suppliers_visits.isNull(suppliers_visits.getColumnIndex(SupplierVisitsEntry.COLUMN_DOCUMENT_NUMBER))) ? JSONObject.NULL : suppliers_visits.getString(suppliers_visits.getColumnIndex(SupplierVisitsEntry.COLUMN_DOCUMENT_NUMBER)));
                        suppliersVisitsJson.put(SupplierVisitsEntry.COLUMN_FULL_NAME,(suppliers_visits.isNull(suppliers_visits.getColumnIndex(SupplierVisitsEntry.COLUMN_FULL_NAME))) ? JSONObject.NULL : suppliers_visits.getString(suppliers_visits.getColumnIndex(SupplierVisitsEntry.COLUMN_FULL_NAME)));
                        suppliersVisitsJson.put(SupplierVisitsEntry.COLUMN_GENDER,(suppliers_visits.isNull(suppliers_visits.getColumnIndex(SupplierVisitsEntry.COLUMN_GENDER))) ? JSONObject.NULL : suppliers_visits.getString(suppliers_visits.getColumnIndex(SupplierVisitsEntry.COLUMN_GENDER)));
                        suppliersVisitsJson.put(SupplierVisitsEntry.COLUMN_BIRTHDATE,(suppliers_visits.isNull(suppliers_visits.getColumnIndex(SupplierVisitsEntry.COLUMN_BIRTHDATE))) ? JSONObject.NULL : suppliers_visits.getString(suppliers_visits.getColumnIndex(SupplierVisitsEntry.COLUMN_BIRTHDATE)));
                        suppliersVisitsJson.put(SupplierVisitsEntry.COLUMN_NATIONALITY,(suppliers_visits.isNull(suppliers_visits.getColumnIndex(SupplierVisitsEntry.COLUMN_NATIONALITY))) ? JSONObject.NULL : suppliers_visits.getString(suppliers_visits.getColumnIndex(SupplierVisitsEntry.COLUMN_NATIONALITY)));
                        suppliersVisitsJson.put(SupplierVisitsEntry.COLUMN_ENTRY,suppliers_visits.getString(suppliers_visits.getColumnIndex(SupplierVisitsEntry.COLUMN_ENTRY)));
                        suppliersVisitsJson.put(SupplierVisitsEntry.COLUMN_SUPPLIER_ID,suppliers_visits.getLong(suppliers_visits.getColumnIndex(SupplierVisitsEntry.COLUMN_SUPPLIER_ID)));
                        suppliersVisitsJson.put(SupplierVisitsEntry.COLUMN_EXIT_SUPPLIER,(suppliers_visits.isNull(suppliers_visits.getColumnIndex(SupplierVisitsEntry.COLUMN_EXIT_SUPPLIER))) ? JSONObject.NULL : suppliers_visits.getString(suppliers_visits.getColumnIndex(SupplierVisitsEntry.COLUMN_EXIT_SUPPLIER)));
                        suppliersVisitsJson.put(SupplierVisitsEntry.COLUMN_EXIT_OBS,(suppliers_visits.isNull(suppliers_visits.getColumnIndex(SupplierVisitsEntry.COLUMN_EXIT_OBS))) ? JSONObject.NULL : suppliers_visits.getString(suppliers_visits.getColumnIndex(SupplierVisitsEntry.COLUMN_EXIT_OBS)));

                        suppliersVisitsJson.put(SupplierVisitsEntry.COLUMN_ENTRY_PORTER_ID,suppliers_visits.getLong(suppliers_visits.getColumnIndex(SupplierVisitsEntry.COLUMN_ENTRY_PORTER_ID)));
                        suppliersVisitsJson.put(SupplierVisitsEntry.COLUMN_EXIT_PORTER_ID,(suppliers_visits.isNull(suppliers_visits.getColumnIndex(SupplierVisitsEntry.COLUMN_EXIT_PORTER_ID))) ? JSONObject.NULL : suppliers_visits.getLong(suppliers_visits.getColumnIndex(SupplierVisitsEntry.COLUMN_EXIT_PORTER_ID)));
                        suppliersVisitsJson.put(SupplierVisitsEntry.COLUMN_LICENSE_PLATE,(suppliers_visits.isNull(suppliers_visits.getColumnIndex(SupplierVisitsEntry.COLUMN_LICENSE_PLATE))) ? JSONObject.NULL : suppliers_visits.getString(suppliers_visits.getColumnIndex(SupplierVisitsEntry.COLUMN_LICENSE_PLATE)));

                        suppliersVisitsArrayJson.put(suppliersVisitsJson);
                    }
                    suppliers_visits.close();
                }

                String[] projection_u = {
                        SupplierVisitsEntry._ID,
                        SupplierVisitsEntry.COLUMN_DOCUMENT_NUMBER,
                        SupplierVisitsEntry.COLUMN_FULL_NAME,
                        SupplierVisitsEntry.COLUMN_ENTRY,
                        SupplierVisitsEntry.COLUMN_SUPPLIER_ID,
                        SupplierVisitsEntry.COLUMN_EXIT_SUPPLIER,
                        SupplierVisitsEntry.COLUMN_EXIT_PORTER_ID,
                        SupplierVisitsEntry.COLUMN_EXIT_OBS,
                        SupplierVisitsEntry.COLUMN_SUPPLIER_VISIT_ID_SERVER
                };


                String selection_u = SupplierVisitsEntry.COLUMN_IS_SYNC + " = ? AND " + SupplierVisitsEntry.COLUMN_IS_UPDATE + " = ? AND " + SupplierVisitsEntry.COLUMN_EXIT_SUPPLIER + " IS NOT NULL ";
                String [] selectionArgs_u = {IS_SYNC,NOT_UPDATE};
                Cursor suppliers_visits_update = db.query(SupplierVisitsEntry.TABLE_NAME,projection_u,selection_u,selectionArgs_u,null,null,null,null);

                if (suppliers_visits_update != null && suppliers_visits_update.getCount()>0){
                    while (suppliers_visits_update.moveToNext()){
                        JSONObject suppliersVisitsUpdateJson = new JSONObject();
                        suppliersVisitsUpdateJson.put(ID_SUPPLIER_VISIT,suppliers_visits_update.getLong(suppliers_visits_update.getColumnIndex(SupplierVisitsEntry._ID)));
                        suppliersVisitsUpdateJson.put(IS_UPDATE,true);
                        suppliersVisitsUpdateJson.put(SupplierVisitsEntry.COLUMN_DOCUMENT_NUMBER,(suppliers_visits_update.isNull(suppliers_visits_update.getColumnIndex(SupplierVisitsEntry.COLUMN_DOCUMENT_NUMBER))) ? JSONObject.NULL : suppliers_visits_update.getString(suppliers_visits_update.getColumnIndex(SupplierVisitsEntry.COLUMN_DOCUMENT_NUMBER)));
                        suppliersVisitsUpdateJson.put(SupplierVisitsEntry.COLUMN_FULL_NAME,(suppliers_visits_update.isNull(suppliers_visits_update.getColumnIndex(SupplierVisitsEntry.COLUMN_FULL_NAME))) ? JSONObject.NULL : suppliers_visits_update.getString(suppliers_visits_update.getColumnIndex(SupplierVisitsEntry.COLUMN_FULL_NAME)));
                        suppliersVisitsUpdateJson.put(SupplierVisitsEntry.COLUMN_ENTRY,suppliers_visits_update.getString(suppliers_visits_update.getColumnIndex(SupplierVisitsEntry.COLUMN_ENTRY)));
                        suppliersVisitsUpdateJson.put(SupplierVisitsEntry.COLUMN_SUPPLIER_ID,suppliers_visits_update.getLong(suppliers_visits_update.getColumnIndex(SupplierVisitsEntry.COLUMN_SUPPLIER_ID)));
                        suppliersVisitsUpdateJson.put(SupplierVisitsEntry.COLUMN_EXIT_SUPPLIER,(suppliers_visits_update.isNull(suppliers_visits_update.getColumnIndex(SupplierVisitsEntry.COLUMN_EXIT_SUPPLIER))) ? JSONObject.NULL : suppliers_visits_update.getString(suppliers_visits_update.getColumnIndex(SupplierVisitsEntry.COLUMN_EXIT_SUPPLIER)));
                        suppliersVisitsUpdateJson.put(SupplierVisitsEntry.COLUMN_EXIT_OBS,(suppliers_visits_update.isNull(suppliers_visits_update.getColumnIndex(SupplierVisitsEntry.COLUMN_EXIT_OBS))) ? JSONObject.NULL : suppliers_visits_update.getString(suppliers_visits_update.getColumnIndex(SupplierVisitsEntry.COLUMN_EXIT_OBS)));
                        suppliersVisitsUpdateJson.put(SupplierVisitsEntry.COLUMN_EXIT_PORTER_ID,(suppliers_visits_update.isNull(suppliers_visits_update.getColumnIndex(SupplierVisitsEntry.COLUMN_EXIT_PORTER_ID))) ? JSONObject.NULL : suppliers_visits_update.getLong(suppliers_visits_update.getColumnIndex(SupplierVisitsEntry.COLUMN_EXIT_PORTER_ID)));
                        suppliersVisitsUpdateJson.put(SupplierVisitsEntry.COLUMN_SUPPLIER_VISIT_ID_SERVER,(suppliers_visits_update.isNull(suppliers_visits_update.getColumnIndex(SupplierVisitsEntry.COLUMN_SUPPLIER_VISIT_ID_SERVER))) ? JSONObject.NULL : suppliers_visits_update.getLong(suppliers_visits_update.getColumnIndex(SupplierVisitsEntry.COLUMN_SUPPLIER_VISIT_ID_SERVER)));

                        suppliersVisitsArrayJson.put(suppliersVisitsUpdateJson);
                    }
                    suppliers_visits_update.close();
                }

                if ((suppliers_visits != null && suppliers_visits.getCount()>0) || (suppliers_visits_update != null && suppliers_visits_update.getCount()>0)){
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.accumulate("building_id",buildingId);
                    jsonObject.accumulate("gateway_id",gatewayId);
                    if (suppliersVisitsArrayJson.length() != 0){
                        jsonObject.accumulate("suppliers_visits",suppliersVisitsArrayJson);
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
                        suppliersVisitsJsonStr = buffer.toString();
                        getSuppliersVisitsDataFromJson(suppliersVisitsJsonStr);
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
                        suppliersVisitsJsonStr = buffer.toString();
                        getSuppliersVisitsDataFromJson(suppliersVisitsJsonStr);
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

    private void getSuppliersVisitsDataFromJson(String suppliersVisitsJsonStr)
            throws JSONException {


        final String SUPPLIER_VISIT_RETURN = "supplier_visit_return";
        final String SUPPLIER_VISIT_UPDATE_RETURN = "supplier_visit_update_return";
        final String ID_SUPPLIER_VISIT = "id_supplier_visit";
        final String INSERT_DATA = "insert_data";
        final String ID_SERVER = "id_server";
        final String UPDATE_DATA = "update_data";

        try {
            JSONObject returnJson = new JSONObject(suppliersVisitsJsonStr);
            JSONArray supplierVisitsReturn = (returnJson.isNull(SUPPLIER_VISIT_RETURN)) ? null : returnJson.getJSONArray(SUPPLIER_VISIT_RETURN);
            JSONArray suppliersVisitsUpdateReturn = (returnJson.isNull(SUPPLIER_VISIT_UPDATE_RETURN)) ? null : returnJson.getJSONArray(SUPPLIER_VISIT_UPDATE_RETURN);

            Vector<ContentValues> vectorSuppliersVisitsReturn;
            if (supplierVisitsReturn != null){
                vectorSuppliersVisitsReturn = new Vector<ContentValues>(supplierVisitsReturn.length());

                for(int i = 0; i < supplierVisitsReturn.length(); i++) {

                    long id_supplier_visit;
                    boolean insert_data;
                    long id_server;

                    JSONObject returnSupplierVisitJson = supplierVisitsReturn.getJSONObject(i);

                    id_supplier_visit = returnSupplierVisitJson.getLong(ID_SUPPLIER_VISIT);
                    insert_data = returnSupplierVisitJson.getBoolean(INSERT_DATA);

                    ContentValues supplierVisitValues = new ContentValues();
                    if (insert_data){
                        supplierVisitValues.put(SupplierVisitsEntry.COLUMN_IS_SYNC, IS_SYNC);
                        id_server = returnSupplierVisitJson.getLong(ID_SERVER);
                        supplierVisitValues.put(SupplierVisitsEntry.COLUMN_SUPPLIER_VISIT_ID_SERVER,id_server);
                    }
                    else{
                        supplierVisitValues.put(SupplierVisitsEntry.COLUMN_IS_SYNC, NOT_SYNC);
                    }
                    supplierVisitValues.put(SupplierVisitsEntry._ID, id_supplier_visit);

                    vectorSuppliersVisitsReturn.add(supplierVisitValues);
                }
            }else{
                vectorSuppliersVisitsReturn = null;
            }

            Vector<ContentValues> vectorSuppliersVisitsUpdateReturn;
            if (suppliersVisitsUpdateReturn != null){
                vectorSuppliersVisitsUpdateReturn = new Vector<ContentValues>(suppliersVisitsUpdateReturn.length());

                for(int i = 0; i < suppliersVisitsUpdateReturn.length(); i++) {

                    long id_supplier_visit;
                    boolean update_data;

                    JSONObject returnSupplierVisitUpdateJson = suppliersVisitsUpdateReturn.getJSONObject(i);

                    id_supplier_visit = returnSupplierVisitUpdateJson.getLong(ID_SUPPLIER_VISIT);
                    update_data = returnSupplierVisitUpdateJson.getBoolean(UPDATE_DATA);

                    ContentValues suppliersVisitUpdateValues = new ContentValues();
                    if (update_data){
                        suppliersVisitUpdateValues.put(SupplierVisitsEntry.COLUMN_IS_UPDATE, IS_UPDATE);
                    }
                    else{
                        suppliersVisitUpdateValues.put(SupplierVisitsEntry.COLUMN_IS_UPDATE, NOT_UPDATE);
                    }
                    suppliersVisitUpdateValues.put(SupplierVisitsEntry._ID, id_supplier_visit);

                    vectorSuppliersVisitsUpdateReturn.add(suppliersVisitUpdateValues);
                }
            }
            else{
                vectorSuppliersVisitsUpdateReturn = null;
            }

            UpdateSyncSuppliersVisits.run(vectorSuppliersVisitsReturn,vectorSuppliersVisitsUpdateReturn);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

}
