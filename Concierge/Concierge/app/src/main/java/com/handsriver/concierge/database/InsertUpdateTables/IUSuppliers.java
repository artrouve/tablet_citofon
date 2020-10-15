package com.handsriver.concierge.database.InsertUpdateTables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import com.handsriver.concierge.database.ConciergeContract.SupplierEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;

import java.util.Vector;

/**
 * Created by Created by alain_r._trouve_silva after 23-03-17.
 */

public class IUSuppliers {

    private static final String TAG = "IUSuppliers";
    private static final int IS_ACTIVE = 1;
    private static final int NOT_ACTIVE = 0;

    public static void run(Vector<ContentValues> cVVector) {
        SQLiteDatabase db;
        String tableName = SupplierEntry.TABLE_NAME;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            String[] projection = {
                    SupplierEntry._ID,
                    SupplierEntry.COLUMN_SUPPLIER_ID_SERVER
            };

            Cursor suppliers = db.query(tableName,projection,null,null,null,null,null);

            if (suppliers != null) {
                while(suppliers.moveToNext()){
                    int flag = 0;
                    for (ContentValues obj:cVVector){
                        if (suppliers.getString(suppliers.getColumnIndex(SupplierEntry.COLUMN_SUPPLIER_ID_SERVER)).equals(obj.getAsString(SupplierEntry.COLUMN_SUPPLIER_ID_SERVER))){
                            flag = 1;
                            break;
                        }
                    }
                    if (flag == 0){
                        String whereClause = SupplierEntry.COLUMN_SUPPLIER_ID_SERVER + " = ?";
                        String [] whereArgs = {suppliers.getString(suppliers.getColumnIndex(SupplierEntry.COLUMN_SUPPLIER_ID_SERVER))};
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(SupplierEntry.COLUMN_ACTIVE,NOT_ACTIVE);
                        db.update(tableName,contentValues,whereClause,whereArgs);
                    }
                }
                suppliers.close();
            }

            for (ContentValues obj:cVVector){
                obj.put(SupplierEntry.COLUMN_ACTIVE,IS_ACTIVE);
                String whereClause = SupplierEntry.COLUMN_SUPPLIER_ID_SERVER + " = ?";
                String [] whereArgs = {obj.getAsString(SupplierEntry.COLUMN_SUPPLIER_ID_SERVER)};
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
