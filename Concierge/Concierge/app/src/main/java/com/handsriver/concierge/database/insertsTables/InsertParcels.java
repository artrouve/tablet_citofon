package com.handsriver.concierge.database.insertsTables;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.handsriver.concierge.database.ConciergeContract.*;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;

/**
 * Created by Created by alain_r._trouve_silva after 03-03-17.
 */

public class InsertParcels extends AsyncTask<Void,Void,Long> {
    private SQLiteDatabase db;
    private String nullColumnHack;
    private String observations;
    private String entry;
    private String fullName;
    private int apartmentId;
    private int gatewayId;
    private int entryPorterId;


    private static final String TAG = "InsertParcels";
    private static final int IS_SYNC = 0;
    private static final int IS_UPDATE = 0;


    public InsertParcels(String nullColumnHack, String observations, String entry, int apartmentId, int gatewayId, int entryPorterId, String fullName){
        this.nullColumnHack = nullColumnHack;
        this.observations = observations;
        this.entry = entry;
        this.apartmentId = apartmentId;
        this.gatewayId = gatewayId;
        this.entryPorterId = entryPorterId;
        this.fullName = fullName;
    }

    @Override
    protected Long doInBackground(Void... params) {
        String tableName = ParcelEntry.TABLE_NAME;
        db = DatabaseManager.getInstance().openDatabase();
        long id = 0;
        try {
            ContentValues values = new ContentValues();
                values.put(ParcelEntry.COLUMN_APARTMENT_ID,apartmentId);
                values.put(ParcelEntry.COLUMN_ENTRY_PARCEL,entry);
                values.put(ParcelEntry.COLUMN_ENTRY_PARCEL_PORTER_ID,entryPorterId);
                values.put(ParcelEntry.COLUMN_FULL_NAME,fullName);
                values.put(ParcelEntry.COLUMN_OBSERVATIONS,observations);
                values.put(ParcelEntry.COLUMN_GATEWAY_ID,gatewayId);
                values.put(ParcelEntry.COLUMN_IS_SYNC,IS_SYNC);
                values.put(ParcelEntry.COLUMN_IS_UPDATE,IS_UPDATE);

                id = db.insert(tableName,nullColumnHack,values);
        } catch (SQLException e){
            Log.e(TAG, "SQLiteException:" + e.getMessage());
            DatabaseManager.getInstance().closeDatabase();
        }
        return id;
    }

    @Override
    protected void onPostExecute(Long aLong) {
        DatabaseManager.getInstance().closeDatabase();
        super.onPostExecute(aLong);
    }
}
