package com.handsriver.concierge.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.view.MotionEvent;

import com.handsriver.concierge.database.ConciergeContract.*;
import com.handsriver.concierge.database.InsertUpdateTables.IUPortersPasswordDataType;
import com.handsriver.concierge.suppliers.SupplierVisit;


/**
 * Created by Created by alain_r._trouve_silva after 12-01-17.
 */

public class ConciergeDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 9;

    static final String DATABASE_NAME = "concierge.db";

    public ConciergeDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private final String SQL_CREATE_PORTERS_TABLE = "CREATE TABLE " + PorterEntry.TABLE_NAME + " (" +
            PorterEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            PorterEntry.COLUMN_FIRST_NAME + " TEXT NOT NULL, " +
            PorterEntry.COLUMN_LAST_NAME + " TEXT NOT NULL, " +
            PorterEntry.COLUMN_RUT + " TEXT NOT NULL, " +
            PorterEntry.COLUMN_PASSWORD + " TEXT NOT NULL, " +
            PorterEntry.COLUMN_ACTIVE + " INTEGER NOT NULL, " +
            PorterEntry.COLUMN_IS_UPDATE_PASSWORD + " INTEGER NOT NULL, " +
            PorterEntry.COLUMN_PORTER_ID_SERVER + " INTEGER UNIQUE " +
            ");";

    private final String SQL_CREATE_PORTER_ID_SERVER_PORTERS_INDEX = "CREATE UNIQUE INDEX porter_id_server_porters_idx ON " + PorterEntry.TABLE_NAME + " (" +
            PorterEntry.COLUMN_PORTER_ID_SERVER +
            ");";


    private final String SQL_CREATE_COMMONSPACES_TABLE = "CREATE TABLE " + CommonspaceEntry.TABLE_NAME + " (" +
            CommonspaceEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            CommonspaceEntry.COLUMN_NAME_COMMONSPACE + " TEXT NOT NULL, " +
            CommonspaceEntry.COLUMN_COMMONSPACE_ID_SERVER + " INTEGER UNIQUE, " +
            CommonspaceEntry.COLUMN_AFORO + " INTEGER, " +
            CommonspaceEntry.COLUMN_ACTIVE + " INTEGER NOT NULL " +
            ");";

    private final String SQL_CREATE_NAME_COMMONSPACE_COMMONSPACES_INDEX = "CREATE INDEX name_commonspace_commonspaces_idx ON " + CommonspaceEntry.TABLE_NAME + " (" +
            CommonspaceEntry.COLUMN_NAME_COMMONSPACE +
            ");";

    private final String SQL_CREATE_COMMONSPACE_ID_SERVER_COMMONSPACES_INDEX = "CREATE UNIQUE INDEX commonspace_id_server_commonspaces_idx ON " + CommonspaceEntry.TABLE_NAME + " (" +
            CommonspaceEntry.COLUMN_COMMONSPACE_ID_SERVER +
            ");";



    private final String SQL_CREATE_COMMONSPACES_VISITS_TABLE = "CREATE TABLE " + CommonspaceVisitsEntry.TABLE_NAME + " (" +
            CommonspaceVisitsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            CommonspaceVisitsEntry.COLUMN_DOCUMENT_NUMBER + " TEXT, " +
            CommonspaceVisitsEntry.COLUMN_FULL_NAME + " TEXT, " +
            CommonspaceVisitsEntry.COLUMN_NATIONALITY + " TEXT, " +
            CommonspaceVisitsEntry.COLUMN_GENDER + " TEXT, " +
            CommonspaceVisitsEntry.COLUMN_BIRTHDATE + " DATETIME, " +
            CommonspaceVisitsEntry.COLUMN_OCR + " TEXT, " +
            CommonspaceVisitsEntry.COLUMN_LICENSE_PLATE + " TEXT, " +
            CommonspaceVisitsEntry.COLUMN_ENTRY + " DATETIME NOT NULL, " +
            CommonspaceVisitsEntry.COLUMN_EXIT_VISIT + " DATETIME, " +
            CommonspaceVisitsEntry.COLUMN_APARTMENT_ID + " INTEGER NOT NULL, " +
            CommonspaceVisitsEntry.COLUMN_OBS + " TEXT, " +
            CommonspaceVisitsEntry.COLUMN_GATEWAY_ID + " INTEGER NOT NULL, " +
            CommonspaceVisitsEntry.COLUMN_COMMONSPACE_ID + " INTEGER NOT NULL, " +
            CommonspaceVisitsEntry.COLUMN_ENTRY_PORTER_ID + " INTEGER NOT NULL, " +
            CommonspaceVisitsEntry.COLUMN_EXIT_PORTER_ID + " INTEGER, " +
            CommonspaceVisitsEntry.COLUMN_COMMONSPACE_VISIT_ID_SERVER + " INTEGER UNIQUE, " +
            CommonspaceVisitsEntry.COLUMN_IS_SYNC + " INTEGER NOT NULL, " +
            CommonspaceVisitsEntry.COLUMN_IS_UPDATE + " INTEGER NOT NULL, " +
            "FOREIGN KEY (" + CommonspaceVisitsEntry.COLUMN_APARTMENT_ID + ") REFERENCES " +
            ApartmentEntry.TABLE_NAME + " (" + ApartmentEntry.COLUMN_APARTMENT_ID_SERVER + "), " +
            "FOREIGN KEY (" + CommonspaceVisitsEntry.COLUMN_COMMONSPACE_ID + ") REFERENCES " +
            CommonspaceEntry.TABLE_NAME + " (" + CommonspaceEntry.COLUMN_COMMONSPACE_ID_SERVER + "), " +
            "FOREIGN KEY (" + CommonspaceVisitsEntry.COLUMN_ENTRY_PORTER_ID + ") REFERENCES " +
            PorterEntry.TABLE_NAME + " (" + PorterEntry.COLUMN_PORTER_ID_SERVER + "), " +
            "FOREIGN KEY (" + CommonspaceVisitsEntry.COLUMN_EXIT_PORTER_ID + ") REFERENCES " +
            PorterEntry.TABLE_NAME + " (" + PorterEntry.COLUMN_PORTER_ID_SERVER + ") " +
            ");";

    private final String SQL_CREATE_ENTRY_COMMONSPACEVISITS_INDEX = "CREATE INDEX entry_commonspacevisits_idx ON " + CommonspaceVisitsEntry.TABLE_NAME + " (" +
            CommonspaceVisitsEntry.COLUMN_ENTRY +
            " DESC);";

    private final String SQL_CREATE_COMMONSPACE_ID_COMMONSPACEVISITS_INDEX = "CREATE INDEX commonspace_id_commonspacevisits_idx ON " + CommonspaceVisitsEntry.TABLE_NAME + " (" +
            CommonspaceVisitsEntry.COLUMN_COMMONSPACE_ID +
            ");";

    private final String SQL_CREATE_COMMONSPACE_VISIT_ID_SERVER_COMMONSPACEVISITS_INDEX = "CREATE UNIQUE INDEX commonspace_visit_id_server_commonspacevisits_idx ON " + CommonspaceVisitsEntry.TABLE_NAME + " (" +
            CommonspaceVisitsEntry.COLUMN_COMMONSPACE_VISIT_ID_SERVER +
            ");";

    private final String SQL_CREATE_APARTMENT_ID_COMMONSPACEVISITS_INDEX = "CREATE INDEX apartment_id_commonspacevisits_idx ON " + CommonspaceVisitsEntry.TABLE_NAME + " (" +
            CommonspaceVisitsEntry.COLUMN_APARTMENT_ID +
            ");";


    private final String SQL_CREATE_RESIDENTSVEHICLES_TABLE = "CREATE TABLE " + ResidentVehicleEntry.TABLE_NAME + " (" +
            ResidentVehicleEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ResidentVehicleEntry.COLUMN_PLATE + " TEXT NOT NULL, " +
            ResidentVehicleEntry.COLUMN_ACTIVE + " INTEGER NOT NULL, " +
            ResidentVehicleEntry.COLUMN_RESIDENTVEHICLE_ID_SERVER + " INTEGER UNIQUE, " +
            ResidentVehicleEntry.COLUMN_APARTMENT_ID + " INTEGER NOT NULL, " +
            ResidentVehicleEntry.COLUMN_IS_SYNC + " INTEGER NOT NULL, " +
            ResidentVehicleEntry.COLUMN_IS_UPDATE + " INTEGER NOT NULL, " +
            "FOREIGN KEY (" + ResidentVehicleEntry.COLUMN_APARTMENT_ID + ") REFERENCES " +
            ApartmentEntry.TABLE_NAME + " (" + ApartmentEntry.COLUMN_APARTMENT_ID_SERVER + ") " +
            ");";

    private final String SQL_CREATE_PLATE_RESIDENTSVEHICLES_INDEX = "CREATE INDEX plate_residentsvehicles_idx ON " + ResidentVehicleEntry.TABLE_NAME + " (" +
            ResidentVehicleEntry.COLUMN_PLATE +
            ");";

    private final String SQL_CREATE_APARTMENT_ID_RESIDENTSVEHICLES_INDEX = "CREATE INDEX apartment_id_residentsvehicles_idx ON " + ResidentVehicleEntry.TABLE_NAME + " (" +
            ResidentVehicleEntry.COLUMN_APARTMENT_ID +
            ");";

    private final String SQL_CREATE_RESIDENTVEHICLE_ID_SERVER_RESIDENTSVEHICLES_INDEX = "CREATE UNIQUE INDEX residentvehicle_id_server_residentsvehicles_idx ON " + ResidentVehicleEntry.TABLE_NAME + " (" +
            ResidentVehicleEntry.COLUMN_RESIDENTVEHICLE_ID_SERVER +
            ");";

    private final String SQL_CREATE_RESIDENTSTEMPS_TABLE = "CREATE TABLE " + ResidentTempEntry.TABLE_NAME + " (" +
            ResidentTempEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ResidentTempEntry.COLUMN_RUT + " TEXT, " +
            ResidentTempEntry.COLUMN_FULL_NAME + " TEXT, " +
            ResidentTempEntry.COLUMN_EMAIL + " TEXT, " +
            ResidentTempEntry.COLUMN_PHONE + " TEXT, " +
            ResidentTempEntry.COLUMN_START_DATE + " DATETIME, " +
            ResidentTempEntry.COLUMN_END_DATE + " DATETIME, " +
            ResidentTempEntry.COLUMN_RESIDENTTEMP_ID_SERVER + " INTEGER UNIQUE, " +
            ResidentTempEntry.COLUMN_APARTMENT_ID + " INTEGER NOT NULL, " +
            ResidentTempEntry.COLUMN_IS_SYNC + " INTEGER NOT NULL, " +
            ResidentTempEntry.COLUMN_IS_UPDATE + " INTEGER NOT NULL, " +
            "FOREIGN KEY (" + ResidentTempEntry.COLUMN_APARTMENT_ID + ") REFERENCES " +
            ApartmentEntry.TABLE_NAME + " (" + ApartmentEntry.COLUMN_APARTMENT_ID_SERVER + ") " +
            ");";


    private final String SQL_CREATE_APARTMENT_ID_RESIDENTSTEMPS_INDEX = "CREATE INDEX apartment_id_residentstemps_idx ON " + ResidentTempEntry.TABLE_NAME + " (" +
            ResidentTempEntry.COLUMN_APARTMENT_ID +
            ");";

    private final String SQL_CREATE_RESIDENTTEMP_ID_SERVER_RESIDENTSTEMPS_INDEX = "CREATE UNIQUE INDEX residenttemp_id_server_residentstemps_idx ON " + ResidentTempEntry.TABLE_NAME + " (" +
            ResidentTempEntry.COLUMN_RESIDENTTEMP_ID_SERVER +
            ");";



    private final String SQL_CREATE_PARCELTYPE_TABLE = "CREATE TABLE " + ParcelTypeEntry.TABLE_NAME + " (" +
            ParcelTypeEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ParcelTypeEntry.COLUMN_PARCELTYPE_TYPE + " TEXT NOT NULL, " +
            ParcelTypeEntry.COLUMN_PARCELTYPE_ID_SERVER + " INTEGER UNIQUE, " +
            ParcelTypeEntry.COLUMN_ACTIVE + " INTEGER NOT NULL " +
            ");";

    private final String SQL_ADD_FIRST_PARCELTYPE = "INSERT INTO "+ParcelTypeEntry.TABLE_NAME+" values (null, 'Encomienda',1,1)";






    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_SUPPLIERS_TABLE = "CREATE TABLE " + SupplierEntry.TABLE_NAME + " (" +
                SupplierEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SupplierEntry.COLUMN_NAME_SUPPLIER + " TEXT NOT NULL, " +
                SupplierEntry.COLUMN_SUPPLIER_ID_SERVER + " INTEGER UNIQUE, " +
                SupplierEntry.COLUMN_ACTIVE + " INTEGER NOT NULL " +
                ");";

        final String SQL_CREATE_NAME_SUPPLIER_SUPPLIERS_INDEX = "CREATE INDEX name_supplier_suppliers_idx ON " + SupplierEntry.TABLE_NAME + " (" +
                SupplierEntry.COLUMN_NAME_SUPPLIER +
                ");";

        final String SQL_CREATE_SUPPLIER_ID_SERVER_SUPPLIERS_INDEX = "CREATE UNIQUE INDEX supplier_id_server_suppliers_idx ON " + SupplierEntry.TABLE_NAME + " (" +
                SupplierEntry.COLUMN_SUPPLIER_ID_SERVER +
                ");";


        final String SQL_CREATE_APARTMENTS_TABLE = "CREATE TABLE " + ApartmentEntry.TABLE_NAME + " (" +
                ApartmentEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ApartmentEntry.COLUMN_APARTMENT_NUMBER + " TEXT NOT NULL, " +
                ApartmentEntry.COLUMN_APARTMENT_ID_SERVER + " INTEGER UNIQUE, " +
                ApartmentEntry.COLUMN_ACTIVE + " INTEGER NOT NULL " +
                ");";

        final String SQL_CREATE_APARTMENT_NUMBER_APARTMENTS_INDEX = "CREATE INDEX apartment_number_apartments_idx ON " + ApartmentEntry.TABLE_NAME + " (" +
                ApartmentEntry.COLUMN_APARTMENT_NUMBER +
                ");";

        final String SQL_CREATE_APARTMENT_ID_SERVER_APARTMENTS_INDEX = "CREATE UNIQUE INDEX apartment_id_server_apartments_idx ON " + ApartmentEntry.TABLE_NAME + " (" +
                ApartmentEntry.COLUMN_APARTMENT_ID_SERVER +
                ");";


        final String SQL_CREATE_TIMEKEEPING_TABLE = "CREATE TABLE " + TimekeepingEntry.TABLE_NAME + " (" +
                TimekeepingEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TimekeepingEntry.COLUMN_PORTER_ID + " INTEGER NOT NULL, " +
                TimekeepingEntry.COLUMN_ENTRY_PORTER + " DATETIME NOT NULL, " +
                TimekeepingEntry.COLUMN_ENTRY_HASH + " TEXT NOT NULL, " +
                TimekeepingEntry.COLUMN_EXIT_PORTER + " DATETIME, " +
                TimekeepingEntry.COLUMN_EXIT_HASH + " TEXT, " +
                TimekeepingEntry.COLUMN_ENTRY_PORTER_ID + " INTEGER NOT NULL, " +
                TimekeepingEntry.COLUMN_EXIT_PORTER_ID + " INTEGER, " +
                TimekeepingEntry.COLUMN_GATEWAY_ID + " INTEGER NOT NULL, " +
                TimekeepingEntry.COLUMN_TIMEKEEPING_ID_SERVER + " INTEGER UNIQUE, " +
                TimekeepingEntry.COLUMN_IS_SYNC + " INTEGER NOT NULL, " +
                TimekeepingEntry.COLUMN_IS_UPDATE + " INTEGER NOT NULL, " +
                "FOREIGN KEY (" + TimekeepingEntry.COLUMN_PORTER_ID + ") REFERENCES " +
                PorterEntry.TABLE_NAME + " (" + PorterEntry.COLUMN_PORTER_ID_SERVER + "), " +
                "FOREIGN KEY (" + TimekeepingEntry.COLUMN_ENTRY_PORTER_ID + ") REFERENCES " +
                PorterEntry.TABLE_NAME + " (" + PorterEntry.COLUMN_PORTER_ID_SERVER + "), " +
                "FOREIGN KEY (" + TimekeepingEntry.COLUMN_EXIT_PORTER_ID + ") REFERENCES " +
                PorterEntry.TABLE_NAME + " (" + PorterEntry.COLUMN_PORTER_ID_SERVER + ") " +
                ");";

        final String SQL_CREATE_ENTRY_PORTER_TIMEKEEPING_INDEX = "CREATE INDEX entry_porter_timekeeping_idx ON " + TimekeepingEntry.TABLE_NAME + " (" +
                TimekeepingEntry.COLUMN_ENTRY_PORTER +
                " DESC);";

        final String SQL_CREATE_PORTER_ID_TIMEKEEPING_INDEX = "CREATE INDEX porter_id_timekeeping_idx ON " + TimekeepingEntry.TABLE_NAME + " (" +
                TimekeepingEntry.COLUMN_PORTER_ID +
                ");";

        final String SQL_CREATE_TIMEKEEPING_ID_SERVER_TIMEKEEPING_INDEX = "CREATE UNIQUE INDEX timekeeping_id_server_timekeeping_idx ON " + TimekeepingEntry.TABLE_NAME + " (" +
                TimekeepingEntry.COLUMN_TIMEKEEPING_ID_SERVER +
                ");";

        final String SQL_CREATE_VEHICLES_TABLE = "CREATE TABLE " + VehicleEntry.TABLE_NAME + " (" +
                VehicleEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                VehicleEntry.COLUMN_LICENSE_PLATE + " TEXT NOT NULL, " +
                VehicleEntry.COLUMN_ENTRY + " DATETIME NOT NULL, " +
                VehicleEntry.COLUMN_EXIT_DATE + " DATETIME, " +
                VehicleEntry.COLUMN_FINE_DATE + " DATETIME, " +
                VehicleEntry.COLUMN_GATEWAY_ID + " INTEGER NOT NULL, " +
                VehicleEntry.COLUMN_APARTMENT_ID + " INTEGER NOT NULL, " +
                VehicleEntry.COLUMN_PORTER_ID + " INTEGER NOT NULL, " +
                VehicleEntry.COLUMN_EXIT_PORTER_ID + " INTEGER, " +
                VehicleEntry.COLUMN_FINE_PORTER_ID + " INTEGER, " +
                VehicleEntry.COLUMN_PARKING_NUMBER + " TEXT, " +
                VehicleEntry.COLUMN_VEHICLE_ID_SERVER + " INTEGER UNIQUE, " +
                VehicleEntry.COLUMN_IS_SYNC + " INTEGER NOT NULL, " +
                VehicleEntry.COLUMN_IS_UPDATE + " INTEGER NOT NULL, " +
                VehicleEntry.COLUMN_IS_UPDATE_FINE + " INTEGER NOT NULL, " +
                VehicleEntry.COLUMN_IS_SEND_ALERT_FINE + " INTEGER, " +
                "FOREIGN KEY (" + VehicleEntry.COLUMN_APARTMENT_ID + ") REFERENCES " +
                ApartmentEntry.TABLE_NAME + " (" + ApartmentEntry.COLUMN_APARTMENT_ID_SERVER + "), " +
                "FOREIGN KEY (" + VehicleEntry.COLUMN_PORTER_ID + ") REFERENCES " +
                PorterEntry.TABLE_NAME + " (" + PorterEntry.COLUMN_PORTER_ID_SERVER + "), " +
                "FOREIGN KEY (" + VehicleEntry.COLUMN_EXIT_PORTER_ID + ") REFERENCES " +
                PorterEntry.TABLE_NAME + " (" + PorterEntry.COLUMN_PORTER_ID_SERVER + "), " +
                "FOREIGN KEY (" + VehicleEntry.COLUMN_FINE_PORTER_ID + ") REFERENCES " +
                PorterEntry.TABLE_NAME + " (" + PorterEntry.COLUMN_PORTER_ID_SERVER + ") " +
                ");";

        final String SQL_CREATE_LICENSE_PLATE_VEHICLES_INDEX = "CREATE INDEX license_plate_vehicles_idx ON " + VehicleEntry.TABLE_NAME + " (" +
                VehicleEntry.COLUMN_LICENSE_PLATE +
                ");";

        final String SQL_CREATE_ENTRY_VEHICLES_INDEX = "CREATE INDEX entry_vehicles_idx ON " + VehicleEntry.TABLE_NAME + " (" +
                VehicleEntry.COLUMN_ENTRY +
                " DESC);";

        final String SQL_CREATE_APARTMENT_ID_VEHICLES_INDEX = "CREATE INDEX apartment_id_vehicles_idx ON " + VehicleEntry.TABLE_NAME + " (" +
                VehicleEntry.COLUMN_APARTMENT_ID +
                ");";

        final String SQL_CREATE_VEHICLE_ID_SERVER_VEHICLES_INDEX = "CREATE UNIQUE INDEX vehicle_id_server_vehicles_idx ON " + VehicleEntry.TABLE_NAME + " (" +
                VehicleEntry.COLUMN_VEHICLE_ID_SERVER +
                ");";

        final String SQL_CREATE_VISITS_TABLE = "CREATE TABLE " + VisitEntry.TABLE_NAME + " (" +
                VisitEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                VisitEntry.COLUMN_DOCUMENT_NUMBER + " TEXT, " +
                VisitEntry.COLUMN_FULL_NAME + " TEXT, " +
                VisitEntry.COLUMN_NATIONALITY + " TEXT, " +
                VisitEntry.COLUMN_GENDER + " TEXT, " +
                VisitEntry.COLUMN_BIRTHDATE + " DATETIME, " +
                VisitEntry.COLUMN_OPTIONAL + " TEXT, " +
                VisitEntry.COLUMN_OCR + " TEXT, " +
                VisitEntry.COLUMN_ENTRY + " DATETIME NOT NULL, " +
                VisitEntry.COLUMN_EXIT_DATE + " DATETIME, " +
                VisitEntry.COLUMN_GATEWAY_ID + " INTEGER NOT NULL, " +
                VisitEntry.COLUMN_APARTMENT_ID + " INTEGER NOT NULL, " +
                VisitEntry.COLUMN_PORTER_ID + " INTEGER NOT NULL, " +
                VisitEntry.COLUMN_EXIT_PORTER_ID + " INTEGER , " +
                VisitEntry.COLUMN_VISIT_ID_SERVER + " INTEGER UNIQUE, " +
                VisitEntry.COLUMN_IS_SYNC + " INTEGER NOT NULL, " +
                VisitEntry.COLUMN_IS_UPDATE + " INTEGER NOT NULL DEFAULT 0, " +
                "FOREIGN KEY (" + VisitEntry.COLUMN_APARTMENT_ID + ") REFERENCES " +
                ApartmentEntry.TABLE_NAME + " (" + ApartmentEntry.COLUMN_APARTMENT_ID_SERVER + "), " +
                "FOREIGN KEY (" + VisitEntry.COLUMN_PORTER_ID + ") REFERENCES " +
                PorterEntry.TABLE_NAME + " (" + PorterEntry.COLUMN_PORTER_ID_SERVER + ") " +
                ");";

        final String SQL_CREATE_FULL_NAME_VISITS_INDEX = "CREATE INDEX full_name_visits_idx ON " + VisitEntry.TABLE_NAME + " (" +
                VisitEntry.COLUMN_FULL_NAME +
                ");";

        final String SQL_CREATE_ENTRY_VISITS_INDEX = "CREATE INDEX entry_visits_idx ON " + VisitEntry.TABLE_NAME + " (" +
                VisitEntry.COLUMN_ENTRY +
                " DESC);";

        final String SQL_CREATE_APARTMENT_ID_VISITS_INDEX = "CREATE INDEX apartment_id_visits_idx ON " + VisitEntry.TABLE_NAME + " (" +
                VisitEntry.COLUMN_APARTMENT_ID +
                ");";

        final String SQL_CREATE_VISIT_ID_SERVER_VISITS_INDEX = "CREATE UNIQUE INDEX visit_id_server_visits_idx ON " + VisitEntry.TABLE_NAME + " (" +
                VisitEntry.COLUMN_VISIT_ID_SERVER +
                ");";

        final String SQL_CREATE_SUPPLIERS_VISITS_TABLE = "CREATE TABLE " + SupplierVisitsEntry.TABLE_NAME + " (" +
                SupplierVisitsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SupplierVisitsEntry.COLUMN_DOCUMENT_NUMBER + " TEXT, " +
                SupplierVisitsEntry.COLUMN_FULL_NAME + " TEXT, " +
                SupplierVisitsEntry.COLUMN_NATIONALITY + " TEXT, " +
                SupplierVisitsEntry.COLUMN_GENDER + " TEXT, " +
                SupplierVisitsEntry.COLUMN_BIRTHDATE + " DATETIME, " +
                SupplierVisitsEntry.COLUMN_OCR + " TEXT, " +
                SupplierVisitsEntry.COLUMN_LICENSE_PLATE + " TEXT, " +
                SupplierVisitsEntry.COLUMN_ENTRY + " DATETIME NOT NULL, " +
                SupplierVisitsEntry.COLUMN_EXIT_SUPPLIER + " DATETIME, " +
                SupplierVisitsEntry.COLUMN_EXIT_OBS + " TEXT, " +
                SupplierVisitsEntry.COLUMN_GATEWAY_ID + " INTEGER NOT NULL, " +
                SupplierVisitsEntry.COLUMN_SUPPLIER_ID + " INTEGER NOT NULL, " +
                SupplierVisitsEntry.COLUMN_ENTRY_PORTER_ID + " INTEGER NOT NULL, " +
                SupplierVisitsEntry.COLUMN_EXIT_PORTER_ID + " INTEGER, " +
                SupplierVisitsEntry.COLUMN_SUPPLIER_VISIT_ID_SERVER + " INTEGER UNIQUE, " +
                SupplierVisitsEntry.COLUMN_IS_SYNC + " INTEGER NOT NULL, " +
                SupplierVisitsEntry.COLUMN_IS_UPDATE + " INTEGER NOT NULL, " +
                "FOREIGN KEY (" + SupplierVisitsEntry.COLUMN_SUPPLIER_ID + ") REFERENCES " +
                SupplierEntry.TABLE_NAME + " (" + SupplierEntry.COLUMN_SUPPLIER_ID_SERVER + "), " +
                "FOREIGN KEY (" + SupplierVisitsEntry.COLUMN_ENTRY_PORTER_ID + ") REFERENCES " +
                PorterEntry.TABLE_NAME + " (" + PorterEntry.COLUMN_PORTER_ID_SERVER + "), " +
                "FOREIGN KEY (" + SupplierVisitsEntry.COLUMN_EXIT_PORTER_ID + ") REFERENCES " +
                PorterEntry.TABLE_NAME + " (" + PorterEntry.COLUMN_PORTER_ID_SERVER + ") " +
                ");";

        final String SQL_CREATE_ENTRY_SUPPLIERVISITS_INDEX = "CREATE INDEX entry_suppliervisits_idx ON " + SupplierVisitsEntry.TABLE_NAME + " (" +
                SupplierVisitsEntry.COLUMN_ENTRY +
                " DESC);";

        final String SQL_CREATE_SUPPLIER_ID_SUPPLIERVISITS_INDEX = "CREATE INDEX supplier_id_suppliervisits_idx ON " + SupplierVisitsEntry.TABLE_NAME + " (" +
                SupplierVisitsEntry.COLUMN_SUPPLIER_ID +
                ");";

        final String SQL_CREATE_SUPPLIER_VISIT_ID_SERVER_SUPPLIERVISITS_INDEX = "CREATE UNIQUE INDEX supplier_visit_id_server_suppliervisits_idx ON " + SupplierVisitsEntry.TABLE_NAME + " (" +
                SupplierVisitsEntry.COLUMN_SUPPLIER_VISIT_ID_SERVER +
                ");";





        final String SQL_CREATE_PARCELS_TABLE = "CREATE TABLE " + ParcelEntry.TABLE_NAME + " (" +
                ParcelEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ParcelEntry.COLUMN_UNIQUE_ID + " TEXT UNIQUE, " +
                ParcelEntry.COLUMN_FULL_NAME + " TEXT NOT NULL, " +
                ParcelEntry.COLUMN_OBSERVATIONS + " TEXT, " +
                ParcelEntry.COLUMN_ENTRY_PARCEL + " DATETIME NOT NULL, " +
                ParcelEntry.COLUMN_EXIT_PARCEL + " DATETIME, " +
                ParcelEntry.COLUMN_EXIT_FULLNAME + " TEXT, " +
                ParcelEntry.COLUMN_EXIT_DOCUMENT_NUMBER + " TEXT, " +
                ParcelEntry.COLUMN_GATEWAY_ID + " INTEGER NOT NULL, " +
                ParcelEntry.COLUMN_APARTMENT_ID + " INTEGER NOT NULL, " +
                ParcelEntry.COLUMN_ENTRY_PARCEL_PORTER_ID + " INTEGER NOT NULL, " +
                ParcelEntry.COLUMN_EXIT_PARCEL_PORTER_ID + " INTEGER, " +
                ParcelEntry.COLUMN_PARCELTYPE_ID + " INTEGER NOT NULL DEFAULT 1, " +
                ParcelEntry.COLUMN_PARCEL_ID_SERVER + " INTEGER UNIQUE, " +
                ParcelEntry.COLUMN_IS_SYNC + " INTEGER NOT NULL, " +
                ParcelEntry.COLUMN_IS_UPDATE + " INTEGER NOT NULL, " +
                "FOREIGN KEY (" + ParcelEntry.COLUMN_APARTMENT_ID + ") REFERENCES " +
                ApartmentEntry.TABLE_NAME + " (" + ApartmentEntry.COLUMN_APARTMENT_ID_SERVER + "), " +
                "FOREIGN KEY (" + ParcelEntry.COLUMN_PARCELTYPE_ID + ") REFERENCES " +
                ParcelTypeEntry.TABLE_NAME + " (" + ParcelTypeEntry.COLUMN_PARCELTYPE_ID_SERVER + "), " +
                "FOREIGN KEY (" + ParcelEntry.COLUMN_ENTRY_PARCEL_PORTER_ID + ") REFERENCES " +
                PorterEntry.TABLE_NAME + " (" + PorterEntry.COLUMN_PORTER_ID_SERVER + "), " +
                "FOREIGN KEY (" + ParcelEntry.COLUMN_EXIT_PARCEL_PORTER_ID + ") REFERENCES " +
                PorterEntry.TABLE_NAME + " (" + PorterEntry.COLUMN_PORTER_ID_SERVER + ") " +
                ");";

        final String SQL_CREATE_FULL_NAME_PARCELS_INDEX = "CREATE INDEX full_name_parcels_idx ON " + ParcelEntry.TABLE_NAME + " (" +
                ParcelEntry.COLUMN_FULL_NAME +
                ");";

        final String SQL_CREATE_ENTRY_PARCEL_PARCELS_INDEX = "CREATE INDEX entry_parcel_parcels_idx ON " + ParcelEntry.TABLE_NAME + " (" +
                ParcelEntry.COLUMN_ENTRY_PARCEL +
                " DESC);";

        final String SQL_CREATE_APARTMENT_ID_PARCELS_INDEX = "CREATE INDEX apartment_id_parcels_idx ON " + ParcelEntry.TABLE_NAME + " (" +
                ParcelEntry.COLUMN_APARTMENT_ID +
                ");";

        final String SQL_CREATE_PARCEL_ID_SERVER_PARCELS_INDEX = "CREATE UNIQUE INDEX parcel_id_server_parcels_idx ON " + ParcelEntry.TABLE_NAME + " (" +
                ParcelEntry.COLUMN_PARCEL_ID_SERVER +
                ");";

        final String SQL_CREATE_UNIQUE_ID_PARCEL_PARCELS_INDEX = "CREATE UNIQUE INDEX unique_id_parcel_parcels_idx ON " + ParcelEntry.TABLE_NAME + " (" +
                ParcelEntry.COLUMN_UNIQUE_ID +
                ");";

        final String SQL_CREATE_RESIDENTS_TABLE = "CREATE TABLE " + ResidentEntry.TABLE_NAME + " (" +
                ResidentEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ResidentEntry.COLUMN_FULL_NAME + " TEXT NOT NULL, " +
                ResidentEntry.COLUMN_EMAIL + " TEXT, " +
                ResidentEntry.COLUMN_PHONE + " TEXT, " +
                ResidentEntry.COLUMN_MOBILE + " TEXT, " +
                ResidentEntry.COLUMN_RUT + " TEXT, " +
                ResidentEntry.COLUMN_RESIDENT_ID_SERVER + " INTEGER UNIQUE, " +
                ResidentEntry.COLUMN_APARTMENT_ID + " INTEGER NOT NULL, " +
                ResidentEntry.COLUMN_TOKEN + " TEXT, " +
                ResidentEntry.COLUMN_PUSH_NOTIFICATIONS + " INTEGER, " +
                ResidentEntry.COLUMN_REQUEST_CODE + " INTEGER NOT NULL, " +

                ResidentEntry.COLUMN_CREATED_AT + " DATETIME, " +
                ResidentEntry.COLUMN_UPDATED_AT + " DATETIME, " +
                ResidentEntry.COLUMN_DELETED_AT + " DATETIME, " +

                ResidentEntry.COLUMN_IS_DELETED + " INTEGER NOT NULL DEFAULT 0, " +
                ResidentEntry.COLUMN_IS_SYNC + " INTEGER NOT NULL, " +
                ResidentEntry.COLUMN_IS_UPDATE + " INTEGER NOT NULL, " +
                "FOREIGN KEY (" + ResidentEntry.COLUMN_APARTMENT_ID + ") REFERENCES " +
                ApartmentEntry.TABLE_NAME + " (" + ApartmentEntry.COLUMN_APARTMENT_ID_SERVER + ") " +
                ");";

        final String SQL_CREATE_FULL_NAME_RESIDENTS_INDEX = "CREATE INDEX full_name_residents_idx ON " + ResidentEntry.TABLE_NAME + " (" +
                ResidentEntry.COLUMN_FULL_NAME +
                ");";

        final String SQL_CREATE_APARTMENT_ID_RESIDENTS_INDEX = "CREATE INDEX apartment_id_residents_idx ON " + ResidentEntry.TABLE_NAME + " (" +
                ResidentEntry.COLUMN_APARTMENT_ID +
                ");";

        final String SQL_CREATE_RESIDENT_ID_SERVER_RESIDENTS_INDEX = "CREATE UNIQUE INDEX resident_id_server_residents_idx ON " + ResidentEntry.TABLE_NAME + " (" +
                ResidentEntry.COLUMN_RESIDENT_ID_SERVER +
                ");";




        final String SQL_CREATE_BLACKLIST_TABLE = "CREATE TABLE " + BlacklistEntry.TABLE_NAME + " (" +
                BlacklistEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                BlacklistEntry.COLUMN_DOCUMENT_NUMBER + " TEXT NOT NULL, " +
                BlacklistEntry.COLUMN_IDENTIFICATION + " TEXT, " +
                BlacklistEntry.COLUMN_OBSERVATIONS + " TEXT, " +
                BlacklistEntry.COLUMN_APARTMENT_ID + " INTEGER NOT NULL, " +
                BlacklistEntry.COLUMN_BLACKLIST_ID_SERVER + " INTEGER UNIQUE, " +
                "FOREIGN KEY (" + BlacklistEntry.COLUMN_APARTMENT_ID + ") REFERENCES " +
                ApartmentEntry.TABLE_NAME + " (" + ApartmentEntry.COLUMN_APARTMENT_ID_SERVER + ") " +
                ");";

        final String SQL_CREATE_DOCUMENT_NUMBER_BLACKLIST_INDEX = "CREATE INDEX document_number_blacklist_idx ON " + BlacklistEntry.TABLE_NAME + " (" +
                BlacklistEntry.COLUMN_DOCUMENT_NUMBER +
                ");";

        final String SQL_CREATE_APARTMENT_ID_BLACKLIST_INDEX = "CREATE INDEX apartment_id_blacklist_idx ON " + BlacklistEntry.TABLE_NAME + " (" +
                BlacklistEntry.COLUMN_APARTMENT_ID +
                ");";

        final String SQL_CREATE_BLACKLIST_ID_SERVER_BLACKLIST_INDEX = "CREATE UNIQUE INDEX blacklist_id_server_blacklist_idx ON " + BlacklistEntry.TABLE_NAME + " (" +
                BlacklistEntry.COLUMN_BLACKLIST_ID_SERVER +
                " DESC);";

        final String SQL_CREATE_WHITELIST_TABLE = "CREATE TABLE " + WhitelistEntry.TABLE_NAME + " (" +
                WhitelistEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                WhitelistEntry.COLUMN_DOCUMENT_NUMBER + " TEXT NOT NULL, " +
                WhitelistEntry.COLUMN_FULL_NAME + " TEXT, " +
                WhitelistEntry.COLUMN_PLATE + " TEXT, " +
                WhitelistEntry.COLUMN_APARTMENT_ID + " INTEGER NOT NULL, " +
                WhitelistEntry.COLUMN_WHITELIST_ID_SERVER + " INTEGER UNIQUE, " +
                "FOREIGN KEY (" + WhitelistEntry.COLUMN_APARTMENT_ID + ") REFERENCES " +
                ApartmentEntry.TABLE_NAME + " (" + ApartmentEntry.COLUMN_APARTMENT_ID_SERVER + ") " +
                ");";

        final String SQL_CREATE_DOCUMENT_NUMBER_WHITELIST_INDEX = "CREATE INDEX document_number_whitelist_idx ON " + WhitelistEntry.TABLE_NAME + " (" +
                WhitelistEntry.COLUMN_DOCUMENT_NUMBER +
                ");";

        final String SQL_CREATE_APARTMENT_ID_WHITELIST_INDEX = "CREATE INDEX apartment_id_whitelist_idx ON " + WhitelistEntry.TABLE_NAME + " (" +
                WhitelistEntry.COLUMN_APARTMENT_ID +
                ");";

        final String SQL_CREATE_WHITELIST_ID_SERVER_WHITELIST_INDEX = "CREATE UNIQUE INDEX whitelist_id_server_whitelist_idx ON " + WhitelistEntry.TABLE_NAME + " (" +
                WhitelistEntry.COLUMN_WHITELIST_ID_SERVER +
                " DESC);";

        final String SQL_CREATE_PAYMENT_TABLE = "CREATE TABLE " + PaymentEntry.TABLE_NAME + " (" +
                PaymentEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PaymentEntry.COLUMN_PAYMENT_TYPE + " INTEGER NOT NULL, " +
                PaymentEntry.COLUMN_DATE_REGISTER + " DATETIME NOT NULL, " +
                PaymentEntry.COLUMN_NUMBER_TRX + " TEXT, " +
                PaymentEntry.COLUMN_NUMBER_RECEIPT + " TEXT, " +
                PaymentEntry.COLUMN_AMOUNT + " INTEGER NOT NULL, " +
                PaymentEntry.COLUMN_APARTMENT_ID + " INTEGER NOT NULL, " +
                PaymentEntry.COLUMN_PAYMENT_ID_SERVER + " INTEGER UNIQUE, " +
                PaymentEntry.COLUMN_PORTER_ID + " INTEGER NOT NULL, " +
                PaymentEntry.COLUMN_IS_SYNC + " INTEGER NOT NULL, " +
                PaymentEntry.COLUMN_GATEWAY_ID + " INTEGER NOT NULL, " +
                "FOREIGN KEY (" + PaymentEntry.COLUMN_PORTER_ID + ") REFERENCES " +
                PorterEntry.TABLE_NAME + " (" + PorterEntry.COLUMN_PORTER_ID_SERVER + "), " +
                "FOREIGN KEY (" + PaymentEntry.COLUMN_APARTMENT_ID + ") REFERENCES " +
                ApartmentEntry.TABLE_NAME + " (" + ApartmentEntry.COLUMN_APARTMENT_ID_SERVER + ") " +
                ");";

        final String SQL_CREATE_ENTRY_PAYMENT_PAYMENTS_INDEX = "CREATE INDEX entry_payment_payments_idx ON " + PaymentEntry.TABLE_NAME + " (" +
                PaymentEntry.COLUMN_DATE_REGISTER +
                " DESC);";

        final String SQL_CREATE_APARTMENT_ID_PAYMENTS_INDEX = "CREATE INDEX apartment_id_payments_idx ON " + PaymentEntry.TABLE_NAME + " (" +
                PaymentEntry.COLUMN_APARTMENT_ID +
                ");";

        final String SQL_CREATE_PAYMENT_ID_SERVER_PAYMENTS_INDEX = "CREATE UNIQUE INDEX payment_id_server_payments_idx ON " + PaymentEntry.TABLE_NAME + " (" +
                PaymentEntry.COLUMN_PAYMENT_ID_SERVER +
                ");";

        final String SQL_CREATE_PARKING_TABLE = "CREATE TABLE " + ParkingEntry.TABLE_NAME + " (" +
                ParkingEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ParkingEntry.COLUMN_PARKING_NUMBER + " TEXT NOT NULL, " +
                ParkingEntry.COLUMN_PARKING_ID_SERVER + " INTEGER UNIQUE, " +
                ParkingEntry.COLUMN_APARTMENT_ID + " INTEGER NOT NULL, " +
                "FOREIGN KEY (" + ParkingEntry.COLUMN_APARTMENT_ID + ") REFERENCES " +
                ApartmentEntry.TABLE_NAME + " (" + ApartmentEntry.COLUMN_APARTMENT_ID_SERVER + ") " +
                ");";

        final String SQL_CREATE_PARKING_NUMBER_PARKING_INDEX = "CREATE INDEX parking_number_parking_idx ON " + ParkingEntry.TABLE_NAME + " (" +
                ParkingEntry.COLUMN_PARKING_NUMBER +
                ");";

        final String SQL_CREATE_APARTMENT_ID_PARKING_INDEX = "CREATE INDEX apartment_id_parking_idx ON " + ParkingEntry.TABLE_NAME + " (" +
                ParkingEntry.COLUMN_APARTMENT_ID +
                ");";

        final String SQL_CREATE_PARKING_ID_SERVER_PARKING_INDEX = "CREATE UNIQUE INDEX parking_id_server_parking_idx ON " + ParkingEntry.TABLE_NAME + " (" +
                ParkingEntry.COLUMN_PARKING_ID_SERVER +
                ");";

        final String SQL_CREATE_WAREHOUSE_TABLE = "CREATE TABLE " + WarehouseEntry.TABLE_NAME + " (" +
                WarehouseEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                WarehouseEntry.COLUMN_WAREHOUSE_NUMBER + " TEXT NOT NULL, " +
                WarehouseEntry.COLUMN_WAREHOUSE_ID_SERVER + " INTEGER UNIQUE, " +
                WarehouseEntry.COLUMN_APARTMENT_ID + " INTEGER NOT NULL, " +
                "FOREIGN KEY (" + WarehouseEntry.COLUMN_APARTMENT_ID + ") REFERENCES " +
                ApartmentEntry.TABLE_NAME + " (" + ApartmentEntry.COLUMN_APARTMENT_ID_SERVER + ") " +
                ");";

        final String SQL_CREATE_WAREHOUSE_NUMBER_WAREHOUSES_INDEX = "CREATE INDEX warehouse_number_warehouses_idx ON " + WarehouseEntry.TABLE_NAME + " (" +
                WarehouseEntry.COLUMN_WAREHOUSE_NUMBER +
                ");";

        final String SQL_CREATE_APARTMENT_ID_WAREHOUSES_INDEX = "CREATE INDEX apartment_id_warehouses_idx ON " + WarehouseEntry.TABLE_NAME + " (" +
                WarehouseEntry.COLUMN_APARTMENT_ID +
                ");";

        final String SQL_CREATE_WAREHOUSE_ID_SERVER_WAREHOUSES_INDEX = "CREATE UNIQUE INDEX warehouse_id_server_warehouses_idx ON " + WarehouseEntry.TABLE_NAME + " (" +
                WarehouseEntry.COLUMN_WAREHOUSE_ID_SERVER +
                ");";

        db.execSQL(SQL_CREATE_PORTERS_TABLE);
        db.execSQL(SQL_CREATE_SUPPLIERS_TABLE);
        db.execSQL(SQL_CREATE_COMMONSPACES_TABLE);

        db.execSQL(SQL_CREATE_APARTMENTS_TABLE);
        db.execSQL(SQL_CREATE_PARCELTYPE_TABLE);
        db.execSQL(SQL_ADD_FIRST_PARCELTYPE);
        db.execSQL(SQL_CREATE_TIMEKEEPING_TABLE);
        db.execSQL(SQL_CREATE_VEHICLES_TABLE);
        db.execSQL(SQL_CREATE_VISITS_TABLE);
        db.execSQL(SQL_CREATE_SUPPLIERS_VISITS_TABLE);
        db.execSQL(SQL_CREATE_COMMONSPACES_VISITS_TABLE);
        db.execSQL(SQL_CREATE_PARCELS_TABLE);
        db.execSQL(SQL_CREATE_RESIDENTS_TABLE);
        db.execSQL(SQL_CREATE_RESIDENTSVEHICLES_TABLE);
        db.execSQL(SQL_CREATE_RESIDENTSTEMPS_TABLE);
        db.execSQL(SQL_CREATE_BLACKLIST_TABLE);
        db.execSQL(SQL_CREATE_WHITELIST_TABLE);
        db.execSQL(SQL_CREATE_PAYMENT_TABLE);
        db.execSQL(SQL_CREATE_PARKING_TABLE);
        db.execSQL(SQL_CREATE_WAREHOUSE_TABLE);

        db.execSQL(SQL_CREATE_PORTER_ID_SERVER_PORTERS_INDEX);
        db.execSQL(SQL_CREATE_NAME_SUPPLIER_SUPPLIERS_INDEX);
        db.execSQL(SQL_CREATE_SUPPLIER_ID_SERVER_SUPPLIERS_INDEX);
        db.execSQL(SQL_CREATE_APARTMENT_NUMBER_APARTMENTS_INDEX);
        db.execSQL(SQL_CREATE_APARTMENT_ID_SERVER_APARTMENTS_INDEX);
        db.execSQL(SQL_CREATE_ENTRY_PORTER_TIMEKEEPING_INDEX);
        db.execSQL(SQL_CREATE_PORTER_ID_TIMEKEEPING_INDEX);
        db.execSQL(SQL_CREATE_TIMEKEEPING_ID_SERVER_TIMEKEEPING_INDEX);
        db.execSQL(SQL_CREATE_APARTMENT_ID_VEHICLES_INDEX);
        db.execSQL(SQL_CREATE_ENTRY_VEHICLES_INDEX);
        db.execSQL(SQL_CREATE_LICENSE_PLATE_VEHICLES_INDEX);
        db.execSQL(SQL_CREATE_VEHICLE_ID_SERVER_VEHICLES_INDEX);
        db.execSQL(SQL_CREATE_APARTMENT_ID_VISITS_INDEX);
        db.execSQL(SQL_CREATE_VISIT_ID_SERVER_VISITS_INDEX);
        db.execSQL(SQL_CREATE_ENTRY_VISITS_INDEX);
        db.execSQL(SQL_CREATE_FULL_NAME_VISITS_INDEX);
        db.execSQL(SQL_CREATE_ENTRY_SUPPLIERVISITS_INDEX);
        db.execSQL(SQL_CREATE_SUPPLIER_ID_SUPPLIERVISITS_INDEX);
        db.execSQL(SQL_CREATE_SUPPLIER_VISIT_ID_SERVER_SUPPLIERVISITS_INDEX);
        db.execSQL(SQL_CREATE_APARTMENT_ID_PARCELS_INDEX);
        db.execSQL(SQL_CREATE_ENTRY_PARCEL_PARCELS_INDEX);
        db.execSQL(SQL_CREATE_FULL_NAME_PARCELS_INDEX);
        db.execSQL(SQL_CREATE_PARCEL_ID_SERVER_PARCELS_INDEX);
        db.execSQL(SQL_CREATE_UNIQUE_ID_PARCEL_PARCELS_INDEX);

        db.execSQL(SQL_CREATE_APARTMENT_ID_RESIDENTS_INDEX);
        db.execSQL(SQL_CREATE_FULL_NAME_RESIDENTS_INDEX);
        db.execSQL(SQL_CREATE_RESIDENT_ID_SERVER_RESIDENTS_INDEX);

        db.execSQL(SQL_CREATE_APARTMENT_ID_RESIDENTSVEHICLES_INDEX);
        db.execSQL(SQL_CREATE_PLATE_RESIDENTSVEHICLES_INDEX);
        db.execSQL(SQL_CREATE_RESIDENTVEHICLE_ID_SERVER_RESIDENTSVEHICLES_INDEX);

        db.execSQL(SQL_CREATE_APARTMENT_ID_RESIDENTSTEMPS_INDEX);
        db.execSQL(SQL_CREATE_RESIDENTTEMP_ID_SERVER_RESIDENTSTEMPS_INDEX);


        db.execSQL(SQL_CREATE_APARTMENT_ID_BLACKLIST_INDEX);
        db.execSQL(SQL_CREATE_DOCUMENT_NUMBER_BLACKLIST_INDEX);
        db.execSQL(SQL_CREATE_BLACKLIST_ID_SERVER_BLACKLIST_INDEX);
        db.execSQL(SQL_CREATE_DOCUMENT_NUMBER_WHITELIST_INDEX);
        db.execSQL(SQL_CREATE_APARTMENT_ID_WHITELIST_INDEX);
        db.execSQL(SQL_CREATE_WHITELIST_ID_SERVER_WHITELIST_INDEX);
        db.execSQL(SQL_CREATE_APARTMENT_ID_PAYMENTS_INDEX);
        db.execSQL(SQL_CREATE_ENTRY_PAYMENT_PAYMENTS_INDEX);
        db.execSQL(SQL_CREATE_PAYMENT_ID_SERVER_PAYMENTS_INDEX);
        db.execSQL(SQL_CREATE_APARTMENT_ID_PARKING_INDEX);
        db.execSQL(SQL_CREATE_PARKING_ID_SERVER_PARKING_INDEX);
        db.execSQL(SQL_CREATE_PARKING_NUMBER_PARKING_INDEX);
        db.execSQL(SQL_CREATE_APARTMENT_ID_WAREHOUSES_INDEX);
        db.execSQL(SQL_CREATE_WAREHOUSE_NUMBER_WAREHOUSES_INDEX);
        db.execSQL(SQL_CREATE_WAREHOUSE_ID_SERVER_WAREHOUSES_INDEX);

        db.execSQL(SQL_CREATE_NAME_COMMONSPACE_COMMONSPACES_INDEX);
        db.execSQL(SQL_CREATE_COMMONSPACE_ID_SERVER_COMMONSPACES_INDEX);

        db.execSQL(SQL_CREATE_ENTRY_COMMONSPACEVISITS_INDEX);
        db.execSQL(SQL_CREATE_COMMONSPACE_ID_COMMONSPACEVISITS_INDEX);
        db.execSQL(SQL_CREATE_COMMONSPACE_VISIT_ID_SERVER_COMMONSPACEVISITS_INDEX);
        db.execSQL(SQL_CREATE_APARTMENT_ID_COMMONSPACEVISITS_INDEX);



    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion < 2) {
            /*
            * Se añade los siguientes cambios:
            *   - Cambio campo password de tabla Porters de INTEGER A TEXT
            *   - Se agrega campo entry_hash a la tabla Timekeeping para comprobar entrada
            *   - Se agrega campo exit_hash a la tabla Timekeeping para comprobar salida
            * */

           ////final String SQL_PRAGMA_ON = "PRAGMA foreign_keys=on;";
            final String SQL_ALTER_TABLE_PORTERS_RENAME = "ALTER TABLE " + PorterEntry.TABLE_NAME + " RENAME TO porters_tmp;";
            final String SQL_DROP_INDEX_PORTERS_PASSWORD = "DROP INDEX IF EXISTS password_porters_idx;";
            final String SQL_DROP_INDEX_PORTERS_ID_PORTER = "DROP INDEX IF EXISTS porter_id_server_porters_idx;";
            final String SQL_DROP_TABLE_TMP = "DROP TABLE IF EXISTS porters_tmp;";
           // db.execSQL(SQL_PRAGMA_OFF);
            db.execSQL(SQL_ALTER_TABLE_PORTERS_RENAME);
            db.execSQL(SQL_CREATE_PORTERS_TABLE);
            IUPortersPasswordDataType.run(db);
            db.execSQL(SQL_DROP_INDEX_PORTERS_PASSWORD);
            db.execSQL(SQL_DROP_INDEX_PORTERS_ID_PORTER);
            db.execSQL(SQL_DROP_TABLE_TMP);
            db.execSQL(SQL_CREATE_PORTER_ID_SERVER_PORTERS_INDEX);
            //db.execSQL(SQL_PRAGMA_ON);


            final String SQL_ALTER_TABLE_TIMEKEEPING_ADD_ENTRY_HASH = "ALTER TABLE " + TimekeepingEntry.TABLE_NAME + " ADD " + TimekeepingEntry.COLUMN_ENTRY_HASH + " TEXT;";
            db.execSQL(SQL_ALTER_TABLE_TIMEKEEPING_ADD_ENTRY_HASH);


            final String SQL_ALTER_TABLE_TIMEKEEPING_ADD_EXIT_HASH = "ALTER TABLE " + TimekeepingEntry.TABLE_NAME + " ADD " + TimekeepingEntry.COLUMN_EXIT_HASH + " TEXT;";
            db.execSQL(SQL_ALTER_TABLE_TIMEKEEPING_ADD_EXIT_HASH);

            final String SQL_ALTER_TABLE_VEHICLES_ADD_SEND_ALERT_FINE = "ALTER TABLE " + VehicleEntry.TABLE_NAME + " ADD " + VehicleEntry.COLUMN_IS_SEND_ALERT_FINE + " INTEGER;";
            db.execSQL(SQL_ALTER_TABLE_VEHICLES_ADD_SEND_ALERT_FINE);


            /*************************************************************************/

            /*
             * Se añade los siguientes cambios:
             *   - se agerga columna opcional a la tabla de visitas
             * */

            //ALTER TABLE visits ADD optional TEXT after birthdate;
            //alter table visits add column `optional` varchar(255) CHARACTER SET utf8 DEFAULT NULL after birthdate;
            final String SQL_ALTER_TABLE_VISITS_ADD_OPTIONAL = "ALTER TABLE " + VisitEntry.TABLE_NAME + " ADD " + VisitEntry.COLUMN_OPTIONAL + " TEXT;";
            db.execSQL(SQL_ALTER_TABLE_VISITS_ADD_OPTIONAL);


            //ALTER TABLE resident ADD phone TEXT after email;
            //ALTER TABLE resident ADD mobile TEXT after phone;
            //ALTER TABLE resident ADD rut TEXT after mobilel;
            final String SQL_ALTER_TABLE_RESIDENT_ADD_PHONE = "ALTER TABLE " + ResidentEntry.TABLE_NAME + " ADD " + ResidentEntry.COLUMN_PHONE + " TEXT;";
            db.execSQL(SQL_ALTER_TABLE_RESIDENT_ADD_PHONE);

            final String SQL_ALTER_TABLE_RESIDENT_ADD_MOBILE = "ALTER TABLE " + ResidentEntry.TABLE_NAME + " ADD " + ResidentEntry.COLUMN_MOBILE + " TEXT;";
            db.execSQL(SQL_ALTER_TABLE_RESIDENT_ADD_MOBILE);

            final String SQL_ALTER_TABLE_RESIDENT_ADD_RUT = "ALTER TABLE " + ResidentEntry.TABLE_NAME + " ADD " + ResidentEntry.COLUMN_RUT + " TEXT;";
            db.execSQL(SQL_ALTER_TABLE_RESIDENT_ADD_RUT);


        }

        if(oldVersion == 2){

            /*************************************************************************/

            /*
             * Se añade los siguientes cambios:
             *   - se agerga columna exit_date a la tabla de visitas
             *   - se agerga columna exit_porter_id a la tabla de visitas
             *   - se agerga columna is_update a la tabla de visitas

             * */

            final String SQL_ALTER_TABLE_VISITS_ADD_EXIT = "ALTER TABLE " + VisitEntry.TABLE_NAME + " ADD " + VisitEntry.COLUMN_EXIT_DATE +  " DATETIME;";
            db.execSQL(SQL_ALTER_TABLE_VISITS_ADD_EXIT);

            final String SQL_ALTER_TABLE_VISITS_ADD_EXIT_PORTER_ID = "ALTER TABLE " + VisitEntry.TABLE_NAME + " ADD " + VisitEntry.COLUMN_EXIT_PORTER_ID +  " INTEGER;";
            db.execSQL(SQL_ALTER_TABLE_VISITS_ADD_EXIT_PORTER_ID);

            final String SQL_ALTER_TABLE_VISITS_ADD_IS_UPDATE = "ALTER TABLE " + VisitEntry.TABLE_NAME + " ADD " + VisitEntry.COLUMN_IS_UPDATE +  " INTEGER NOT NULL DEFAULT 0;";
            db.execSQL(SQL_ALTER_TABLE_VISITS_ADD_IS_UPDATE);

            final String SQL_ALTER_TABLE_VISITS_SUPPLIERS_ADD_EXIT_OBS = "ALTER TABLE " + SupplierVisitsEntry.TABLE_NAME + " ADD " + SupplierVisitsEntry.COLUMN_EXIT_OBS +  " TEXT;";
            db.execSQL(SQL_ALTER_TABLE_VISITS_SUPPLIERS_ADD_EXIT_OBS);


            db.execSQL(SQL_CREATE_COMMONSPACES_TABLE);
            db.execSQL(SQL_CREATE_COMMONSPACES_VISITS_TABLE);
            db.execSQL(SQL_CREATE_NAME_COMMONSPACE_COMMONSPACES_INDEX);
            db.execSQL(SQL_CREATE_COMMONSPACE_ID_SERVER_COMMONSPACES_INDEX);

            db.execSQL(SQL_CREATE_ENTRY_COMMONSPACEVISITS_INDEX);
            db.execSQL(SQL_CREATE_COMMONSPACE_ID_COMMONSPACEVISITS_INDEX);
            db.execSQL(SQL_CREATE_COMMONSPACE_VISIT_ID_SERVER_COMMONSPACEVISITS_INDEX);
            db.execSQL(SQL_CREATE_APARTMENT_ID_COMMONSPACEVISITS_INDEX);

        }

        if(oldVersion == 3){

            db.execSQL(SQL_CREATE_COMMONSPACES_TABLE);
            db.execSQL(SQL_CREATE_COMMONSPACES_VISITS_TABLE);
            db.execSQL(SQL_CREATE_NAME_COMMONSPACE_COMMONSPACES_INDEX);
            db.execSQL(SQL_CREATE_COMMONSPACE_ID_SERVER_COMMONSPACES_INDEX);

            db.execSQL(SQL_CREATE_ENTRY_COMMONSPACEVISITS_INDEX);
            db.execSQL(SQL_CREATE_COMMONSPACE_ID_COMMONSPACEVISITS_INDEX);
            db.execSQL(SQL_CREATE_COMMONSPACE_VISIT_ID_SERVER_COMMONSPACEVISITS_INDEX);
            db.execSQL(SQL_CREATE_APARTMENT_ID_COMMONSPACEVISITS_INDEX);

        }

        if(oldVersion == 4){

            db.execSQL(SQL_CREATE_RESIDENTSVEHICLES_TABLE);
            db.execSQL(SQL_CREATE_PLATE_RESIDENTSVEHICLES_INDEX);
            db.execSQL(SQL_CREATE_RESIDENTVEHICLE_ID_SERVER_RESIDENTSVEHICLES_INDEX);
            db.execSQL(SQL_CREATE_APARTMENT_ID_RESIDENTSVEHICLES_INDEX);

        }


        if(oldVersion == 5){

            db.execSQL(SQL_CREATE_PARCELTYPE_TABLE);
            db.execSQL(SQL_ADD_FIRST_PARCELTYPE);
            String SQL_ALTER_TABLE_PARCELS_ADD_PARCELTYPE = "ALTER TABLE " + ParcelEntry.TABLE_NAME + " ADD " + ParcelEntry.COLUMN_PARCELTYPE_ID +  "  INTEGER NOT NULL DEFAULT 1 REFERENCES "+ ParcelTypeEntry.TABLE_NAME + " ("+ParcelTypeEntry.COLUMN_PARCELTYPE_ID_SERVER + ")";
            db.execSQL(SQL_ALTER_TABLE_PARCELS_ADD_PARCELTYPE);

        }

        if(oldVersion == 6){

            db.execSQL(SQL_CREATE_RESIDENTSTEMPS_TABLE);
            db.execSQL(SQL_CREATE_RESIDENTTEMP_ID_SERVER_RESIDENTSTEMPS_INDEX);
            db.execSQL(SQL_CREATE_APARTMENT_ID_RESIDENTSTEMPS_INDEX);

        }

        if(oldVersion == 7){

            String SQL_ALTER_TABLE_RESIDENT_ADD_IS_DELETED = "ALTER TABLE " + ResidentEntry.TABLE_NAME + " ADD " + ResidentEntry.COLUMN_IS_DELETED +  "  INTEGER NOT NULL DEFAULT 0";
            String SQL_ALTER_TABLE_WHITELIST_ADD_PLATE = "ALTER TABLE " + WhitelistEntry.TABLE_NAME + " ADD " + WhitelistEntry.COLUMN_PLATE +  "  TEXT";
            db.execSQL(SQL_ALTER_TABLE_RESIDENT_ADD_IS_DELETED);
            db.execSQL(SQL_ALTER_TABLE_WHITELIST_ADD_PLATE);

        }

        if(oldVersion == 8){

            String SQL_ALTER_TABLE_RESIDENT_ADD_CREATED_AT = "ALTER TABLE " + ResidentEntry.TABLE_NAME + " ADD " + ResidentEntry.COLUMN_CREATED_AT +  "  DATETIME DEFAULT NULL";
            String SQL_ALTER_TABLE_RESIDENT_ADD_UPDATED_AT = "ALTER TABLE " + ResidentEntry.TABLE_NAME + " ADD " + ResidentEntry.COLUMN_UPDATED_AT +  "  DATETIME DEFAULT NULL";
            String SQL_ALTER_TABLE_RESIDENT_ADD_DELETED_AT = "ALTER TABLE " + ResidentEntry.TABLE_NAME + " ADD " + ResidentEntry.COLUMN_DELETED_AT +  "  DATETIME DEFAULT NULL";

            db.execSQL(SQL_ALTER_TABLE_RESIDENT_ADD_CREATED_AT);
            db.execSQL(SQL_ALTER_TABLE_RESIDENT_ADD_UPDATED_AT);
            db.execSQL(SQL_ALTER_TABLE_RESIDENT_ADD_DELETED_AT);


        }





    }
}
