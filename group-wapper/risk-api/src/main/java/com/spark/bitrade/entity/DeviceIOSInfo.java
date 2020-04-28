package com.spark.bitrade.entity;

/**
 * @author Zhang Yanjun
 * @time 2018.12.26 15:37
 */
public class DeviceIOSInfo extends DeviceInfo {
    private String imei;
    private String phone;
    private String uuid;

    public DeviceIOSInfo(String imei, String phone, String uuid) {
        super("IOS");
        this.imei = imei;
        this.phone = phone;
        this.uuid = uuid;
    }

    public static DeviceIOSInfo.DeviceIOSInfoBuilder builder() {
        return new DeviceIOSInfo.DeviceIOSInfoBuilder();
    }

    public String getImei() {
        return this.imei;
    }

    public String getPhone() {
        return this.phone;
    }

    public String getUuid() {
        return this.uuid;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return "DeviceIOSInfo(imei=" + this.getImei() + ", phone=" + this.getPhone() + ", uuid=" + this.getUuid() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if(o == this) {
            return true;
        } else if(!(o instanceof DeviceIOSInfo)) {
            return false;
        } else {
            DeviceIOSInfo other = (DeviceIOSInfo)o;
            if(!other.canEqual(this)) {
                return false;
            } else if(!super.equals(o)) {
                return false;
            } else {
                label49: {
                    Object this$imei = this.getImei();
                    Object other$imei = other.getImei();
                    if(this$imei == null) {
                        if(other$imei == null) {
                            break label49;
                        }
                    } else if(this$imei.equals(other$imei)) {
                        break label49;
                    }

                    return false;
                }

                Object this$phone = this.getPhone();
                Object other$phone = other.getPhone();
                if(this$phone == null) {
                    if(other$phone != null) {
                        return false;
                    }
                } else if(!this$phone.equals(other$phone)) {
                    return false;
                }

                Object this$uuid = this.getUuid();
                Object other$uuid = other.getUuid();
                if(this$uuid == null) {
                    if(other$uuid != null) {
                        return false;
                    }
                } else if(!this$uuid.equals(other$uuid)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof DeviceIOSInfo;
    }

    @Override
    public int hashCode() {
        boolean PRIME = true;
        int result = super.hashCode();
        Object $imei = this.getImei();
        result = result * 59 + ($imei == null?43:$imei.hashCode());
        Object $phone = this.getPhone();
        result = result * 59 + ($phone == null?43:$phone.hashCode());
        Object $uuid = this.getUuid();
        result = result * 59 + ($uuid == null?43:$uuid.hashCode());
        return result;
    }

    public static class DeviceIOSInfoBuilder {
        private String imei;
        private String phone;
        private String uuid;

        DeviceIOSInfoBuilder() {
        }

        public DeviceIOSInfo.DeviceIOSInfoBuilder imei(String imei) {
            this.imei = imei;
            return this;
        }

        public DeviceIOSInfo.DeviceIOSInfoBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public DeviceIOSInfo.DeviceIOSInfoBuilder uuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

        public DeviceIOSInfo build() {
            return new DeviceIOSInfo(this.imei, this.phone, this.uuid);
        }

        @Override
        public String toString() {
            return "DeviceIOSInfo.DeviceIOSInfoBuilder(imei=" + this.imei + ", phone=" + this.phone + ", uuid=" + this.uuid + ")";
        }
    }
}
