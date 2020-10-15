package com.handsriver.concierge.database.updatesTables;

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

public class UpdateResidents extends AsyncTask<Void,Void,Void> {
    private SQLiteDatabase db;
    private String email;
    private String mobile;
    private String phone;
    private String rut;
    private long id;
    private Context mContext;

    private static final String TAG = "UpdateResident";
    private static final int IS_UPDATE = 1;


    public UpdateResidents(String email,String mobile,String phone,String rut, long id, Context mContext){
        this.email = email;
        this.mobile = mobile;
        this.phone = phone;
        this.rut = rut;
        this.id = id;
        this.mContext = mContext;
    }

    @Override
    protected Void doInBackground(Void... params) {
        String tableName = ResidentEntry.TABLE_NAME;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(ResidentEntry.COLUMN_EMAIL,email);
            values.put(ResidentEntry.COLUMN_MOBILE,mobile);
            values.put(ResidentEntry.COLUMN_PHONE,phone);
            values.put(ResidentEntry.COLUMN_RUT,rut);

            values.put(ResidentEntry.COLUMN_IS_UPDATE,IS_UPDATE);

            String whereClause = ResidentEntry._ID + " = ? ";
            String [] whereArgs = {String.valueOf(id)};

            db.update(tableName,values,whereClause,whereArgs);

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
