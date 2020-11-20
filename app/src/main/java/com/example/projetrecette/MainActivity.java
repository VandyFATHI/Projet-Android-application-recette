package com.example.projetrecette;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.projetrecette.Drawer.Login.LoginActivity;
import com.example.projetrecette.Drawer.SignUp.SignUpActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private AppBarConfiguration mAppBarConfiguration;
    NavController navController;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navController = Navigation.findNavController(this, R.id.MainFragment);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigationView);
        Appbar();
        Drawer();
        checkLogin();

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkLogin();
    }

    public void Appbar(){
        BottomNavigationView btnavView = findViewById(R.id.nav_view);

        NavigationUI.setupWithNavController(btnavView, navController);
    }

    public void Drawer(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.navigation_login:
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                break;
            case R.id.navigation_signup:
                Intent intent2 = new Intent(this, SignUpActivity.class);
                startActivity(intent2);
                break;
            case R.id.navigation_signout:
                FirebaseAuth.getInstance().signOut();
                checkLogin();
                break;

        }
        return true;
    }

    public void checkLogin(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {

            /*Items*/
            navigationView.getMenu().setGroupVisible(R.id.drawerLoggedIn,true);
            navigationView.getMenu().setGroupVisible(R.id.drawerUnlogged,false);

            /*Header*/
            navigationView.removeHeaderView(navigationView.getHeaderView(0));
            navigationView.inflateHeaderView(R.layout.drawer_header);
        }else{
            /*Items*/
            navigationView.getMenu().setGroupVisible(R.id.drawerLoggedIn,false);
            navigationView.getMenu().setGroupVisible(R.id.drawerUnlogged,true);

            /*Header*/
            navigationView.removeHeaderView(navigationView.getHeaderView(0));
            navigationView.inflateHeaderView(R.layout.drawer_header_unlogged);
        }
    }
}