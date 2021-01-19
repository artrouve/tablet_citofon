package com.handsriver.concierge.database.updatesTables;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.handsriver.concierge.database.ConciergeContract.CommonspaceVisitsEntry;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.sync.ConfigureSyncAccount;

import java.util.ArrayList;

/**
 * Created by Created by alain_r._trouve_silva after 23-03-17.
 */

public class UpdateExitCommonspacesVisit extends AsyncTask<Void,Void,Integer> {
    private SQLiteDatabase db;
    private String exit;
    private int porterId;
    private String IdVisit;
    private Context mContext;

    private static final String TAG = "UpdateExitCommonspaceVisit";

    public UpdateExitCommonspacesVisit(String exit,int porterId,String IdVisit, Context mContext){
        this.exit = exit;
        this.porterId = porterId;
        this.IdVisit = IdVisit;
        this.mContext = mContext;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        String tableName = CommonspaceVisitsEntry.TABLE_NAME;
        int count = 0;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            ContentValues values = new ContentValues();

            values.put(CommonspaceVisitsEntry.COLUMN_EXIT_VISIT,exit);
            //values.put(CommonspaceVisitsEntry.COLUMN_EXIT_OBS,obsExitSupplier);
            values.put(CommonspaceVisitsEntry.COLUMN_EXIT_PORTER_ID, porterId);

            String whereClause = CommonspaceVisitsEntry._ID + " = ?";
            String [] whereArgs = {IdVisit};

            count = db.update(tableName,values,whereClause,whereArgs);

            ConfigureSyncAccount.syncImmediatelyVisitsCommonspaces(mContext);
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
