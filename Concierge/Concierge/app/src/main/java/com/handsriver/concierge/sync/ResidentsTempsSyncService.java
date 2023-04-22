package com.handsriver.concierge.sync;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
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

public class ResidentsTempsSyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private static ResidentsTempsSyncAdapter sResidentsTempsSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("ResidentsTemSyncService", "onCreate - ResidentsTempsSyncService");
        ConciergeDbHelper helper= new ConciergeDbHelper(getApplicationContext());
        DatabaseManager.initializeInstance(helper);
        synchronized (sSyncAdapterLock) {
            if (sResidentsTempsSyncAdapter == null) {
                sResidentsTempsSyncAdapter = new ResidentsTempsSyncAdapter(getApplicationContext(), true);
            }
        }

    }

    public boolean checkService(String service) {

        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (RunningServiceInfo services : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (service.equals(services.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return sResidentsTempsSyncAdapter.getSyncAdapterBinder();
    }
}
