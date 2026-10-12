package com.vamshigollapelly.lostfoundapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.libraries.places.api.Places;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!Places.isInitialized()) {
            Places.initialize(
                getApplicationContext(),
                "YOUR_API_KEY_HERE"
            );
        }

        Button btnHome = findViewById(R.id.btnHomeItems);
        Button btnCreate = findViewById(R.id.btnCreateAdvert);
        Button btnShow = findViewById(R.id.btnShowAll);
        Button btnMap = findViewById(R.id.btnShowOnMap);

        btnHome.setOnClickListener(v ->
            startActivity(new Intent(this, HomeItemsActivity.class))
        );

        btnCreate.setOnClickListener(v ->
            startActivity(new Intent(this, CreateAdvertActivity.class))
        );

        btnShow.setOnClickListener(v ->
            startActivity(new Intent(this, ShowAllItemsActivity.class))
        );

        btnMap.setOnClickListener(v ->
            startActivity(new Intent(this, MapActivity.class))
        );
    }
}
