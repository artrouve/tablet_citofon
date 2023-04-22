package com.handsriver.concierge.database.updatesTables;

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

public class UpdateResidentsTemps extends AsyncTask<Void,Void,Void> {
    private SQLiteDatabase db;
    private String full_name;
    private String email;
    private String phone;
    private String rut;
    private String startDate;
    private String endDate;

    private long id;
    private Context mContext;

    private static final String TAG = "UpdateResidentTemp";
    private static final int IS_UPDATE = 1;


    public UpdateResidentsTemps(String full_name,String email, String phone, String rut, String startDate, String endDate,long id, Context mContext){
        this.full_name = full_name;
        this.email = email;
        this.phone = phone;
        this.rut = rut;
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.mContext = mContext;
    }

    @Override
    protected Void doInBackground(Void... params) {
        String tableName = ResidentTempEntry.TABLE_NAME;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(ResidentTempEntry.COLUMN_FULL_NAME,full_name);
            values.put(ResidentTempEntry.COLUMN_EMAIL,email);
            values.put(ResidentTempEntry.COLUMN_PHONE,phone);
            values.put(ResidentTempEntry.COLUMN_RUT,rut);

            values.put(ResidentTempEntry.COLUMN_START_DATE,startDate);
            values.put(ResidentTempEntry.COLUMN_END_DATE,endDate);

            values.put(ResidentTempEntry.COLUMN_IS_UPDATE,IS_UPDATE);

            String whereClause = ResidentTempEntry._ID + " = ? ";
            String [] whereArgs = {String.valueOf(id)};

            db.update(tableName,values,whereClause,whereArgs);

            ConfigureSyncAccount.syncImmediatelyResidentsTempsTablet(mContext);

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
