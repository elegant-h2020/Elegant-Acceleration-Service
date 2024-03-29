/*
 * This file is part of the ELEGANT Acceleration Service.
 * URL: https://github.com/elegant-h2020/Elegant-Acceleration-Service.git
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
package uk.ac.manchester.elegant.acceleration.service.controller;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CompilationRequest {
    public enum State {
        INITIAL, SUBMITTED, COMPLETED, UNSUPPORTED, FAILED
    }

    private long id;
    private FileInfo fileInfo;
    private DeviceInfo deviceInfo;

    private ParameterInfo parameterInfo;

    private State state;

    public CompilationRequest(FileInfo fileInfo, DeviceInfo deviceInfo, ParameterInfo parameterInfo) {
        this.fileInfo = fileInfo;
        this.deviceInfo = deviceInfo;
        this.parameterInfo = parameterInfo;
        this.state = State.INITIAL;
    }

    public CompilationRequest(long id, FileInfo fileInfo, DeviceInfo deviceInfo, ParameterInfo parameterInfo) {
        this.id = id;
        this.fileInfo = fileInfo;
        this.deviceInfo = deviceInfo;
        this.parameterInfo = parameterInfo;
        this.state = State.INITIAL;
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

    public ParameterInfo getParameterInfo() {
        return parameterInfo;
    }

    public State getState() {
        return state;
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

    public void setParameterInfo(ParameterInfo parameterInfo) {
        this.parameterInfo = parameterInfo;
    }

    public void setState(State state) {
        this.state = state;
    }
}
