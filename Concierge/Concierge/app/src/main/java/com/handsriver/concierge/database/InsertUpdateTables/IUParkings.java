package com.handsriver.concierge.database.InsertUpdateTables;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.handsriver.concierge.database.ConciergeContract.ParkingEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by Created by alain_r._trouve_silva after 23-03-17.
 */

public class IUParkings{
    private static final String TAG = "IUParkings";

    public static void run(Vector<ContentValues> cVVector) {
        SQLiteDatabase db;
        String tableName = ParkingEntry.TABLE_NAME;
        ArrayList<String> mParkingsNew = new ArrayList<String>();
        db = DatabaseManager.getInstance().openDatabase();

        try {

            for (ContentValues obj:cVVector){
                mParkingsNew.add(obj.getAsString(ParkingEntry.COLUMN_PARKING_ID_SERVER));
            }

            String args = TextUtils.join(",",mParkingsNew);

            String whereClause = ParkingEntry.COLUMN_PARKING_ID_SERVER + " NOT IN (?)";
            String [] whereArgs = {args};
            db.delete(tableName,whereClause,whereArgs);

            db.beginTransaction();
            for (ContentValues obj:cVVector){
                String whereClause1 = ParkingEntry.COLUMN_PARKING_ID_SERVER + " = ?";
                String [] whereArgs1 = {obj.getAsString(ParkingEntry.COLUMN_PARKING_ID_SERVER)};
                int numUpdated = db.update(tableName,obj,whereClause1,whereArgs1);
                if(numUpdated == 0){
                    db.insert(tableName,null,obj);
                }
            }
            db.setTransactionSuccessful();
        } catch (SQLException e){
            Log.e(TAG, "SQLiteException:" + e.getMessage());
        } finally {
            db.endTransaction();
            DatabaseManager.getInstance().closeDatabase();
        }

    }
}
