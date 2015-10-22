package com.wheredatapp.startupcalc;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.NumberFormat;

/*
User input, SeekBar 1: current active user count (e.g. 70)
User input, SeekBar 2: target weekly growth rate (e.g. 5%)

Result 1: required users for next week (e.g. 74)
Result 2: expected yearly multiple (e.g. 12.6x)
Result 3: expected users in a year (e.g. 12.6x70 = 882, e.g. 70 * (1.05 ^52))
*/

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TextView userCountLabel;
    private SeekBar userCountSeekBar;
    private TextView userGrowthRateLabel;
    private SeekBar userGrowthRateSeekBar;

    private TextView requiredUsersNextWeekView;
    private TextView expectedYearlyMultipleView;
    private TextView expectedUsersInYearView;
    private String sharingText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (sharingText == null) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out " + appPlayStoreLink());
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                } else {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, sharingText + "\n\n-- via " + appPlayStoreLink());
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        findViews();
        setListeners(userCountSeekBar, userGrowthRateSeekBar);
        reCalculate();
    }

    private String appPlayStoreLink() {
        return "https://play.google.com/store/apps/details?id=" + getPackageName();
    }

    final static int weeksInYear = 52;

    void reCalculate() {
        int userCountSeekBarProgress = getProgress(userCountSeekBar);
        int userGrowthRateSeekBarProgress = getProgress(userGrowthRateSeekBar);
        userCountLabel.setText(userCountSeekBarProgress + " Active users / Revenue");
        userGrowthRateLabel.setText(userGrowthRateSeekBarProgress + "% Weekly growth rate");

        double growthPercentage = userGrowthRateSeekBarProgress / 100d;

        int requiredUsersNextWeek = (int) (userCountSeekBarProgress * (1 + growthPercentage));
        requiredUsersNextWeekView.setText(format(requiredUsersNextWeek));

        double yearlyPercentage = Math.pow(1 + growthPercentage, weeksInYear);
        expectedYearlyMultipleView.setText(format(yearlyPercentage) + "x");

        int expectedUsersInYear = (int) (yearlyPercentage * userCountSeekBarProgress);
        expectedUsersInYearView.setText(format(expectedUsersInYear));

        sharingText = String.format(getString(R.string.sharing_text), format(requiredUsersNextWeek), format(expectedUsersInYear));
    }

    private String format(Object number) {
        NumberFormat f = NumberFormat.getNumberInstance();
        f.setMaximumFractionDigits(1);
        return f.format(number);
    }

    int getProgress(SeekBar seekBar) {
        int power = getStepSize(seekBar);
        int progress = seekBar.getProgress();
        int result = progress * power;
        return result;
    }

    private int getStepSize(SeekBar seekBar) {
        switch (seekBar.getId()) {
            case R.id.user_growth_rate_seek_bar:
                return 1;
            case R.id.user_count_seek_bar:
                return 50;
        }
        return 1;
    }

    private void findViews() {
        userCountLabel = (TextView) findViewById(R.id.user_count_label);
        userCountSeekBar = (SeekBar) findViewById(R.id.user_count_seek_bar);
        userGrowthRateLabel = (TextView) findViewById(R.id.user_growth_rate_label);
        userGrowthRateSeekBar = (SeekBar) findViewById(R.id.user_growth_rate_seek_bar);

        requiredUsersNextWeekView = (TextView) findViewById(R.id.required_users_next_week);
        expectedYearlyMultipleView = (TextView) findViewById(R.id.expected_yearly_multiple);
        expectedUsersInYearView = (TextView) findViewById(R.id.expected_users_in_a_year);
    }

    private void setListeners(SeekBar... seekBars) {

        for (SeekBar seekBar : seekBars) {
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    reCalculate();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.send_feedback) {
            browse("mailto:joanna@wheredatapp.com");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.inspired_by) {
            browse("http://www.paulgraham.com/growth.html");
        } else if (id == R.id.github_code) {
            browse("https://github.com/odedhb/startupcalc");
        } else if (id == R.id.created_by) {
            browse("http://wheredatapp.com");

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    void browse(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }
}
