/*
 * This file is part of the ELEGANT Acceleration Service.
 * URL: https://github.com/stratika/elegant-acceleration-service
 *
 * Copyright (c) 2022, APT Group, Department of Computer Science,
 * The University of Manchester. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.manchester.acceleration.service.elegant.controller;

public class DeviceInfo {
    private String deviceName;
    private boolean doubleFPSupport;
    private MaxWorkItems maxWorkItems;
    private int deviceAddressBits;
    private String deviceType;
    private String deviceExtensions;
    private int availableProcessors;

    public DeviceInfo() {
    }

    public DeviceInfo(String deviceName, boolean doubleFPSupport, MaxWorkItems maxWorkItems, int deviceAddressBits, String deviceType, String deviceExtensions, int availableProcessors) {
        this.deviceName = deviceName;
        this.doubleFPSupport = doubleFPSupport;
        this.maxWorkItems = maxWorkItems;
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

    public MaxWorkItems getMaxWorkItems() {
        return maxWorkItems;
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

    public void setMaxWorkItems(MaxWorkItems maxWorkItems) {
        this.maxWorkItems = maxWorkItems;
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
