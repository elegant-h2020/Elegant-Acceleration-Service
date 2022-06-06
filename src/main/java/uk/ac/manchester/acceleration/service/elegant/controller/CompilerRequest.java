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
