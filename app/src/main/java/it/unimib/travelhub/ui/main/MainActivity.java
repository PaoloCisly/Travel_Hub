package it.unimib.travelhub.ui.main;

import static it.unimib.travelhub.util.Constants.LAST_UPDATE;
import static it.unimib.travelhub.util.Constants.SHARED_PREFERENCES_FILE_NAME;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import it.unimib.travelhub.R;
import it.unimib.travelhub.databinding.ActivityMainBinding;
import it.unimib.travelhub.ui.travels.AddTravelActivity;
import it.unimib.travelhub.util.SharedPreferencesUtil;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding binding;
    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        BottomNavigationView bottom_menu = findViewById(R.id.bottom_navigation);
        binding.viewPagerMain.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                binding.viewPagerMain.setUserInputEnabled(position != 1);
                bottom_menu.getMenu().getItem(position).setChecked(true);
            }
        });

        binding.viewPagerMain.setOnTouchListener((v, event) -> {
            binding.viewPagerMain.setUserInputEnabled(event.getAction() != MotionEvent.ACTION_MOVE);
            return false;
        });


        bottom_menu.setOnItemSelectedListener(item -> {
            Log.d(TAG, "Item selected: " + item.getItemId() + " " + R.id.mapFragment);
            if(item.getItemId() == R.id.homeFragment) {
                binding.viewPagerMain.setCurrentItem(0);
                return true;
//            } else if(item.getItemId() == R.id.communityFragment){
//                binding.viewPagerMain.setCurrentItem(1);
//                return true;
            } else if(item.getItemId() == R.id.mapFragment_button){
                Log.d(TAG, "MapFragment selected");
                binding.viewPagerMain.setCurrentItem(1);
                return true;
            } else if(item.getItemId() == R.id.profileFragment){
                binding.viewPagerMain.setCurrentItem(2);
                return true;
            }
            return false;
        });




        findViewById(R.id.fab_add_travel).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddTravelActivity.class);
            startActivity(intent);
        });


        FragmentManager fragmentManager = getSupportFragmentManager();
        MainFragmentAdapter myFragmentAdapter = new MainFragmentAdapter(fragmentManager, getLifecycle());
        binding.viewPagerMain.setAdapter(myFragmentAdapter);
        }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferencesUtil sharedPreferencesUtil = new SharedPreferencesUtil(getApplication());
        sharedPreferencesUtil.writeStringData(SHARED_PREFERENCES_FILE_NAME, LAST_UPDATE, "0");
        Log.d("MainActivity", "Last update deleted: " + sharedPreferencesUtil.readStringData(SHARED_PREFERENCES_FILE_NAME, LAST_UPDATE));
    }
}

