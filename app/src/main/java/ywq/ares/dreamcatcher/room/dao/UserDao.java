package ywq.ares.dreamcatcher.room.dao;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import ywq.ares.dreamcatcher.ui.bean.User;

@Dao
public interface UserDao {


    @Insert
    void insertUser(User user);


    @Update
    int updateUser(User user);


    @Delete
    int deleteUser(User user);


    @Query("select * from user where user_id = :id limit 1")
    List<User> getUser(String id);



}
