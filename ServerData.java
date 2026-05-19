package com.pulsevpn.app;

import java.util.Arrays;
import java.util.List;

public class ServerData {
    public static final List<Server> ALL = Arrays.asList(
        // FREE (3 серверов)
        new Server(1,  "🇸🇪", "Швеция",         "Стокгольм",    41,  4, "🆓", "WireGuard", true),
        new Server(2,  "🇳🇱", "Нидерланды",     "Амстердам",    22,  4, "🆓", "WireGuard", true),
        new Server(3,  "🇩🇪", "Германия",        "Франкфурт",    18,  4, "🆓", "WireGuard", true),

        // PREMIUM (9 серверов)
        new Server(4,  "🇺🇸", "США",             "Нью-Йорк",     89,  3, "⚡", "OpenVPN",   false),
        new Server(5,  "🇺🇸", "США",             "Лос-Анджелес", 112, 2, "",   "WireGuard", false),
        new Server(6,  "🇬🇧", "Великобритания", "Лондон",       34,  4, "",   "OpenVPN",   false),
        new Server(7,  "🇫🇷", "Франция",         "Париж",        28,  4, "🕵️","WireGuard", false),
        new Server(8,  "🇸🇬", "Сингапур",        "Сингапур",     67,  3, "⚡", "WireGuard", false),
        new Server(9,  "🇯🇵", "Япония",          "Токио",        94,  3, "",   "WireGuard", false),
        new Server(10, "🇨🇭", "Швейцария",       "Цюрих",        31,  4, "🕵️","WireGuard", false),
        new Server(11, "🇰🇷", "Южная Корея",     "Сеул",         78,  3, "",   "WireGuard", false),
        new Server(12, "🇦🇺", "Австралия",       "Сидней",       188, 1, "",   "OpenVPN",   false)
    );
}
