package ywq.ares.dreamcatcher.ui.bean;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user")
public class User {


    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "user_id")
    private String userId = System.currentTimeMillis()+"";

    @ColumnInfo(name = "admin")
    private boolean admin =false;

    @ColumnInfo(name = "score")
    private int score = 0;

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", admin=" + admin +
                ", score=" + score +
                '}';
    }
}
