package com.handsriver.concierge.database.insertsTables;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.handsriver.concierge.database.ConciergeContract.ResidentEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.sync.ConfigureSyncAccount;

/**
 * Created by Created by alain_r._trouve_silva after 03-03-17.
 */

public class InsertResidents extends AsyncTask<Void,Void,Void> {
    private SQLiteDatabase db;
    private String nullColumnHack;
    private String email;
    private String fullName;
    private int apartmentId;
    private Context mContext;


    private static final String TAG = "InsertResident";
    private static final int IS_SYNC = 0;
    private static final int IS_UPDATE = 0;
    private static final int REQUEST_CODE = 0;



    public InsertResidents(String nullColumnHack, String email, String fullName, int apartmentId, Context mContext){
        this.nullColumnHack = nullColumnHack;
        this.email = email;
        this.apartmentId = apartmentId;
        this.fullName = fullName;
        this.mContext = mContext;
    }

    @Override
    protected Void doInBackground(Void... params) {
        String tableName = ResidentEntry.TABLE_NAME;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(ResidentEntry.COLUMN_APARTMENT_ID,apartmentId);
            values.put(ResidentEntry.COLUMN_FULL_NAME,fullName);
            values.put(ResidentEntry.COLUMN_EMAIL,email);
            values.put(ResidentEntry.COLUMN_IS_SYNC,IS_SYNC);
            values.put(ResidentEntry.COLUMN_IS_UPDATE,IS_UPDATE);
            values.put(ResidentEntry.COLUMN_REQUEST_CODE,REQUEST_CODE);
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
