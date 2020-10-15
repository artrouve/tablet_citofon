package com.handsriver.concierge.database.updatesTables;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import com.handsriver.concierge.database.ConciergeContract.TimekeepingEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.sync.ConfigureSyncAccount;
import com.handsriver.concierge.utilities.Utility;

/**
 * Created by Created by alain_r._trouve_silva after 23-03-17.
 */

public class UpdateTimekeeping extends AsyncTask<Void,Void,Integer> {
    private SQLiteDatabase db;
    private String exit;
    private String porterId;
    private int exitPorterId;
    private Context mContext;

    private static final String TAG = "UpdateTimekeeping";

    public UpdateTimekeeping(String exit, String porterId, int exitPorterId, Context mContext){
        this.exit = exit;
        this.porterId = porterId;
        this.exitPorterId = exitPorterId;
        this.mContext = mContext;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        String tableName = TimekeepingEntry.TABLE_NAME;
        String hash = Utility.generateHash(exit+porterId);
        int count = 0;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(TimekeepingEntry.COLUMN_EXIT_PORTER,exit);
            values.put(TimekeepingEntry.COLUMN_EXIT_PORTER_ID,exitPorterId);
            values.put(TimekeepingEntry.COLUMN_EXIT_HASH,hash);

            String whereClause = TimekeepingEntry.COLUMN_PORTER_ID + " = ? AND " + TimekeepingEntry.COLUMN_EXIT_PORTER  + " IS NULL AND " + TimekeepingEntry.COLUMN_ENTRY_PORTER + " = (SELECT MAX (" + TimekeepingEntry.COLUMN_ENTRY_PORTER + ") FROM " + TimekeepingEntry.TABLE_NAME + " WHERE " + TimekeepingEntry.COLUMN_PORTER_ID + " = ? )";
            String [] whereArgs = {porterId,porterId};

            count = db.update(tableName,values,whereClause,whereArgs);

            ConfigureSyncAccount.syncImmediatelyTimekeeping(mContext);

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
