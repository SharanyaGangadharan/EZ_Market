package com.example.shara.courseproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class BottomNavigation extends AppCompatActivity {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    HomeFragment fragment = new HomeFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("signedIn", "SignedIn");
                    fragment.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment).addToBackStack(null).commit();
                    return true;
                case R.id.nav_categories:
                    CategoryFragment fragment1 = new CategoryFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment1).addToBackStack(null).commit();
                    return true;
                case R.id.nav_sell:
                    SellFragment fragment2 = new SellFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment2).addToBackStack(null).commit();
                    return true;
                case R.id.nav_chat:
                    ChatListFragment fragment3 = new ChatListFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment3).addToBackStack(null).commit();
                    return true;
                case R.id.nav_cart:
                    CartFragment fragment4 = new CartFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment4).addToBackStack(null).commit();
                    return true;
            }
            return false;
        }

    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            Intent go1 = new Intent(this,LoginActivity.class);
            startActivity(go1);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);
        setTitle("Trade-Off");
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        HomeFragment fragment = new HomeFragment();
        Bundle bundle = new Bundle();
        bundle.putString("signedIn", "SignedIn");
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment).addToBackStack(null).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_user_listing, menu);
        return true;
    }

}
