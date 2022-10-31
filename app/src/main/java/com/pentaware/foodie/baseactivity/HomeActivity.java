package com.pentaware.foodie.baseactivity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.pentaware.foodie.R;
import com.pentaware.foodie.databinding.ActivityHomeBinding;
import com.pentaware.foodie.utils.CommonVariables;
import com.pentaware.foodie.utils.InitialData;

public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding binding;

    private AppBarConfiguration appBarConfiguration;
    private NavController navController;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if(getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setupActivity();
    }

    private void setupActivity() {
        appBarConfiguration = new AppBarConfiguration.Builder(
                // Add Top Level Fragments Here
        ).build();

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        if(CommonVariables.loggedInUserDetails.ongoingOrderId != null &&
                !CommonVariables.loggedInUserDetails.ongoingOrderId.isEmpty()) {
            navController.popBackStack(R.id.nav_home, true);
            navController.navigate(R.id.nav_ongoing_order);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
