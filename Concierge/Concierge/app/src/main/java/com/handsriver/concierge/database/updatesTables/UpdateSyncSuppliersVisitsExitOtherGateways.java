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

public class UpdateSyncSuppliersVisitsExitOtherGateways{
    private static final String TAG = "UpdateSyncSupplierVisit";
    private static final String IS_UPDATE = "1";

    public static void run(Vector<ContentValues> vectorSuppliersVisitsExitOthersGatewaysReturn) {
        SQLiteDatabase db;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            if (vectorSuppliersVisitsExitOthersGatewaysReturn != null){
                for (ContentValues supplier_visit:vectorSuppliersVisitsExitOthersGatewaysReturn){
                    if(supplier_visit.getAsString(SupplierVisitsEntry.COLUMN_EXIT_SUPPLIER) != null){
                        String whereClause = SupplierVisitsEntry._ID + " = ? ";
                        String [] whereArgs = {supplier_visit.getAsString(SupplierVisitsEntry._ID)};
                        supplier_visit.put(SupplierVisitsEntry.COLUMN_IS_UPDATE,IS_UPDATE);
                        db.update(SupplierVisitsEntry.TABLE_NAME,supplier_visit,whereClause,whereArgs);
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
