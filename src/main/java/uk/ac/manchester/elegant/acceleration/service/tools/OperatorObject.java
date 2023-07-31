/*
 * This file is part of the ELEGANT Acceleration Service.
 * URL: https://github.com/elegant-h2020/Elegant-Acceleration-Service.git
 *
 * Copyright (c) 2023, APT Group, Department of Computer Science,
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
package uk.ac.manchester.elegant.acceleration.service.tools;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class OperatorObject {
    ConcurrentHashMap<String, String> mapTypeToFieldName;
    ArrayList<String> listOfTypes;
    boolean isObjectPublic;
    boolean isObjectStatic;
    String className;

    public OperatorObject() {
        mapTypeToFieldName = new ConcurrentHashMap<>();
        listOfTypes = new ArrayList<>();
    }

    public OperatorObject(String name) {
        super();
        this.className = name;
    }

    public void addEntryInMapTypeToVariableName(String type, String fieldName) {
        mapTypeToFieldName.put(fieldName, type);
    }

    public void addEntryInMapTypeParentObjectName(String type) {
        listOfTypes.add(type);
    }

    public String getTypeOfField(String fieldName) {
        return mapTypeToFieldName.get(fieldName);
    }

    public ArrayList getListOfTypes() {
        return listOfTypes;
    }

}