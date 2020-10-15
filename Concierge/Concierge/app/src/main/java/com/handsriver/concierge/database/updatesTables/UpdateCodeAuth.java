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

public class UpdateCodeAuth extends AsyncTask<Void,Void,Void> {
    private SQLiteDatabase db;
    private long id;
    private Context mContext;


    private static final String TAG = "UpdateResident";
    private static final int IS_CODE_REQUEST = 1;


    public UpdateCodeAuth(long id, Context mContext){
        this.id = id;
        this.mContext = mContext;
    }

    @Override
    protected Void doInBackground(Void... params) {
        String tableName = ResidentEntry.TABLE_NAME;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(ResidentEntry.COLUMN_REQUEST_CODE,IS_CODE_REQUEST);

            String whereClause = ResidentEntry._ID + " = ? ";
            String [] whereArgs = {String.valueOf(id)};

            db.update(tableName,values,whereClause,whereArgs);

            ConfigureSyncAccount.syncImmediatelyCodeAuth(mContext);

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
