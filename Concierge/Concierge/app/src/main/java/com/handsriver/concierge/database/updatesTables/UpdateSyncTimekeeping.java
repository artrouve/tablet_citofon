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

public class UpdateSyncTimekeeping {
    private static final String TAG = "UpdateSyncTimekeeping";
    private static final String IS_SYNC = "1";
    private static final String IS_UPDATE = "1";

    public static void run(Vector<ContentValues> vectorTimekeepingReturn, Vector<ContentValues> vectorTimekeepingUpdateReturn) {
        SQLiteDatabase db;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            if (vectorTimekeepingReturn != null){
                for (ContentValues timekeeping:vectorTimekeepingReturn){
                    if (timekeeping.getAsString(TimekeepingEntry.COLUMN_IS_SYNC).equals(IS_SYNC)){
                        int count = 0;

                        String whereClause = TimekeepingEntry._ID + " = ? AND " + TimekeepingEntry.COLUMN_EXIT_PORTER + " IS NOT NULL";
                        String [] whereArgs = {timekeeping.getAsString(TimekeepingEntry._ID)};
                        timekeeping.put(TimekeepingEntry.COLUMN_IS_UPDATE,IS_UPDATE);
                        count = db.update(TimekeepingEntry.TABLE_NAME,timekeeping,whereClause,whereArgs);

                        if (count == 0){
                            String whereClause1 = TimekeepingEntry._ID + " = ?";
                            String [] whereArgs1 = {timekeeping.getAsString(TimekeepingEntry._ID)};
                            timekeeping.remove(TimekeepingEntry.COLUMN_IS_UPDATE);
                            db.update(TimekeepingEntry.TABLE_NAME,timekeeping,whereClause1,whereArgs1);
                        }
                    }
                }
            }

            if (vectorTimekeepingUpdateReturn != null) {
                for (ContentValues timekeeping_update:vectorTimekeepingUpdateReturn){
                    if (timekeeping_update.getAsString(TimekeepingEntry.COLUMN_IS_UPDATE).equals(IS_UPDATE)){
                        String whereClause = TimekeepingEntry._ID + " = ? AND " + TimekeepingEntry.COLUMN_EXIT_PORTER + " IS NOT NULL";
                        String [] whereArgs = {timekeeping_update.getAsString(TimekeepingEntry._ID)};
                        db.update(TimekeepingEntry.TABLE_NAME,timekeeping_update,whereClause,whereArgs);
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
