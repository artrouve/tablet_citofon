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

public class UpdateSyncCommonspacesVisits {
    private static final String TAG = "UpdateSyncCommonspacesVisits";
    private static final String IS_SYNC = "1";
    private static final String IS_UPDATE = "1";

    public static void run(Vector<ContentValues> vectorCommonspaceVisitsReturn, Vector<ContentValues> vectorCommonspaceVisitsUpdateReturn) {
        SQLiteDatabase db;
        db = DatabaseManager.getInstance().openDatabase();
        try {
            if (vectorCommonspaceVisitsReturn != null){
                for (ContentValues commonspaces_visits:vectorCommonspaceVisitsReturn){
                    if (commonspaces_visits.getAsString(CommonspaceVisitsEntry.COLUMN_IS_SYNC).equals(IS_SYNC)){
                        int count = 0;

                        String whereClause = CommonspaceVisitsEntry._ID + " = ? AND " + CommonspaceVisitsEntry.COLUMN_EXIT_VISIT + " IS NOT NULL";
                        String [] whereArgs = {commonspaces_visits.getAsString(CommonspaceVisitsEntry._ID)};
                        commonspaces_visits.put(CommonspaceVisitsEntry.COLUMN_IS_UPDATE,IS_UPDATE);
                        count = db.update(CommonspaceVisitsEntry.TABLE_NAME,commonspaces_visits,whereClause,whereArgs);

                        if (count == 0){
                            String whereClause1 = CommonspaceVisitsEntry._ID + " = ?";
                            String [] whereArgs1 = {commonspaces_visits.getAsString(CommonspaceVisitsEntry._ID)};
                            commonspaces_visits.remove(CommonspaceVisitsEntry.COLUMN_IS_UPDATE);
                            db.update(CommonspaceVisitsEntry.TABLE_NAME,commonspaces_visits,whereClause1,whereArgs1);
                        }
                    }
                }
            }

            if (vectorCommonspaceVisitsUpdateReturn != null) {
                for (ContentValues commonspaces_visits_update:vectorCommonspaceVisitsUpdateReturn){
                    if (commonspaces_visits_update.getAsString(CommonspaceVisitsEntry.COLUMN_IS_UPDATE).equals(IS_UPDATE)){
                        String whereClause = CommonspaceVisitsEntry._ID + " = ? AND " + CommonspaceVisitsEntry.COLUMN_EXIT_VISIT + " IS NOT NULL";
                        String [] whereArgs = {commonspaces_visits_update.getAsString(CommonspaceVisitsEntry._ID)};
                        db.update(CommonspaceVisitsEntry.TABLE_NAME,commonspaces_visits_update,whereClause,whereArgs);
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
