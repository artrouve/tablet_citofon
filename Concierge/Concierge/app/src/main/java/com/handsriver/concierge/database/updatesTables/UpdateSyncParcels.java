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
import com.handsriver.concierge.push_notifications.SendPushNotifications;

import java.util.Vector;

/**
 * Created by Created by alain_r._trouve_silva after 23-03-17.
 */

public class UpdateSyncParcels {

    private static final String TAG = "UpdateSyncParcels";
    private static final String IS_SYNC = "1";
    private static final String IS_UPDATE = "1";

    public static void run(Vector<ContentValues> vectorParcelsReturn, Vector<ContentValues> vectorParcelsUpdateReturn, Context mContext) {
        SQLiteDatabase db;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            if (vectorParcelsReturn != null){
                for (ContentValues parcels:vectorParcelsReturn){
                    if (parcels.getAsString(ParcelEntry.COLUMN_IS_SYNC).equals(IS_SYNC)){
                        int count;

                        String whereClause = ParcelEntry._ID + " = ? AND " + ParcelEntry.COLUMN_EXIT_PARCEL + " IS NOT NULL";
                        String [] whereArgs = {parcels.getAsString(ParcelEntry._ID)};
                        parcels.put(ParcelEntry.COLUMN_IS_UPDATE,IS_UPDATE);
                        count = db.update(ParcelEntry.TABLE_NAME,parcels,whereClause,whereArgs);

                        if (count == 0){
                            String whereClause1 = ParcelEntry._ID + " = ?";
                            String [] whereArgs1 = {parcels.getAsString(ParcelEntry._ID)};
                            parcels.remove(ParcelEntry.COLUMN_IS_UPDATE);
                            db.update(ParcelEntry.TABLE_NAME,parcels,whereClause1,whereArgs1);
                            SendPushNotifications.sendPushNotification(parcels.getAsString(ParcelEntry._ID));
                        }
                    }
                }
            }

            if (vectorParcelsUpdateReturn != null) {
                for (ContentValues parcels_update:vectorParcelsUpdateReturn){
                    if (parcels_update.getAsString(ParcelEntry.COLUMN_IS_UPDATE).equals(IS_UPDATE)){
                        String whereClause = ParcelEntry._ID + " = ? AND " + ParcelEntry.COLUMN_EXIT_PARCEL + " IS NOT NULL";
                        String [] whereArgs = {parcels_update.getAsString(ParcelEntry._ID)};
                        db.update(ParcelEntry.TABLE_NAME,parcels_update,whereClause,whereArgs);
                    }
                }
            }

        } catch (SQLException e){
            Log.e(TAG, "SQLiteException:" + e.getMessage());
            DatabaseManager.getInstance().closeDatabase();
        }
        DatabaseManager.getInstance().closeDatabase();
    }

}
