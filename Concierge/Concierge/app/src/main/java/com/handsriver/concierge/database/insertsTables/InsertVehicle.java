package com.handsriver.concierge.database.insertsTables;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.handsriver.concierge.database.ConciergeContract.*;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;

/**
 * Created by Created by alain_r._trouve_silva after 03-03-17.
 */

public class InsertVehicle extends AsyncTask<Void,Void,Void> {
    private SQLiteDatabase db;
    private String nullColumnHack;
    private String licensePlate;
    private String entry;
    private int apartmentId;
    private int porterId;
    private int gatewayId;
    private String parkingNumber;

    private static final String TAG = "InsertVisits";
    private static final int IS_SYNC = 0;
    private static final int IS_UPDATE = 0;
    private static final int IS_UPDATE_FINE = 0;
    private static final int IS_SEND_ALERT = 0;



    public InsertVehicle(String nullColumnHack, String licensePlate, String entry, int apartmentId, int porterId, int gatewayId, String parkingNumber){
        this.nullColumnHack = nullColumnHack;
        this.licensePlate = licensePlate;
        this.entry = entry;
        this.apartmentId = apartmentId;
        this.porterId = porterId;
        this.gatewayId = gatewayId;
        this.parkingNumber = parkingNumber;
    }

    @Override
    protected Void doInBackground(Void... params) {
        String tableName = VehicleEntry.TABLE_NAME;
        db = DatabaseManager.getInstance().openDatabase();

        try {
            ContentValues values = new ContentValues();

            values.put(VehicleEntry.COLUMN_LICENSE_PLATE,licensePlate);
            values.put(VehicleEntry.COLUMN_APARTMENT_ID,apartmentId);
            values.put(VehicleEntry.COLUMN_PORTER_ID,porterId);
            values.put(VehicleEntry.COLUMN_ENTRY,entry);
            values.put(VehicleEntry.COLUMN_GATEWAY_ID,gatewayId);
            values.put(VehicleEntry.COLUMN_IS_SYNC,IS_SYNC);
            values.put(VehicleEntry.COLUMN_IS_UPDATE,IS_UPDATE);
            values.put(VehicleEntry.COLUMN_IS_UPDATE_FINE,IS_UPDATE_FINE);
            values.put(VehicleEntry.COLUMN_IS_SEND_ALERT_FINE,IS_SEND_ALERT);
            if (parkingNumber != null){
                values.put(VehicleEntry.COLUMN_PARKING_NUMBER,parkingNumber);
            }

            db.insert(tableName,nullColumnHack,values);

        } catch (SQLException e){
            Log.e(TAG, "SQLiteException:" + e.getMessage());
            DatabaseManager.getInstance().closeDatabase();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        DatabaseManager.getInstance().closeDatabase();
        super.onPostExecute(aVoid);
    }
}
