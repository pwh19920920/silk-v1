package com.spark.bitrade.entity;

/**
 * @author Zhang Yanjun
 * @time 2018.12.26 15:37
 */
public class DeviceAndroidInfo extends DeviceInfo {
    private String imei;
    private String mac;
    private String phone;
    private String androidId;
    private String serialNo;

    public DeviceAndroidInfo(String imei, String mac, String phone, String androidId, String serialNo) {
        super("Android");
        this.imei = imei;
        this.mac = mac;
        this.phone = phone;
        this.androidId = androidId;
        this.serialNo = serialNo;
    }

    public static DeviceAndroidInfo.DeviceAndroidInfoBuilder builder() {
        return new DeviceAndroidInfo.DeviceAndroidInfoBuilder();
    }

    public String getImei() {
        return this.imei;
    }

    public String getMac() {
        return this.mac;
    }

    public String getPhone() {
        return this.phone;
    }

    public String getAndroidId() {
        return this.androidId;
    }

    public String getSerialNo() {
        return this.serialNo;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAndroidId(String androidId) {
        this.androidId = androidId;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    @Override
    public String toString() {
        return "DeviceAndroidInfo(imei=" + this.getImei() + ", mac=" + this.getMac() + ", phone=" + this.getPhone() + ", androidId=" + this.getAndroidId() + ", serialNo=" + this.getSerialNo() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if(o == this) {
            return true;
        } else if(!(o instanceof DeviceAndroidInfo)) {
            return false;
        } else {
            DeviceAndroidInfo other = (DeviceAndroidInfo)o;
            if(!other.canEqual(this)) {
                return false;
            } else if(!super.equals(o)) {
                return false;
            } else {
                label73: {
                    Object this$imei = this.getImei();
                    Object other$imei = other.getImei();
                    if(this$imei == null) {
                        if(other$imei == null) {
                            break label73;
                        }
                    } else if(this$imei.equals(other$imei)) {
                        break label73;
                    }

                    return false;
                }

                Object this$mac = this.getMac();
                Object other$mac = other.getMac();
                if(this$mac == null) {
                    if(other$mac != null) {
                        return false;
                    }
                } else if(!this$mac.equals(other$mac)) {
                    return false;
                }

                label59: {
                    Object this$phone = this.getPhone();
                    Object other$phone = other.getPhone();
                    if(this$phone == null) {
                        if(other$phone == null) {
                            break label59;
                        }
                    } else if(this$phone.equals(other$phone)) {
                        break label59;
                    }

                    return false;
                }

                Object this$androidId = this.getAndroidId();
                Object other$androidId = other.getAndroidId();
                if(this$androidId == null) {
                    if(other$androidId != null) {
                        return false;
                    }
                } else if(!this$androidId.equals(other$androidId)) {
                    return false;
                }

                Object this$serialNo = this.getSerialNo();
                Object other$serialNo = other.getSerialNo();
                if(this$serialNo == null) {
                    if(other$serialNo != null) {
                        return false;
                    }
                } else if(!this$serialNo.equals(other$serialNo)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof DeviceAndroidInfo;
    }

    @Override
    public int hashCode() {
        boolean PRIME = true;
        int result = super.hashCode();
        Object $imei = this.getImei();
        result = result * 59 + ($imei == null?43:$imei.hashCode());
        Object $mac = this.getMac();
        result = result * 59 + ($mac == null?43:$mac.hashCode());
        Object $phone = this.getPhone();
        result = result * 59 + ($phone == null?43:$phone.hashCode());
        Object $androidId = this.getAndroidId();
        result = result * 59 + ($androidId == null?43:$androidId.hashCode());
        Object $serialNo = this.getSerialNo();
        result = result * 59 + ($serialNo == null?43:$serialNo.hashCode());
        return result;
    }

    public static class DeviceAndroidInfoBuilder {
        private String imei;
        private String mac;
        private String phone;
        private String androidId;
        private String serialNo;

        DeviceAndroidInfoBuilder() {
        }

        public DeviceAndroidInfo.DeviceAndroidInfoBuilder imei(String imei) {
            this.imei = imei;
            return this;
        }

        public DeviceAndroidInfo.DeviceAndroidInfoBuilder mac(String mac) {
            this.mac = mac;
            return this;
        }

        public DeviceAndroidInfo.DeviceAndroidInfoBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public DeviceAndroidInfo.DeviceAndroidInfoBuilder androidId(String androidId) {
            this.androidId = androidId;
            return this;
        }

        public DeviceAndroidInfo.DeviceAndroidInfoBuilder serialNo(String serialNo) {
            this.serialNo = serialNo;
            return this;
        }

        public DeviceAndroidInfo build() {
            return new DeviceAndroidInfo(this.imei, this.mac, this.phone, this.androidId, this.serialNo);
        }

        @Override
        public String toString() {
            return "DeviceAndroidInfo.DeviceAndroidInfoBuilder(imei=" + this.imei + ", mac=" + this.mac + ", phone=" + this.phone + ", androidId=" + this.androidId + ", serialNo=" + this.serialNo + ")";
        }
    }
}
