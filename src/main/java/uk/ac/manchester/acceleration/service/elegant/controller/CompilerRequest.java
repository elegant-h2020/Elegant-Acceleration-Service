package uk.ac.manchester.acceleration.service.elegant.controller;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CompilerRequest {
    private long id;
    private String functionCode;
    private String deviceInfo;

    public CompilerRequest() {

    }

    public CompilerRequest(long id, String functionCode, String deviceInfo) {
        this.id = id;
        this.functionCode = functionCode;
        this.deviceInfo = deviceInfo;
    }

    public long getId() {
        return id;
    }

    public String getFunctionCode() {
        return functionCode;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setFunctionCode(String functionCode) {
        this.functionCode = functionCode;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
}
