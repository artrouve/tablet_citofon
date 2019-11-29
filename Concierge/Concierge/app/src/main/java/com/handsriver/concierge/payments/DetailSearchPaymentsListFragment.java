package com.handsriver.concierge.payments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.handsriver.concierge.R;
import com.handsriver.concierge.utilities.Utility;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by Created by alain_r._trouve_silva after 13-04-17.
 */

public class DetailSearchPaymentsListFragment extends Fragment {
    View rootView;
    TextView textViewPaymentType;
    TextView textViewDocumentNumber;
    TextView textViewDocumentNumberBuilding;
    TextView textViewEntryPayment;
    TextView textViewAmount;
    TextView textViewApartmentNumber;
    TextView textViewFullName;
    String paymentType;
    String documentNumber;
    String documentNumberBuilding;
    String entryPayment;
    String amount;
    String apartmentNumber;
    String fullName;


    public static final String NO_AVAILABLE = "Sin Información";
    public static final String EFECTIVO = "Efectivo";
    public static final String CHEQUE = "Cheque";
    public static final String TARJETA_CREDITO = "Tarjeta de Crédito";
    public static final String TARJETA_DEBITO = "Tarjeta de Débito";
    public static final String DEPOSITO = "Depósito";
    public static final String TRANSFERENCIA = "Transferencia";



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();

        paymentType = args.getString("paymentType", NO_AVAILABLE);
        documentNumber = args.getString("documentNumber", NO_AVAILABLE);
        documentNumberBuilding = args.getString("documentNumberBuilding", NO_AVAILABLE);
        entryPayment = Utility.changeDateFormat(args.getString("entryPayment"), "ENTRY");
        amount = args.getString("amount",NO_AVAILABLE);
        apartmentNumber = args.getString("apartmentNumber", NO_AVAILABLE);
        fullName =  args.getString("fullName", NO_AVAILABLE);

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

        DecimalFormat formatter = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        formatter.setDecimalFormatSymbols(symbols);
        formatter.setMaximumFractionDigits(0);

        rootView = inflater.inflate(R.layout.detail_payments, container, false);

        textViewPaymentType = (TextView) rootView.findViewById(R.id.textViewDetailPaymentTypePayment);
        textViewPaymentType.setText(paymentType);
        textViewFullName = (TextView) rootView.findViewById(R.id.textViewDetailPorterPayment);
        textViewFullName.setText(fullName);
        textViewDocumentNumber = (TextView) rootView.findViewById(R.id.textViewDetailDocumentNumberPayment);
        textViewDocumentNumber.setText(documentNumber);
        textViewDocumentNumberBuilding = (TextView) rootView.findViewById(R.id.textViewDetailDocumentNumberBuildingPayment);
        textViewDocumentNumberBuilding.setText(documentNumberBuilding);
        textViewEntryPayment = (TextView) rootView.findViewById(R.id.textViewDetailEntryPayment);
        textViewEntryPayment.setText(entryPayment);
        textViewAmount = (TextView) rootView.findViewById(R.id.textViewDetailAmountPayment);
        textViewAmount.setText(formatter.format(Integer.parseInt(amount)));
        textViewApartmentNumber = (TextView) rootView.findViewById(R.id.textViewDetailApartmentNumber);
        textViewApartmentNumber.setText(apartmentNumber);

        return rootView;
    }
}
