package com.handsriver.concierge.database.updatesTables;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.handsriver.concierge.database.ConciergeContract;
import com.handsriver.concierge.database.ConciergeContract.VisitEntry;
import com.handsriver.concierge.database.ConciergeContract.VehicleEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;

import java.util.Vector;

/**
 * Created by Created by alain_r._trouve_silva after 23-03-17.
 */

public class UpdateSyncVisitsVehicles {

    private static final String TAG = "UpdateVisitsVehicles";
    private static final String IS_SYNC = "1";
    private static final String IS_UPDATE = "1";
    private static final String NOT_UPDATE = "0";

    public static void run(Vector<ContentValues> vectorVisitsReturn, Vector<ContentValues> vectorVehiclesReturn, Vector<ContentValues> vectorVehiclesUpdateReturn, Boolean isMarkExit, int hours) {
        SQLiteDatabase db;
        String tableNameVisit = VisitEntry.TABLE_NAME;
        String tableNameVehicle = VehicleEntry.TABLE_NAME;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            if (vectorVisitsReturn != null){
                for (ContentValues visits:vectorVisitsReturn){
                    if (visits.getAsString(VisitEntry.COLUMN_IS_SYNC).equals(IS_SYNC)){
                        String whereClause = VisitEntry._ID + " = ?";
                        String [] whereArgs = {visits.getAsString(VisitEntry._ID)};
                        db.update(tableNameVisit,visits,whereClause,whereArgs);
                    }
                }
            }

            if (vectorVehiclesReturn != null){
                for (ContentValues vehicles:vectorVehiclesReturn){
                    if (vehicles.getAsString(VehicleEntry.COLUMN_IS_SYNC).equals(IS_SYNC)){
                        int count = 0;
                        if (isMarkExit && hours > 0){
                            String whereClause = VehicleEntry._ID + " = ? AND " + VehicleEntry.COLUMN_EXIT_DATE + " IS NOT NULL";
                            String [] whereArgs = {vehicles.getAsString(VehicleEntry._ID)};
                            vehicles.put(VehicleEntry.COLUMN_IS_UPDATE,IS_UPDATE);
                            vehicles.put(VehicleEntry.COLUMN_IS_UPDATE_FINE,IS_UPDATE);
                            count = db.update(tableNameVehicle,vehicles,whereClause,whereArgs);

                            if (count == 0){
                                String whereClause1 = VehicleEntry._ID + " = ? AND " + VehicleEntry.COLUMN_EXIT_DATE + " IS NULL AND " + VehicleEntry.COLUMN_FINE_DATE + " IS NOT NULL";
                                String [] whereArgs1 = {vehicles.getAsString(VehicleEntry._ID)};
                                vehicles.remove(VehicleEntry.COLUMN_IS_UPDATE);
                                count = db.update(tableNameVehicle,vehicles,whereClause1,whereArgs1);
                            }

                        }else if (isMarkExit && hours == 0){
                            String whereClause = VehicleEntry._ID + " = ? AND " + VehicleEntry.COLUMN_EXIT_DATE + " IS NOT NULL";
                            String [] whereArgs = {vehicles.getAsString(VehicleEntry._ID)};
                            vehicles.put(VehicleEntry.COLUMN_IS_UPDATE,IS_UPDATE);
                            count = db.update(tableNameVehicle,vehicles,whereClause,whereArgs);

                        }else if (!isMarkExit && hours > 0){
                            String whereClause = VehicleEntry._ID + " = ? AND " + VehicleEntry.COLUMN_FINE_DATE + " IS NOT NULL";
                            String [] whereArgs = {vehicles.getAsString(VehicleEntry._ID)};
                            vehicles.put(VehicleEntry.COLUMN_IS_UPDATE,IS_UPDATE);
                            vehicles.put(VehicleEntry.COLUMN_IS_UPDATE_FINE,IS_UPDATE);
                            count = db.update(tableNameVehicle,vehicles,whereClause,whereArgs);

                        }else if (!isMarkExit && hours == 0){
                            String whereClause = VehicleEntry._ID + " = ?";
                            String [] whereArgs = {vehicles.getAsString(VehicleEntry._ID)};
                            count = db.update(tableNameVehicle,vehicles,whereClause,whereArgs);
                        }
                        if (count == 0){
                            String whereClause = VehicleEntry._ID + " = ?";
                            String [] whereArgs = {vehicles.getAsString(VehicleEntry._ID)};
                            vehicles.remove(VehicleEntry.COLUMN_IS_UPDATE);
                            vehicles.remove(VehicleEntry.COLUMN_IS_UPDATE_FINE);
                            db.update(tableNameVehicle,vehicles,whereClause,whereArgs);
                        }

                    }

                }
            }

            if (vectorVehiclesUpdateReturn != null) {
                for (ContentValues vehicles_update:vectorVehiclesUpdateReturn){
                    if (vehicles_update.getAsString(VehicleEntry.COLUMN_IS_UPDATE).equals(IS_UPDATE)){
                        if (isMarkExit && hours == 0){
                            String whereClause = VehicleEntry._ID + " = ? AND " + VehicleEntry.COLUMN_EXIT_DATE + " IS NOT NULL";
                            String [] whereArgs = {vehicles_update.getAsString(VehicleEntry._ID)};
                            db.update(tableNameVehicle,vehicles_update,whereClause,whereArgs);

                        }else if (isMarkExit && hours > 0){
                            int count = 0;
                            String whereClause = VehicleEntry._ID + " = ? AND " + VehicleEntry.COLUMN_EXIT_DATE + " IS NULL AND " + VehicleEntry.COLUMN_FINE_DATE + " IS NOT NULL AND " + VehicleEntry.COLUMN_IS_UPDATE_FINE + " = ? ";
                            String [] whereArgs = {vehicles_update.getAsString(VehicleEntry._ID),NOT_UPDATE};
                            vehicles_update.put(VehicleEntry.COLUMN_IS_UPDATE_FINE,IS_UPDATE);
                            vehicles_update.remove(VehicleEntry.COLUMN_IS_UPDATE);
                            count = db.update(tableNameVehicle,vehicles_update,whereClause,whereArgs);
                            if (count == 0){
                                String whereClause1 = VehicleEntry._ID + " = ? AND " + VehicleEntry.COLUMN_EXIT_DATE + " IS NOT NULL ";
                                String [] whereArgs1 = {vehicles_update.getAsString(VehicleEntry._ID)};
                                vehicles_update.put(VehicleEntry.COLUMN_IS_UPDATE, IS_UPDATE);
                                db.update(tableNameVehicle,vehicles_update,whereClause1,whereArgs1);
                            }
                        } else if (!isMarkExit && hours > 0) {
                            String whereClause = VehicleEntry._ID + " = ? AND " + VehicleEntry.COLUMN_FINE_DATE + " IS NOT NULL";
                            String[] whereArgs = {vehicles_update.getAsString(VehicleEntry._ID)};
                            vehicles_update.put(VehicleEntry.COLUMN_IS_UPDATE, IS_UPDATE);
                            vehicles_update.put(VehicleEntry.COLUMN_IS_UPDATE_FINE, IS_UPDATE);
                            db.update(tableNameVehicle, vehicles_update, whereClause, whereArgs);
                        }
                    }
                }
            }

        } catch (SQLException e){
            Log.e(TAG, "SQLiteException:" + e.getMessage());
            DatabaseManager.getInstance().closeDatabase();
        }
        DatabaseManager.getInstance().closeDatabase();
    }
}
