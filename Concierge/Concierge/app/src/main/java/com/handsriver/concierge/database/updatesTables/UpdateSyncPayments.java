package com.handsriver.concierge.database.updatesTables;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.handsriver.concierge.database.ConciergeContract.PaymentEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;

import java.util.Vector;

/**
 * Created by Created by alain_r._trouve_silva after 23-03-17.
 */

public class UpdateSyncPayments{
    private static final String TAG = "UpdateSyncPayments";
    private static final String IS_SYNC = "1";


    public static void run(Vector<ContentValues> vectorPaymentsReturn) {
        SQLiteDatabase db;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            if (vectorPaymentsReturn != null){
                for (ContentValues payments:vectorPaymentsReturn){
                    if (payments.getAsString(PaymentEntry.COLUMN_IS_SYNC).equals(IS_SYNC)){
                        String whereClause = PaymentEntry._ID + " = ? ";
                        String [] whereArgs = {payments.getAsString(PaymentEntry._ID)};
                        db.update(PaymentEntry.TABLE_NAME,payments,whereClause,whereArgs);
                    }
                }
            }

        } catch (SQLException e){
            Log.e(TAG, "SQLiteException:" + e.getMessage());
            DatabaseManager.getInstance().closeDatabase();
        }
        DatabaseManager.getInstance().closeDatabase();
    }
}
