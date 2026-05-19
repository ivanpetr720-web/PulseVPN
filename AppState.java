package com.pulsevpn.app;

import java.util.ArrayList;
import java.util.List;

public class AppState {
    private static AppState instance;

    public boolean connected = false;
    public boolean connecting = false;
    public int selectedServerId = -1; // -1 = Smart
    public String protocol = "wireguard";
    public int timerSec = 0;
    public double dlMbps = 0;
    public double ulMbps = 0;
    public String realIp = "";
    public String realCountry = "";
    public String realFlag = "🌐";
    public boolean ipLoaded = false;

    // Listeners
    public interface StateListener {
        void onStateChanged();
    }
    private List<StateListener> listeners = new ArrayList<>();

    public static AppState get() {
        if (instance == null) instance = new AppState();
        return instance;
    }

    public void addListener(StateListener l) { listeners.add(l); }
    public void removeListener(StateListener l) { listeners.remove(l); }
    public void notifyAll2() { for (StateListener l : listeners) l.onStateChanged(); }

    public Server getSelectedServer() {
        if (selectedServerId == -1) return null;
        for (Server s : ServerData.ALL) {
            if (s.id == selectedServerId) return s;
        }
        return null;
    }
}
