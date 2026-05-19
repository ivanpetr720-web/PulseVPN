package com.pulsevpn.app;

public class Server {
    public int id;
    public String flag;
    public String name;
    public String city;
    public int ping;
    public int bars;
    public String tag;
    public String proto;
    public boolean isFree;

    public Server(int id, String flag, String name, String city, int ping, int bars, String tag, String proto, boolean isFree) {
        this.id = id;
        this.flag = flag;
        this.name = name;
        this.city = city;
        this.ping = ping;
        this.bars = bars;
        this.tag = tag;
        this.proto = proto;
        this.isFree = isFree;
    }

    public String getBarsText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) sb.append(i < bars ? "▮" : "▯");
        return sb.toString();
    }
}
