package com.handsriver.concierge.database.InsertUpdateTables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.handsriver.concierge.database.ConciergeContract.PorterEntry;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.utilities.BCrypt;

/**
 * Created by Created by alain_r._trouve_silva after 23-03-17.
 */

public class IUPortersPasswordDataType {
    private static final String TAG = "IUPortersPassType";
    private static final String TABLE_TMP = "porters_tmp";
    private static final int is_update_password = 0;


    public static void run(SQLiteDatabase db) {
        String tableName = PorterEntry.TABLE_NAME;

        try {
            String[] projection = {
                    PorterEntry.COLUMN_FIRST_NAME,
                    PorterEntry.COLUMN_LAST_NAME,
                    PorterEntry.COLUMN_RUT,
                    PorterEntry.COLUMN_ACTIVE,
                    PorterEntry.COLUMN_PORTER_ID_SERVER,
                    PorterEntry.COLUMN_PASSWORD
            };

            Cursor result = db.query(TABLE_TMP,projection,null,null,null,null,null);

            while (result.moveToNext()) {

                ContentValues newRegister = new ContentValues();
                newRegister.put(PorterEntry.COLUMN_FIRST_NAME, result.getString(result.getColumnIndex(PorterEntry.COLUMN_FIRST_NAME)));
                newRegister.put(PorterEntry.COLUMN_LAST_NAME, result.getString(result.getColumnIndex(PorterEntry.COLUMN_LAST_NAME)));
                newRegister.put(PorterEntry.COLUMN_RUT, result.getString(result.getColumnIndex(PorterEntry.COLUMN_RUT)));
                newRegister.put(PorterEntry.COLUMN_PASSWORD, BCrypt.hashpw(result.getString(result.getColumnIndex(PorterEntry.COLUMN_PASSWORD)), BCrypt.gensalt(10)));
                newRegister.put(PorterEntry.COLUMN_ACTIVE, result.getInt(result.getColumnIndex(PorterEntry.COLUMN_ACTIVE)));
                newRegister.put(PorterEntry.COLUMN_PORTER_ID_SERVER, result.getInt(result.getColumnIndex(PorterEntry.COLUMN_PORTER_ID_SERVER)));
                newRegister.put(PorterEntry.COLUMN_IS_UPDATE_PASSWORD, is_update_password);
                db.insert(tableName, null, newRegister);
            }

            result.close();

        } catch (SQLException e){
            Log.e(TAG, "SQLiteException:" + e.getMessage());
        }
    }
}
