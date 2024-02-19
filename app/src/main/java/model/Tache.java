package model;

import java.io.Serializable;

public class Tache implements Serializable {

    private String title;
    private String description;
    private String deadline;
    private String img;
    private String doc_uri;

    public Tache(String title, String description, String deadline, String img){
        this.title=title;
        this.description=description;
        this.deadline=deadline;
        this.img=img;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getDoc_uri() {
        return doc_uri;
    }

    public void setDoc_uri(String doc_uri) {
        this.doc_uri = doc_uri;
    }
}
