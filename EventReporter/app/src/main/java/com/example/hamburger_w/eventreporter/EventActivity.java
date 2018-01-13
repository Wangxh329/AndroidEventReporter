/**
 * Add map fragment and events fragment to Event Activity, and set map Fragment as default fragment to be added to the activity
 */

package com.example.hamburger_w.eventreporter;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

public class EventActivity extends AppCompatActivity {
    private Fragment mEventsFragment;
    private Fragment mEventMapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        if (mEventsFragment == null) {
            mEventsFragment = new EventsFragment();
        }

        // default show events list
        getSupportFragmentManager().beginTransaction().add(R.id.relativelayout_event, mEventsFragment).commit(); // add fragment to activity

        // lazy loading(when using the fragment map, we then load it)
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        // Set Item click listener to the menu items
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_event_list:
                        item.setChecked(true);
                        getSupportFragmentManager().beginTransaction().replace(R.id.relativelayout_event, mEventsFragment).commit();
                        break;
                    case R.id.action_event_map:
                        if (mEventMapFragment == null) {
                            mEventMapFragment = new EventMapFragment();
                        }
                        item.setChecked(true);
                        getSupportFragmentManager().beginTransaction().replace(R.id.relativelayout_event, mEventMapFragment).commit();
                }
                return false;
            }
        });
    }
}
