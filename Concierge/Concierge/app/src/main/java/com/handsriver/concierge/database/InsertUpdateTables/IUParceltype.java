package com.handsriver.concierge.database.InsertUpdateTables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.handsriver.concierge.database.ConciergeContract;
import com.handsriver.concierge.database.ConciergeContract.ParcelTypeEntry;
import com.handsriver.concierge.database.DatabaseManager;

import java.util.Vector;

/**
 * Created by Created by alain_r._trouve_silva after 23-03-17.
 */

public class IUParceltype {
    private static final String TAG = "IUParceltype";
    private static final int IS_ACTIVE = 1;
    private static final int NOT_ACTIVE = 0;

    public static void run(Vector<ContentValues> cVVector) {
        SQLiteDatabase db;
        String tableName = ParcelTypeEntry.TABLE_NAME;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            String[] projection = {
                    ParcelTypeEntry._ID,
                    ParcelTypeEntry.COLUMN_PARCELTYPE_TYPE,
                    ParcelTypeEntry.COLUMN_PARCELTYPE_ID_SERVER,
                    ParcelTypeEntry.COLUMN_ACTIVE

            };

            String selection = ParcelTypeEntry.COLUMN_ACTIVE + " = ?";
            String [] selectionArgs = {String.valueOf(IS_ACTIVE)};

            Cursor parceltypes = db.query(tableName,projection,selection,selectionArgs,null,null,null);

            if (parceltypes != null) {
                while(parceltypes.moveToNext()){
                    int flag = 0;
                    for (ContentValues obj:cVVector){
                        if (parceltypes.getString(parceltypes.getColumnIndex(ParcelTypeEntry.COLUMN_PARCELTYPE_ID_SERVER)).equals(obj.getAsString(ParcelTypeEntry.COLUMN_PARCELTYPE_ID_SERVER))){
                            flag = 1;
                            break;
                        }
                    }
                    if (flag == 0){
                        String whereClause = ParcelTypeEntry.COLUMN_PARCELTYPE_ID_SERVER + " = ?";
                        String [] whereArgs = {parceltypes.getString(parceltypes.getColumnIndex(ParcelTypeEntry.COLUMN_PARCELTYPE_ID_SERVER))};
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(ParcelTypeEntry.COLUMN_ACTIVE,NOT_ACTIVE);
                        db.update(tableName,contentValues,whereClause,whereArgs);
                    }
                }
                parceltypes.close();
            }

            for (ContentValues obj:cVVector){
                obj.put(ParcelTypeEntry.COLUMN_ACTIVE,IS_ACTIVE);
                String whereClause = ParcelTypeEntry.COLUMN_PARCELTYPE_ID_SERVER + " = ?";
                String [] whereArgs = {obj.getAsString(ParcelTypeEntry.COLUMN_PARCELTYPE_ID_SERVER)};
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
