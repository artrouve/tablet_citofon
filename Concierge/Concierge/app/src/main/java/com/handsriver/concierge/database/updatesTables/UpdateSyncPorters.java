package com.handsriver.concierge.database.updatesTables;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.handsriver.concierge.database.ConciergeContract.PorterEntry;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.sync.ConfigureSyncAccount;

import java.util.Vector;

/**
 * Created by Created by alain_r._trouve_silva after 23-03-17.
 */

public class UpdateSyncPorters {
    private static final String TAG = "UpdateSyncPorters";
    private static final String NOT_UPDATE = "0";


    public static void run(Vector<ContentValues> vectorPortersUpdateReturn) {
        SQLiteDatabase db;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            if (vectorPortersUpdateReturn != null) {
                for (ContentValues porters_update:vectorPortersUpdateReturn){
                    if (porters_update.getAsString(PorterEntry.COLUMN_IS_UPDATE_PASSWORD).equals(NOT_UPDATE)){
                        String whereClause = PorterEntry.COLUMN_PORTER_ID_SERVER + " = ?";
                        String [] whereArgs = {porters_update.getAsString(PorterEntry.COLUMN_PORTER_ID_SERVER)};
                        db.update(PorterEntry.TABLE_NAME,porters_update,whereClause,whereArgs);
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
