package com.handsriver.concierge.database.InsertUpdateTables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import com.handsriver.concierge.database.ConciergeContract.BlacklistEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;

import java.util.Vector;

/**
 * Created by Created by alain_r._trouve_silva after 23-03-17.
 */

public class IUBlacklist {
    private static final String TAG = "IUBlacklist";

    public static void run(Vector<ContentValues> cVVector) {
        SQLiteDatabase db;
        String tableName = BlacklistEntry.TABLE_NAME;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            String[] projection = {
                    BlacklistEntry._ID,
                    BlacklistEntry.COLUMN_BLACKLIST_ID_SERVER
            };

            Cursor blacklist = db.query(tableName,projection,null,null,null,null,null);

            if (blacklist != null) {
                while(blacklist.moveToNext()){
                    int flag = 0;
                    for (ContentValues obj:cVVector){
                        if (blacklist.getString(blacklist.getColumnIndex(BlacklistEntry.COLUMN_BLACKLIST_ID_SERVER)).equals(obj.getAsString(BlacklistEntry.COLUMN_BLACKLIST_ID_SERVER))){
                            flag = 1;
                            break;
                        }
                    }
                    if (flag == 0){
                        String whereClause = BlacklistEntry.COLUMN_BLACKLIST_ID_SERVER + " = ?";
                        String [] whereArgs = {blacklist.getString(blacklist.getColumnIndex(BlacklistEntry.COLUMN_BLACKLIST_ID_SERVER))};
                        db.delete(tableName,whereClause,whereArgs);
                    }
                }
                blacklist.close();
            }

            for (ContentValues obj:cVVector){
                String whereClause = BlacklistEntry.COLUMN_BLACKLIST_ID_SERVER + " = ?";
                String [] whereArgs = {obj.getAsString(BlacklistEntry.COLUMN_BLACKLIST_ID_SERVER)};
                int numUpdated = db.update(tableName,obj,whereClause,whereArgs);
                if(numUpdated == 0){
                    db.insert(tableName,null,obj);
                }
            }
        } catch (SQLException e){
            Log.e(TAG, "SQLiteException:" + e.getMessage());
            DatabaseManager.getInstance().closeDatabase();
        }
        DatabaseManager.getInstance().closeDatabase();
    }
}
