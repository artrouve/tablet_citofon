package com.handsriver.concierge.parcels;

/**
 * Created by Created by alain_r._trouve_silva after 08-07-17.
 */

public class Parcel {
    private String id;
    private String uniqueId;
    private String fullName;
    private String typeParcel;
    private String observations;
    private String entryParcel;
    private String exitParcel;
    private String apartmentNumber;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getTypeParcel() {
        return typeParcel;
    }

    public void setTypeParcel(String typeParcel) {
        this.typeParcel = typeParcel;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public String getEntryParcel() {
        return entryParcel;
    }

    public void setEntryParcel(String entryParcel) {
        this.entryParcel = entryParcel;
    }

    public String getExitParcel() {
        return exitParcel;
    }

    public void setExitParcel(String exitParcel) {
        this.exitParcel = exitParcel;
    }

    public String getApartmentNumber() {
        return apartmentNumber;
    }

    public void setApartmentNumber(String apartmentNumber) {
        this.apartmentNumber = apartmentNumber;
    }
}
