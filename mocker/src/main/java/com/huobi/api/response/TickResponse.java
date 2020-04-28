package com.huobi.api.response;

public class TickResponse {
    public String status;
    public String ch;
    public long ts;
    public TickDetail tick;

    public class TickDetail {
        public long id;
        public long ts;
        public double open;
        public double close;
        public double high;
        public double low;

    }
}
