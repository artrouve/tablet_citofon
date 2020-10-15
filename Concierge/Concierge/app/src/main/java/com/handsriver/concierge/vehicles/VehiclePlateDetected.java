package com.handsriver.concierge.vehicles;

import java.io.Serializable;

/**
 * Created by Created by alain_r._trouve_silva after 08-07-17.
 */

public class VehiclePlateDetected implements Serializable {

    private String licensePlate;
    private String Date;
    private String urlImage;
    private Boolean selected = false;

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }


    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String Date) {
        this.Date = Date;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

}
