package com.handsriver.concierge.database.updatesTables;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.handsriver.concierge.database.ConciergeContract.ResidentEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;

import java.util.Vector;

/**
 * Created by Created by alain_r._trouve_silva after 23-03-17.
 */

public class UpdateSyncCodeAuth {
    private static final String TAG = "UpdateSyncCodeAuth";
    private static final String NOT_REQUEST_CODE = "0";

    public static void run(Vector<ContentValues> vectorResidentsReturn) {
        SQLiteDatabase db;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            if (vectorResidentsReturn != null){
                for (ContentValues residents:vectorResidentsReturn){
                    if (residents.getAsString(ResidentEntry.COLUMN_REQUEST_CODE).equals(NOT_REQUEST_CODE)){
                        String whereClause = ResidentEntry.COLUMN_RESIDENT_ID_SERVER + " = ?";
                        String [] whereArgs = {residents.getAsString(ResidentEntry.COLUMN_RESIDENT_ID_SERVER)};
                        db.update(ResidentEntry.TABLE_NAME,residents,whereClause,whereArgs);
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
