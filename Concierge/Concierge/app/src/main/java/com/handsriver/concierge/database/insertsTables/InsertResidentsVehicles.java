package com.handsriver.concierge.database.insertsTables;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.handsriver.concierge.database.ConciergeContract.ResidentVehicleEntry;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.sync.ConfigureSyncAccount;

/**
 * Created by Created by alain_r._trouve_silva after 03-03-17.
 */

public class InsertResidentsVehicles extends AsyncTask<Void,Void,Void> {
    private SQLiteDatabase db;
    private String nullColumnHack;
    private String plate;
    private int active;

    private int apartmentId;
    private Context mContext;


    private static final String TAG = "InsertResidentVehicle";
    private static final int IS_SYNC = 0;
    private static final int IS_UPDATE = 0;


    public InsertResidentsVehicles(String nullColumnHack, String plate, int active, int apartmentId, Context mContext){
        this.nullColumnHack = nullColumnHack;
        this.plate = plate;
        this.active = active;

        this.apartmentId = apartmentId;
        this.mContext = mContext;
    }

    @Override
    protected Void doInBackground(Void... params) {
        String tableName = ResidentVehicleEntry.TABLE_NAME;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(ResidentVehicleEntry.COLUMN_APARTMENT_ID,apartmentId);
            values.put(ResidentVehicleEntry.COLUMN_PLATE,plate);
            values.put(ResidentVehicleEntry.COLUMN_ACTIVE,active);

            values.put(ResidentVehicleEntry.COLUMN_IS_SYNC,IS_SYNC);
            values.put(ResidentVehicleEntry.COLUMN_IS_UPDATE,IS_UPDATE);

            db.insert(tableName,nullColumnHack,values);

            ConfigureSyncAccount.syncImmediatelyResidentsVehiclesTablet(mContext);
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
