package com.handsriver.concierge.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by Created by alain_r._trouve_silva after 28-02-17.
 */

public class SelectToDB extends AsyncTask<Void,Void,Cursor>{
    private String tableName;
    private String[] projection;
    private String selection;
    private String[] selectionArgs;
    private String groupBy;
    private String having;
    private String orderBy;
    private String limit;
    private static final String TAG = "SelectToDB";

    public SelectToDB(String tableName, String[] projection, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit){
        this.tableName = tableName;
        this.projection = projection;
        this.selection = selection;
        this.selectionArgs = selectionArgs;
        this.groupBy = groupBy;
        this.having = having;
        this.orderBy = orderBy;
        this.limit = limit;
    }

    @Override
    protected Cursor doInBackground(Void... params) {
        SQLiteDatabase db;
        Cursor cursor = null;
        try {
            db = DatabaseManager.getInstance().openDatabase();
            cursor = db.query(tableName,projection,selection,selectionArgs,groupBy,having,orderBy,limit);
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
