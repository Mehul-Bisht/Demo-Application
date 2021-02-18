package com.example.demoapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import com.example.demoapplication.Adapters.ViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    ViewPager2 viewPager2;
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager2 = findViewById(R.id.viewpager2);
        tabLayout = findViewById(R.id.tab_layout);

        viewPager2.setAdapter(new ViewPagerAdapter(this));

        new TabLayoutMediator(tabLayout,viewPager2, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Users");
                break;
                case 1: tab.setText("Enroll");
                break;
            }
                }
        ).attach();
    }
}