package com.handsriver.concierge.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncRequest;
import android.os.Build;
import android.os.Bundle;

import com.handsriver.concierge.R;

/**
 * Created by Created by alain_r._trouve_silva after 03-05-17.
 */

public class ConfigureSyncAccount {

    private static final int MIN_IN_SECS = 60;
    private static final int HOUR_IN_SECS = 60 * MIN_IN_SECS;

    private static final int SYNC_INTERVAL_APARTMENTS = 24 * HOUR_IN_SECS;
    private static final int SYNC_FLEXTIME_APARTMENTS = 30 * MIN_IN_SECS;

    private static final int SYNC_INTERVAL_PORTERS = 5 * HOUR_IN_SECS;
    private static final int SYNC_FLEXTIME_PORTERS = 20 * MIN_IN_SECS;

    private static final int SYNC_INTERVAL_PORTERS_TABLET = 5 * HOUR_IN_SECS;
    private static final int SYNC_FLEXTIME_PORTERS_TABLET = 15 * MIN_IN_SECS;

    private static final int SYNC_INTERVAL_RESIDENTS = 5 * MIN_IN_SECS;
    private static final int SYNC_FLEXTIME_RESIDENTS = MIN_IN_SECS;

    private static final int SYNC_INTERVAL_FINE_ALERT_AUTOMATIC = 2 * MIN_IN_SECS;
    private static final int SYNC_FLEXTIME_FINE_ALERT_AUTOMATIC = MIN_IN_SECS;

    private static final int SYNC_INTERVAL_ALERT_AUTOMATIC = 2 * MIN_IN_SECS;
    private static final int SYNC_FLEXTIME_ALERT_AUTOMATIC = MIN_IN_SECS;

    private static final int SYNC_INTERVAL_RESIDENTS_TABLET = HOUR_IN_SECS;
    private static final int SYNC_FLEXTIME_RESIDENTS_TABLET = 10 * MIN_IN_SECS;

    private static final int SYNC_INTERVAL_BLACKLIST = HOUR_IN_SECS;
    private static final int SYNC_FLEXTIME_BLACKLIST = 15 * MIN_IN_SECS;

    private static final int SYNC_INTERVAL_SUPPLIERS = 24 * HOUR_IN_SECS;
    private static final int SYNC_FLEXTIME_SUPPLIERS = 20 * MIN_IN_SECS;

    private static final int SYNC_INTERVAL_VISITS_VEHICLES = HOUR_IN_SECS;
    private static final int SYNC_FLEXTIME_VISITS_VEHICLES = 25 * MIN_IN_SECS;

    private static final int SYNC_INTERVAL_VISITS_VEHICLES_DELETE = 24 * HOUR_IN_SECS;
    private static final int SYNC_FLEXTIME_VISITS_VEHICLES_DELETE = 35 * MIN_IN_SECS;

    private static final int SYNC_INTERVAL_PARCELS = HOUR_IN_SECS;
    private static final int SYNC_FLEXTIME_PARCELS = 7 * MIN_IN_SECS;

    private static final int SYNC_INTERVAL_PARCELS_DELETE = 24 * HOUR_IN_SECS;
    private static final int SYNC_FLEXTIME_PARCELS_DELETE = 25 * MIN_IN_SECS;

    private static final int SYNC_INTERVAL_PAYMENTS = HOUR_IN_SECS;
    private static final int SYNC_FLEXTIME_PAYMENTS = 13 * MIN_IN_SECS;

    private static final int SYNC_INTERVAL_PAYMENTS_DELETE = 24 * HOUR_IN_SECS;
    private static final int SYNC_FLEXTIME_PAYMENTS_DELETE = 40 * MIN_IN_SECS;

    private static final int SYNC_INTERVAL_PARKINGS = 24 * HOUR_IN_SECS;
    private static final int SYNC_FLEXTIME_PARKINGS = 25 * MIN_IN_SECS;

    private static final int SYNC_INTERVAL_WAREHOUSES = 24 * HOUR_IN_SECS;
    private static final int SYNC_FLEXTIME_WAREHOUSES = 40 * MIN_IN_SECS;

    private static final int SYNC_INTERVAL_TIMEKEEPING = HOUR_IN_SECS;
    private static final int SYNC_FLEXTIME_TIMEKEEPING = 17 * MIN_IN_SECS;

    private static final int SYNC_INTERVAL_TIMEKEEPING_DELETE = 24 * HOUR_IN_SECS;
    private static final int SYNC_FLEXTIME_TIMEKEEPING_DELETE = 45 * MIN_IN_SECS;

