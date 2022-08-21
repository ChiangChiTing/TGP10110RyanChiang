package idv.tgp10110.tgp10110ryanchiang;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SecondActivity extends AppCompatActivity {

    private BottomNavigationView bnv_menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        findViews();
        handleBottomNavigationView();
    }

    private void handleBottomNavigationView() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fcv_bottom);
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(bnv_menu, navController);
    }

    private void findViews() {
        bnv_menu = findViewById(R.id.bnv_menu);
    }
}