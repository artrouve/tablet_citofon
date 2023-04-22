package com.handsriver.concierge.database.InsertUpdateTables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;
import android.util.LongSparseArray;

import com.handsriver.concierge.database.ConciergeContract.ResidentVehicleEntry;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.residents.ResidentVehicle;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by Created by alain_r._trouve_silva after 23-03-17.
 */

public class IUResidentsVehicles {

    private static final String TAG = "IUResidentVehicle";
    private static final int IS_SYNC = 1;
    private static final int NOT_UPDATE = 0;
    private static ArrayList<String> mResidentVehicleNew;
    private static LongSparseArray<ResidentVehicle> mResidentVehicleMap;

    public static void run(Vector<ContentValues> cVVector) {
        SQLiteDatabase db;
        mResidentVehicleNew = new ArrayList<String>();
        mResidentVehicleMap = new LongSparseArray<ResidentVehicle>();
        String tableName = ResidentVehicleEntry.TABLE_NAME;
        db = DatabaseManager.getInstance().openDatabase();

        try {

            String[] projection = {
                    ResidentVehicleEntry._ID,
                    ResidentVehicleEntry.COLUMN_RESIDENTVEHICLE_ID_SERVER,
                    ResidentVehicleEntry.COLUMN_IS_UPDATE
            };

            Cursor residentsvehicles = db.query(tableName,projection,null,null,null,null,null);

            if (residentsvehicles != null){
                while (residentsvehicles.moveToNext()){
                    ResidentVehicle resident = new ResidentVehicle();
                    resident.setId(residentsvehicles.getLong(residentsvehicles.getColumnIndex(ResidentVehicleEntry._ID)));
                    resident.setResidentVehicleIdServer(residentsvehicles.getLong(residentsvehicles.getColumnIndex(ResidentVehicleEntry.COLUMN_RESIDENTVEHICLE_ID_SERVER)));
                    resident.setIsUpdate(residentsvehicles.getString(residentsvehicles.getColumnIndex(ResidentVehicleEntry.COLUMN_IS_UPDATE)));
                    mResidentVehicleMap.append(resident.getResidentVehicleIdServer(),resident);

                }
                residentsvehicles.close();

                for (ContentValues obj:cVVector){
                    mResidentVehicleNew.add(obj.getAsString(ResidentVehicleEntry.COLUMN_RESIDENTVEHICLE_ID_SERVER));
                }

                String args = TextUtils.join(",",mResidentVehicleNew);

                String whereClause = ResidentVehicleEntry.COLUMN_RESIDENTVEHICLE_ID_SERVER + " NOT IN (?) AND " + ResidentVehicleEntry.COLUMN_IS_SYNC + " = ?";
                String [] whereArgs = {args,String.valueOf(IS_SYNC)};
                db.delete(tableName,whereClause,whereArgs);

            }

            db.beginTransaction();

            for (ContentValues obj:cVVector){

                ResidentVehicle residentvehicle = mResidentVehicleMap.get(obj.getAsLong(ResidentVehicleEntry.COLUMN_RESIDENTVEHICLE_ID_SERVER));

                if (residentvehicle != null){
                    if (residentvehicle.getIsUpdate().equals(String.valueOf(NOT_UPDATE))){
                        String whereClause = ResidentVehicleEntry.COLUMN_RESIDENTVEHICLE_ID_SERVER + " = ?";
                        String [] whereArgs = {obj.getAsString(ResidentVehicleEntry.COLUMN_RESIDENTVEHICLE_ID_SERVER)};
                        int numUpdated = db.update(tableName,obj,whereClause,whereArgs);
                        if(numUpdated == 0){
                            obj.put(ResidentVehicleEntry.COLUMN_IS_SYNC,IS_SYNC);
                            obj.put(ResidentVehicleEntry.COLUMN_IS_UPDATE,NOT_UPDATE);
                            db.insert(tableName,null,obj);
                        }
                    }
                }
                else{
                    obj.put(ResidentVehicleEntry.COLUMN_IS_SYNC,IS_SYNC);
                    obj.put(ResidentVehicleEntry.COLUMN_IS_UPDATE,NOT_UPDATE);
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
