package com.pulsevpn.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

public class AccountFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_account, container, false);

        TextView btnBuy = v.findViewById(R.id.btn_buy);
        SwitchCompat swKs = v.findViewById(R.id.sw_ks);
        SwitchCompat swStealth = v.findViewById(R.id.sw_stealth);
        SwitchCompat swAds = v.findViewById(R.id.sw_ads);

        btnBuy.setOnClickListener(view ->
            MainActivity.get(this).showToast("💳 Оплата — скоро! Следи за обновлениями"));

        swKs.setOnCheckedChangeListener((btn, checked) ->
            MainActivity.get(this).showToast(checked ? "🔒 Kill Switch включён" : "🔓 Kill Switch выключен"));

        swStealth.setOnCheckedChangeListener((btn, checked) ->
            MainActivity.get(this).showToast(checked ? "🕵️ Stealth Mode активен" : "👁 Stealth выключен"));

        swAds.setOnCheckedChangeListener((btn, checked) ->
            MainActivity.get(this).showToast(checked ? "🛡️ Реклама заблокирована" : "⚠️ Реклама разблокирована"));

        return v;
    }
}
