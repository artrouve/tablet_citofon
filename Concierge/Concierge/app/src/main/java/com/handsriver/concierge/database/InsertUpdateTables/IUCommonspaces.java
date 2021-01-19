package com.handsriver.concierge.database.InsertUpdateTables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.handsriver.concierge.database.ConciergeContract;
import com.handsriver.concierge.database.ConciergeContract.CommonspaceEntry;
import com.handsriver.concierge.database.DatabaseManager;

import java.util.Vector;

/**
 * Created by Created by alain_r._trouve_silva after 23-03-17.
 */

public class IUCommonspaces {

    private static final String TAG = "IUCommonspaces";
    private static final int IS_ACTIVE = 1;
    private static final int NOT_ACTIVE = 0;

    public static void run(Vector<ContentValues> cVVector) {
        SQLiteDatabase db;
        String tableName = CommonspaceEntry.TABLE_NAME;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            String[] projection = {
                    CommonspaceEntry._ID,
                    CommonspaceEntry.COLUMN_COMMONSPACE_ID_SERVER
            };

            Cursor commonspaces = db.query(tableName,projection,null,null,null,null,null);

            if (commonspaces != null) {
                while(commonspaces.moveToNext()){
                    int flag = 0;
                    for (ContentValues obj:cVVector){
                        if (commonspaces.getString(commonspaces.getColumnIndex(CommonspaceEntry.COLUMN_COMMONSPACE_ID_SERVER)).equals(obj.getAsString(CommonspaceEntry.COLUMN_COMMONSPACE_ID_SERVER))){
                            flag = 1;
                            break;
                        }
                    }
                    if (flag == 0){
                        String whereClause = CommonspaceEntry.COLUMN_COMMONSPACE_ID_SERVER + " = ?";
                        String [] whereArgs = {commonspaces.getString(commonspaces.getColumnIndex(CommonspaceEntry.COLUMN_COMMONSPACE_ID_SERVER))};
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(CommonspaceEntry.COLUMN_ACTIVE,NOT_ACTIVE);
                        db.update(tableName,contentValues,whereClause,whereArgs);
                    }
                }
                commonspaces.close();
            }

            for (ContentValues obj:cVVector){
                obj.put(CommonspaceEntry.COLUMN_ACTIVE,IS_ACTIVE);
                String whereClause = CommonspaceEntry.COLUMN_COMMONSPACE_ID_SERVER + " = ?";
                String [] whereArgs = {obj.getAsString(CommonspaceEntry.COLUMN_COMMONSPACE_ID_SERVER)};
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
