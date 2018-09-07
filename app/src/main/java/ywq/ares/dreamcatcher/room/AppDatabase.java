package ywq.ares.dreamcatcher.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import ywq.ares.dreamcatcher.room.dao.SoundDao;
import ywq.ares.dreamcatcher.room.dao.UserDao;
import ywq.ares.dreamcatcher.ui.bean.SoundRecord;
import ywq.ares.dreamcatcher.ui.bean.User;

@Database(version = 2,entities = {SoundRecord.class,User.class})
public abstract class AppDatabase extends RoomDatabase {


    public abstract SoundDao soundDao();
    public abstract UserDao userDao();
}
