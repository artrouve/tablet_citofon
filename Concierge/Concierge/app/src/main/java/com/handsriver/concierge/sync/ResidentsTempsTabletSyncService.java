package com.handsriver.concierge.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;

/**
 * Created by Created by alain_r._trouve_silva after 27-04-17.
 */

public class ResidentsTempsTabletSyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private static ResidentsTempsTabletSyncAdapter sResidentsTempsTabletSyncAdapter = null;


    @Override
    public void onCreate() {
        Log.d("ResidentsTemTabletSyncA", "onCreate");
        ConciergeDbHelper helper= new ConciergeDbHelper(getApplicationContext());
        DatabaseManager.initializeInstance(helper);
        synchronized (sSyncAdapterLock){
            if(sResidentsTempsTabletSyncAdapter == null){
                sResidentsTempsTabletSyncAdapter = new ResidentsTempsTabletSyncAdapter(getApplicationContext(),true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return sResidentsTempsTabletSyncAdapter.getSyncAdapterBinder();
    }
}
