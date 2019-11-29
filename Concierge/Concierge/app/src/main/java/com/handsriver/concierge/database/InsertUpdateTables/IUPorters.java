package com.handsriver.concierge.database.InsertUpdateTables;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.handsriver.concierge.database.ConciergeContract.PorterEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;

import java.util.Vector;

/**
 * Created by Created by alain_r._trouve_silva after 23-03-17.
 */

public class IUPorters {
    private static final String TAG = "IUPorters";
    private static final String ID_PORTED = "porter_id_server";

    public static void run(Vector<ContentValues> cVVector) {
        SQLiteDatabase db;
        String tableName = PorterEntry.TABLE_NAME;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            for (ContentValues obj:cVVector){
                String whereClause = PorterEntry.COLUMN_PORTER_ID_SERVER + " = ?";
                String [] whereArgs = {obj.getAsString(ID_PORTED)};
                int numUpdated = db.update(tableName,obj,whereClause,whereArgs);
                if(numUpdated == 0){
                    db.insert(tableName,null,obj);
                }

            }
        } catch (SQLException e){
            Log.e(TAG, "SQLiteException:" + e.getMessage());
            DatabaseManager.getInstance().closeDatabase();
        }
        DatabaseManager.getInstance().closeDatabase();
    }
}
