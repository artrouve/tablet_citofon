package com.handsriver.concierge.payments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.handsriver.concierge.R;
import com.handsriver.concierge.utilities.Utility;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Created by alain_r._trouve_silva after 10-07-17.
 */

public class PaymentAdapterSearchList extends BaseAdapter implements Filterable {
    private Context mContext;
    private ArrayList<Payment> mList;
    private ArrayList<Payment> mPayments;
    private static final String EFECTIVO = "Efectivo";
    private static final String CHEQUE = "Cheque";
    private static final String TARJETA_CREDITO = "Tarjeta de Crédito";
    private static final String TARJETA_DEBITO = "Tarjeta de Débito";
    private static final String DEPOSITO = "Depósito";
    private static final String TRANSFERENCIA = "Transferencia";


    public PaymentAdapterSearchList(Context context,ArrayList<Payment> payments) {
        mContext = context;
        mList = payments;
        mPayments = payments;
    }

    private PaymentFilter mPaymentFilter;

    @Override
    public int getCount() {
        if (mList == null) {
            return 0;
        }
        else{
            return mList.size();
        }
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        Payment payment = mList.get(position);

        if (convertView == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_payment_search,parent,false);
        }
        else{
            view = convertView;
        }

        String paymentType = payment.getPaymentType();

        switch (paymentType){
            case "0":{
                paymentType = EFECTIVO;
                break;
            }
            case "1":{
                paymentType = TARJETA_DEBITO;
                break;
            }
            case "2":{
                paymentType = TARJETA_CREDITO;
                break;
            }
            case "3":{
                paymentType = CHEQUE;
                break;
            }
            case "4":{
                paymentType = TRANSFERENCIA;
                break;
            }
            case "5":{
                paymentType = DEPOSITO;
                break;
            }
        }

        TextView textViewRegisterDateListPayment = (TextView) view.findViewById(R.id.textViewRegisterDateListPayment);
        TextView textViewPaymentTypePayment = (TextView) view.findViewById(R.id.textViewPaymentTypePayment);
        TextView textViewApartmentListPayment = (TextView) view.findViewById(R.id.textViewApartmentListPayment);

        textViewRegisterDateListPayment.setText(Utility.changeDateFormat(payment.getDateRegister(),"ENTRY"));
        textViewPaymentTypePayment.setText(paymentType);
        textViewApartmentListPayment.setText(payment.getApartmentNumber());

        return view;
    }

    @Override
    public Filter getFilter() {
        if (mPaymentFilter == null)
            mPaymentFilter = new PaymentFilter();

        return mPaymentFilter;
    }

    public class PaymentFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();

            if (constraint == null || constraint.length() == 0){
                results.values = mPayments;
                results.count = mPayments.size();
            }
            else{
                ArrayList<Payment> filteredPayment = new ArrayList<Payment>();

                for (Payment payment : mPayments){
                    Pattern pattern = Pattern.compile("(^|\\s)" + Pattern.quote(Normalizer.normalize(constraint.toString().toUpperCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")));
                    Matcher matcher = pattern.matcher(Normalizer.normalize(payment.getApartmentNumber().toUpperCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""));

                    if (matcher.find()){
                        filteredPayment.add(payment);
                    }
                }

                results.values = filteredPayment;
                results.count = filteredPayment.size();
            }
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mList = (ArrayList<Payment>) results.values;
            notifyDataSetChanged();
        }
    }
}
