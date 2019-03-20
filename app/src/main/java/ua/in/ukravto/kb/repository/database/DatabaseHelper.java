package ua.in.ukravto.kb.repository.database;

import android.content.Context;

import androidx.room.Room;

public class DatabaseHelper {

    private static AppDatabase instanse;

    public static AppDatabase getInstanseDB(Context ctx){
        if (instanse == null){
            instanse = Room.databaseBuilder(ctx, AppDatabase.class, "database-phones")
                    .allowMainThreadQueries()
                    .build();
        }
        return instanse;
    }

}
