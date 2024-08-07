package com.handsriver.concierge.residents;

/**
 * Created by Created by alain_r._trouve_silva after 09-07-17.
 */

public class Resident {
    private long id;
    private String fullName;
    private String email;
    private String mobile;
    private String phone;
    private String rut;

    private String isSync;
    private String isUpdate;
    private String isDelete;
    private String apartmentNumber;
    private long residentIdServer;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getMobile() {
        return mobile;
    }

    public String getPhone() {
        return phone;
    }

    public String getRut() {
        return rut;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setRut(String rut) {
        this.rut = rut;
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


    public String getIsDelete() {
        return isDelete;
    }
    public void setIsDelete(String isDelete) {
        this.isDelete = isDelete;
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

    public long getResidentIdServer() {
        return residentIdServer;
    }

    public void setResidentIdServer(long residentIdServer) {
        this.residentIdServer = residentIdServer;
    }
}
