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
package uk.ac.manchester.acceleration.service.elegant.controller;

public class ParameterInfo {
    private String[] keys;
    private int[] values;

    public ParameterInfo(String[] keys, int[] values) {
        this.keys = keys;
        this.values = values;
    }

    public String[] getKeys() {
        return keys;
    }

    public int[] getValues() {
        return values;
    }

    public void setKeys(String[] keys) {
        this.keys = keys;
    }

    public void setValues(int[] values) {
        this.values = values;
    }
}
