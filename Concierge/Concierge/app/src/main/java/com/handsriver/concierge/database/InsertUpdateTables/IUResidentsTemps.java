package com.handsriver.concierge.database.InsertUpdateTables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;
import android.util.LongSparseArray;

import com.handsriver.concierge.database.ConciergeContract.ResidentTempEntry;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.residents.ResidentTemp;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by Created by alain_r._trouve_silva after 23-03-17.
 */

public class IUResidentsTemps {

    private static final String TAG = "IUResidentTemp";
    private static final int IS_SYNC = 1;
    private static final int NOT_REQUEST_CODE = 0;
    private static final int NOT_UPDATE = 0;
    private static ArrayList<String> mResidenttempNew;
    private static LongSparseArray<ResidentTemp> mResidenttempMap;

    public static void run(Vector<ContentValues> cVVector) {
        SQLiteDatabase db;
        mResidenttempNew = new ArrayList<String>();
        mResidenttempMap = new LongSparseArray<ResidentTemp>();

        String tableName = ResidentTempEntry.TABLE_NAME;
        db = DatabaseManager.getInstance().openDatabase();

        try {

            String[] projection = {
                    ResidentTempEntry._ID,
                    ResidentTempEntry.COLUMN_RESIDENTTEMP_ID_SERVER,
                    ResidentTempEntry.COLUMN_IS_UPDATE
            };

            Cursor residentstemps = db.query(tableName,projection,null,null,null,null,null);

            if (residentstemps != null){
                while (residentstemps.moveToNext()){
                    ResidentTemp resident = new ResidentTemp();
                    resident.setId(residentstemps.getLong(residentstemps.getColumnIndex(ResidentTempEntry._ID)));
                    resident.setResidenttempIdServer(residentstemps.getLong(residentstemps.getColumnIndex(ResidentTempEntry.COLUMN_RESIDENTTEMP_ID_SERVER)));
                    resident.setIsUpdate(residentstemps.getString(residentstemps.getColumnIndex(ResidentTempEntry.COLUMN_IS_UPDATE)));
                    mResidenttempMap.append(resident.getResidenttempIdServer(),resident);

                }
                residentstemps.close();

                for (ContentValues obj:cVVector){
                    mResidenttempNew.add(obj.getAsString(ResidentTempEntry.COLUMN_RESIDENTTEMP_ID_SERVER));
                }

                String args = TextUtils.join(",",mResidenttempNew);

                String whereClause = ResidentTempEntry.COLUMN_RESIDENTTEMP_ID_SERVER + " NOT IN (?) AND " + ResidentTempEntry.COLUMN_IS_SYNC + " = ?";
                String [] whereArgs = {args,String.valueOf(IS_SYNC)};
                db.delete(tableName,whereClause,whereArgs);

            }

            db.beginTransaction();

            for (ContentValues obj:cVVector){

                ResidentTemp resident = mResidenttempMap.get(obj.getAsLong(ResidentTempEntry.COLUMN_RESIDENTTEMP_ID_SERVER));

                if (resident != null){
                    if (resident.getIsUpdate().equals(String.valueOf(NOT_UPDATE))){
                        String whereClause = ResidentTempEntry.COLUMN_RESIDENTTEMP_ID_SERVER + " = ?";
                        String [] whereArgs = {obj.getAsString(ResidentTempEntry.COLUMN_RESIDENTTEMP_ID_SERVER)};
                        int numUpdated = db.update(tableName,obj,whereClause,whereArgs);
                        if(numUpdated == 0){
                            obj.put(ResidentTempEntry.COLUMN_IS_SYNC,IS_SYNC);
                            obj.put(ResidentTempEntry.COLUMN_IS_UPDATE,NOT_UPDATE);
                            db.insert(tableName,null,obj);
                        }
                    }
                }
                else{
                    obj.put(ResidentTempEntry.COLUMN_IS_SYNC,IS_SYNC);
                    obj.put(ResidentTempEntry.COLUMN_IS_UPDATE,NOT_UPDATE);
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
