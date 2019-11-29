package com.handsriver.concierge.database;

import android.provider.BaseColumns;

/**
 * Created by Created by alain_r._trouve_silva after 12-01-17.
 */

public class ConciergeContract {

    public static final class PorterEntry implements BaseColumns{

        /*
        * TABLE NAME
        * */
        public static final String TABLE_NAME = "porters";

        /*
        * COLUMNS TABLE PORTERS
        * */
        public static final String COLUMN_FIRST_NAME = "first_name";
        public static final String COLUMN_LAST_NAME = "last_name";
        public static final String COLUMN_RUT = "rut";
        public static final String COLUMN_PASSWORD = "password";
        public static final String COLUMN_IS_UPDATE_PASSWORD = "is_update";
        public static final String COLUMN_ACTIVE = "active";
        public static final String COLUMN_PORTER_ID_SERVER = "porter_id_server";

    }

    public static final class TimekeepingEntry implements BaseColumns{

        /*
        * TABLE NAME
        * */
        public static final String TABLE_NAME = "timekeeping";

        /*
        * COLUMNS TABLE TIMEKEEPING
        * */
        public static final String COLUMN_ENTRY_PORTER = "entry_porter";
        public static final String COLUMN_ENTRY_HASH = "entry_hash";
        public static final String COLUMN_EXIT_PORTER = "exit_porter";
        public static final String COLUMN_EXIT_HASH = "exit_hash";
        public static final String COLUMN_GATEWAY_ID = "gateway_id";
        public static final String COLUMN_ENTRY_PORTER_ID = "entry_porter_id";
        public static final String COLUMN_EXIT_PORTER_ID = "exit_porter_id";
        public static final String COLUMN_PORTER_ID = "porter_id";
        public static final String COLUMN_TIMEKEEPING_ID_SERVER = "timekeeping_id_server";
        public static final String COLUMN_IS_SYNC = "is_sync";
        public static final String COLUMN_IS_UPDATE = "is_update";

    }

    public static final class SupplierEntry implements BaseColumns{

        /*
        * TABLE NAME
        * */
        public static final String TABLE_NAME = "suppliers";

        /*
        * COLUMNS TABLE SUPPLIERS
        * */
        public static final String COLUMN_NAME_SUPPLIER = "name_supplier";
        public static final String COLUMN_SUPPLIER_ID_SERVER = "supplier_id_server";
        public static final String COLUMN_ACTIVE = "active";

    }

    public static final class SupplierVisitsEntry implements BaseColumns{

        /*
        * TABLE NAME
        * */
        public static final String TABLE_NAME = "suppliers_visits";

         /*
        * COLUMNS TABLE SUPPLIERS_VISITS
        * */
        public static final String COLUMN_DOCUMENT_NUMBER = "document_number";
        public static final String COLUMN_FULL_NAME = "full_name";
        public static final String COLUMN_NATIONALITY = "nationality";
        public static final String COLUMN_GENDER = "gender";
        public static final String COLUMN_BIRTHDATE = "birthdate";
        public static final String COLUMN_OCR = "ocr";
        public static final String COLUMN_LICENSE_PLATE = "license_plate";
        public static final String COLUMN_ENTRY = "entry";
        public static final String COLUMN_EXIT_SUPPLIER = "exit_supplier";
        public static final String COLUMN_GATEWAY_ID = "gateway_id";
        public static final String COLUMN_SUPPLIER_ID = "supplier_id";
        public static final String COLUMN_ENTRY_PORTER_ID = "entry_porter_id";
        public static final String COLUMN_EXIT_PORTER_ID = "exit_porter_id";
        public static final String COLUMN_SUPPLIER_VISIT_ID_SERVER = "supplier_visit_id_server";
        public static final String COLUMN_IS_SYNC = "is_sync";
        public static final String COLUMN_IS_UPDATE = "is_update";
    }

    public static final class VehicleEntry implements BaseColumns{

