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

public class ResidentsVehiclesSyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private static ResidentsVehiclesSyncAdapter sResidentsVehiclesSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("ResidentsSyncService", "onCreate - ResidentsVehiclesSyncService");
        ConciergeDbHelper helper= new ConciergeDbHelper(getApplicationContext());
        DatabaseManager.initializeInstance(helper);
        synchronized (sSyncAdapterLock) {
            if (sResidentsVehiclesSyncAdapter == null) {
                sResidentsVehiclesSyncAdapter = new ResidentsVehiclesSyncAdapter(getApplicationContext(), true);
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
        return sResidentsVehiclesSyncAdapter.getSyncAdapterBinder();
    }
}