    private static final int SYNC_INTERVAL_VISITS_SUPPLIERS = HOUR_IN_SECS;
    private static final int SYNC_FLEXTIME_VISITS_SUPPLIERS = 15 * MIN_IN_SECS;

    private static final int SYNC_INTERVAL_VISITS_OTHER_GATEWAYS = 2 * HOUR_IN_SECS;
    private static final int SYNC_FLEXTIME_VISITS_OTHER_GATEWAYS = 27 * MIN_IN_SECS;

    private static final int SYNC_INTERVAL_TIMEKEEPING_OTHER_GATEWAYS = 5 * HOUR_IN_SECS;
    private static final int SYNC_FLEXTIME_TIMEKEEPING_OTHER_GATEWAYS = 33 * MIN_IN_SECS;

    private static final int SYNC_INTERVAL_TIMEKEEPING_EXIT_OTHER_GATEWAYS = HOUR_IN_SECS;
    private static final int SYNC_FLEXTIME_TIMEKEEPING_EXIT_OTHER_GATEWAYS = 7 * MIN_IN_SECS;

    private static final int SYNC_INTERVAL_VISITS_SUPPLIERS_OTHER_GATEWAYS = HOUR_IN_SECS;
    private static final int SYNC_FLEXTIME_VISITS_SUPPLIERS_OTHER_GATEWAYS = 21 * MIN_IN_SECS;

    private static final int SYNC_INTERVAL_VISITS_SUPPLIERS_EXIT_OTHER_GATEWAYS = HOUR_IN_SECS;
    private static final int SYNC_FLEXTIME_VISITS_SUPPLIERS_EXIT_OTHER_GATEWAYS = 27 * MIN_IN_SECS;

    private static final int SYNC_INTERVAL_CODE_AUTH = HOUR_IN_SECS;
    private static final int SYNC_FLEXTIME_CODE_AUTH = 16 * MIN_IN_SECS;

    private static final int SYNC_INTERVAL_WHITELIST = HOUR_IN_SECS;
    private static final int SYNC_FLEXTIME_WHITELIST = 12 * MIN_IN_SECS;

    private static final int SYNC_INTERVAL_GOOGLE_PLAY_SERVICE = 24 * HOUR_IN_SECS;
    private static final int SYNC_FLEXTIME_GOOGLE_PLAY_SERVICE = 30 * MIN_IN_SECS;

