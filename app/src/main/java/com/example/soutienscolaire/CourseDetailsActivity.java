package com.example.soutienscolaire;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import model.Course;

public class CourseDetailsActivity extends AppCompatActivity {

    private TextView subjectTextView;
    private TextView descriptionTextView;
    private TextView dateTextView;
    private TextView teacherTextView;
    private ImageView courseImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_details);

        // Configurer la barre d'état
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.purple_200));

        // Initialiser les éléments de l'interface utilisateur
        subjectTextView = findViewById(R.id.subjectTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        dateTextView = findViewById(R.id.dateTextView);
        teacherTextView = findViewById(R.id.teacherTextView);
        courseImageView = findViewById(R.id.courseImageView);

        // Obtenir les détails du cours depuis l'intent
        Intent intent = getIntent();
        Course selectedCourse = (Course) intent.getSerializableExtra("course");

        // Afficher les détails dans l'interface utilisateur
        if (selectedCourse != null) {
            subjectTextView.setText(selectedCourse.getSubject());
            descriptionTextView.setText(selectedCourse.getDescription());
            dateTextView.setText(selectedCourse.getDate());
            teacherTextView.setText(selectedCourse.getTeacher());

            // Charger l'image avec Glide
            Glide.with(this)
                    .load(selectedCourse.getDocUri())
                    .into(courseImageView);
        }
    }
}
