package com.handsriver.concierge.database.updatesTables;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.handsriver.concierge.database.ConciergeContract.ParcelEntry;
import com.handsriver.concierge.database.DatabaseManager;

import java.util.Vector;

/**
 * Created by Created by alain_r._trouve_silva after 23-03-17.
 */

public class UpdateSyncParcelsExitOtherGateways {
    private static final String TAG = "UpdateSyncParcel";
    private static final String IS_UPDATE = "1";

    public static void run(Vector<ContentValues> vectorParcelsExitOthersGatewaysReturn) {
        SQLiteDatabase db;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            if (vectorParcelsExitOthersGatewaysReturn != null){
                for (ContentValues parcel:vectorParcelsExitOthersGatewaysReturn){
                    if(parcel.getAsString(ParcelEntry.COLUMN_EXIT_PARCEL) != null){
                        String whereClause = ParcelEntry._ID + " = ? ";
                        String [] whereArgs = {parcel.getAsString(ParcelEntry._ID)};
                        parcel.put(ParcelEntry.COLUMN_IS_UPDATE,IS_UPDATE);
                        db.update(ParcelEntry.TABLE_NAME,parcel,whereClause,whereArgs);
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
