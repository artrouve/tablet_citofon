package com.handsriver.concierge.login;

/**
 * Created by Created by alain_r._trouve_silva after 12-03-18.
 */

public class Porter {
    private String id;
    private String firstName;
    private String lastName;
    private String rut;
    private String password;
    private int porterIdServer;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName(){ return this.firstName+" "+this.lastName; }

    public int getPorterIdServer() {
        return porterIdServer;
    }

    public void setPorterIdServer(int porterIdServer) {
        this.porterIdServer = porterIdServer;
    }
}
