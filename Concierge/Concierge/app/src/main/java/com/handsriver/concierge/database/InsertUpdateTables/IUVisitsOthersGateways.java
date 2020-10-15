package com.handsriver.concierge.database.InsertUpdateTables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.handsriver.concierge.database.ConciergeContract;
import com.handsriver.concierge.database.ConciergeContract.VisitEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;

import java.util.Vector;

/**
 * Created by Created by alain_r._trouve_silva after 23-03-17.
 */

public class IUVisitsOthersGateways{
    private static final String TAG = "IUVisitsIG";

    public static void run(Vector<ContentValues> cVVector) {
        SQLiteDatabase db;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            for (ContentValues obj:cVVector){

                String[] projection = {
                        VisitEntry._ID
                };

                String selection = VisitEntry.COLUMN_VISIT_ID_SERVER + " = ?";
                String [] selectionArgs = {String.valueOf(obj.getAsLong(VisitEntry.COLUMN_VISIT_ID_SERVER))};

                Cursor visit= db.query(VisitEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,null,null);

                if (visit.getCount() == 0){
                    db.insert(VisitEntry.TABLE_NAME,null,obj);
                }
                visit.close();
            }
        } catch (SQLException e){
            Log.e(TAG, "SQLiteException:" + e.getMessage());
            DatabaseManager.getInstance().closeDatabase();
        }
        DatabaseManager.getInstance().closeDatabase();
    }
}
