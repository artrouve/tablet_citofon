package com.handsriver.concierge.database.deleteTables;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.handsriver.concierge.database.ConciergeContract.PaymentEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.utilities.Utility;

/**
 * Created by Created by alain_r._trouve_silva after 03-03-17.
 */

public class DeletePayments{
    private static final String TAG = "DeletePayments";
    private static final String IS_SYNC = "1";


    public static void run() {
        SQLiteDatabase db;
        String tableName = PaymentEntry.TABLE_NAME;
        db = DatabaseManager.getInstance().openDatabase();
        try {

            String whereClause = PaymentEntry.COLUMN_IS_SYNC + " = ? AND " + PaymentEntry.COLUMN_DATE_REGISTER + " < '" + Utility.getDateSimpleForServer7Days()+"'";

            Log.e(TAG, "whereClause:" + whereClause);

            String [] whereArgs = {IS_SYNC};

            db.delete(tableName,whereClause,whereArgs);

        } catch (SQLException e){
            Log.e(TAG, "SQLiteException:" + e.getMessage());
            DatabaseManager.getInstance().closeDatabase();
        }
        DatabaseManager.getInstance().closeDatabase();
    }

}
