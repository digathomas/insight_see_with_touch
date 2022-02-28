package com.example.insight;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import detection.CameraActivity;
import detection.DetectorActivity;
import lidar.LidarActivity;

public class MainActivity extends AppCompatActivity {

    private Button lidarButton;
    private Button detectionButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = (Button)findViewById(R.id.button_maps);
        lidarButton = findViewById(R.id.lidarButton);
        detectionButton = findViewById(R.id.ObjDetectButton);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, GpsActivity.class));
            }
        });

        lidarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LidarActivity.class));
            }
        });

        detectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DetectorActivity.class));
            }
        });

    }

}