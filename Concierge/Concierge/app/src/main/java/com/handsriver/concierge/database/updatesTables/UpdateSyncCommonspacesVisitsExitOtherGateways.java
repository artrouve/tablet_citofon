package com.handsriver.concierge.database.updatesTables;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.handsriver.concierge.database.ConciergeContract.CommonspaceVisitsEntry;
import com.handsriver.concierge.database.DatabaseManager;

import java.util.Vector;

/**
 * Created by Created by alain_r._trouve_silva after 23-03-17.
 */

public class UpdateSyncCommonspacesVisitsExitOtherGateways {
    private static final String TAG = "UpdateSyncCommonspaceVisit";
    private static final String IS_UPDATE = "1";

    public static void run(Vector<ContentValues> vectorCommonspacesVisitsExitOthersGatewaysReturn) {
        SQLiteDatabase db;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            if (vectorCommonspacesVisitsExitOthersGatewaysReturn != null){
                for (ContentValues commonspace_visit:vectorCommonspacesVisitsExitOthersGatewaysReturn){
                    if(commonspace_visit.getAsString(CommonspaceVisitsEntry.COLUMN_EXIT_VISIT) != null){
                        String whereClause = CommonspaceVisitsEntry._ID + " = ? ";
                        String [] whereArgs = {commonspace_visit.getAsString(CommonspaceVisitsEntry._ID)};
                        commonspace_visit.put(CommonspaceVisitsEntry.COLUMN_IS_UPDATE,IS_UPDATE);
                        db.update(CommonspaceVisitsEntry.TABLE_NAME,commonspace_visit,whereClause,whereArgs);
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
