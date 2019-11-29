package com.handsriver.concierge.database.InsertUpdateTables;

import android.content.ContentValues;
import android.database.Cursor;
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

public class IUSuppliersVisitsOthersGateways{
    private static final String TAG = "IUSuppliersVisitsIG";

    public static void run(Vector<ContentValues> cVVector) {
        SQLiteDatabase db;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            for (ContentValues obj:cVVector){

                String[] projection = {
                        SupplierVisitsEntry._ID
                };

                String selection = SupplierVisitsEntry.COLUMN_SUPPLIER_VISIT_ID_SERVER + " = ?";
                String [] selectionArgs = {String.valueOf(obj.getAsLong(SupplierVisitsEntry.COLUMN_SUPPLIER_VISIT_ID_SERVER))};

                Cursor visit_supplier= db.query(SupplierVisitsEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,null,null);

                if (visit_supplier.getCount() == 0){
                    db.insert(SupplierVisitsEntry.TABLE_NAME,null,obj);
                }
                visit_supplier.close();

            }
        } catch (SQLException e){
            Log.e(TAG, "SQLiteException:" + e.getMessage());
            DatabaseManager.getInstance().closeDatabase();

        }
        DatabaseManager.getInstance().closeDatabase();
    }
}
