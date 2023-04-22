package com.handsriver.concierge.database.updatesTables;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.handsriver.concierge.database.ConciergeContract.ResidentVehicleEntry;
import com.handsriver.concierge.database.DatabaseManager;

import java.util.Vector;

/**
 * Created by Created by alain_r._trouve_silva after 23-03-17.
 */

public class UpdateSyncResidentsVehicles {
    private static final String TAG = "UpdateSyncResidentsVeh";
    private static final String IS_SYNC = "1";
    private static final String NOT_UPDATE = "0";

    public static void run(Vector<ContentValues> vectorResidentsVehiclesReturn, Vector<ContentValues> vectorResidentsVehiclesUpdateReturn) {
        SQLiteDatabase db;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            if (vectorResidentsVehiclesReturn != null){
                for (ContentValues residentsvehicles:vectorResidentsVehiclesReturn){
                    if (residentsvehicles.getAsString(ResidentVehicleEntry.COLUMN_IS_SYNC).equals(IS_SYNC)){

                        String whereClause = ResidentVehicleEntry._ID + " = ?";
                        String [] whereArgs = {residentsvehicles.getAsString(ResidentVehicleEntry._ID)};
                        residentsvehicles.put(ResidentVehicleEntry.COLUMN_IS_UPDATE,NOT_UPDATE);
                        db.update(ResidentVehicleEntry.TABLE_NAME,residentsvehicles,whereClause,whereArgs);
                    }
                }
            }

            if (vectorResidentsVehiclesUpdateReturn != null) {
                for (ContentValues residentsvehicles_update:vectorResidentsVehiclesUpdateReturn){
                    if (residentsvehicles_update.getAsString(ResidentVehicleEntry.COLUMN_IS_UPDATE).equals(NOT_UPDATE)){
                        String whereClause = ResidentVehicleEntry._ID + " = ?";
                        String [] whereArgs = {residentsvehicles_update.getAsString(ResidentVehicleEntry._ID)};
                        db.update(ResidentVehicleEntry.TABLE_NAME,residentsvehicles_update,whereClause,whereArgs);
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
