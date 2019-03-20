package ua.in.ukravto.kb.repository.database.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "organizations")
public class EmployeeOrganizationModel {
    @SerializedName("ID")
    @Expose
    @PrimaryKey
    @ColumnInfo(name = "id")
    private int iD;

    @SerializedName("Name")
    @Expose
    @ColumnInfo(name = "name")
    private String name;

    @SerializedName("IsDelete")
    @Expose
    @ColumnInfo(name = "is_delete_api")
    private Boolean isDelete;

    @ColumnInfo(name = "is_delete")
    private Boolean isDeleteBase;

    @ColumnInfo(name = "is_checked")
    private Boolean isChecked = false;

    public int getID() {
        return iD;
    }

    public void setID(Integer iD) {
        this.iD = iD;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Boolean isDelete) {
        this.isDelete = isDelete;
    }

    public Boolean getIsChecked() {
        return isChecked;
    }

    public void setIsChecked(Boolean isChecked) {
        this.isChecked = isChecked;
    }

    public Boolean getDeleteBase() {
        if (isDeleteBase == null){
            isDeleteBase = false;
        }
        return isDeleteBase;
    }

    public void setDeleteBase(Boolean deleteBase) {
        isDeleteBase = deleteBase;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this)return true;
        EmployeeOrganizationModel model = (EmployeeOrganizationModel)obj;
        return model.getID() == iD && model.getName().equals(name);
    }
}
