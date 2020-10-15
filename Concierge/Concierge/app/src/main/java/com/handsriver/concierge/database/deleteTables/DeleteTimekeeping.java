package com.handsriver.concierge.database.deleteTables;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.handsriver.concierge.database.ConciergeContract.TimekeepingEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.utilities.Utility;

/**
 * Created by Created by alain_r._trouve_silva after 03-03-17.
 */

public class DeleteTimekeeping{
    private static final String TAG = "DeleteTimekeeping";
    private static final String IS_SYNC = "1";
    private static final String IS_UPDATE = "1";


    public static void run() {
        SQLiteDatabase db;
        String tableName = TimekeepingEntry.TABLE_NAME;
        db = DatabaseManager.getInstance().openDatabase();
        try {

            String whereClause = TimekeepingEntry.COLUMN_IS_SYNC + " = ? AND " + TimekeepingEntry.COLUMN_IS_UPDATE + " = ? AND " + TimekeepingEntry.COLUMN_ENTRY_PORTER +  " < '" + Utility.getDateSimpleForServer7Days()+"'";
            String [] whereArgs = {IS_SYNC,IS_UPDATE};

            db.delete(tableName,whereClause,whereArgs);

        } catch (SQLException e){
            Log.e(TAG, "SQLiteException:" + e.getMessage());
            DatabaseManager.getInstance().closeDatabase();
        }
        DatabaseManager.getInstance().closeDatabase();
    }
}
