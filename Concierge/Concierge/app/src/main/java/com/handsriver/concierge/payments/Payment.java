package com.handsriver.concierge.payments;

/**
 * Created by Created by alain_r._trouve_silva after 10-07-17.
 */

public class Payment {
    private String paymentType;
    private String numberTrx;
    private String numberReceipt;
    private String dateRegister;
    private String amount;
    private String firstName;
    private String lastName;
    private String apartmentNumber;

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getNumberTrx() {
        return numberTrx;
    }

    public void setNumberTrx(String numberTrx) {
        this.numberTrx = numberTrx;
    }

    public String getNumberReceipt() {
        return numberReceipt;
    }

    public void setNumberReceipt(String numberReceipt) {
        this.numberReceipt = numberReceipt;
    }

    public String getDateRegister() {
        return dateRegister;
    }

    public void setDateRegister(String dateRegister) {
        this.dateRegister = dateRegister;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getApartmentNumber() {
        return apartmentNumber;
    }

    public void setApartmentNumber(String apartmentNumber) {
        this.apartmentNumber = apartmentNumber;
    }
}
