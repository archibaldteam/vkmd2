package ru.scorpio92.vkmd2.data.repository.db.base;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import java.io.File;

import ru.scorpio92.vkmd2.data.entity.CachedTrack;
import ru.scorpio92.vkmd2.data.entity.OfflineSearchItem;
import ru.scorpio92.vkmd2.data.entity.OnlineTrack;
import ru.scorpio92.vkmd2.data.entity.Track;
import ru.scorpio92.vkmd2.data.repository.db.dao.CacheDAO;
import ru.scorpio92.vkmd2.data.repository.db.dao.OfflineSearchDAO;
import ru.scorpio92.vkmd2.data.repository.db.dao.OnlineTrackDAO;
import ru.scorpio92.vkmd2.data.repository.db.dao.TrackDAO;

import static ru.scorpio92.vkmd2.App.APP_DIR;
import static ru.scorpio92.vkmd2.BuildConfig.DB_FOLDER;
import static ru.scorpio92.vkmd2.BuildConfig.DB_NAME;


@Database(entities = {Track.class, CachedTrack.class, OnlineTrack.class, OfflineSearchItem.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public static void initDB(Context context) {
        File dbDir = new File(APP_DIR + "/" + DB_FOLDER);
        if (!dbDir.exists())
            dbDir.mkdirs();

        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, APP_DIR + "/" + DB_FOLDER + "/" + DB_NAME).allowMainThreadQueries().build();
        }
    }

    public static void closeDB() {
        if (instance != null) {
            instance.close();
            instance = null;
        }
    }

    public static AppDatabase getInstance() {
        return instance;
    }

    public abstract TrackDAO trackDAO();

    public abstract CacheDAO cacheDAO();

    public abstract OfflineSearchDAO offlineSearchDAO();

    public abstract OnlineTrackDAO onlineTrackDAO();
}