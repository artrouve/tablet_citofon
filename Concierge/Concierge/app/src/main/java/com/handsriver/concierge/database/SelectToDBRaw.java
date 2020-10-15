package com.handsriver.concierge.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by Created by alain_r._trouve_silva after 28-02-17.
 */

public class SelectToDBRaw extends AsyncTask<Void,Void,Cursor>{
    private String query;
    private String[] selectionArgs;
    private static final String TAG = "SelectToDBRaw";

    public SelectToDBRaw(String query, String[] selectionArgs){
        this.query = query;
        this.selectionArgs = selectionArgs;
    }

    @Override
    protected Cursor doInBackground(Void... params) {
        SQLiteDatabase db;
        Cursor cursor = null;
        try {
            db = DatabaseManager.getInstance().openDatabase();
            cursor = db.rawQuery(query,selectionArgs);
        } catch  (SQLiteException e){
            Log.e(TAG, "SQLiteException:" + e.getMessage());
            DatabaseManager.getInstance().closeDatabase();
        }
        return cursor;
    }

    @Override
    protected void onPostExecute(Cursor cursor) {
        DatabaseManager.getInstance().closeDatabase();
        super.onPostExecute(cursor);
    }
}
