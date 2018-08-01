
package com.owners.pet.petowners;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.owners.pet.petowners.adapters.ViewPagerAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private FirebaseAuth mAuth;
    @BindView(R.id.tab_layout)
    TabLayout tabLayout;
    @BindView(R.id.view_pager)
    ViewPager viewPager;

    private ViewPagerAdapter viewPagerAdapter;
    private Fragment mapSearchFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setElevation(0);
        }


        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        // Add  MapSearchFragment and MessagesFragment
        mapSearchFragment = new MapSearchFragment();
        Fragment chatFragment = new ChatFragment();
        viewPagerAdapter.addFragment(mapSearchFragment, getString(R.string.map_search_fragment_tab_title));
        viewPagerAdapter.addFragment(chatFragment, getString(R.string.chat_fragment_tab_title));


        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.profile:
                Intent profileActivityIntent = new Intent(this, ProfileActivity.class);
                startActivity(profileActivityIntent);
                return true;
            case R.id.all_users:
                Intent usersActivityIntent = new Intent(this, UsersActivity.class);
                startActivity(usersActivityIntent);
                return true;
            case R.id.explore:
                Intent exploreActivityIntent = new Intent(this, ExploreActivity.class);
                startActivity(exploreActivityIntent);
                return true;
            case R.id.settings:
                Intent settingsActivityIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsActivityIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mapSearchFragment.onActivityResult(requestCode, resultCode, data);
    }
}
