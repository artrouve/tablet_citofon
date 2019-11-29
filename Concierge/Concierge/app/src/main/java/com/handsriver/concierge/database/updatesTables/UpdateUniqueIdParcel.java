package com.handsriver.concierge.database.updatesTables;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.handsriver.concierge.database.ConciergeContract.ParcelEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;

/**
 * Created by Created by alain_r._trouve_silva after 23-03-17.
 */

public class UpdateUniqueIdParcel extends AsyncTask<Void,Void,Integer> {
    private SQLiteDatabase db;
    private String id;
    private String unique_id;

    private static final String TAG = "UpdateUniqueIdParcel";

    public UpdateUniqueIdParcel(String id, String unique_id){
        this.id = id;
        this.unique_id = unique_id;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        String tableName = ParcelEntry.TABLE_NAME;
        int count = 0;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(ParcelEntry.COLUMN_UNIQUE_ID,unique_id);

            String whereClause = ParcelEntry._ID + " = ?";
            String [] whereArgs = {id};

            count = db.update(tableName,values,whereClause,whereArgs);

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
