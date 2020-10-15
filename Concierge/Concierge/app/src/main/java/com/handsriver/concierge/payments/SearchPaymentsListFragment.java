package com.handsriver.concierge.payments;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.TextView;

import com.handsriver.concierge.R;
import com.handsriver.concierge.database.ConciergeContract.PaymentEntry;
import com.handsriver.concierge.database.ConciergeContract.PorterEntry;
import com.handsriver.concierge.database.ConciergeContract.ApartmentEntry;
import com.handsriver.concierge.database.ConciergeDbHelper;
import com.handsriver.concierge.database.DatabaseManager;
import com.handsriver.concierge.database.SelectToDBRaw;
import com.handsriver.concierge.utilities.Utility;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by Created by alain_r._trouve_silva after 12-04-17.
 */

public class SearchPaymentsListFragment extends Fragment {
    View rootView;
    Callback mCallback;
    PaymentAdapterSearchList paymentsAdapter;
    ListView viewPayments;
    SearchView searchPayments;
    ArrayList<Payment> mPayments;

    public static final String EFECTIVO = "Efectivo";
    public static final String CHEQUE = "Cheque";
    public static final String TARJETA_CREDITO = "Tarjeta de Crédito";
    public static final String TARJETA_DEBITO = "Tarjeta de Débito";
    public static final String DEPOSITO = "Depósito";
    public static final String TRANSFERENCIA = "Transferencia";

    public interface Callback{
        public void onItemSelectedPayments (Payment payment);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConciergeDbHelper helper= new ConciergeDbHelper(getContext());
        DatabaseManager.initializeInstance(helper);
        mPayments = new ArrayList<Payment>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final String query = "SELECT " + PaymentEntry.TABLE_NAME + "." + PaymentEntry._ID + " as " + PaymentEntry._ID + "," +
                PaymentEntry.COLUMN_PAYMENT_TYPE + "," +
                PaymentEntry.COLUMN_NUMBER_TRX + "," +
                PaymentEntry.COLUMN_NUMBER_RECEIPT + "," +
                PaymentEntry.COLUMN_DATE_REGISTER + "," +
                PaymentEntry.COLUMN_AMOUNT + "," +
                ApartmentEntry.TABLE_NAME + "." + ApartmentEntry.COLUMN_APARTMENT_NUMBER + " as " + ApartmentEntry.COLUMN_APARTMENT_NUMBER + "," +
                PorterEntry.TABLE_NAME + "." + PorterEntry.COLUMN_FIRST_NAME + " as " + PorterEntry.COLUMN_FIRST_NAME + "," +
                PorterEntry.TABLE_NAME + "." + PorterEntry.COLUMN_LAST_NAME + " as " + PorterEntry.COLUMN_LAST_NAME +
                " FROM " + PaymentEntry.TABLE_NAME + "," + ApartmentEntry.TABLE_NAME + "," + PorterEntry.TABLE_NAME +
                " WHERE " + ApartmentEntry.TABLE_NAME + "." + ApartmentEntry.COLUMN_APARTMENT_ID_SERVER + " = " + PaymentEntry.TABLE_NAME + "." + PaymentEntry.COLUMN_APARTMENT_ID +
                " AND " + PorterEntry.TABLE_NAME + "." + PorterEntry.COLUMN_PORTER_ID_SERVER + " = " + PaymentEntry.TABLE_NAME + "." + PaymentEntry.COLUMN_PORTER_ID +
                " ORDER BY " + PaymentEntry.COLUMN_DATE_REGISTER + " DESC";


        Cursor c;
        try {
            SelectToDBRaw selectPayments = new SelectToDBRaw(query,null);
            c = selectPayments.execute().get();
        }catch (Exception e){
            c = null;
        }

        if (c != null){
            while (c.moveToNext()){
                Payment payment = new Payment();
                payment.setPaymentType(c.getString(c.getColumnIndex(PaymentEntry.COLUMN_PAYMENT_TYPE)));
                payment.setNumberTrx(c.getString(c.getColumnIndex(PaymentEntry.COLUMN_NUMBER_TRX)));
                payment.setNumberReceipt(c.getString(c.getColumnIndex(PaymentEntry.COLUMN_NUMBER_RECEIPT)));
                payment.setDateRegister(c.getString(c.getColumnIndex(PaymentEntry.COLUMN_DATE_REGISTER)));
                payment.setAmount(c.getString(c.getColumnIndex(PaymentEntry.COLUMN_AMOUNT)));
                payment.setApartmentNumber(c.getString(c.getColumnIndex(ApartmentEntry.COLUMN_APARTMENT_NUMBER)));
                payment.setFirstName(c.getString(c.getColumnIndex(PorterEntry.COLUMN_FIRST_NAME)));
                payment.setLastName(c.getString(c.getColumnIndex(PorterEntry.COLUMN_LAST_NAME)));
                mPayments.add(payment);
            }
            c.close();
            paymentsAdapter = new PaymentAdapterSearchList(getActivity(),mPayments);
        }

        rootView = inflater.inflate(R.layout.fragment_list_search_payments, container, false);

        viewPayments = (ListView) rootView.findViewById(R.id.listViewPayments);
        viewPayments.setAdapter(paymentsAdapter);

        searchPayments = (SearchView) rootView.findViewById(R.id.searchPayments);
        searchPayments.setIconified(true);

        searchPayments.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                paymentsAdapter.getFilter().filter(newText);
                return true;
            }
        });

        viewPayments.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Payment payment = (Payment) parent.getItemAtPosition(position);

                if (payment != null)
                {
                    mCallback.onItemSelectedPayments(payment);
                }

            }
        });

        viewPayments.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Utility.hideKeyboard(v,getContext());
                return false;
            }
        });

        return rootView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (Callback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement Callback");
        }
    }
}
