package com.oliverscherf.tetheringwithtrafficcontrol;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;


public class MainActivity extends AppCompatActivity {

    private SectionsPagerAdapter sectionsPagerAdapter;

    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.sectionsPagerAdapter = new SectionsPagerAdapter(this.getSupportFragmentManager());
        this.viewPager = (ViewPager) findViewById(R.id.view_pager);
        this.viewPager.setAdapter(this.sectionsPagerAdapter);
        this.viewPager.setOffscreenPageLimit(4);
    }
}