        /*
        * TABLE NAME
        * */
        public static final String TABLE_NAME = "vehicles";

         /*
        * COLUMNS TABLE VEHICLES
        * */
        public static final String COLUMN_LICENSE_PLATE = "license_plate";
        public static final String COLUMN_ENTRY = "entry";
        public static final String COLUMN_FINE_DATE = "fine_date";
        public static final String COLUMN_GATEWAY_ID = "gateway_id";
        public static final String COLUMN_APARTMENT_ID = "apartment_id";
        public static final String COLUMN_PORTER_ID = "porter_id";
        public static final String COLUMN_FINE_PORTER_ID = "fine_porter_id";
        public static final String COLUMN_EXIT_DATE = "exit_date";
        public static final String COLUMN_PARKING_NUMBER = "parking_number";
        public static final String COLUMN_EXIT_PORTER_ID = "exit_porter_id";
        public static final String COLUMN_VEHICLE_ID_SERVER = "vehicle_id_server";
        public static final String COLUMN_IS_SYNC = "is_sync";
        public static final String COLUMN_IS_UPDATE = "is_update";
        public static final String COLUMN_IS_UPDATE_FINE = "is_update_fine";
        public static final String COLUMN_IS_SEND_ALERT_FINE = "is_send_alert";

    }

    public static final class VisitEntry implements BaseColumns{

        /*
        * TABLE NAME
        * */
        public static final String TABLE_NAME = "visits";

         /*
        * COLUMNS TABLE VISITS
        * */
        public static final String COLUMN_DOCUMENT_NUMBER = "document_number";
        public static final String COLUMN_FULL_NAME = "full_name";
        public static final String COLUMN_NATIONALITY = "nationality";
        public static final String COLUMN_GENDER = "gender";
        public static final String COLUMN_BIRTHDATE = "birthdate";
        public static final String COLUMN_OCR = "ocr";
        public static final String COLUMN_ENTRY = "entry";
        public static final String COLUMN_GATEWAY_ID = "gateway_id";
        public static final String COLUMN_APARTMENT_ID = "apartment_id";
        public static final String COLUMN_PORTER_ID = "porter_id";
        public static final String COLUMN_VISIT_ID_SERVER = "visit_id_server";
        public static final String COLUMN_IS_SYNC = "is_sync";

    }

    public static final class ParcelEntry implements BaseColumns{

        /*
        * TABLE NAME
        * */
        public static final String TABLE_NAME = "parcels";

         /*
        * COLUMNS TABLE PARCELS
        * */
        public static final String COLUMN_UNIQUE_ID = "unique_id_parcel";
        public static final String COLUMN_FULL_NAME = "full_name";
        public static final String COLUMN_OBSERVATIONS = "observations";
        public static final String COLUMN_ENTRY_PARCEL = "entry_parcel";
        public static final String COLUMN_EXIT_PARCEL = "exit_parcel";
        public static final String COLUMN_EXIT_FULLNAME = "exit_full_name";
        public static final String COLUMN_EXIT_DOCUMENT_NUMBER = "exit_document_number";
        public static final String COLUMN_GATEWAY_ID = "gateway_id";
        public static final String COLUMN_APARTMENT_ID = "apartment_id";
        public static final String COLUMN_ENTRY_PARCEL_PORTER_ID = "entry_parcel_porter_id";
        public static final String COLUMN_EXIT_PARCEL_PORTER_ID = "exit_parcel_porter_id";
        public static final String COLUMN_PARCEL_ID_SERVER = "parcel_id_server";
        public static final String COLUMN_IS_SYNC = "is_sync";
        public static final String COLUMN_IS_UPDATE = "is_update";
    }

    public static final class ApartmentEntry implements BaseColumns{

        /*
        * TABLE NAME
        * */
        public static final String TABLE_NAME = "apartments";

        /*
        * COLUMNS TABLE APARTMENTS
        * */
        public static final String COLUMN_APARTMENT_NUMBER = "apartment_number";
        public static final String COLUMN_APARTMENT_ID_SERVER = "apartment_id_server";
        public static final String COLUMN_ACTIVE = "active";

    }

