package com.handsriver.concierge.database.InsertUpdateTables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.handsriver.concierge.database.ConciergeContract.ParcelEntry;
import com.handsriver.concierge.database.DatabaseManager;

import java.util.Vector;

/**
 * Created by Created by alain_r._trouve_silva after 23-03-17.
 */

public class IUParcelsOthersGateways {
    private static final String TAG = "IUParcelsIG";

    public static void run(Vector<ContentValues> cVVector) {
        SQLiteDatabase db;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            for (ContentValues obj:cVVector){

                String[] projection = {
                        ParcelEntry._ID
                };

                String selection = ParcelEntry.COLUMN_PARCEL_ID_SERVER + " = ?";
                String [] selectionArgs = {String.valueOf(obj.getAsLong(ParcelEntry.COLUMN_PARCEL_ID_SERVER))};

                Cursor parcel= db.query(ParcelEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,null,null);

                if (parcel.getCount() == 0){
                    db.insert(ParcelEntry.TABLE_NAME,null,obj);
                }
                parcel.close();

            }
        } catch (SQLException e){
            Log.e(TAG, "SQLiteException:" + e.getMessage());
            DatabaseManager.getInstance().closeDatabase();

        }
        DatabaseManager.getInstance().closeDatabase();
    }
}
