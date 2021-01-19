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
import com.handsriver.concierge.database.ConciergeContract;
import com.handsriver.concierge.database.ConciergeContract.CommonspaceVisitsEntry;

import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
//import com.handsriver.concierge.database.updatesTables.UpdateSyncSuppliersVisitsExitOtherGateways;
import com.handsriver.concierge.database.updatesTables.UpdateSyncCommonspacesVisitsExitOtherGateways;


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

public class VisitsCommonspacesExitOthersGatewaysSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = VisitsCommonspacesExitOthersGatewaysSyncAdapter.class.getSimpleName();
    public static final String COMMONSPACES_VISITS = "commonspace_visits_exit";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    private Context mContext;
    ConciergeDbHelper dbHelper;
    SQLiteDatabase db;

    public static final String NOT_UPDATE = "0";


    public VisitsCommonspacesExitOthersGatewaysSyncAdapter(Context context, boolean autoInitialize) {
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

        String commonspacesVisitsExitOtherGatewaysJsonStr;

        final String ID_COMMONSPACE_VISIT = "id_commonspace_visit";


        try {
            SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            final String BASE_URL = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_URL_key),"");
            final int gatewayId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_gateway_key),"0"));
            final int buildingId = Integer.parseInt(settingsPrefs.getString(mContext.getResources().getString(R.string.pref_id_building_key),"0"));
            final String API_KEY = settingsPrefs.getString(mContext.getResources().getString(R.string.pref_apikey_key),"");

            if (BASE_URL.length()>0 && (BASE_URL.startsWith(HTTP) || BASE_URL.startsWith(HTTPS))){
                String[] projection = {
                        ConciergeContract.CommonspaceVisitsEntry._ID,
                        ConciergeContract.CommonspaceVisitsEntry.COLUMN_COMMONSPACE_ID,
                        ConciergeContract.CommonspaceVisitsEntry.COLUMN_ENTRY,
                        ConciergeContract.CommonspaceVisitsEntry.COLUMN_DOCUMENT_NUMBER,
                        ConciergeContract.CommonspaceVisitsEntry.COLUMN_FULL_NAME,
                        ConciergeContract.CommonspaceVisitsEntry.COLUMN_APARTMENT_ID,
                        ConciergeContract.CommonspaceVisitsEntry.COLUMN_COMMONSPACE_VISIT_ID_SERVER
                };

                String selection = CommonspaceVisitsEntry.COLUMN_EXIT_VISIT + " IS NULL AND " + CommonspaceVisitsEntry.COLUMN_IS_UPDATE + " = ? ";
                String [] selectionArgs = {NOT_UPDATE};

                Cursor commonspaces_visits;

                commonspaces_visits = db.query(CommonspaceVisitsEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,null,null);

                JSONArray commonspacesVisitExitArrayJson = new JSONArray();

                if (commonspaces_visits != null && commonspaces_visits.getCount()>0){
                    while (commonspaces_visits.moveToNext()){
                        JSONObject commonspacesVisitExitJson = new JSONObject();
                        commonspacesVisitExitJson.put(ID_COMMONSPACE_VISIT,commonspaces_visits.getLong(commonspaces_visits.getColumnIndex(CommonspaceVisitsEntry._ID)));
                        commonspacesVisitExitJson.put(CommonspaceVisitsEntry.COLUMN_COMMONSPACE_ID,commonspaces_visits.getLong(commonspaces_visits.getColumnIndex(CommonspaceVisitsEntry.COLUMN_COMMONSPACE_ID)));
                        commonspacesVisitExitJson.put(CommonspaceVisitsEntry.COLUMN_ENTRY,commonspaces_visits.getString(commonspaces_visits.getColumnIndex(CommonspaceVisitsEntry.COLUMN_ENTRY)));
                        commonspacesVisitExitJson.put(CommonspaceVisitsEntry.COLUMN_APARTMENT_ID,commonspaces_visits.getString(commonspaces_visits.getColumnIndex(CommonspaceVisitsEntry.COLUMN_APARTMENT_ID)));
                        commonspacesVisitExitJson.put(CommonspaceVisitsEntry.COLUMN_DOCUMENT_NUMBER,(commonspaces_visits.isNull(commonspaces_visits.getColumnIndex(CommonspaceVisitsEntry.COLUMN_DOCUMENT_NUMBER))) ? JSONObject.NULL : commonspaces_visits.getString(commonspaces_visits.getColumnIndex(CommonspaceVisitsEntry.COLUMN_DOCUMENT_NUMBER)));
                        commonspacesVisitExitJson.put(CommonspaceVisitsEntry.COLUMN_FULL_NAME,(commonspaces_visits.isNull(commonspaces_visits.getColumnIndex(CommonspaceVisitsEntry.COLUMN_FULL_NAME))) ? JSONObject.NULL : commonspaces_visits.getString(commonspaces_visits.getColumnIndex(CommonspaceVisitsEntry.COLUMN_FULL_NAME)));
                        commonspacesVisitExitJson.put(CommonspaceVisitsEntry.COLUMN_COMMONSPACE_VISIT_ID_SERVER,(commonspaces_visits.isNull(commonspaces_visits.getColumnIndex(CommonspaceVisitsEntry.COLUMN_COMMONSPACE_VISIT_ID_SERVER))) ? JSONObject.NULL : commonspaces_visits.getLong(commonspaces_visits.getColumnIndex(CommonspaceVisitsEntry.COLUMN_COMMONSPACE_VISIT_ID_SERVER)));
                        commonspacesVisitExitArrayJson.put(commonspacesVisitExitJson);
                    }
                    commonspaces_visits.close();
                }

                if ((commonspaces_visits != null && commonspaces_visits.getCount()>0)){
                    Uri buildUri = Uri.parse(BASE_URL).buildUpon().appendPath(COMMONSPACES_VISITS).build();
                    URL url = new URL(buildUri.toString());

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.accumulate("building_id",buildingId);
                    jsonObject.accumulate("commonspaces_check",commonspacesVisitExitArrayJson);

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
                        commonspacesVisitsExitOtherGatewaysJsonStr = buffer.toString();
                        getCommonspacesVisitsExitOthersGatewaysDataFromJson(commonspacesVisitsExitOtherGatewaysJsonStr);
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
                        commonspacesVisitsExitOtherGatewaysJsonStr = buffer.toString();
                        getCommonspacesVisitsExitOthersGatewaysDataFromJson(commonspacesVisitsExitOtherGatewaysJsonStr);
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

    private void getCommonspacesVisitsExitOthersGatewaysDataFromJson(String commonspaceVisitsExitOtherGatewaysJsonStr)
            throws JSONException {

        final String ID_COMMONSPACE_VISIT = "id_commonspace_visit";
        final String COMMONSPACES_VISITS_EXIT_RETURN = "commonspace_visit_exit_return";

        try {
            JSONObject returnJson = new JSONObject(commonspaceVisitsExitOtherGatewaysJsonStr);
            JSONArray commonspacesVisitsExitOthersGatewaysArray = (returnJson.isNull(COMMONSPACES_VISITS_EXIT_RETURN)) ? null : returnJson.getJSONArray(COMMONSPACES_VISITS_EXIT_RETURN);

            Vector<ContentValues> cVVector;

            if (commonspacesVisitsExitOthersGatewaysArray != null){
                cVVector = new Vector<ContentValues>(commonspacesVisitsExitOthersGatewaysArray.length());

                for(int i = 0; i < commonspacesVisitsExitOthersGatewaysArray.length(); i++) {

                    long id_commonspace_visit;
                    String exit_visit;
                    long exit_porter_id;

                    JSONObject commonspacesVisitsExitJson = commonspacesVisitsExitOthersGatewaysArray.getJSONObject(i);

                    id_commonspace_visit = commonspacesVisitsExitJson.getLong(ID_COMMONSPACE_VISIT);
                    exit_visit = (commonspacesVisitsExitJson.isNull(CommonspaceVisitsEntry.COLUMN_EXIT_VISIT)) ? null : commonspacesVisitsExitJson.getString(CommonspaceVisitsEntry.COLUMN_EXIT_VISIT);

                    ContentValues commonspacesVisitsExitValues = new ContentValues();

                    commonspacesVisitsExitValues.put(CommonspaceVisitsEntry._ID, id_commonspace_visit);
                    commonspacesVisitsExitValues.put(CommonspaceVisitsEntry.COLUMN_EXIT_VISIT, exit_visit);
                    if(!commonspacesVisitsExitJson.isNull(CommonspaceVisitsEntry.COLUMN_EXIT_PORTER_ID)){
                        exit_porter_id = commonspacesVisitsExitJson.getLong(CommonspaceVisitsEntry.COLUMN_EXIT_PORTER_ID);
                        commonspacesVisitsExitValues.put(CommonspaceVisitsEntry.COLUMN_EXIT_PORTER_ID, exit_porter_id);

                    }

                    cVVector.add(commonspacesVisitsExitValues);
                }
            }
            else{
                cVVector = null;
            }

            UpdateSyncCommonspacesVisitsExitOtherGateways.run(cVVector);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

}
