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
import com.handsriver.concierge.database.ConciergeContract.CommonspaceVisitsEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.updatesTables.UpdateSyncCommonspacesVisits;

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

public class VisitsCommonspacesSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = VisitsCommonspacesSyncAdapter.class.getSimpleName();
    public static final String VISITS_COMMONSPACES = "visits_commonspaces";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    private Context mContext;
    ConciergeDbHelper dbHelper;
    SQLiteDatabase db;
    public static final String NOT_SYNC = "0";
    public static final String IS_SYNC = "1";
    public static final String NOT_UPDATE = "0";
    public static final String IS_UPDATE = "1";



    public VisitsCommonspacesSyncAdapter(Context context, boolean autoInitialize) {
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
        String commonspacesVisitsJsonStr;

        final String ID_COMMONSPACE_VISIT = "id_commonspace_visit";
        final String IS_UPDATE = "is_update";


        try {
            SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            final String BASE_URL = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_URL_key),"");
            final int gatewayId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_gateway_key),"0"));
            final int buildingId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_building_key),"0"));
            final String API_KEY = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_apikey_key),"");

            if (BASE_URL.length()>0 && (BASE_URL.startsWith(HTTP) || BASE_URL.startsWith(HTTPS))){
                Uri buildUri = Uri.parse(BASE_URL).buildUpon().appendPath(VISITS_COMMONSPACES).build();
                URL url = new URL(buildUri.toString());

                JSONArray commonspacesVisitsArrayJson = new JSONArray();

                String[] projection = {
                        CommonspaceVisitsEntry._ID,
                        CommonspaceVisitsEntry.COLUMN_DOCUMENT_NUMBER,
                        CommonspaceVisitsEntry.COLUMN_FULL_NAME,
                        CommonspaceVisitsEntry.COLUMN_GENDER,
                        CommonspaceVisitsEntry.COLUMN_BIRTHDATE,
                        CommonspaceVisitsEntry.COLUMN_NATIONALITY,
                        CommonspaceVisitsEntry.COLUMN_ENTRY,
                        CommonspaceVisitsEntry.COLUMN_COMMONSPACE_ID,
                        CommonspaceVisitsEntry.COLUMN_APARTMENT_ID,
                        CommonspaceVisitsEntry.COLUMN_EXIT_VISIT,
                        //CommonspaceVisitsEntry.COLUMN_EXIT_OBS,
                        CommonspaceVisitsEntry.COLUMN_ENTRY_PORTER_ID,
                        CommonspaceVisitsEntry.COLUMN_EXIT_PORTER_ID,
                        CommonspaceVisitsEntry.COLUMN_LICENSE_PLATE

                };

                String selection = CommonspaceVisitsEntry.COLUMN_IS_SYNC + " = ? AND " + CommonspaceVisitsEntry.COLUMN_GATEWAY_ID + " = ? AND " + CommonspaceVisitsEntry.COLUMN_IS_UPDATE + " = ? ";
                String [] selectionArgs = {NOT_SYNC,String.valueOf(gatewayId),NOT_UPDATE};

                Cursor commonspaces_visits = db.query(CommonspaceVisitsEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,null,null);

                if (commonspaces_visits != null && commonspaces_visits.getCount()>0){
                    while (commonspaces_visits.moveToNext()){
                        JSONObject commonspacesVisitsJson = new JSONObject();
                        commonspacesVisitsJson.put(ID_COMMONSPACE_VISIT,commonspaces_visits.getLong(commonspaces_visits.getColumnIndex(CommonspaceVisitsEntry._ID)));
                        commonspacesVisitsJson.put(IS_UPDATE,false);
                        commonspacesVisitsJson.put(CommonspaceVisitsEntry.COLUMN_DOCUMENT_NUMBER,(commonspaces_visits.isNull(commonspaces_visits.getColumnIndex(CommonspaceVisitsEntry.COLUMN_DOCUMENT_NUMBER))) ? JSONObject.NULL : commonspaces_visits.getString(commonspaces_visits.getColumnIndex(CommonspaceVisitsEntry.COLUMN_DOCUMENT_NUMBER)));
                        commonspacesVisitsJson.put(CommonspaceVisitsEntry.COLUMN_FULL_NAME,(commonspaces_visits.isNull(commonspaces_visits.getColumnIndex(CommonspaceVisitsEntry.COLUMN_FULL_NAME))) ? JSONObject.NULL : commonspaces_visits.getString(commonspaces_visits.getColumnIndex(CommonspaceVisitsEntry.COLUMN_FULL_NAME)));
                        commonspacesVisitsJson.put(CommonspaceVisitsEntry.COLUMN_GENDER,(commonspaces_visits.isNull(commonspaces_visits.getColumnIndex(CommonspaceVisitsEntry.COLUMN_GENDER))) ? JSONObject.NULL : commonspaces_visits.getString(commonspaces_visits.getColumnIndex(CommonspaceVisitsEntry.COLUMN_GENDER)));
                        commonspacesVisitsJson.put(CommonspaceVisitsEntry.COLUMN_BIRTHDATE,(commonspaces_visits.isNull(commonspaces_visits.getColumnIndex(CommonspaceVisitsEntry.COLUMN_BIRTHDATE))) ? JSONObject.NULL : commonspaces_visits.getString(commonspaces_visits.getColumnIndex(CommonspaceVisitsEntry.COLUMN_BIRTHDATE)));
                        commonspacesVisitsJson.put(CommonspaceVisitsEntry.COLUMN_NATIONALITY,(commonspaces_visits.isNull(commonspaces_visits.getColumnIndex(CommonspaceVisitsEntry.COLUMN_NATIONALITY))) ? JSONObject.NULL : commonspaces_visits.getString(commonspaces_visits.getColumnIndex(CommonspaceVisitsEntry.COLUMN_NATIONALITY)));
                        commonspacesVisitsJson.put(CommonspaceVisitsEntry.COLUMN_ENTRY,commonspaces_visits.getString(commonspaces_visits.getColumnIndex(CommonspaceVisitsEntry.COLUMN_ENTRY)));
                        commonspacesVisitsJson.put(CommonspaceVisitsEntry.COLUMN_COMMONSPACE_ID,commonspaces_visits.getLong(commonspaces_visits.getColumnIndex(CommonspaceVisitsEntry.COLUMN_COMMONSPACE_ID)));
                        commonspacesVisitsJson.put(CommonspaceVisitsEntry.COLUMN_EXIT_VISIT,(commonspaces_visits.isNull(commonspaces_visits.getColumnIndex(CommonspaceVisitsEntry.COLUMN_EXIT_VISIT))) ? JSONObject.NULL : commonspaces_visits.getString(commonspaces_visits.getColumnIndex(CommonspaceVisitsEntry.COLUMN_EXIT_VISIT)));
                        //suppliersVisitsJson.put(CommonspaceVisitsEntry.COLUMN_EXIT_OBS,(commonspaces_visits.isNull(suppliers_visits.getColumnIndex(CommonspaceVisitsEntry.COLUMN_EXIT_OBS))) ? JSONObject.NULL : suppliers_visits.getString(suppliers_visits.getColumnIndex(CommonspaceVisitsEntry.COLUMN_EXIT_OBS)));

                        commonspacesVisitsJson.put(CommonspaceVisitsEntry.COLUMN_ENTRY_PORTER_ID,commonspaces_visits.getLong(commonspaces_visits.getColumnIndex(CommonspaceVisitsEntry.COLUMN_ENTRY_PORTER_ID)));
                        commonspacesVisitsJson.put(CommonspaceVisitsEntry.COLUMN_APARTMENT_ID,commonspaces_visits.getLong(commonspaces_visits.getColumnIndex(CommonspaceVisitsEntry.COLUMN_APARTMENT_ID)));
                        commonspacesVisitsJson.put(CommonspaceVisitsEntry.COLUMN_EXIT_PORTER_ID,(commonspaces_visits.isNull(commonspaces_visits.getColumnIndex(CommonspaceVisitsEntry.COLUMN_EXIT_PORTER_ID))) ? JSONObject.NULL : commonspaces_visits.getLong(commonspaces_visits.getColumnIndex(CommonspaceVisitsEntry.COLUMN_EXIT_PORTER_ID)));
                        commonspacesVisitsJson.put(CommonspaceVisitsEntry.COLUMN_LICENSE_PLATE,(commonspaces_visits.isNull(commonspaces_visits.getColumnIndex(CommonspaceVisitsEntry.COLUMN_LICENSE_PLATE))) ? JSONObject.NULL : commonspaces_visits.getString(commonspaces_visits.getColumnIndex(CommonspaceVisitsEntry.COLUMN_LICENSE_PLATE)));

                        commonspacesVisitsArrayJson.put(commonspacesVisitsJson);
                    }
                    commonspaces_visits.close();
                }

                String[] projection_u = {
                        CommonspaceVisitsEntry._ID,
                        CommonspaceVisitsEntry.COLUMN_DOCUMENT_NUMBER,
                        CommonspaceVisitsEntry.COLUMN_FULL_NAME,
                        CommonspaceVisitsEntry.COLUMN_ENTRY,
                        CommonspaceVisitsEntry.COLUMN_COMMONSPACE_ID,
                        CommonspaceVisitsEntry.COLUMN_EXIT_VISIT,
                        CommonspaceVisitsEntry.COLUMN_EXIT_PORTER_ID,
                        CommonspaceVisitsEntry.COLUMN_APARTMENT_ID,
                        //CommonspaceVisitsEntry.COLUMN_EXIT_OBS,
                        CommonspaceVisitsEntry.COLUMN_COMMONSPACE_VISIT_ID_SERVER
                };


                String selection_u = CommonspaceVisitsEntry.COLUMN_IS_SYNC + " = ? AND " + CommonspaceVisitsEntry.COLUMN_IS_UPDATE + " = ? AND " + CommonspaceVisitsEntry.COLUMN_EXIT_VISIT + " IS NOT NULL ";
                String [] selectionArgs_u = {IS_SYNC,NOT_UPDATE};
                Cursor commonspaces_visits_update = db.query(CommonspaceVisitsEntry.TABLE_NAME,projection_u,selection_u,selectionArgs_u,null,null,null,null);

                if (commonspaces_visits_update != null && commonspaces_visits_update.getCount()>0){
                    while (commonspaces_visits_update.moveToNext()){
                        JSONObject commonspacesVisitsUpdateJson = new JSONObject();
                        commonspacesVisitsUpdateJson.put(ID_COMMONSPACE_VISIT,commonspaces_visits_update.getLong(commonspaces_visits_update.getColumnIndex(CommonspaceVisitsEntry._ID)));
                        commonspacesVisitsUpdateJson.put(IS_UPDATE,true);
                        commonspacesVisitsUpdateJson.put(CommonspaceVisitsEntry.COLUMN_DOCUMENT_NUMBER,(commonspaces_visits_update.isNull(commonspaces_visits_update.getColumnIndex(CommonspaceVisitsEntry.COLUMN_DOCUMENT_NUMBER))) ? JSONObject.NULL : commonspaces_visits_update.getString(commonspaces_visits_update.getColumnIndex(CommonspaceVisitsEntry.COLUMN_DOCUMENT_NUMBER)));
                        commonspacesVisitsUpdateJson.put(CommonspaceVisitsEntry.COLUMN_FULL_NAME,(commonspaces_visits_update.isNull(commonspaces_visits_update.getColumnIndex(CommonspaceVisitsEntry.COLUMN_FULL_NAME))) ? JSONObject.NULL : commonspaces_visits_update.getString(commonspaces_visits_update.getColumnIndex(CommonspaceVisitsEntry.COLUMN_FULL_NAME)));
                        commonspacesVisitsUpdateJson.put(CommonspaceVisitsEntry.COLUMN_ENTRY,commonspaces_visits_update.getString(commonspaces_visits_update.getColumnIndex(CommonspaceVisitsEntry.COLUMN_ENTRY)));
                        commonspacesVisitsUpdateJson.put(CommonspaceVisitsEntry.COLUMN_COMMONSPACE_ID,commonspaces_visits_update.getLong(commonspaces_visits_update.getColumnIndex(CommonspaceVisitsEntry.COLUMN_COMMONSPACE_ID)));
                        commonspacesVisitsUpdateJson.put(CommonspaceVisitsEntry.COLUMN_EXIT_VISIT,(commonspaces_visits_update.isNull(commonspaces_visits_update.getColumnIndex(CommonspaceVisitsEntry.COLUMN_EXIT_VISIT))) ? JSONObject.NULL : commonspaces_visits_update.getString(commonspaces_visits_update.getColumnIndex(CommonspaceVisitsEntry.COLUMN_EXIT_VISIT)));
                        //commonspacesVisitsUpdateJson.put(CommonspaceVisitsEntry.COLUMN_EXIT_OBS,(commonspaces_visits_update.isNull(commonspaces_visits_update.getColumnIndex(CommonspaceVisitsEntry.COLUMN_EXIT_OBS))) ? JSONObject.NULL : commonspaces_visits_update.getString(commonspaces_visits_update.getColumnIndex(CommonspaceVisitsEntry.COLUMN_EXIT_OBS)));
                        commonspacesVisitsUpdateJson.put(CommonspaceVisitsEntry.COLUMN_APARTMENT_ID,(commonspaces_visits_update.isNull(commonspaces_visits_update.getColumnIndex(CommonspaceVisitsEntry.COLUMN_APARTMENT_ID))) ? JSONObject.NULL : commonspaces_visits_update.getLong(commonspaces_visits_update.getColumnIndex(CommonspaceVisitsEntry.COLUMN_APARTMENT_ID)));
                        commonspacesVisitsUpdateJson.put(CommonspaceVisitsEntry.COLUMN_EXIT_PORTER_ID,(commonspaces_visits_update.isNull(commonspaces_visits_update.getColumnIndex(CommonspaceVisitsEntry.COLUMN_EXIT_PORTER_ID))) ? JSONObject.NULL : commonspaces_visits_update.getLong(commonspaces_visits_update.getColumnIndex(CommonspaceVisitsEntry.COLUMN_EXIT_PORTER_ID)));
                        commonspacesVisitsUpdateJson.put(CommonspaceVisitsEntry.COLUMN_COMMONSPACE_VISIT_ID_SERVER,(commonspaces_visits_update.isNull(commonspaces_visits_update.getColumnIndex(CommonspaceVisitsEntry.COLUMN_COMMONSPACE_VISIT_ID_SERVER))) ? JSONObject.NULL : commonspaces_visits_update.getLong(commonspaces_visits_update.getColumnIndex(CommonspaceVisitsEntry.COLUMN_COMMONSPACE_VISIT_ID_SERVER)));

                        commonspacesVisitsArrayJson.put(commonspacesVisitsUpdateJson);
                    }
                    commonspaces_visits_update.close();
                }

                if ((commonspaces_visits != null && commonspaces_visits.getCount()>0) || (commonspaces_visits_update != null && commonspaces_visits_update.getCount()>0)){
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.accumulate("building_id",buildingId);
                    jsonObject.accumulate("gateway_id",gatewayId);
                    if (commonspacesVisitsArrayJson.length() != 0){
                        jsonObject.accumulate("commonspaces_visits",commonspacesVisitsArrayJson);
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
                        commonspacesVisitsJsonStr = buffer.toString();
                        getCommonspacesVisitsDataFromJson(commonspacesVisitsJsonStr);
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
                        commonspacesVisitsJsonStr = buffer.toString();
                        getCommonspacesVisitsDataFromJson(commonspacesVisitsJsonStr);
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

    private void getCommonspacesVisitsDataFromJson(String commonspacesVisitsJsonStr)
            throws JSONException {


        final String COMMONSPACE_VISIT_RETURN = "commonspace_visit_return";
        final String COMMONSPACE_VISIT_UPDATE_RETURN = "commonspace_visit_update_return";
        final String ID_COMMONSPACE_VISIT = "id_commonspace_visit";
        final String INSERT_DATA = "insert_data";
        final String ID_SERVER = "id_server";
        final String UPDATE_DATA = "update_data";

        try {
            JSONObject returnJson = new JSONObject(commonspacesVisitsJsonStr);
            JSONArray commonspaceVisitsReturn = (returnJson.isNull(COMMONSPACE_VISIT_RETURN)) ? null : returnJson.getJSONArray(COMMONSPACE_VISIT_RETURN);
            JSONArray commonspacesVisitsUpdateReturn = (returnJson.isNull(COMMONSPACE_VISIT_UPDATE_RETURN)) ? null : returnJson.getJSONArray(COMMONSPACE_VISIT_UPDATE_RETURN);

            Vector<ContentValues> vectorCommonspacesVisitsReturn;
            if (commonspaceVisitsReturn != null){
                vectorCommonspacesVisitsReturn = new Vector<ContentValues>(commonspaceVisitsReturn.length());

                for(int i = 0; i < commonspaceVisitsReturn.length(); i++) {

                    long id_commonspace_visit;
                    boolean insert_data;
                    long id_server;

                    JSONObject returnSupplierVisitJson = commonspaceVisitsReturn.getJSONObject(i);

                    id_commonspace_visit = returnSupplierVisitJson.getLong(ID_COMMONSPACE_VISIT);
                    insert_data = returnSupplierVisitJson.getBoolean(INSERT_DATA);

                    ContentValues commonspaceVisitValues = new ContentValues();
                    if (insert_data){
                        commonspaceVisitValues.put(CommonspaceVisitsEntry.COLUMN_IS_SYNC, IS_SYNC);
                        id_server = returnSupplierVisitJson.getLong(ID_SERVER);
                        commonspaceVisitValues.put(CommonspaceVisitsEntry.COLUMN_COMMONSPACE_VISIT_ID_SERVER,id_server);
                    }
                    else{
                        commonspaceVisitValues.put(CommonspaceVisitsEntry.COLUMN_IS_SYNC, NOT_SYNC);
                    }
                    commonspaceVisitValues.put(CommonspaceVisitsEntry._ID, id_commonspace_visit);

                    vectorCommonspacesVisitsReturn.add(commonspaceVisitValues);
                }
            }else{
                vectorCommonspacesVisitsReturn = null;
            }

            Vector<ContentValues> vectorCommonspacesVisitsUpdateReturn;
            if (commonspacesVisitsUpdateReturn != null){
                vectorCommonspacesVisitsUpdateReturn = new Vector<ContentValues>(commonspacesVisitsUpdateReturn.length());

                for(int i = 0; i < commonspacesVisitsUpdateReturn.length(); i++) {

                    long id_commonspace_visit;
                    boolean update_data;

                    JSONObject returnCommonspaceVisitUpdateJson = commonspacesVisitsUpdateReturn.getJSONObject(i);

                    id_commonspace_visit = returnCommonspaceVisitUpdateJson.getLong(ID_COMMONSPACE_VISIT);
                    update_data = returnCommonspaceVisitUpdateJson.getBoolean(UPDATE_DATA);

                    ContentValues commonspacesVisitUpdateValues = new ContentValues();
                    if (update_data){
                        commonspacesVisitUpdateValues.put(CommonspaceVisitsEntry.COLUMN_IS_UPDATE, IS_UPDATE);
                    }
                    else{
                        commonspacesVisitUpdateValues.put(CommonspaceVisitsEntry.COLUMN_IS_UPDATE, NOT_UPDATE);
                    }
                    commonspacesVisitUpdateValues.put(CommonspaceVisitsEntry._ID, id_commonspace_visit);

                    vectorCommonspacesVisitsUpdateReturn.add(commonspacesVisitUpdateValues);
                }
            }
            else{
                vectorCommonspacesVisitsUpdateReturn = null;
            }

            UpdateSyncCommonspacesVisits.run(vectorCommonspacesVisitsReturn,vectorCommonspacesVisitsUpdateReturn);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

}
