package com.handsriver.concierge.database.deleteTables;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.handsriver.concierge.database.ConciergeContract.ResidentEntry;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.sync.ConfigureSyncAccount;

/**
 * Created by Created by alain_r._trouve_silva after 23-03-17.
 */

public class DeleteResident extends AsyncTask<Void,Void,Integer> {
    private SQLiteDatabase db;
    private String id;
    private Context mContext;
    private static final String TAG = "UpdateDeleteResident";
    public static final String LOG_TAG = DeleteResident.class.getSimpleName();
    public static final String HTTP = "http";
    public static final String HTTPS = "https";


    public DeleteResident(String id, Context mContext){
        this.id = id;
        this.mContext = mContext;

    }

    @Override
    protected Integer doInBackground(Void... params) {
        String tableName = ResidentEntry.TABLE_NAME;
        int count = 0;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(ResidentEntry.COLUMN_IS_DELETED,1);
            //values.put(ResidentEntry.COLUMN_IS_UPDATE,1);
            String whereClause = ResidentEntry._ID + " = ?";
            String [] whereArgs = {id};
            count = db.update(tableName,values,whereClause,whereArgs);
            ConfigureSyncAccount.syncImmediatelyResidentsTablet(mContext);
        } catch (SQLException e){
            Log.e(TAG, "SQLiteException:" + e.getMessage());
            DatabaseManager.getInstance().closeDatabase();
        }
        return count;
    }

    @Override
    protected void onPostExecute(Integer aInt) {
        DatabaseManager.getInstance().closeDatabase();
        super.onPostExecute(aInt);
    }


}
