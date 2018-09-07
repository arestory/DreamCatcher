package ywq.ares.dreamcatcher.room.dao;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import io.reactivex.Flowable;
import ywq.ares.dreamcatcher.ui.bean.SoundRecord;

@Dao
public interface SoundDao {


    @Query("select * from sound_record order by time_stamp desc")
    Flowable<List<SoundRecord>> queryAll();


    @Delete
    int delete(SoundRecord record);



    @Delete
   void deleteRecords(List<SoundRecord> soundRecords);


    @Insert
   long insert(SoundRecord soundRecord);


    @Insert
    long[] insertList(List<SoundRecord> soundRecords);



}
