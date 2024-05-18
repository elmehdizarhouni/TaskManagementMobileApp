package model;

import java.io.Serializable;
public class notes implements Serializable{
    private String title;
    private String description;
    private String id;
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public notes (String title, String description , String id){
        this.title=title;
        this.description=description;
        this.id=id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
