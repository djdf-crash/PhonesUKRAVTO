package ua.in.ukravto.kb.repository.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import ua.in.ukravto.kb.repository.database.model.EmployeeOrganizationModel;

@Database(entities = {EmployeeOrganizationModel.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract OrganizationDao organizationDao();
}
