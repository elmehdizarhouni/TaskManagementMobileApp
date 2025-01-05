package model;

import java.io.Serializable;

public class Course implements Serializable {

    private String subject; // Matière du cours (équivalent à "title" dans Tache)
    private String description; // Description du cours
    private String date; // Date du cours (équivalent à "deadline" dans Tache)
    private String teacher; // Nom ou identifiant de l'enseignant
    private String id; // Identifiant unique du cours
    private String docUri; // Document lié au cours (facultatif)

    // Constructeur principal
    public Course(String subject, String description, String date, String teacher, String id) {
        this.subject = subject;
        this.description = description;
        this.date = date;
        this.teacher = teacher;
        this.id = id;
    }

    // Constructeur vide (nécessaire pour Firebase Firestore)
    public Course() {
    }

    // Getters et Setters
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDocUri() {
        return docUri;
    }

    public void setDocUri(String docUri) {
        this.docUri = docUri;
    }
}
