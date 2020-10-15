package com.handsriver.concierge.database.updatesTables;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.handsriver.concierge.database.ConciergeContract.SupplierVisitsEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;

import java.util.Vector;

/**
 * Created by Created by alain_r._trouve_silva after 23-03-17.
 */

public class UpdateSyncSuppliersVisits{
    private static final String TAG = "UpdateSyncSupVisits";
    private static final String IS_SYNC = "1";
    private static final String IS_UPDATE = "1";

    public static void run(Vector<ContentValues> vectorSupplierVisitsReturn, Vector<ContentValues> vectorSupplierVisitsUpdateReturn) {
        SQLiteDatabase db;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            if (vectorSupplierVisitsReturn != null){
                for (ContentValues suppliers_visits:vectorSupplierVisitsReturn){
                    if (suppliers_visits.getAsString(SupplierVisitsEntry.COLUMN_IS_SYNC).equals(IS_SYNC)){
                        int count = 0;

                        String whereClause = SupplierVisitsEntry._ID + " = ? AND " + SupplierVisitsEntry.COLUMN_EXIT_SUPPLIER + " IS NOT NULL";
                        String [] whereArgs = {suppliers_visits.getAsString(SupplierVisitsEntry._ID)};
                        suppliers_visits.put(SupplierVisitsEntry.COLUMN_IS_UPDATE,IS_UPDATE);
                        count = db.update(SupplierVisitsEntry.TABLE_NAME,suppliers_visits,whereClause,whereArgs);

                        if (count == 0){
                            String whereClause1 = SupplierVisitsEntry._ID + " = ?";
                            String [] whereArgs1 = {suppliers_visits.getAsString(SupplierVisitsEntry._ID)};
                            suppliers_visits.remove(SupplierVisitsEntry.COLUMN_IS_UPDATE);
                            db.update(SupplierVisitsEntry.TABLE_NAME,suppliers_visits,whereClause1,whereArgs1);
                        }
                    }
                }
            }

            if (vectorSupplierVisitsUpdateReturn != null) {
                for (ContentValues suppliers_visits_update:vectorSupplierVisitsUpdateReturn){
                    if (suppliers_visits_update.getAsString(SupplierVisitsEntry.COLUMN_IS_UPDATE).equals(IS_UPDATE)){
                        String whereClause = SupplierVisitsEntry._ID + " = ? AND " + SupplierVisitsEntry.COLUMN_EXIT_SUPPLIER + " IS NOT NULL";
                        String [] whereArgs = {suppliers_visits_update.getAsString(SupplierVisitsEntry._ID)};
                        db.update(SupplierVisitsEntry.TABLE_NAME,suppliers_visits_update,whereClause,whereArgs);
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
