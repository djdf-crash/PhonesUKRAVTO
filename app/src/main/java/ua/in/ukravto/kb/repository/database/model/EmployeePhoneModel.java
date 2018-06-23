package ua.in.ukravto.kb.repository.database.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EmployeePhoneModel {
    @SerializedName("ID")
    @Expose
    private int iD;
    @SerializedName("full_name")
    @Expose
    private String fullName;
    @SerializedName("post")
    @Expose
    private String post;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("ContactInfo")
    @Expose
    private String contactInfo;
    @SerializedName("Phone")
    @Expose
    private String phone;
    @SerializedName("organization_name")
    @Expose
    private String organizationName;
    @SerializedName("OrganizationID")
    @Expose
    private int organizationID;
    @SerializedName("real_phone")
    @Expose
    private String realPhone;
    @SerializedName("last_update")
    @Expose
    private String lastUpdate;
    @SerializedName("delete")
    @Expose
    private boolean delete;

    public int getID() {
        return iD;
    }

    public void setID(int iD) {
        this.iD = iD;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public int getOrganizationID() {
        return organizationID;
    }

    public void setOrganizationID(int organizationID) {
        this.organizationID = organizationID;
    }

    public String getRealPhone() {
        return realPhone;
    }

    public void setRealPhone(String realPhone) {
        this.realPhone = realPhone;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

}
