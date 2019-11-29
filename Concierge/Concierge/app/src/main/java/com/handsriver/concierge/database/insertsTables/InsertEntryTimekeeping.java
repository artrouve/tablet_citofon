package com.handsriver.concierge.database.insertsTables;

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
import com.handsriver.concierge.visits.Visit;

import java.sql.Time;
import java.util.ArrayList;

/**
 * Created by Created by alain_r._trouve_silva after 03-03-17.
 */

public class InsertEntryTimekeeping extends AsyncTask<Void,Void,Void> {
    private SQLiteDatabase db;
    private String nullColumnHack;
    private String entry;
    private int gatewayId;
    private int porterId;
    private int entryPorterId;
    private Context mContext;

    private static final String TAG = "InsertEntryTimekeeping";
    private static final int IS_SYNC = 0;
    private static final int IS_UPDATE = 0;


    public InsertEntryTimekeeping(String nullColumnHack, String entry, int gatewayId, int porterId, int entryPorterId, Context mContext){
        this.nullColumnHack = nullColumnHack;
        this.entry = entry;
        this.gatewayId = gatewayId;
        this.porterId = porterId;
        this.entryPorterId = entryPorterId;
        this.mContext = mContext;
    }

    @Override
    protected Void doInBackground(Void... params) {
        String tableName = TimekeepingEntry.TABLE_NAME;
        String hash = Utility.generateHash(entry+porterId+gatewayId);
        db = DatabaseManager.getInstance().openDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(TimekeepingEntry.COLUMN_ENTRY_PORTER,entry);
            values.put(TimekeepingEntry.COLUMN_GATEWAY_ID,gatewayId);
            values.put(TimekeepingEntry.COLUMN_PORTER_ID,porterId);
            values.put(TimekeepingEntry.COLUMN_ENTRY_PORTER_ID,entryPorterId);
            values.put(TimekeepingEntry.COLUMN_IS_SYNC,IS_SYNC);
            values.put(TimekeepingEntry.COLUMN_IS_UPDATE,IS_UPDATE);
            values.put(TimekeepingEntry.COLUMN_ENTRY_HASH, hash);

            db.insert(tableName,nullColumnHack,values);

            ConfigureSyncAccount.syncImmediatelyTimekeeping(mContext);

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
