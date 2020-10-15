package com.handsriver.concierge.database.InsertUpdateTables;

import android.content.ContentValues;
import android.database.Cursor;
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

public class IUTimekeepingOthersGateways{
    private static final String TAG = "IUTimekeepingIG";

    public static void run(Vector<ContentValues> cVVector) {
        SQLiteDatabase db;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            for (ContentValues obj:cVVector){

                String[] projection = {
                        TimekeepingEntry._ID
                };

                String selection = TimekeepingEntry.COLUMN_TIMEKEEPING_ID_SERVER + " = ?";
                String [] selectionArgs = {String.valueOf(obj.getAsLong(TimekeepingEntry.COLUMN_TIMEKEEPING_ID_SERVER))};

                Cursor timekeeping= db.query(TimekeepingEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,null,null);

                if (timekeeping.getCount() == 0){
                    db.insert(TimekeepingEntry.TABLE_NAME,null,obj);
                }
                timekeeping.close();
            }
        } catch (SQLException e){
            Log.e(TAG, "SQLiteException:" + e.getMessage());
            DatabaseManager.getInstance().closeDatabase();
        }
        DatabaseManager.getInstance().closeDatabase();
    }
}
