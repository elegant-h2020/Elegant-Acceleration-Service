package uk.ac.manchester.acceleration.service.elegant.controller;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CompilerRequest {
    private long id;
    private FileInfo fileInfo;
    private DeviceInfo deviceInfo;

    public CompilerRequest() {

    }

    public CompilerRequest(long id, FileInfo fileInfo, DeviceInfo deviceInfo) {
        this.id = id;
        this.fileInfo = fileInfo;
        this.deviceInfo = deviceInfo;
    }

    public long getId() {
        return id;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public String getFunctionProgrammingLanguage() { return this.fileInfo.getProgrammingLanguage(); }

    public String getFunctionName() { return this.fileInfo.getFunctionName(); }

    public String getDirectory() { return this.fileInfo.getDirectory(); }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setFileInfo(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
}
