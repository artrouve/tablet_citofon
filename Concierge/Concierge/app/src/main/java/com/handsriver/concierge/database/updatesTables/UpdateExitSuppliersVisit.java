package com.handsriver.concierge.database.updatesTables;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.handsriver.concierge.database.ConciergeContract.SupplierVisitsEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.sync.ConfigureSyncAccount;

import java.util.ArrayList;

/**
 * Created by Created by alain_r._trouve_silva after 23-03-17.
 */

public class UpdateExitSuppliersVisit extends AsyncTask<Void,Void,Integer> {
    private SQLiteDatabase db;
    private String exit;
    private String obsExitSupplier;
    private int porterId;
    private ArrayList<String> arrayIdsVisit;
    private Context mContext;

    private static final String TAG = "UpdateExitSupplierVisit";

    public UpdateExitSuppliersVisit(String exit,String obsExitSupplier ,int porterId, ArrayList<String> arrayIdsVisit, Context mContext){
        this.exit = exit;
        this.porterId = porterId;
        this.obsExitSupplier = obsExitSupplier;
        this.arrayIdsVisit = arrayIdsVisit;
        this.mContext = mContext;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        String tableName = SupplierVisitsEntry.TABLE_NAME;
        int count = 0;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            ContentValues values = new ContentValues();
            for (String ids:arrayIdsVisit){
                values.put(SupplierVisitsEntry.COLUMN_EXIT_SUPPLIER,exit);
                values.put(SupplierVisitsEntry.COLUMN_EXIT_OBS,obsExitSupplier);
                values.put(SupplierVisitsEntry.COLUMN_EXIT_PORTER_ID, porterId);

                String whereClause = SupplierVisitsEntry._ID + " = ?";
                String [] whereArgs = {ids};

                count = db.update(tableName,values,whereClause,whereArgs);
            }
            ConfigureSyncAccount.syncImmediatelyVisitsSuppliers(mContext);
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
