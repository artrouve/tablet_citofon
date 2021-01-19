package com.handsriver.concierge.database.InsertUpdateTables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.handsriver.concierge.database.ConciergeContract.CommonspaceVisitsEntry;
import com.handsriver.concierge.database.DatabaseManager;

import java.util.Vector;

/**
 * Created by Created by alain_r._trouve_silva after 23-03-17.
 */

public class IUCommonspacesVisitsOthersGateways {
    private static final String TAG = "IUCommonspacesVisitsIG";

    public static void run(Vector<ContentValues> cVVector) {
        SQLiteDatabase db;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            for (ContentValues obj:cVVector){

                String[] projection = {
                        CommonspaceVisitsEntry._ID
                };

                String selection = CommonspaceVisitsEntry.COLUMN_COMMONSPACE_VISIT_ID_SERVER + " = ?";
                String [] selectionArgs = {String.valueOf(obj.getAsLong(CommonspaceVisitsEntry.COLUMN_COMMONSPACE_VISIT_ID_SERVER))};

                Cursor visit_commonspace= db.query(CommonspaceVisitsEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,null,null);

                if (visit_commonspace.getCount() == 0){
                    db.insert(CommonspaceVisitsEntry.TABLE_NAME,null,obj);
                }
                visit_commonspace.close();

            }
        } catch (SQLException e){
            Log.e(TAG, "SQLiteException:" + e.getMessage());
            DatabaseManager.getInstance().closeDatabase();

        }
        DatabaseManager.getInstance().closeDatabase();
    }
}
