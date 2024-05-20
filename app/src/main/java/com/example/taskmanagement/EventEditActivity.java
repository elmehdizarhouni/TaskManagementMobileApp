package com.example.taskmanagement;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import model.Event;

public class EventEditActivity extends HomeActivity {
    private EditText eventNameET;
    private TextView eventDateTV, eventTimeTV;

    private LocalTime time;
    FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private Button add_event;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_edit);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        add_event = findViewById(R.id.addEvent);
        // Set the status bar color
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.purple_200));

        // Initialize widgets
        initWidgets();

        // Initialize selected date and time
        time = LocalTime.now(); // Initialize to the current time
        eventDateTV.setText("Date: " + CalendarUtils.formattedDate(CalendarUtils.selectedDate));
        eventTimeTV.setText("Time: " + CalendarUtils.formattedTime(time));

        // Set click listener to show TimePickerDialog
        eventTimeTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });
        add_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveEventAction();
            }
        });
        // Setup navigation drawer
        setupDrawer();
    }

    private void initWidgets() {
        eventNameET = findViewById(R.id.eventNameET);
        eventDateTV = findViewById(R.id.eventDateTV);
        eventTimeTV = findViewById(R.id.eventTimeTV);
    }

    private void setupDrawer() {
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open_nav, R.string.close_nav);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.black));
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void showTimePickerDialog() {
        // Get the current time
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Create and show the TimePickerDialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                // Update the time variable
                time = LocalTime.of(hourOfDay, minute);
                // Update the TextView with the selected time
                eventTimeTV.setText("Time " + CalendarUtils.formattedTime(time));
            }
        }, hour, minute, true);

        timePickerDialog.show();
    }

    public void saveEventAction() {
        // Get the event name from EditText
        String eventName = eventNameET.getText().toString();
        if (eventName.isEmpty()) {
            eventNameET.setError("Event name is required");
            return;
        }

        // Create a new event with the selected date and time
        Event newEvent = new Event(eventName, CalendarUtils.selectedDate, time);
        Event.eventsList.add(newEvent);
        FirebaseUser user = mAuth.getCurrentUser();
        Map<String, Object> event = new HashMap<>();
        event.put("name",eventName);
        event.put("date",eventDateTV.getText().toString());
        event.put("time",time);
        db.collection("user").document(user.getEmail()).collection("events").add(event)
                .addOnSuccessListener(documentReference -> {
                    Log.d("event add", "success");
                    //startActivity(new Intent(getApplicationContext() , WeekViewActivity.class));
                });
        // Close the activity
        finish();
    }
}