package com.handsriver.concierge.database.updatesTables;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.handsriver.concierge.database.ConciergeContract.TimekeepingEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;

import java.util.Vector;

/**
 * Created by Created by alain_r._trouve_silva after 23-03-17.
 */

public class UpdateSyncTimekeepingExitOtherGateways {
    private static final String TAG = "UpdateSyncTimekeeping";
    private static final String IS_UPDATE = "1";

    public static void run(Vector<ContentValues> vectorTimekeepingExitOthersGatewaysReturn) {
        SQLiteDatabase db;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            if (vectorTimekeepingExitOthersGatewaysReturn != null){
                for (ContentValues timekeeping:vectorTimekeepingExitOthersGatewaysReturn){
                    if(timekeeping.getAsString(TimekeepingEntry.COLUMN_EXIT_PORTER) != null){
                        String whereClause = TimekeepingEntry._ID + " = ? ";
                        String [] whereArgs = {timekeeping.getAsString(TimekeepingEntry._ID)};
                        timekeeping.put(TimekeepingEntry.COLUMN_IS_UPDATE,IS_UPDATE);
                        db.update(TimekeepingEntry.TABLE_NAME,timekeeping,whereClause,whereArgs);
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