    public static final class ResidentEntry implements BaseColumns{

        /*
        * TABLE NAME
        * */
        public static final String TABLE_NAME = "residents";

        /*
        * COLUMNS TABLE RESIDENTS
        * */
        public static final String COLUMN_FULL_NAME = "full_name";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_APARTMENT_ID = "apartment_id";
        public static final String COLUMN_TOKEN = "token";
        public static final String COLUMN_PUSH_NOTIFICATIONS = "push_notifications";
        public static final String COLUMN_RESIDENT_ID_SERVER = "resident_id_server";
        public static final String COLUMN_REQUEST_CODE = "request_code";
        public static final String COLUMN_IS_SYNC = "is_sync";
        public static final String COLUMN_IS_UPDATE = "is_update";
    }

    public static final class BlacklistEntry implements BaseColumns{

        /*
        * TABLE NAME
        * */
        public static final String TABLE_NAME = "blacklist";

        /*
        * COLUMNS TABLE BLACKLIST
        * */
        public static final String COLUMN_DOCUMENT_NUMBER = "document_number";
        public static final String COLUMN_IDENTIFICATION = "identification";
        public static final String COLUMN_OBSERVATIONS = "observations";
        public static final String COLUMN_APARTMENT_ID = "apartment_id";
        public static final String COLUMN_BLACKLIST_ID_SERVER = "blacklist_id_server";

    }

    public static final class WhitelistEntry implements BaseColumns{

        /*
        * TABLE NAME
        * */
        public static final String TABLE_NAME = "whitelist";

        /*
        * COLUMNS TABLE WHITELIST
        * */
        public static final String COLUMN_DOCUMENT_NUMBER = "document_number";
        public static final String COLUMN_FULL_NAME = "full_name";
        public static final String COLUMN_APARTMENT_ID = "apartment_id";
        public static final String COLUMN_WHITELIST_ID_SERVER = "whitelist_id_server";

    }

    public static final class PaymentEntry implements BaseColumns{
        /*
       * TABLE NAME
       * */
        public static final String TABLE_NAME = "payments";

        /*
        * COLUMNS TABLE PAYMENT
        * */
        public static final String COLUMN_PAYMENT_TYPE = "payment_type";
        public static final String COLUMN_NUMBER_TRX = "number_trx";
        public static final String COLUMN_NUMBER_RECEIPT = "number_receipt";
        public static final String COLUMN_DATE_REGISTER = "date_register";
        public static final String COLUMN_AMOUNT = "amount";
        public static final String COLUMN_APARTMENT_ID = "apartment_id";
        public static final String COLUMN_PORTER_ID = "porter_id";
        public static final String COLUMN_PAYMENT_ID_SERVER = "payment_id_server";
        public static final String COLUMN_IS_SYNC = "is_sync";
        public static final String COLUMN_GATEWAY_ID = "gateway_id";
    }

    public static final class ParkingEntry implements BaseColumns{

        /*
        * TABLE NAME
        * */
        public static final String TABLE_NAME = "parking";

        /*
        * COLUMNS TABLE PARKING
        * */
        public static final String COLUMN_PARKING_NUMBER = "parking_number";
        public static final String COLUMN_APARTMENT_ID = "apartment_id";
        public static final String COLUMN_PARKING_ID_SERVER = "parking_id_server";


    }

    public static final class WarehouseEntry implements BaseColumns{

        /*
        * TABLE NAME
        * */
        public static final String TABLE_NAME = "warehouses";

        /*
        * COLUMNS TABLE WAREHOUSE
        * */
        public static final String COLUMN_WAREHOUSE_NUMBER = "warehouse_number";
        public static final String COLUMN_APARTMENT_ID = "apartment_id";
        public static final String COLUMN_WAREHOUSE_ID_SERVER = "warehouse_id_server";


    }


}
