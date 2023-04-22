package com.handsriver.concierge.database.updatesTables;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.handsriver.concierge.database.ConciergeContract.ResidentTempEntry;
import com.handsriver.concierge.database.DatabaseManager;

import java.util.Vector;

/**
 * Created by Created by alain_r._trouve_silva after 23-03-17.
 */

public class UpdateSyncResidentsTemps {
    private static final String TAG = "UpdateSyncResidentsTemp";
    private static final String IS_SYNC = "1";
    private static final String NOT_UPDATE = "0";

    public static void run(Vector<ContentValues> vectorResidentsTempsReturn, Vector<ContentValues> vectorResidentsTempsUpdateReturn) {
        SQLiteDatabase db;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            if (vectorResidentsTempsReturn != null){
                for (ContentValues residentstemps:vectorResidentsTempsReturn){
                    if (residentstemps.getAsString(ResidentTempEntry.COLUMN_IS_SYNC).equals(IS_SYNC)){

                        String whereClause = ResidentTempEntry._ID + " = ?";
                        String [] whereArgs = {residentstemps.getAsString(ResidentTempEntry._ID)};
                        residentstemps.put(ResidentTempEntry.COLUMN_IS_UPDATE,NOT_UPDATE);
                        db.update(ResidentTempEntry.TABLE_NAME,residentstemps,whereClause,whereArgs);
                    }
                }
            }

            if (vectorResidentsTempsUpdateReturn != null) {
                for (ContentValues residentstemps_update:vectorResidentsTempsUpdateReturn){
                    if (residentstemps_update.getAsString(ResidentTempEntry.COLUMN_IS_UPDATE).equals(NOT_UPDATE)){
                        String whereClause = ResidentTempEntry._ID + " = ?";
                        String [] whereArgs = {residentstemps_update.getAsString(ResidentTempEntry._ID)};
                        db.update(ResidentTempEntry.TABLE_NAME,residentstemps_update,whereClause,whereArgs);
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
