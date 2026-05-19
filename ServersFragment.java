package com.pulsevpn.app;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.List;
import java.util.stream.Collectors;

public class ServersFragment extends Fragment {

    private LinearLayout serverListContainer;
    private TextView tabFree, tabPremium;
    private EditText etSearch;
    private boolean showFree = true;
    private AppState st;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_servers, container, false);
        st = AppState.get();

        serverListContainer = v.findViewById(R.id.server_list_container);
        tabFree = v.findViewById(R.id.tab_free);
        tabPremium = v.findViewById(R.id.tab_premium);
        etSearch = v.findViewById(R.id.et_search);

        tabFree.setOnClickListener(view -> switchTab(true));
        tabPremium.setOnClickListener(view -> switchTab(false));

        etSearch.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            public void onTextChanged(CharSequence s, int i, int b, int c) { renderList(); }
            public void afterTextChanged(Editable s) {}
        });

        renderList();
        return v;
    }

    private void switchTab(boolean free) {
        showFree = free;
        int green = Color.parseColor("#00FF88");
        int black = Color.parseColor("#000000");
        int dim = Color.parseColor("#8CA8A0");
        int bg3 = Color.parseColor("#0F2328");

        tabFree.setTextColor(free ? black : dim);
        tabFree.setBackgroundResource(free ? R.drawable.btn_green : 0);
        tabFree.setBackgroundColor(free ? 0 : 0);
        if (free) tabFree.setBackgroundResource(R.drawable.btn_green);
        else tabFree.setBackgroundColor(Color.TRANSPARENT);

        tabPremium.setTextColor(free ? dim : black);
        if (!free) tabPremium.setBackgroundResource(R.drawable.btn_green);
        else tabPremium.setBackgroundColor(Color.TRANSPARENT);

        renderList();
    }

    private void renderList() {
        if (serverListContainer == null) return;
        serverListContainer.removeAllViews();
        String q = etSearch.getText().toString().toLowerCase().trim();

        List<Server> filtered = ServerData.ALL.stream()
            .filter(s -> s.isFree == showFree)
            .filter(s -> q.isEmpty() || s.name.toLowerCase().contains(q) || s.city.toLowerCase().contains(q))
            .collect(Collectors.toList());

        if (!showFree) {
            // Premium header
            View header = LayoutInflater.from(getContext()).inflate(R.layout.item_server, serverListContainer, false);
            LinearLayout root = header.findViewById(R.id.server_card_root);
            root.setBackgroundResource(R.drawable.premium_card_bg);
            TextView flag = header.findViewById(R.id.tv_flag);
            flag.setText("✨");
            TextView name = header.findViewById(R.id.tv_name);
            name.setText("Premium серверы");
            name.setTextColor(Color.parseColor("#00FF88"));
            TextView sub = header.findViewById(R.id.tv_sub);
            sub.setText("9 серверов по всему миру");
            header.findViewById(R.id.tv_ping).setVisibility(View.GONE);
            header.findViewById(R.id.tv_bars).setVisibility(View.GONE);
            serverListContainer.addView(header);
        }

        if (filtered.isEmpty()) {
            TextView empty = new TextView(getContext());
            empty.setText("Серверов не найдено 🔍");
            empty.setTextColor(Color.parseColor("#8CA8A0"));
            empty.setTextSize(14);
            empty.setPadding(0, 32, 0, 32);
            empty.setGravity(android.view.Gravity.CENTER);
            serverListContainer.addView(empty);
            return;
        }

        for (Server srv : filtered) {
            addServerCard(srv);
        }
    }

    private void addServerCard(Server srv) {
        View card = LayoutInflater.from(getContext()).inflate(R.layout.item_server, serverListContainer, false);
        LinearLayout root = card.findViewById(R.id.server_card_root);
        TextView tvFlag = card.findViewById(R.id.tv_flag);
        TextView tvName = card.findViewById(R.id.tv_name);
        TextView tvSub = card.findViewById(R.id.tv_sub);
        TextView tvPing = card.findViewById(R.id.tv_ping);
        TextView tvBars = card.findViewById(R.id.tv_bars);
        TextView tvTag = card.findViewById(R.id.tv_tag);
        TextView tvLocked = card.findViewById(R.id.tv_locked);

        tvFlag.setText(srv.flag);
        tvName.setText(srv.name);
        tvSub.setText(srv.city + " · " + srv.proto);
        tvBars.setText(srv.getBarsText());

        if (!srv.tag.isEmpty()) {
            tvTag.setText(srv.tag);
            tvTag.setVisibility(View.VISIBLE);
        }

        // Ping color
        int pingColor;
        if (srv.ping < 50) pingColor = Color.parseColor("#00FF88");
        else if (srv.ping < 100) pingColor = Color.parseColor("#FFCC00");
        else pingColor = Color.parseColor("#FF4466");
        tvPing.setText(srv.ping + "ms");
        tvPing.setTextColor(pingColor);

        // Selected highlight
        if (st.selectedServerId == srv.id) {
            root.setBackgroundResource(R.drawable.card_selected);
        }

        // Premium lock
        if (!srv.isFree) {
            tvLocked.setVisibility(View.VISIBLE);
        }

        card.setOnClickListener(v -> {
            // Premium servers need subscription
            if (!srv.isFree) {
                MainActivity.get(this).showToast("🔒 Нужен Premium · Купи в Аккаунте");
                return;
            }
            st.selectedServerId = srv.id;
            renderList();
            MainActivity.get(this).showToast(srv.flag + " Выбран: " + srv.name + ", " + srv.city);
            if (st.connected) {
                // Reconnect hint
                MainActivity.get(this).showToast("💡 Переподключись для смены сервера");
            }
        });

        serverListContainer.addView(card);
    }
}
