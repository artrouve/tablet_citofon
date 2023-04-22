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

public class UpdateSyncResidents {
    private static final String TAG = "UpdateSyncResidents";
    private static final String IS_SYNC = "1";
    private static final String NOT_UPDATE = "0";

    public static void run(Vector<ContentValues> vectorResidentsReturn, Vector<ContentValues> vectorResidentsUpdateReturn, Vector<ContentValues> vectorResidentsDeleteReturn) {
        SQLiteDatabase db;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            if (vectorResidentsReturn != null){
                for (ContentValues residents:vectorResidentsReturn){
                    if (residents.getAsString(ResidentEntry.COLUMN_IS_SYNC).equals(IS_SYNC)){

                        String whereClause = ResidentEntry._ID + " = ?";
                        String [] whereArgs = {residents.getAsString(ResidentEntry._ID)};
                        residents.put(ResidentEntry.COLUMN_IS_UPDATE,NOT_UPDATE);
                        db.update(ResidentEntry.TABLE_NAME,residents,whereClause,whereArgs);
                    }
                }
            }

            if (vectorResidentsUpdateReturn != null) {
                for (ContentValues residents_update:vectorResidentsUpdateReturn){
                    if (residents_update.getAsString(ResidentEntry.COLUMN_IS_UPDATE).equals(NOT_UPDATE)){
                        String whereClause = ResidentEntry._ID + " = ?";
                        String [] whereArgs = {residents_update.getAsString(ResidentEntry._ID)};
                        db.update(ResidentEntry.TABLE_NAME,residents_update,whereClause,whereArgs);
                    }
                }
            }

            if (vectorResidentsDeleteReturn != null) {
                for (ContentValues residents_delete:vectorResidentsDeleteReturn){
                    if (residents_delete.getAsString(ResidentEntry.COLUMN_IS_UPDATE).equals(NOT_UPDATE)){
                        String whereClause = ResidentEntry._ID + " = ?";
                        String [] whereArgs = {residents_delete.getAsString(ResidentEntry._ID)};
                        db.delete(ResidentEntry.TABLE_NAME,whereClause,whereArgs);
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
