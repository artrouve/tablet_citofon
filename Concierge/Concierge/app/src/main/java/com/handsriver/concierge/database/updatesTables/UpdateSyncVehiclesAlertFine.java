package com.handsriver.concierge.database.updatesTables;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.handsriver.concierge.database.ConciergeContract.VehicleEntry;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.sync.ConfigureSyncAccount;

import java.util.Vector;

/**
 * Created by Created by alain_r._trouve_silva after 23-03-17.
 */

public class UpdateSyncVehiclesAlertFine {

    private static final String TAG = "UpdateVehiclesAlertFine";

    public static void run(Vector<ContentValues> vectorVehicles, Context mContext) {
        SQLiteDatabase db;
        String tableNameVehicle = VehicleEntry.TABLE_NAME;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            if (vectorVehicles != null){
                for (ContentValues vehicles:vectorVehicles){
                    String whereClause = VehicleEntry._ID + " = ?";
                    String [] whereArgs = {vehicles.getAsString(VehicleEntry._ID)};
                    db.update(tableNameVehicle,vehicles,whereClause,whereArgs);
                }
            }
            ConfigureSyncAccount.syncImmediatelyVisitsAndVehicles(mContext);

        } catch (SQLException e){
            Log.e(TAG, "SQLiteException:" + e.getMessage());
            DatabaseManager.getInstance().closeDatabase();
        }
        DatabaseManager.getInstance().closeDatabase();
    }
}
