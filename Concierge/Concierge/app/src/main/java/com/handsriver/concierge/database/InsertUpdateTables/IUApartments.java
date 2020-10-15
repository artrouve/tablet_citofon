package com.handsriver.concierge.database.InsertUpdateTables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import com.handsriver.concierge.database.ConciergeContract.ApartmentEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;

import java.util.Vector;

/**
 * Created by Created by alain_r._trouve_silva after 23-03-17.
 */

public class IUApartments{
    private static final String TAG = "IUApartments";
    private static final int IS_ACTIVE = 1;
    private static final int NOT_ACTIVE = 0;

    public static void run(Vector<ContentValues> cVVector) {
        SQLiteDatabase db;
        String tableName = ApartmentEntry.TABLE_NAME;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            String[] projection = {
                    ApartmentEntry._ID,
                    ApartmentEntry.COLUMN_APARTMENT_NUMBER,
                    ApartmentEntry.COLUMN_APARTMENT_ID_SERVER,
                    ApartmentEntry.COLUMN_ACTIVE
            };

            String selection = ApartmentEntry.COLUMN_ACTIVE + " = ?";
            String [] selectionArgs = {String.valueOf(IS_ACTIVE)};

            Cursor apartments = db.query(tableName,projection,selection,selectionArgs,null,null,null);

            if (apartments != null) {
                while(apartments.moveToNext()){
                    int flag = 0;
                    for (ContentValues obj:cVVector){
                        if (apartments.getString(apartments.getColumnIndex(ApartmentEntry.COLUMN_APARTMENT_ID_SERVER)).equals(obj.getAsString(ApartmentEntry.COLUMN_APARTMENT_ID_SERVER))){
                            flag = 1;
                            break;
                        }
                    }
                    if (flag == 0){
                        String whereClause = ApartmentEntry.COLUMN_APARTMENT_ID_SERVER + " = ?";
                        String [] whereArgs = {apartments.getString(apartments.getColumnIndex(ApartmentEntry.COLUMN_APARTMENT_ID_SERVER))};
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(ApartmentEntry.COLUMN_ACTIVE,NOT_ACTIVE);
                        db.update(tableName,contentValues,whereClause,whereArgs);
                    }
                }
                apartments.close();
            }

            for (ContentValues obj:cVVector){
                obj.put(ApartmentEntry.COLUMN_ACTIVE,IS_ACTIVE);
                String whereClause = ApartmentEntry.COLUMN_APARTMENT_ID_SERVER + " = ?";
                String [] whereArgs = {obj.getAsString(ApartmentEntry.COLUMN_APARTMENT_ID_SERVER)};
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
