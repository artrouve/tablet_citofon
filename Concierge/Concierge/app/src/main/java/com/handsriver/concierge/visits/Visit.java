package com.handsriver.concierge.visits;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Created by alain_r._trouve_silva after 10-02-17.
 */

public class Visit implements Parcelable {
    private String id;
    private String documentNumber;
    private String fullName;
    private String nationality;
    private String gender;
    private String birthdate;
    private String entry;
    private String exitDate;
    private String apartmentNumber;
    private String optional;


    public static final Parcelable.Creator<Visit> CREATOR = new Parcelable.Creator<Visit>(){
        @Override
        public Visit createFromParcel(Parcel source) {
            return null;
        }

        @Override
        public Visit[] newArray(int size) {
            return new Visit[0];
        }
    };

    public void setId(String id){
        this.id = id;
    }
    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public void setEntry(String entry) {
        this.entry = entry;
    }

    public void setExitDate(String exitDate) {
        this.exitDate = exitDate;
    }

    public void setApartmentNumber(String apartmentNumber) {
        this.apartmentNumber = apartmentNumber;
    }

    public void setOptional(String optional) {
        this.optional = optional;
    }

    public String getId() {
        return id;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public String getNationality() {
        return nationality;
    }

    public String getGender() {
        return gender;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public String getEntry() {
        return entry;
    }

    public String getExitDate() {
        return exitDate;
    }

    public String getApartmentNumber() {
        return apartmentNumber;
    }

    public String getOptional() {
        return optional;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(documentNumber);
        dest.writeString(fullName);
        dest.writeString(nationality);
        dest.writeString(gender);
        dest.writeString(birthdate);
        dest.writeString(entry);
        dest.writeString(apartmentNumber);
        dest.writeString(optional);

    }
}
