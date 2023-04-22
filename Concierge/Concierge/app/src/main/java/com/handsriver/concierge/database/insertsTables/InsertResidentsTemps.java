package com.handsriver.concierge.database.insertsTables;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.handsriver.concierge.database.ConciergeContract.ResidentTempEntry;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.sync.ConfigureSyncAccount;

/**
 * Created by Created by alain_r._trouve_silva after 03-03-17.
 */

public class InsertResidentsTemps extends AsyncTask<Void,Void,Void> {
    private SQLiteDatabase db;
    private String nullColumnHack;
    private String email;
    private String fullName;


    private String phone;
    private String rut;
    private String startDate;
    private String endDate;


    private int apartmentId;
    private Context mContext;


    private static final String TAG = "InsertResident";
    private static final int IS_SYNC = 0;
    private static final int IS_UPDATE = 0;


    public InsertResidentsTemps(String nullColumnHack, String email, String fullName, String mobile, String phone, String rut, String startDate, String endDate, int apartmentId, Context mContext){
        this.nullColumnHack = nullColumnHack;
        this.email = email;

        this.phone = phone;
        this.rut = rut;
        this.startDate = startDate;
        this.endDate = endDate;

        this.apartmentId = apartmentId;
        this.fullName = fullName;
        this.mContext = mContext;
    }

    @Override
    protected Void doInBackground(Void... params) {
        String tableName = ResidentTempEntry.TABLE_NAME;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(ResidentTempEntry.COLUMN_APARTMENT_ID,apartmentId);
            values.put(ResidentTempEntry.COLUMN_FULL_NAME,fullName);
            values.put(ResidentTempEntry.COLUMN_EMAIL,email);


            values.put(ResidentTempEntry.COLUMN_PHONE,phone);
            values.put(ResidentTempEntry.COLUMN_RUT,rut);

            values.put(ResidentTempEntry.COLUMN_START_DATE,startDate);
            values.put(ResidentTempEntry.COLUMN_END_DATE,endDate);

            values.put(ResidentTempEntry.COLUMN_IS_SYNC,IS_SYNC);
            values.put(ResidentTempEntry.COLUMN_IS_UPDATE,IS_UPDATE);
            db.insert(tableName,nullColumnHack,values);

            ConfigureSyncAccount.syncImmediatelyResidentsTablet(mContext);
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
