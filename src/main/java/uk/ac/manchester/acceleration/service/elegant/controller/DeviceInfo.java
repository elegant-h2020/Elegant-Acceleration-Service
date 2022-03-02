package uk.ac.manchester.acceleration.service.elegant.controller;

public class DeviceInfo {
    private String deviceName;
    private boolean doubleFPSupport;
    private int deviceAddressBits;
    private String deviceType;
    private String deviceExtensions;
    private int availableProcessors;

    public DeviceInfo(String deviceName, boolean doubleFPSupport, int deviceAddressBits, String deviceType, String deviceExtensions, int availableProcessors) {
        this.deviceName = deviceName;
        this.doubleFPSupport = doubleFPSupport;
        this.deviceAddressBits = deviceAddressBits;
        this.deviceType = deviceType;
        this.deviceExtensions = deviceExtensions;
        this.availableProcessors = availableProcessors;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public boolean isDoubleFPSupport() {
        return doubleFPSupport;
    }

    public int getDeviceAddressBits() {
        return deviceAddressBits;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getDeviceExtensions() {
        return deviceExtensions;
    }

    public int getAvailableProcessors() {
        return availableProcessors;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setDoubleFPSupport(boolean doubleFPSupport) {
        this.doubleFPSupport = doubleFPSupport;
    }

    public void setDeviceAddressBits(int deviceAddressBits) {
        this.deviceAddressBits = deviceAddressBits;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public void setDeviceExtensions(String deviceExtensions) {
        this.deviceExtensions = deviceExtensions;
    }

    public void setAvailableProcessors(int availableProcessors) {
        this.availableProcessors = availableProcessors;
    }
}
