package com.spark.bitrade.entity;

/**
 * @author Zhang Yanjun
 * @time 2018.12.26 15:37
 */
public class DevicePCInfo extends DeviceInfo {

    private String referer;
    private String userAgent;

    public DevicePCInfo(String referer, String userAgent) {
        super("PC");
        this.referer = referer;
        this.userAgent = userAgent;
    }

    public static DevicePCInfo.DevicePCInfoBuilder builder() {
        return new DevicePCInfo.DevicePCInfoBuilder();
    }

    public String getReferer() {
        return this.referer;
    }

    public String getUserAgent() {
        return this.userAgent;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public String toString() {
        return "DevicePCInfo(referer=" + this.getReferer() + ", userAgent=" + this.getUserAgent() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if(o == this) {
            return true;
        } else if(!(o instanceof DevicePCInfo)) {
            return false;
        } else {
            DevicePCInfo other = (DevicePCInfo)o;
            if(!other.canEqual(this)) {
                return false;
            } else if(!super.equals(o)) {
                return false;
            } else {
                Object this$referer = this.getReferer();
                Object other$referer = other.getReferer();
                if(this$referer == null) {
                    if(other$referer != null) {
                        return false;
                    }
                } else if(!this$referer.equals(other$referer)) {
                    return false;
                }

                Object this$userAgent = this.getUserAgent();
                Object other$userAgent = other.getUserAgent();
                if(this$userAgent == null) {
                    if(other$userAgent != null) {
                        return false;
                    }
                } else if(!this$userAgent.equals(other$userAgent)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof DevicePCInfo;
    }


    @Override
    public int hashCode() {
        boolean PRIME = true;
        int result = super.hashCode();
        Object $referer = this.getReferer();
        result = result * 59 + ($referer == null?43:$referer.hashCode());
        Object $userAgent = this.getUserAgent();
        result = result * 59 + ($userAgent == null?43:$userAgent.hashCode());
        return result;
    }

    public static class DevicePCInfoBuilder {
        private String referer;
        private String userAgent;

        DevicePCInfoBuilder() {
        }

        public DevicePCInfo.DevicePCInfoBuilder referer(String referer) {
            this.referer = referer;
            return this;
        }

        public DevicePCInfo.DevicePCInfoBuilder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public DevicePCInfo build() {
            return new DevicePCInfo(this.referer, this.userAgent);
        }

        @Override
        public String toString() {
            return "DevicePCInfo.DevicePCInfoBuilder(referer=" + this.referer + ", userAgent=" + this.userAgent + ")";
        }
    }
}
