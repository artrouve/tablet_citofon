package com.handsriver.concierge.database.deleteTables;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.handsriver.concierge.database.ConciergeContract.ParcelEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.utilities.Utility;

/**
 * Created by Created by alain_r._trouve_silva after 03-03-17.
 */

public class DeleteParcels {
    private static final String TAG = "DeleteParcels";
    private static final String IS_SYNC = "1";
    private static final String IS_UPDATE = "1";

    public static void run() {
        SQLiteDatabase db;
        String tableName = ParcelEntry.TABLE_NAME;
        db = DatabaseManager.getInstance().openDatabase();
        try {

            String whereClause = ParcelEntry.COLUMN_IS_SYNC + " = ? AND " + ParcelEntry.COLUMN_IS_UPDATE + " = ? AND " + ParcelEntry.COLUMN_ENTRY_PARCEL + " < '" + Utility.getDateSimpleForServer7Days()+"'";
            String [] whereArgs = {IS_SYNC,IS_UPDATE};

            db.delete(tableName,whereClause,whereArgs);

        } catch (SQLException e){
            Log.e(TAG, "SQLiteException:" + e.getMessage());
            DatabaseManager.getInstance().closeDatabase();
        }
        DatabaseManager.getInstance().closeDatabase();
    }
}
