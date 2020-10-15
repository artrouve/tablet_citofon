package com.handsriver.concierge.database.updatesTables;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.handsriver.concierge.database.ConciergeContract.ParcelEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.sync.ConfigureSyncAccount;

import java.util.ArrayList;

/**
 * Created by Created by alain_r._trouve_silva after 23-03-17.
 */

public class UpdateExitParcel extends AsyncTask<Void,Void,Integer> {
    private SQLiteDatabase db;
    private String exit;
    private int porterId;
    private String id;
    private String documentNumber;
    private String fullName;
    private Context mContext;

    private static final String TAG = "UpdateExitParcel";

    public UpdateExitParcel(String exit, int porterId, String id, String fullName, String documentNumber, Context mContext){
        this.exit = exit;
        this.porterId = porterId;
        this.id = id;
        this.fullName = fullName;
        this.documentNumber = documentNumber;
        this.mContext = mContext;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        String tableName = ParcelEntry.TABLE_NAME;
        int count = 0;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(ParcelEntry.COLUMN_EXIT_PARCEL,exit);
            values.put(ParcelEntry.COLUMN_EXIT_PARCEL_PORTER_ID, porterId);
            if (fullName.length() != 0){
                values.put(ParcelEntry.COLUMN_EXIT_FULLNAME,fullName);
            }
            if (documentNumber.length() != 0){
                values.put(ParcelEntry.COLUMN_EXIT_DOCUMENT_NUMBER,documentNumber);
            }

            String whereClause = ParcelEntry._ID + " = ?";
            String [] whereArgs = {id};

            count = db.update(tableName,values,whereClause,whereArgs);

            ConfigureSyncAccount.syncImmediatelyParcels(mContext);
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
