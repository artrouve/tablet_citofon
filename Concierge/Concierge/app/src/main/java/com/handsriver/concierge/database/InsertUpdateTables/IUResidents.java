package com.handsriver.concierge.database.InsertUpdateTables;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.util.LongSparseArray;

import com.handsriver.concierge.database.ConciergeContract.ResidentEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.residents.Resident;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by Created by alain_r._trouve_silva after 23-03-17.
 */

public class IUResidents{

    private static final String TAG = "IUResident";
    private static final int IS_SYNC = 1;
    private static final int NOT_REQUEST_CODE = 0;
    private static final int NOT_UPDATE = 0;
    private static final int NOT_DELETE = 0;
    private static ArrayList<String> mResidentNew;
    private static LongSparseArray<Resident> mResidentMap;

    @SuppressLint("Range")
    public static void run(Vector<ContentValues> cVVector) {
        SQLiteDatabase db;
        mResidentNew = new ArrayList<String>();
        mResidentMap = new LongSparseArray<Resident>();
        String tableName = ResidentEntry.TABLE_NAME;
        db = DatabaseManager.getInstance().openDatabase();

        try {

            String[] projection = {
                    ResidentEntry._ID,
                    ResidentEntry.COLUMN_RESIDENT_ID_SERVER,
                    ResidentEntry.COLUMN_IS_UPDATE
            };

            Cursor residents = db.query(tableName,projection,null,null,null,null,null);

            if (residents != null){
                while (residents.moveToNext()){
                    Resident resident = new Resident();
                    resident.setId(residents.getLong(residents.getColumnIndex(ResidentEntry._ID)));
                    resident.setResidentIdServer(residents.getLong(residents.getColumnIndex(ResidentEntry.COLUMN_RESIDENT_ID_SERVER)));
                    resident.setIsUpdate(residents.getString(residents.getColumnIndex(ResidentEntry.COLUMN_IS_UPDATE)));
                    mResidentMap.append(resident.getResidentIdServer(),resident);

                }
                residents.close();

                for (ContentValues obj:cVVector){
                    mResidentNew.add(obj.getAsString(ResidentEntry.COLUMN_RESIDENT_ID_SERVER));
                }

                String args = TextUtils.join(",",mResidentNew);

                String whereClause = ResidentEntry.COLUMN_RESIDENT_ID_SERVER + " NOT IN (?) AND " + ResidentEntry.COLUMN_IS_SYNC + " = ?";
                String [] whereArgs = {args,String.valueOf(IS_SYNC)};
                db.delete(tableName,whereClause,whereArgs);

            }

            db.beginTransaction();

            for (ContentValues obj:cVVector){

                Resident resident = mResidentMap.get(obj.getAsLong(ResidentEntry.COLUMN_RESIDENT_ID_SERVER));

                if (resident != null){
                    if (resident.getIsUpdate().equals(String.valueOf(NOT_UPDATE))){
                        String whereClause = ResidentEntry.COLUMN_RESIDENT_ID_SERVER + " = ?";
                        String [] whereArgs = {obj.getAsString(ResidentEntry.COLUMN_RESIDENT_ID_SERVER)};
                        int numUpdated = db.update(tableName,obj,whereClause,whereArgs);
                        if(numUpdated == 0){
                            obj.put(ResidentEntry.COLUMN_IS_SYNC,IS_SYNC);
                            obj.put(ResidentEntry.COLUMN_IS_UPDATE,NOT_UPDATE);
                            obj.put(ResidentEntry.COLUMN_REQUEST_CODE,NOT_REQUEST_CODE);
                            obj.put(ResidentEntry.COLUMN_IS_DELETED,NOT_DELETE);
                            db.insert(tableName,null,obj);
                        }
                    }
                }
                else{
                    obj.put(ResidentEntry.COLUMN_IS_SYNC,IS_SYNC);
                    obj.put(ResidentEntry.COLUMN_IS_UPDATE,NOT_UPDATE);
                    obj.put(ResidentEntry.COLUMN_REQUEST_CODE,NOT_REQUEST_CODE);
                    obj.put(ResidentEntry.COLUMN_IS_DELETED,NOT_DELETE);
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