    private static void configurePeriodicSync(Context context) {
        Account account = getSyncAccount(context);
        String authority_apartments = context.getString(R.string.content_authority_apartments);
        String authority_porters = context.getString(R.string.content_authority_porters);
        String authority_porters_tablet = context.getString(R.string.content_authority_porters_tablet);
        String authority_residents = context.getString(R.string.content_authority_residents);
        String authority_residents_tablet = context.getString(R.string.content_authority_residents_tablet);
        String authority_blacklist = context.getString(R.string.content_authority_blacklist);
        String authority_suppliers = context.getString(R.string.content_authority_suppliers);
        String authority_visits_vehicles = context.getString(R.string.content_authority_visits_vehicles);
        String authority_visits_vehicles_delete = context.getString(R.string.content_authority_visits_vehicles_delete);
        String authority_parcels = context.getString(R.string.content_authority_parcels);
        String authority_parcels_delete = context.getString(R.string.content_authority_parcels_delete);
        String authority_payments = context.getString(R.string.content_authority_payments);
        String authority_payments_delete = context.getString(R.string.content_authority_payments_delete);
        String authority_parkings = context.getString(R.string.content_authority_parkings);
        String authority_warehouses = context.getString(R.string.content_authority_warehouses);
        String authority_timekeeping = context.getString(R.string.content_authority_timekeeping);
        String authority_timekeeping_delete = context.getString(R.string.content_authority_timekeeping_delete);
        String authority_visits_suppliers = context.getString(R.string.content_authority_visits_suppliers);
        String authority_visits_others_gateways = context.getString(R.string.content_authority_visits_others_gateways);
        String authority_timekeeping_others_gateways = context.getString(R.string.content_authority_timekeeping_others_gateways);
        String authority_timekeeping_exit_others_gateways = context.getString(R.string.content_authority_timekeeping_exit_others_gateways);
        String authority_visits_suppliers_others_gateways = context.getString(R.string.content_authority_visits_suppliers_others_gateways);
        String authority_visits_suppliers_exit_others_gateways = context.getString(R.string.content_authority_visits_suppliers_exit_others_gateways);
        String authority_code_auth = context.getString(R.string.content_authority_residents_codeauth);
        String authority_whitelist = context.getString(R.string.content_authority_whitelist);
        String authority_fine_automatic = context.getString(R.string.content_authority_vehicle_fine_alert_automatic);
        String authority_alert_automatic = context.getString(R.string.content_authority_vehicle_alert_automatic);
        String authority_google_play_service = context.getString(R.string.content_authority_update_google_play_service);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SyncRequest request_apartments = new SyncRequest.Builder().syncPeriodic(SYNC_INTERVAL_APARTMENTS, SYNC_FLEXTIME_APARTMENTS).setSyncAdapter(account, authority_apartments).setExtras(new Bundle()).build();
            SyncRequest request_porters = new SyncRequest.Builder().syncPeriodic(SYNC_INTERVAL_PORTERS, SYNC_FLEXTIME_PORTERS).setSyncAdapter(account, authority_porters).setExtras(new Bundle()).build();
            SyncRequest request_porters_tablet = new SyncRequest.Builder().syncPeriodic(SYNC_INTERVAL_PORTERS_TABLET, SYNC_FLEXTIME_PORTERS_TABLET).setSyncAdapter(account, authority_porters_tablet).setExtras(new Bundle()).build();
            SyncRequest request_residents_tablet = new SyncRequest.Builder().syncPeriodic(SYNC_INTERVAL_RESIDENTS_TABLET, SYNC_FLEXTIME_RESIDENTS_TABLET).setSyncAdapter(account, authority_residents_tablet).setExtras(new Bundle()).build();
            SyncRequest request_residents = new SyncRequest.Builder().syncPeriodic(SYNC_INTERVAL_RESIDENTS, SYNC_FLEXTIME_RESIDENTS).setSyncAdapter(account, authority_residents).setExtras(new Bundle()).build();
            SyncRequest request_blacklist = new SyncRequest.Builder().syncPeriodic(SYNC_INTERVAL_BLACKLIST, SYNC_FLEXTIME_BLACKLIST).setSyncAdapter(account, authority_blacklist).setExtras(new Bundle()).build();
            SyncRequest request_suppliers = new SyncRequest.Builder().syncPeriodic(SYNC_INTERVAL_SUPPLIERS, SYNC_FLEXTIME_SUPPLIERS).setSyncAdapter(account, authority_suppliers).setExtras(new Bundle()).build();
            SyncRequest request_visits_vehicles = new SyncRequest.Builder().syncPeriodic(SYNC_INTERVAL_VISITS_VEHICLES, SYNC_FLEXTIME_VISITS_VEHICLES).setSyncAdapter(account, authority_visits_vehicles).setExtras(new Bundle()).build();
            SyncRequest request_visits_vehicles_delete = new SyncRequest.Builder().syncPeriodic(SYNC_INTERVAL_VISITS_VEHICLES_DELETE, SYNC_FLEXTIME_VISITS_VEHICLES_DELETE).setSyncAdapter(account, authority_visits_vehicles_delete).setExtras(new Bundle()).build();
            SyncRequest request_parcels = new SyncRequest.Builder().syncPeriodic(SYNC_INTERVAL_PARCELS, SYNC_FLEXTIME_PARCELS).setSyncAdapter(account, authority_parcels).setExtras(new Bundle()).build();
            SyncRequest request_parcels_delete = new SyncRequest.Builder().syncPeriodic(SYNC_INTERVAL_PARCELS_DELETE, SYNC_FLEXTIME_PARCELS_DELETE).setSyncAdapter(account, authority_parcels_delete).setExtras(new Bundle()).build();
            SyncRequest request_payments = new SyncRequest.Builder().syncPeriodic(SYNC_INTERVAL_PAYMENTS, SYNC_FLEXTIME_PAYMENTS).setSyncAdapter(account, authority_payments).setExtras(new Bundle()).build();
            SyncRequest request_payments_delete = new SyncRequest.Builder().syncPeriodic(SYNC_INTERVAL_PAYMENTS_DELETE, SYNC_FLEXTIME_PAYMENTS_DELETE).setSyncAdapter(account, authority_payments_delete).setExtras(new Bundle()).build();
            SyncRequest request_parkings = new SyncRequest.Builder().syncPeriodic(SYNC_INTERVAL_PARKINGS, SYNC_FLEXTIME_PARKINGS).setSyncAdapter(account, authority_parkings).setExtras(new Bundle()).build();
            SyncRequest request_warehouses = new SyncRequest.Builder().syncPeriodic(SYNC_INTERVAL_WAREHOUSES, SYNC_FLEXTIME_WAREHOUSES).setSyncAdapter(account, authority_warehouses).setExtras(new Bundle()).build();
            SyncRequest request_timekeeping = new SyncRequest.Builder().syncPeriodic(SYNC_INTERVAL_TIMEKEEPING, SYNC_FLEXTIME_TIMEKEEPING).setSyncAdapter(account, authority_timekeeping).setExtras(new Bundle()).build();
            SyncRequest request_timekeeping_delete = new SyncRequest.Builder().syncPeriodic(SYNC_INTERVAL_TIMEKEEPING_DELETE, SYNC_FLEXTIME_TIMEKEEPING_DELETE).setSyncAdapter(account, authority_timekeeping_delete).setExtras(new Bundle()).build();
            SyncRequest request_visits_suppliers = new SyncRequest.Builder().syncPeriodic(SYNC_INTERVAL_VISITS_SUPPLIERS, SYNC_FLEXTIME_VISITS_SUPPLIERS).setSyncAdapter(account, authority_visits_suppliers).setExtras(new Bundle()).build();
            SyncRequest request_visits_others_gateways = new SyncRequest.Builder().syncPeriodic(SYNC_INTERVAL_VISITS_OTHER_GATEWAYS, SYNC_FLEXTIME_VISITS_OTHER_GATEWAYS).setSyncAdapter(account, authority_visits_others_gateways).setExtras(new Bundle()).build();
            SyncRequest request_timekeeping_others_gateways = new SyncRequest.Builder().syncPeriodic(SYNC_INTERVAL_TIMEKEEPING_OTHER_GATEWAYS, SYNC_FLEXTIME_TIMEKEEPING_OTHER_GATEWAYS).setSyncAdapter(account, authority_timekeeping_others_gateways).setExtras(new Bundle()).build();
            SyncRequest request_timekeeping_exit_others_gateways = new SyncRequest.Builder().syncPeriodic(SYNC_INTERVAL_TIMEKEEPING_EXIT_OTHER_GATEWAYS, SYNC_FLEXTIME_TIMEKEEPING_EXIT_OTHER_GATEWAYS).setSyncAdapter(account, authority_timekeeping_exit_others_gateways).setExtras(new Bundle()).build();
            SyncRequest request_visits_suppliers_others_gateways = new SyncRequest.Builder().syncPeriodic(SYNC_INTERVAL_VISITS_SUPPLIERS_OTHER_GATEWAYS, SYNC_FLEXTIME_VISITS_SUPPLIERS_OTHER_GATEWAYS).setSyncAdapter(account, authority_visits_suppliers_others_gateways).setExtras(new Bundle()).build();
            SyncRequest request_visits_suppliers_exit_others_gateways = new SyncRequest.Builder().syncPeriodic(SYNC_INTERVAL_VISITS_SUPPLIERS_EXIT_OTHER_GATEWAYS, SYNC_FLEXTIME_VISITS_SUPPLIERS_EXIT_OTHER_GATEWAYS).setSyncAdapter(account, authority_visits_suppliers_exit_others_gateways).setExtras(new Bundle()).build();
            SyncRequest request_code_auth = new SyncRequest.Builder().syncPeriodic(SYNC_INTERVAL_CODE_AUTH, SYNC_FLEXTIME_CODE_AUTH).setSyncAdapter(account, authority_code_auth).setExtras(new Bundle()).build();
            SyncRequest request_whitelist = new SyncRequest.Builder().syncPeriodic(SYNC_INTERVAL_WHITELIST, SYNC_FLEXTIME_WHITELIST).setSyncAdapter(account, authority_whitelist).setExtras(new Bundle()).build();
            SyncRequest request_fine_automatic = new SyncRequest.Builder().syncPeriodic(SYNC_INTERVAL_FINE_ALERT_AUTOMATIC, SYNC_FLEXTIME_FINE_ALERT_AUTOMATIC).setSyncAdapter(account, authority_fine_automatic).setExtras(new Bundle()).build();
            SyncRequest request_alert_automatic = new SyncRequest.Builder().syncPeriodic(SYNC_INTERVAL_ALERT_AUTOMATIC, SYNC_FLEXTIME_ALERT_AUTOMATIC).setSyncAdapter(account, authority_alert_automatic).setExtras(new Bundle()).build();
            SyncRequest request_google_play_services = new SyncRequest.Builder().syncPeriodic(SYNC_INTERVAL_GOOGLE_PLAY_SERVICE, SYNC_FLEXTIME_GOOGLE_PLAY_SERVICE).setSyncAdapter(account, authority_google_play_service).setExtras(new Bundle()).build();

            ContentResolver.requestSync(request_apartments);
            ContentResolver.requestSync(request_porters);
            ContentResolver.requestSync(request_porters_tablet);
            ContentResolver.requestSync(request_residents_tablet);
            ContentResolver.requestSync(request_residents);
            ContentResolver.requestSync(request_blacklist);
            ContentResolver.requestSync(request_suppliers);
            ContentResolver.requestSync(request_visits_vehicles);
            ContentResolver.requestSync(request_visits_vehicles_delete);
            ContentResolver.requestSync(request_parcels);
            ContentResolver.requestSync(request_parcels_delete);
            ContentResolver.requestSync(request_payments);
            ContentResolver.requestSync(request_payments_delete);
            ContentResolver.requestSync(request_parkings);
            ContentResolver.requestSync(request_warehouses);
            ContentResolver.requestSync(request_timekeeping);
            ContentResolver.requestSync(request_timekeeping_delete);
            ContentResolver.requestSync(request_visits_suppliers);
            ContentResolver.requestSync(request_visits_others_gateways);
            ContentResolver.requestSync(request_timekeeping_others_gateways);
            ContentResolver.requestSync(request_timekeeping_exit_others_gateways);
            ContentResolver.requestSync(request_visits_suppliers_others_gateways);
            ContentResolver.requestSync(request_visits_suppliers_exit_others_gateways);
            ContentResolver.requestSync(request_code_auth);
            ContentResolver.requestSync(request_whitelist);
            ContentResolver.requestSync(request_fine_automatic);
            ContentResolver.requestSync(request_alert_automatic);
            ContentResolver.requestSync(request_google_play_services);


        } else {
            ContentResolver.addPeriodicSync(account, authority_apartments, new Bundle(), SYNC_INTERVAL_APARTMENTS);
            ContentResolver.addPeriodicSync(account, authority_porters, new Bundle(), SYNC_INTERVAL_PORTERS);
            ContentResolver.addPeriodicSync(account, authority_porters_tablet, new Bundle(), SYNC_INTERVAL_PORTERS_TABLET);
            ContentResolver.addPeriodicSync(account, authority_residents_tablet, new Bundle(), SYNC_INTERVAL_RESIDENTS_TABLET);
            ContentResolver.addPeriodicSync(account, authority_residents, new Bundle(), SYNC_INTERVAL_RESIDENTS);
            ContentResolver.addPeriodicSync(account, authority_blacklist, new Bundle(), SYNC_INTERVAL_BLACKLIST);
            ContentResolver.addPeriodicSync(account, authority_suppliers, new Bundle(), SYNC_INTERVAL_SUPPLIERS);
            ContentResolver.addPeriodicSync(account, authority_visits_vehicles, new Bundle(), SYNC_INTERVAL_VISITS_VEHICLES);
            ContentResolver.addPeriodicSync(account, authority_visits_vehicles_delete, new Bundle(), SYNC_INTERVAL_VISITS_VEHICLES_DELETE);
            ContentResolver.addPeriodicSync(account, authority_parcels, new Bundle(), SYNC_INTERVAL_PARCELS);
            ContentResolver.addPeriodicSync(account, authority_parcels_delete, new Bundle(), SYNC_INTERVAL_PARCELS_DELETE);
            ContentResolver.addPeriodicSync(account, authority_payments, new Bundle(), SYNC_INTERVAL_PAYMENTS);
            ContentResolver.addPeriodicSync(account, authority_payments_delete, new Bundle(), SYNC_INTERVAL_PAYMENTS_DELETE);
            ContentResolver.addPeriodicSync(account, authority_parkings, new Bundle(), SYNC_INTERVAL_PARKINGS);
            ContentResolver.addPeriodicSync(account, authority_warehouses, new Bundle(), SYNC_INTERVAL_WAREHOUSES);
            ContentResolver.addPeriodicSync(account, authority_timekeeping, new Bundle(), SYNC_INTERVAL_TIMEKEEPING);
            ContentResolver.addPeriodicSync(account, authority_timekeeping_delete, new Bundle(), SYNC_INTERVAL_TIMEKEEPING_DELETE);
            ContentResolver.addPeriodicSync(account, authority_visits_suppliers, new Bundle(), SYNC_INTERVAL_VISITS_SUPPLIERS);
            ContentResolver.addPeriodicSync(account, authority_visits_others_gateways, new Bundle(), SYNC_INTERVAL_VISITS_OTHER_GATEWAYS);
            ContentResolver.addPeriodicSync(account, authority_timekeeping_others_gateways, new Bundle(), SYNC_INTERVAL_TIMEKEEPING_OTHER_GATEWAYS);
            ContentResolver.addPeriodicSync(account, authority_timekeeping_exit_others_gateways, new Bundle(), SYNC_INTERVAL_TIMEKEEPING_EXIT_OTHER_GATEWAYS);
            ContentResolver.addPeriodicSync(account, authority_visits_suppliers_others_gateways, new Bundle(), SYNC_INTERVAL_VISITS_SUPPLIERS_OTHER_GATEWAYS);
            ContentResolver.addPeriodicSync(account, authority_visits_suppliers_exit_others_gateways, new Bundle(), SYNC_INTERVAL_VISITS_SUPPLIERS_EXIT_OTHER_GATEWAYS);
            ContentResolver.addPeriodicSync(account, authority_code_auth, new Bundle(), SYNC_INTERVAL_CODE_AUTH);
            ContentResolver.addPeriodicSync(account, authority_whitelist, new Bundle(), SYNC_INTERVAL_WHITELIST);
            ContentResolver.addPeriodicSync(account, authority_fine_automatic, new Bundle(), SYNC_INTERVAL_FINE_ALERT_AUTOMATIC);
            ContentResolver.addPeriodicSync(account, authority_alert_automatic, new Bundle(), SYNC_INTERVAL_ALERT_AUTOMATIC);
            ContentResolver.addPeriodicSync(account, authority_google_play_service, new Bundle(), SYNC_INTERVAL_GOOGLE_PLAY_SERVICE);

        }
    }

    public static void syncImmediatelyPortersTablet(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.content_authority_porters_tablet), bundle);
    }

    public static void syncImmediatelyResidentsTablet(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.content_authority_residents_tablet), bundle);
    }

    public static void syncImmediatelyVisitsAndVehicles(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.content_authority_visits_vehicles), bundle);
    }

    public static void syncImmediatelyParcels(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.content_authority_parcels), bundle);
    }

    public static void syncImmediatelyPayments(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.content_authority_payments), bundle);
    }

    public static void syncImmediatelyTimekeeping(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.content_authority_timekeeping), bundle);
    }

    public static void syncImmediatelyVisitsSuppliers(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.content_authority_visits_suppliers), bundle);
    }

    public static void syncImmediatelyCodeAuth(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);

        ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.content_authority_residents_codeauth), bundle);
    }

    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.content_authority_parkings), bundle);
        ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.content_authority_warehouses), bundle);
        ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.content_authority_residents), bundle);
        ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.content_authority_blacklist), bundle);
        ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.content_authority_suppliers), bundle);
        ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.content_authority_apartments), bundle);
        ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.content_authority_porters), bundle);
        ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.content_authority_whitelist), bundle);
    }

    private static Account getSyncAccount(Context context) {
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        Account newAccount = new Account(context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        if ( null == accountManager.getPassword(newAccount) ) {

            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }

            onAccountCreated(newAccount, context);
        }

        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {

        ConfigureSyncAccount.configurePeriodicSync(context);

        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority_apartments), true);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority_porters), true);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority_porters_tablet), true);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority_residents_tablet), true);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority_residents), true);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority_blacklist), true);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority_suppliers), true);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority_visits_vehicles), true);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority_visits_vehicles_delete), true);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority_parcels), true);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority_parcels_delete), true);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority_payments), true);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority_payments_delete), true);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority_parkings), true);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority_warehouses), true);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority_timekeeping), true);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority_timekeeping_delete), true);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority_visits_suppliers), true);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority_residents_codeauth), true);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority_whitelist), true);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority_vehicle_fine_alert_automatic), true);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority_vehicle_alert_automatic), true);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority_update_google_play_service), true);

        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority_visits_others_gateways), false);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority_timekeeping_others_gateways), false);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority_timekeeping_exit_others_gateways), false);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority_visits_suppliers_others_gateways), false);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority_visits_suppliers_exit_others_gateways), false);

    }

    public static void initializeSyncAdapter (Context context){
        getSyncAccount(context);
    }

}
