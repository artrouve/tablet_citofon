package com.handsriver.concierge.residents;

/**
 * Created by Created by alain_r._trouve_silva after 09-07-17.
 */

public class ResidentTemp {
    private long id;
    private String fullName;
    private String email;
    private String phone;
    private String rut;

    private String isSync;
    private String isUpdate;
    private String apartmentNumber;
    private String startDate;
    private String endDate;

    private long residenttempIdServer;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getRut() {
        return rut;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
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

    public long getResidenttempIdServer() {
        return residenttempIdServer;
    }

    public void setResidenttempIdServer(long residenttempIdServer) {
        this.residenttempIdServer = residenttempIdServer;
    }
}
