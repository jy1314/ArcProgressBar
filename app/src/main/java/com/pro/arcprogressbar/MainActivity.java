package com.pro.arcprogressbar;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    ArcProgressBar marcProgressBar;
    Button button1;
    int a = 90;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button1 = (Button) findViewById(R.id.button1);
        marcProgressBar = (ArcProgressBar) findViewById(R.id.myArcProgressBar);
        marcProgressBar.setIsNeedTitle(true);
        marcProgressBar.setTitle("123");
        marcProgressBar.setCurrentValues(20);
        final int [] colors = new int[]{
                ContextCompat.getColor(this,R.color.aqua),
                ContextCompat.getColor(this, R.color.blue),
                ContextCompat.getColor(this,R.color.mediumvioletred),
                ContextCompat.getColor(this,R.color.lime),
                ContextCompat.getColor(this,R.color.aqua)
        };
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                marcProgressBar.setColors(colors);
                marcProgressBar.setCurrentValues(100);

            }
        });


    }
}
