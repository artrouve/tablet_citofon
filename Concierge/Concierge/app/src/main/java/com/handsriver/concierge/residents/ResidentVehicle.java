package com.handsriver.concierge.residents;

/**
 * Created by Created by alain_r._trouve_silva after 09-07-17.
 */

public class ResidentVehicle {
    private long id;
    private String plate;
    private String active;

    private String isSync;
    private String isUpdate;
    private String apartmentNumber;
    private long residentvehicleIdServer;

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getIsSync() {
        return isSync;
    }

    public void setIsSync(String isSync) {
        this.isSync = isSync;
    }

    public String getIsUpdate() {
        return isUpdate;
    }

    public void setIsUpdate(String isUpdate) {
        this.isUpdate = isUpdate;
    }

    public String getApartmentNumber() {
        return apartmentNumber;
    }

    public void setApartmentNumber(String apartmentNumber) {
        this.apartmentNumber = apartmentNumber;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getResidentVehicleIdServer() {
        return residentvehicleIdServer;
    }

    public void setResidentVehicleIdServer(long residentvehicleIdServer) {
        this.residentvehicleIdServer = residentvehicleIdServer;
    }
}
