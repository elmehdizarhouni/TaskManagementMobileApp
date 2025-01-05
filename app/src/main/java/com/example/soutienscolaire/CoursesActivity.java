package com.example.soutienscolaire;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import androidx.appcompat.widget.SearchView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.LinkedList;

import model.Course;

public class CoursesActivity extends HomeActivity {

    private FirebaseFirestore db;
    private LinkedList<Course> courses;
    private RecyclerView myRecycler;
    private MyAdapter myAdapter;
    private FloatingActionButton addCourse;
    private FirebaseAuth mAuth;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the content view to the activity_courses layout
        setContentView(R.layout.activity_courses);

        // Set the status bar color
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.purple_200));

        // Initialize Firebase Firestore and FirebaseAuth
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize the list of courses and UI elements
        courses = new LinkedList<>();
        addCourse = findViewById(R.id.fab_add);
        myRecycler = findViewById(R.id.recycler_courses);
        myAdapter = new MyAdapter(courses, this);
        myRecycler.setLayoutManager(new LinearLayoutManager(this));
        myRecycler.setAdapter(myAdapter);
        searchView = findViewById(R.id.search_view);

        // Set listener for FloatingActionButton to open AddCourseActivity
        addCourse.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), AddCourseActivity.class));
        });

        // Fetch courses from Firestore
        getCourses();

        // Set up the SearchView to filter courses
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return false;
            }
        });

        // Set up navigation drawer
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open_nav,
                R.string.close_nav);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.black));
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    // Fetch courses from Firestore
    private void getCourses() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            DocumentReference userRef = db.collection("user").document(user.getEmail());
            userRef.collection("Courses").get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            courses.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Course course = new Course(
                                        document.getString("subject"),
                                        document.getString("description"),
                                        document.getString("date"),
                                        document.getString("teacher"),
                                        document.getString("img"),
                                        document.getId()
                                );
                                courses.add(course);
                            }
                            myRecycler.setHasFixedSize(true);
                            // use a linear layout manager
                            LinearLayoutManager layoutManager = new LinearLayoutManager(CoursesActivity.this);
                            myRecycler.setLayoutManager(layoutManager);
                            // specify an adapter
                            MyAdapter myAdapter = new MyAdapter(courses, CoursesActivity.this);
                            myRecycler.setAdapter(myAdapter);
                        } else {
                            Log.e("CoursesActivity", "Error fetching courses: ", task.getException());
                        }
                    });
        }
    }


    // Perform search in Firestore
    private void performSearch(String query) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("user").document(user.getEmail())
                    .collection("Courses")
                    .orderBy("subject")
                    .startAt(query.toLowerCase())
                    .endAt(query.toLowerCase() + "\uf8ff")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            courses.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Course course = new Course(
                                        document.getString("subject"),
                                        document.getString("description"),
                                        document.getString("date"),
                                        document.getString("teacher"),
                                        document.getString("img"),
                                        document.getId()
                                );
                                courses.add(course);
                            }
                            myAdapter.notifyDataSetChanged();
                        } else {
                            Log.e("CoursesActivity", "Error performing search: ", task.getException());
                        }
                    });
        }
    }
}
