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
import com.handsriver.concierge.database.ConciergeContract;
import com.handsriver.concierge.database.ConciergeContract.VisitEntry;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.sync.ConfigureSyncAccount;
import com.handsriver.concierge.visits.Visit;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
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

public class UpdateExitVisit extends AsyncTask<Void,Void,Integer> {
    private SQLiteDatabase db;
    private String exit;

    private int porterId;
    private String id;
    private String button;

    private Context mContext;

    private static final String TAG = "UpdateExitVisit";
    public static final String EXIT = "exit";

    public static final String LOG_TAG = UpdateExitVisit.class.getSimpleName();
    public static final String HTTP = "http";
    public static final String HTTPS = "https";


    public UpdateExitVisit(String exit, int porterId, String id, String button, Context mContext){
        this.exit = exit;
        this.porterId = porterId;
        this.id = id;
        this.button = button;
        this.mContext = mContext;

    }

    @Override
    protected Integer doInBackground(Void... params) {
        String tableName = VisitEntry.TABLE_NAME;

        int count = 0;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(VisitEntry.COLUMN_EXIT_DATE,exit);
            values.put(VisitEntry.COLUMN_EXIT_PORTER_ID, porterId);
            String whereClause = VisitEntry._ID + " = ?";
            String [] whereArgs = {id};

            count = db.update(tableName,values,whereClause,whereArgs);

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


}
