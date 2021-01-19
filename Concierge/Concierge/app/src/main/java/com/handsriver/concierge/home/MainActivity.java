package com.handsriver.concierge.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.handsriver.concierge.R;
import com.handsriver.concierge.commonspaces.CommonspaceVisit;
import com.handsriver.concierge.commonspaces.CommonspacesvisitRegisterFragment;
import com.handsriver.concierge.commonspaces.DetailSearchCommonspacesvisitsListFragment;
import com.handsriver.concierge.commonspaces.SearchCommonspacesvisitsFragment;
import com.handsriver.concierge.commonspaces.SearchCommonspacesvisitsListFragment;
import com.handsriver.concierge.parcels.DetailSearchParcelsListFragment;
import com.handsriver.concierge.parcels.Parcel;
import com.handsriver.concierge.parcels.ParcelsRegisterFragment;
import com.handsriver.concierge.parcels.SearchParcelsFragment;
import com.handsriver.concierge.parcels.SearchParcelsListFragment;
import com.handsriver.concierge.payments.DetailSearchPaymentsListFragment;
import com.handsriver.concierge.payments.Payment;
import com.handsriver.concierge.payments.PaymentsRegisterFragment;
import com.handsriver.concierge.payments.SearchPaymentsFragment;
import com.handsriver.concierge.payments.SearchPaymentsListFragment;
import com.handsriver.concierge.residents.ResidentRegisterFragment;
import com.handsriver.concierge.residents.SearchParkingFragment;
import com.handsriver.concierge.residents.SearchResidentsFragment;
import com.handsriver.concierge.residents.SearchWarehouseFragment;
import com.handsriver.concierge.suppliers.DetailSearchSuppliersListFragment;
import com.handsriver.concierge.suppliers.SearchSuppliersFragment;
import com.handsriver.concierge.suppliers.SearchSuppliersListFragment;
import com.handsriver.concierge.suppliers.SupplierVisit;
import com.handsriver.concierge.suppliers.SuppliersRegisterFragment;
import com.handsriver.concierge.timekeeping.DatePickerFragment;
import com.handsriver.concierge.timekeeping.DetailSearchTimekeepingListFragment;
import com.handsriver.concierge.timekeeping.EntryTimekeepingFragment;
import com.handsriver.concierge.timekeeping.ExitTimekeepingFragment;
import com.handsriver.concierge.timekeeping.SearchTimekeepingFragment;
import com.handsriver.concierge.timekeeping.SearchTimekeepingListFragment;
import com.handsriver.concierge.timekeeping.Timekeeping;
import com.handsriver.concierge.utilities.Utility;
import com.handsriver.concierge.vehicles.DetailSearchVehiclesListFragment;
import com.handsriver.concierge.vehicles.SearchVehiclesFragment;
import com.handsriver.concierge.vehicles.SearchVehiclesListFragment;
import com.handsriver.concierge.vehicles.Vehicle;
import com.handsriver.concierge.visits.DetailSearchVisitsListFragment;
import com.handsriver.concierge.visits.SearchVisitsFragment;
import com.handsriver.concierge.visits.SearchVisitsListFragment;
import com.handsriver.concierge.visits.Visit;
import com.handsriver.concierge.visits.VisitsRegisterFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        SearchVisitsListFragment.Callback,
        SearchVehiclesListFragment.Callback,
        SearchParcelsListFragment.Callback,
        SearchSuppliersListFragment.Callback,
        SearchCommonspacesvisitsListFragment.Callback,
        SearchTimekeepingListFragment.Callback,
        SearchPaymentsListFragment.Callback{

    DrawerLayout drawerLayout;
    Toolbar toolbar;
    NavigationView navigationView;
    TextView textViewFullNamePorter;
    public static final String PREFS_NAME = "PorterPrefs";
    String fullname;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Utility.hideKeyboard(v,getBaseContext());
                return false;
            }
        });

        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
        drawerLayout.setScrimColor(Color.TRANSPARENT);

        navigationView = (NavigationView)findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);

        SharedPreferences porterPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        fullname = porterPrefs.getString(getString(R.string.fullNameVar),"Sistema Concierge");

        textViewFullNamePorter = (TextView) header.findViewById(R.id.textViewfullNamePorter);
        textViewFullNamePorter.setText(fullname);

        HomeFragment fragmentHome = new HomeFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.frame_main,fragmentHome).commit();
    }

    @Override
    public void onBackPressed() {
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);

        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else{
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.home) {
            HomeFragment fragmentHome = new HomeFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_main,fragmentHome).commit();
            toolbar.setTitle(getString(R.string.app_name));
            return true;

        } else if (id == R.id.register_visits){
            VisitsRegisterFragment fragmentRegisterVisits = new VisitsRegisterFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_main,fragmentRegisterVisits).commit();
            toolbar.setTitle(getString(R.string.visits_register));
            return true;

        } else if (id == R.id.search_visits){
            SearchVisitsFragment fragmentSearchVisits = new SearchVisitsFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_main,fragmentSearchVisits).commit();
            toolbar.setTitle(getString(R.string.visits_search));
            return true;

        } else if (id == R.id.search_vehicles) {
            SearchVehiclesFragment fragmentSearchVehicles = new SearchVehiclesFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_main,fragmentSearchVehicles).commit();
            toolbar.setTitle(getString(R.string.vehicles_search));
            return true;

        } else if (id == R.id.register_parcels){
            ParcelsRegisterFragment fragmentParcelsRegister = new ParcelsRegisterFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_main,fragmentParcelsRegister).commit();
            toolbar.setTitle(getString(R.string.parcels_register));
            return true;

        } else if (id == R.id.search_parcels) {
            SearchParcelsFragment fragmentSearchParcels = new SearchParcelsFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_main,fragmentSearchParcels).commit();
            toolbar.setTitle(getString(R.string.parcels_search));
            return true;

        } else if (id == R.id.register_suppliers) {
            SuppliersRegisterFragment fragmentRegisterSuppliers = new SuppliersRegisterFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_main,fragmentRegisterSuppliers).commit();
            toolbar.setTitle(getString(R.string.suppliers_register));
            return true;

        } else if (id == R.id.search_suppliers) {
            SearchSuppliersFragment fragmentSearchSuppliers = new SearchSuppliersFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_main,fragmentSearchSuppliers).commit();
            toolbar.setTitle(getString(R.string.suppliers_search));
            return true;

        } else if (id == R.id.register_visitscommonspaces) {
            CommonspacesvisitRegisterFragment fragmentRegisterCommonspacesvisit = new CommonspacesvisitRegisterFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_main,fragmentRegisterCommonspacesvisit).commit();
            toolbar.setTitle(getString(R.string.commonspacesvisits_register));
            return true;

        } else if (id == R.id.search_commonspacesvisits) {
            SearchCommonspacesvisitsFragment fragmentSearchCommonspacesvisits = new SearchCommonspacesvisitsFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_main,fragmentSearchCommonspacesvisits).commit();
            toolbar.setTitle(getString(R.string.commonspacesvisits_search));
            return true;


        } else if (id == R.id.entryPorter) {
            EntryTimekeepingFragment fragmentEntryTimekeeping = new EntryTimekeepingFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_main,fragmentEntryTimekeeping).commit();
            toolbar.setTitle(getString(R.string.entry_porter));
            return true;

        } else if (id == R.id.exitPorter){
            ExitTimekeepingFragment fragmentExitTimekeeping = new ExitTimekeepingFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_main,fragmentExitTimekeeping).commit();
            toolbar.setTitle(getString(R.string.exit_porter));
            return true;

        } else if (id == R.id.searchEntryExit) {
            SearchTimekeepingFragment fragmentSearchTimekeeping = new SearchTimekeepingFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_main,fragmentSearchTimekeeping).commit();
            toolbar.setTitle(getString(R.string.search_entry_exit));
            return true;

        } else if (id == R.id.registerResidents) {
            ResidentRegisterFragment fragmentRegisterResidents = new ResidentRegisterFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_main,fragmentRegisterResidents).commit();
            toolbar.setTitle(getString(R.string.registerResident));
            return true;

        } else if (id == R.id.searchResidents) {
            SearchResidentsFragment fragmentSearchResidents = new SearchResidentsFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_main,fragmentSearchResidents).commit();
            toolbar.setTitle(getString(R.string.searchResident));
            return true;

        } else if (id == R.id.searchParking) {
            SearchParkingFragment fragmentSearchParking = new SearchParkingFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_main,fragmentSearchParking).commit();
            toolbar.setTitle(getString(R.string.searchParking));
            return true;

        } else if (id == R.id.searchWarehouse) {
            SearchWarehouseFragment fragmentSearchWarehouses = new SearchWarehouseFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_main,fragmentSearchWarehouses).commit();
            toolbar.setTitle(getString(R.string.searchWarehouse));
            return true;

        } else if (id == R.id.paymentsRegister) {
            PaymentsRegisterFragment fragmentRegisterPayments = new PaymentsRegisterFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_main,fragmentRegisterPayments).commit();
            toolbar.setTitle(getString(R.string.paymentsRegister));
            return true;

        } else if (id == R.id.searchPayments) {
            SearchPaymentsFragment fragmentSearchPayment = new SearchPaymentsFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_main,fragmentSearchPayment).commit();
            toolbar.setTitle(getString(R.string.searchPayments));
            return true;

        } else if (id == R.id.action_change_password){
            DialogChangePassword dialog = new DialogChangePassword();
            dialog.show(getSupportFragmentManager(), "dialog");
            return true;

        }else if (id == R.id.action_exit) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return true;
    }

    @Override
    public void onItemSelected(Visit visit) {

        Bundle args = new Bundle();

        args.putString("id",visit.getId());
        args.putString("fullname",visit.getFullName());
        args.putString("documentNumber",visit.getDocumentNumber());
        args.putString("nationality",visit.getNationality());
        args.putString("gender",visit.getGender());
        args.putString("birthdate",visit.getBirthdate());
        args.putString("entry",visit.getEntry());
        args.putString("apartmentNumber",visit.getApartmentNumber());
        args.putString("exitDate",visit.getExitDate());
        args.putString("optional",visit.getOptional());

        DetailSearchVisitsListFragment detailFragment = new DetailSearchVisitsListFragment();
        detailFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction().replace(R.id.detail_visits,detailFragment).commit();
    }

    @Override
    public void onItemSelectedVehicle(Vehicle vehicle) {

        Bundle args = new Bundle();
        args.putString("id",vehicle.getId());
        args.putString("licensePlate",vehicle.getLicensePlate());
        args.putString("entry",vehicle.getEntry());
        args.putString("apartmentNumber",vehicle.getApartmentNumber());
        args.putString("parkingNumber",vehicle.getParkingNumber());
        args.putString("exitDate",vehicle.getExitDate());
        args.putString("fineDate",vehicle.getFineDate());

        DetailSearchVehiclesListFragment detailFragment = new DetailSearchVehiclesListFragment();
        detailFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction().replace(R.id.detail_vehicles,detailFragment).commit();
    }

    @Override
    public void onItemSelectedParcels(Parcel parcel) {
        Bundle args = new Bundle();
        args.putString("id",parcel.getId());
        args.putString("uniqueId",parcel.getUniqueId());
        args.putString("fullName",parcel.getFullName());
        args.putString("observations",parcel.getObservations());
        args.putString("entryParcel",parcel.getEntryParcel());
        args.putString("apartmentNumber",parcel.getApartmentNumber());

        DetailSearchParcelsListFragment detailFragment = new DetailSearchParcelsListFragment();
        detailFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction().replace(R.id.detail_parcel,detailFragment).commit();
    }

    @Override
    public void onItemSelectedSuppliers(SupplierVisit visit) {
        Bundle args = new Bundle();
        args.putString("entry",visit.getEntrySupplier());
        args.putString("nameSupplier",visit.getNameSupplier());
        args.putString("gatewayId",visit.getGatewayId());
        args.putString("supplierId",visit.getSupplierId());
        args.putString("exitSupplier",visit.getExitSupplier());

        DetailSearchSuppliersListFragment detailFragment = new DetailSearchSuppliersListFragment();
        detailFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction().replace(R.id.detail_suppliers,detailFragment).commit();
    }

    @Override
    public void onItemSelectedCommonspacevisit(CommonspaceVisit visit) {

        Bundle args = new Bundle();
        args.putString("id",visit.getId());
        args.putString("commonspace",visit.getCommonspaceName());
        args.putString("name",visit.getFullName());
        args.putString("document_number",visit.getDocumentNumber());
        args.putString("entry",visit.getEntry());
        args.putString("apartmentNumber",visit.getApartmentNumber());
        args.putString("exitDate",visit.getExitDate());


        DetailSearchCommonspacesvisitsListFragment detailFragment = new DetailSearchCommonspacesvisitsListFragment();
        detailFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction().replace(R.id.detail_commonspacevisits,detailFragment).commit();
    }



    @Override
    public void onItemSelectedTimekeeping(Timekeeping timekeeping) {
        Bundle args = new Bundle();
        args.putString("firstName",timekeeping.getFirstName());
        args.putString("lastName",timekeeping.getLastName());
        args.putString("rut",timekeeping.getRut());
        args.putString("entryPorter",timekeeping.getEntryPorter());
        args.putString("exitPorter",timekeeping.getExitPorter());

        DetailSearchTimekeepingListFragment detailFragment = new DetailSearchTimekeepingListFragment();
        detailFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction().replace(R.id.detail_timekeeping,detailFragment).commit();
    }

    @Override
    public void onItemSelectedPayments(Payment payment) {
        Bundle args = new Bundle();
        args.putString("paymentType",payment.getPaymentType());
        args.putString("documentNumber",payment.getNumberTrx());
        args.putString("documentNumberBuilding",payment.getNumberReceipt());
        args.putString("entryPayment",payment.getDateRegister());
        args.putString("amount",payment.getAmount());
        args.putString("apartmentNumber",payment.getApartmentNumber());
        args.putString("fullName",payment.getFirstName()+" "+payment.getLastName());

        DetailSearchPaymentsListFragment detailFragment = new DetailSearchPaymentsListFragment();
        detailFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction().replace(R.id.detail_payment,detailFragment).commit();
    }


}
