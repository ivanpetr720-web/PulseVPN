package com.pulsevpn.app;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    private TextView tvStatus, tvTimer, tvDl, tvUl, tvIp, tvIpLocation, tvIpFlag;
    private TextView btnConnect, tvSelectedFlag, tvSelectedName, tvSelectedSub, tvSelectedPing;
    private LinearLayout statsRow;
    private Button btnWg, btnOvpn, btnAutoProto;
    private View ring1, ring2;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable, speedRunnable, pulseRunnable;
    private AppState st;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Random rnd = new Random();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        st = AppState.get();

        tvStatus = v.findViewById(R.id.tv_status);
        tvTimer = v.findViewById(R.id.tv_timer);
        tvDl = v.findViewById(R.id.tv_dl);
        tvUl = v.findViewById(R.id.tv_ul);
        tvIp = v.findViewById(R.id.tv_ip);
        tvIpLocation = v.findViewById(R.id.tv_ip_location);
        tvIpFlag = v.findViewById(R.id.tv_ip_flag);
        statsRow = v.findViewById(R.id.stats_row);
        btnConnect = v.findViewById(R.id.btn_connect);
        ring1 = v.findViewById(R.id.ring1);
        ring2 = v.findViewById(R.id.ring2);
        btnWg = v.findViewById(R.id.btn_wg);
        btnOvpn = v.findViewById(R.id.btn_ovpn);
        btnAutoProto = v.findViewById(R.id.btn_auto_proto);
        tvSelectedFlag = v.findViewById(R.id.tv_selected_flag);
        tvSelectedName = v.findViewById(R.id.tv_selected_name);
        tvSelectedSub = v.findViewById(R.id.tv_selected_sub);
        tvSelectedPing = v.findViewById(R.id.tv_selected_ping);

        btnConnect.setOnClickListener(view -> onConnectClick());
        btnWg.setOnClickListener(view -> setProtocol("wireguard"));
        btnOvpn.setOnClickListener(view -> setProtocol("openvpn"));
        btnAutoProto.setOnClickListener(view -> setProtocol("auto"));

        // Fetch real IP
        if (!st.ipLoaded) fetchRealIp();
        else updateIpDisplay(false);

        updateUI();
        if (st.connected) {
            startTimer();
            startSpeedSim();
            startPulse();
        }

        return v;
    }

    // ====== CONNECT ======
    private void onConnectClick() {
        if (st.connecting) return;
        if (st.connected) doDisconnect();
        else doConnect();
    }

    private void doConnect() {
        st.connecting = true;
        updateUI();
        handler.postDelayed(() -> {
            st.connecting = false;
            st.connected = true;
            updateUI();
            startTimer();
            startSpeedSim();
            startPulse();
            Server srv = st.getSelectedServer();
            String name = srv != null ? srv.flag + " " + srv.name : "⚡ Smart Server";
            MainActivity.get(this).showToast("✅ Подключён — " + name);
            updateIpDisplay(true);
        }, 2200);
    }

    private void doDisconnect() {
        st.connected = false;
        st.connecting = false;
        st.timerSec = 0;
        st.dlMbps = 0;
        st.ulMbps = 0;
        stopAll();
        updateUI();
        updateIpDisplay(false);
        MainActivity.get(this).showToast("🔌 Отключён");
    }

    // ====== UI ======
    private void updateUI() {
        if (!isAdded()) return;
        int green = Color.parseColor("#00FF88");
        int yellow = Color.parseColor("#FFCC00");
        int dim = Color.parseColor("#8CA8A0");
        int black = Color.parseColor("#000000");

        if (st.connected) {
            tvStatus.setText("● ПОДКЛЮЧЁН");
            tvStatus.setTextColor(green);
            btnConnect.setText("⏻\nОТКЛЮЧИТЬ");
            btnConnect.setTextColor(green);
            btnConnect.setBackgroundResource(R.drawable.connect_btn_connected);
            statsRow.setVisibility(View.VISIBLE);
            tvTimer.setVisibility(View.VISIBLE);
        } else if (st.connecting) {
            tvStatus.setText("● ПОДКЛЮЧЕНИЕ…");
            tvStatus.setTextColor(yellow);
            btnConnect.setText("⏻\n…");
            btnConnect.setTextColor(yellow);
            btnConnect.setBackgroundResource(R.drawable.connect_btn_normal);
            statsRow.setVisibility(View.GONE);
            tvTimer.setVisibility(View.GONE);
            startConnectingAnim();
        } else {
            tvStatus.setText("● НЕ ПОДКЛЮЧЁН");
            tvStatus.setTextColor(dim);
            btnConnect.setText("⏻\nCONNECT");
            btnConnect.setTextColor(dim);
            btnConnect.setBackgroundResource(R.drawable.connect_btn_normal);
            statsRow.setVisibility(View.GONE);
            tvTimer.setVisibility(View.GONE);
        }

        // Selected server card
        Server srv = st.getSelectedServer();
        if (srv != null) {
            tvSelectedFlag.setText(srv.flag);
            tvSelectedName.setText(srv.name + ", " + srv.city);
            tvSelectedSub.setText(srv.city + " · " + srv.proto);
            tvSelectedPing.setText(srv.ping + " ms");
        } else {
            tvSelectedFlag.setText("⚡");
            tvSelectedName.setText("Лучший сервер");
            tvSelectedSub.setText("Авто · WireGuard");
            tvSelectedPing.setText("12 ms");
        }
    }

    // ====== TIMER ======
    private void startTimer() {
        stopTimer();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isAdded() || !st.connected) return;
                st.timerSec++;
                int h = st.timerSec / 3600;
                int m = (st.timerSec % 3600) / 60;
                int s = st.timerSec % 60;
                tvTimer.setText(String.format(Locale.US, "%02d:%02d:%02d", h, m, s));
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(timerRunnable);
    }

    private void stopTimer() {
        if (timerRunnable != null) handler.removeCallbacks(timerRunnable);
        timerRunnable = null;
        if (isAdded()) tvTimer.setText("");
    }

    // ====== SPEED SIM ======
    private void startSpeedSim() {
        stopSpeedSim();
        speedRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isAdded() || !st.connected) return;
                st.dlMbps = 2 + rnd.nextDouble() * 8;
                st.ulMbps = 0.5 + rnd.nextDouble() * 3;
                tvDl.setText(String.format(Locale.US, "%.1f MB/s", st.dlMbps));
                tvUl.setText(String.format(Locale.US, "%.1f MB/s", st.ulMbps));
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(speedRunnable);
    }

    private void stopSpeedSim() {
        if (speedRunnable != null) handler.removeCallbacks(speedRunnable);
        speedRunnable = null;
    }

    // ====== PULSE ANIMATION ======
    private void startPulse() {
        stopPulse();
        pulseRunnable = new Runnable() {
            boolean state = true;
            @Override
            public void run() {
                if (!isAdded() || !st.connected) return;
                float alpha = state ? 0.35f : 0.1f;
                ring1.animate().alpha(alpha).setDuration(800).start();
                ring2.animate().alpha(alpha * 0.6f).setDuration(1000).start();
                state = !state;
                handler.postDelayed(this, 900);
            }
        };
        handler.post(pulseRunnable);
    }

    private void stopPulse() {
        if (pulseRunnable != null) handler.removeCallbacks(pulseRunnable);
        pulseRunnable = null;
        if (isAdded()) { ring1.setAlpha(0.15f); ring2.setAlpha(0.2f); }
    }

    // ====== CONNECTING ANIMATION ======
    private void startConnectingAnim() {
        AlphaAnimation anim = new AlphaAnimation(0.3f, 1f);
        anim.setDuration(600);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        btnConnect.startAnimation(anim);
    }

    private void stopAll() {
        btnConnect.clearAnimation();
        stopTimer();
        stopSpeedSim();
        stopPulse();
    }

    // ====== PROTOCOL ======
    private void setProtocol(String p) {
        st.protocol = p;
        int green = Color.parseColor("#00FF88");
        int dim = Color.parseColor("#8CA8A0");
        int black = Color.parseColor("#000000");
        btnWg.setTextColor(p.equals("wireguard") ? black : dim);
        btnOvpn.setTextColor(p.equals("openvpn") ? black : dim);
        btnAutoProto.setTextColor(p.equals("auto") ? black : dim);
        btnWg.setBackgroundResource(p.equals("wireguard") ? R.drawable.btn_proto_active : R.drawable.btn_proto_inactive);
        btnOvpn.setBackgroundResource(p.equals("openvpn") ? R.drawable.btn_proto_active : R.drawable.btn_proto_inactive);
        btnAutoProto.setBackgroundResource(p.equals("auto") ? R.drawable.btn_proto_active : R.drawable.btn_proto_inactive);
        String name = p.equals("wireguard") ? "WireGuard" : p.equals("openvpn") ? "OpenVPN" : "Auto";
        MainActivity.get(this).showToast("⚡ Протокол: " + name);
    }

    // ====== REAL IP FETCH ======
    private void fetchRealIp() {
        tvIp.setText("Определяется…");
        tvIpLocation.setText("Загрузка…");
        tvIpFlag.setText("🌐");
        executor.execute(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL("https://ipapi.co/json/").openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestProperty("User-Agent", "PulseVPN/1.0");
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();
                JSONObject json = new JSONObject(sb.toString());
                st.realIp = json.optString("ip", "");
                st.realCountry = json.optString("country_name", "") + ", " + json.optString("city", "");
                String cc = json.optString("country_code", "").toUpperCase();
                st.realFlag = countryToFlag(cc);
                st.ipLoaded = true;
                handler.post(() -> { if (isAdded()) updateIpDisplay(st.connected); });
            } catch (Exception e) {
                handler.post(() -> {
                    if (!isAdded()) return;
                    tvIp.setText("Нет подключения");
                    tvIpLocation.setText("Проверьте интернет");
                    tvIpFlag.setText("❌");
                });
            }
        });
    }

    private void updateIpDisplay(boolean isVpn) {
        if (!isAdded()) return;
        if (isVpn) {
            Server srv = st.getSelectedServer();
            // Show masked IP when connected
            tvIp.setText("10." + rnd.nextInt(255) + "." + rnd.nextInt(255) + "." + rnd.nextInt(255));
            tvIpLocation.setText("📍 " + (srv != null ? srv.city + ", VPN" : "Smart Server, VPN"));
            tvIpFlag.setText(srv != null ? srv.flag : "⚡");
        } else {
            if (st.ipLoaded && !st.realIp.isEmpty()) {
                tvIp.setText(st.realIp);
                tvIpLocation.setText("📍 " + st.realCountry);
                tvIpFlag.setText(st.realFlag);
            } else {
                fetchRealIp();
            }
        }
    }

    // Country code → emoji flag
    private String countryToFlag(String cc) {
        if (cc.length() != 2) return "🌐";
        int offset = 0x1F1E6;
        int first = cc.charAt(0) - 'A' + offset;
        int second = cc.charAt(1) - 'A' + offset;
        return new String(Character.toChars(first)) + new String(Character.toChars(second));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
        executor.shutdown();
    }
}
