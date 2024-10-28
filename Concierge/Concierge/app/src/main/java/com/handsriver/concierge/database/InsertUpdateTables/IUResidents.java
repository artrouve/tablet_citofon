package com.handsriver.concierge.database.InsertUpdateTables;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
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
import com.handsriver.concierge.isapi.terminalfacial.ResidentsFaceSyncIsapi;
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
    public static void run(Vector<ContentValues> cVVector, Context context) {
        SQLiteDatabase db;


        ResidentsFaceSyncIsapi faceIsapi = new ResidentsFaceSyncIsapi(context);
        mResidentNew = new ArrayList<String>();
        mResidentMap = new LongSparseArray<Resident>();
        String tableName = ResidentEntry.TABLE_NAME;
        db = DatabaseManager.getInstance().openDatabase();

        try {

            String[] projection = {
                    ResidentEntry._ID,
                    ResidentEntry.COLUMN_RESIDENT_ID_SERVER,
                    ResidentEntry.COLUMN_IS_UPDATE,
                    ResidentEntry.COLUMN_IS_DELETED
            };

            Cursor residents = db.query(tableName,projection,null,null,null,null,null);

            if (residents != null){
                while (residents.moveToNext()){
                    Resident resident = new Resident();
                    resident.setId(residents.getLong(residents.getColumnIndex(ResidentEntry._ID)));
                    resident.setResidentIdServer(residents.getLong(residents.getColumnIndex(ResidentEntry.COLUMN_RESIDENT_ID_SERVER)));
                    resident.setIsUpdate(residents.getString(residents.getColumnIndex(ResidentEntry.COLUMN_IS_UPDATE)));
                    resident.setIsDelete(residents.getString(residents.getColumnIndex(ResidentEntry.COLUMN_IS_DELETED)));
                    mResidentMap.append(resident.getResidentIdServer(),resident);

                }
                residents.close();

                for (ContentValues obj:cVVector){
                    mResidentNew.add(obj.getAsString(ResidentEntry.COLUMN_RESIDENT_ID_SERVER));
                }

                String args = TextUtils.join(",",mResidentNew);

                String whereClause = ResidentEntry.COLUMN_RESIDENT_ID_SERVER + " NOT IN ("+ args +") AND " + ResidentEntry.COLUMN_IS_SYNC + " = ? ";
                String [] whereArgs = {String.valueOf(IS_SYNC)};

                //LOS QUE SE ELIMINAN DEBER SER ELIMINADOS DE LA DETECCION FACIAL
                //SE DEBE SELECCIONAR UNO POR UNO PARA QUE ESTOS SEAN ELIMINADOS
                //DEL SISTEMA DE DETECCION FACIAL

                Cursor residents_to_delete_isapi = db.query(tableName,null,whereClause,whereArgs,null,null,null);
                if (residents_to_delete_isapi != null) {
                    while (residents_to_delete_isapi.moveToNext()) {
                        Resident resident = new Resident();
                        resident.setId(residents_to_delete_isapi.getLong(residents_to_delete_isapi.getColumnIndex(ResidentEntry._ID)));
                        resident.setResidentIdServer(residents_to_delete_isapi.getLong(residents_to_delete_isapi.getColumnIndex(ResidentEntry.COLUMN_RESIDENT_ID_SERVER)));
                        faceIsapi.deleteResident(resident);
                    }
                    residents_to_delete_isapi.close();
                }

                //SE ELIMINA DE MANERA GENERAL SIN IMPORTAR SI TIENE O NO TERMINAL FACIAL
                db.delete(tableName,whereClause,whereArgs);

            }

            db.beginTransaction();

            for (ContentValues obj:cVVector){

                Resident resident = mResidentMap.get(obj.getAsLong(ResidentEntry.COLUMN_RESIDENT_ID_SERVER));

                if (resident != null){
                    if (resident.getIsUpdate().equals(String.valueOf(NOT_UPDATE)) && resident.getIsDelete().equals(String.valueOf(NOT_DELETE))){
                        String whereClause = ResidentEntry.COLUMN_RESIDENT_ID_SERVER + " = ?";
                        String [] whereArgs = {obj.getAsString(ResidentEntry.COLUMN_RESIDENT_ID_SERVER)};

                        //SE DEBE VERIFICAR QUE LA FECHA DE ACTUALIZACION SEA DIFERENTE
                        Cursor residents_to_update_isapi = db.query(tableName,null,whereClause,whereArgs,null,null,null);
                        if (residents_to_update_isapi != null) {
                            while (residents_to_update_isapi.moveToNext()) {


                                

                                if(residents_to_update_isapi.getLong(residents_to_update_isapi.getColumnIndex(ResidentEntry.COLUMN_UPDATED_AT)) != )

                                    resident_to_update.setResidentUpdatedAt();

                                Resident resident_to_update = new Resident();
                                resident_to_update.setId(residents_to_update_isapi.getLong(residents_to_update_isapi.getColumnIndex(ResidentEntry._ID)));
                                resident_to_update.setResidentIdServer(residents_to_update_isapi.getLong(residents_to_update_isapi.getColumnIndex(ResidentEntry.COLUMN_RESIDENT_ID_SERVER)));


                                faceIsapi.updateResident(resident_to_update);
                            }
                            residents_to_update_isapi.close();
                        }


                        int numUpdated = db.update(tableName,obj,whereClause,whereArgs);
                        if(numUpdated == 0){
                            obj.put(ResidentEntry.COLUMN_IS_SYNC,IS_SYNC);
                            obj.put(ResidentEntry.COLUMN_IS_UPDATE,NOT_UPDATE);
                            obj.put(ResidentEntry.COLUMN_REQUEST_CODE,NOT_REQUEST_CODE);
                            obj.put(ResidentEntry.COLUMN_IS_DELETED,NOT_DELETE);

                            //SE REALIZA UNA INSERCION EN LA BASE DE DATOS
                            //SE DEBE REALIZAR LA INSERCION EN LA DETECCION FACIAL EN CASO DE
                            //EXISTIR
                            Resident new_resident = new Resident();
                            new_resident.setId(obj.getAsLong(ResidentEntry.COLUMN_RESIDENT_ID_SERVER));
                            faceIsapi.addResident(new_resident);

                            db.insert(tableName,null,obj);
                        }
                        else{

                            //SE DEBE VALIDAR SI LA FECHA DE ULTIMA ACTUALIZACION CORRESPONDE
                            //PARA PODER REALIZAR LA ACTUALIZACION
                            Resident new_resident = new Resident();
                            new_resident.setId(obj.getAsLong(ResidentEntry.COLUMN_RESIDENT_ID_SERVER));
                            faceIsapi.addResident(new_resident);

                        }
                    }
                }
                else{
                    obj.put(ResidentEntry.COLUMN_IS_SYNC,IS_SYNC);
                    obj.put(ResidentEntry.COLUMN_IS_UPDATE,NOT_UPDATE);
                    obj.put(ResidentEntry.COLUMN_REQUEST_CODE,NOT_REQUEST_CODE);
                    obj.put(ResidentEntry.COLUMN_IS_DELETED,NOT_DELETE);
                    
                    Resident new_resident = new Resident();
                    new_resident.setId(obj.getAsLong(ResidentEntry.COLUMN_RESIDENT_ID_SERVER));
                    faceIsapi.addResident(new_resident);
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
