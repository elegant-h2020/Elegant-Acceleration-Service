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

public class StaticAnalyzer {
    static boolean isLineEmpty(String string) {
        return string.equals("\t") || string.equals(" ");
    }

    static boolean lineStartsAComment(String line) {
        return line.startsWith("//");
    }

    static boolean lineStartsABlockComment(String line) {
        return line.startsWith("/*");
    }

    static boolean lineContainsStaticClass(String line) {
        return line.contains("static class") || (line.contains("class") && !(line.contains("public")));
    }

    static boolean lineHasConstantValue(String line) {
        return line.contains("final");
    }

    static boolean lineContainsStatic(String line) {
        return line.contains("static");
    }

    static boolean lineEndsABlockComment(String line) {
        return line.contains("*/");
    }

    static boolean lineStartsAMainMethod(String line) {
        return line.contains("public static void main");
    }

    static boolean isLineOutOfScope(String line) {
        return line.startsWith("import") || line.startsWith("package");
    }

    static boolean lineImplementsAFunction(String line) {
        return line.contains("implements");
    }
}
