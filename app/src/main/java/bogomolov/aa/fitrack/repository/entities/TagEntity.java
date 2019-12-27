package bogomolov.aa.fitrack.repository.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class TagEntity{
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String name;


    public String toString(){
        return name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
