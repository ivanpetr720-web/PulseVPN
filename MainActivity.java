package com.pulsevpn.app;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    private LinearLayout navHome, navServers, navAccount;
    private TextView navHomeLabel, navServersLabel, navAccountLabel;
    TextView toastView;
    private Handler toastHandler = new Handler();
    private Runnable toastHide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navHome = findViewById(R.id.nav_home);
        navServers = findViewById(R.id.nav_servers);
        navAccount = findViewById(R.id.nav_account);
        navHomeLabel = findViewById(R.id.nav_home_label);
        navServersLabel = findViewById(R.id.nav_servers_label);
        navAccountLabel = findViewById(R.id.nav_account_label);
        toastView = findViewById(R.id.toast_view);

        navHome.setOnClickListener(v -> switchTab(0));
        navServers.setOnClickListener(v -> switchTab(1));
        navAccount.setOnClickListener(v -> switchTab(2));

        switchTab(0);
    }

    void switchTab(int tab) {
        Fragment f;
        switch (tab) {
            case 1: f = new ServersFragment(); break;
            case 2: f = new AccountFragment(); break;
            default: f = new HomeFragment(); break;
        }
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_container, f)
            .commit();

        // Update nav labels
        int greenColor = getColor(R.color.green);
        int dimColor = getColor(R.color.text2);
        navHomeLabel.setTextColor(tab == 0 ? greenColor : dimColor);
        navServersLabel.setTextColor(tab == 1 ? greenColor : dimColor);
        navAccountLabel.setTextColor(tab == 2 ? greenColor : dimColor);
    }

    public void showToast(String msg) {
        toastView.setText(msg);
        toastView.setVisibility(View.VISIBLE);
        toastView.animate().alpha(1f).setDuration(250).start();
        if (toastHide != null) toastHandler.removeCallbacks(toastHide);
        toastHide = () -> toastView.animate().alpha(0f).setDuration(300)
            .withEndAction(() -> toastView.setVisibility(View.GONE)).start();
        toastHandler.postDelayed(toastHide, 2800);
    }

    public static MainActivity get(Fragment f) {
        return (MainActivity) f.getActivity();
    }
}
