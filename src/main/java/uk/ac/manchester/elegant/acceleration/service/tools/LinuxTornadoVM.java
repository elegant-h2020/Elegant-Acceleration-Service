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

import uk.ac.manchester.elegant.acceleration.service.controller.EnvironmentVariables;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class LinuxTornadoVM implements TornadoVMInterface {
    ProcessBuilder tornadoVMProcessBuilder;
    static Map<String, String> environmentTornadoVM;
    Process tornadoVMProcess;
    int exitCode;
    String output;

    public LinuxTornadoVM() {
        super();
        this.tornadoVMProcessBuilder = new ProcessBuilder();
        initializeEnvironment();
        tornadoVMProcessBuilder.directory(new File(environmentTornadoVM.get("SERVICE_HOME")));
    }

    public static String getEnvironmentVariable(String key) {
        return environmentTornadoVM.get(key);
    }

    @Override
    public void initializeEnvironment() {
        environmentTornadoVM = tornadoVMProcessBuilder.environment();
        environmentTornadoVM.put(EnvironmentVariables.JAVA_HOME, System.getenv("JAVA_HOME"));
        environmentTornadoVM.put(EnvironmentVariables.SERVICE_HOME, System.getenv("SERVICE_HOME"));
        environmentTornadoVM.put(EnvironmentVariables.TORNADOVM_ROOT, System.getenv("TORNADOVM_ROOT"));
        environmentTornadoVM.put(EnvironmentVariables.TORNADO_SDK, System.getenv("TORNADOVM_ROOT") + "/bin/sdk");
        environmentTornadoVM.put(EnvironmentVariables.UPLOADED_DIR, System.getenv("SERVICE_HOME") + "/service_db/uploaded");
        environmentTornadoVM.put(EnvironmentVariables.GENERATED_KERNELS_DIR, System.getenv("SERVICE_HOME") + "/service_db/generated");
        environmentTornadoVM.put(EnvironmentVariables.BOILERPLATE_DIR, System.getenv("SERVICE_HOME") + "/service_db/boilerplate");
    }

    @Override
    public int getExitCode() {
        return 0;
    }

    private String[] getCommandForCompileToBytecode(long id, String functionName) {
        ArrayList<String> args = new ArrayList<>();
        args.add(environmentTornadoVM.get(EnvironmentVariables.JAVA_HOME) + "/bin/javac");
        args.add("-cp");
        args.add(environmentTornadoVM.get(EnvironmentVariables.TORNADOVM_ROOT) + "/dist/tornado-sdk/tornado-sdk-0.15.2-dev-b19aa7f/share/java/tornado/tornado-api-0.15.2-dev.jar");
        args.add("-g:vars");
        args.add(environmentTornadoVM.get(EnvironmentVariables.BOILERPLATE_DIR) + "/" + id + "/" + ClassGenerator.getVirtualClassFileName(functionName));
        return args.toArray(new String[args.size()]);
    }

    private String[] getCommandForVirtualCompilation(long id, String deviceDescriptionJsonFileName, String kernelName, String parameterSizeJsonFileName, String generatedKernelFileName) {
        ArrayList<String> args = new ArrayList<>();
        String classpath = environmentTornadoVM.get(EnvironmentVariables.BOILERPLATE_DIR) + "/" + id;
        String classFile = environmentTornadoVM.get(EnvironmentVariables.BOILERPLATE_DIR) + "/" + id + File.separator + ClassGenerator.getVirtualClassName(kernelName) + ".class";
        args.add(environmentTornadoVM.get(EnvironmentVariables.SERVICE_HOME) + "/bin/runCompilation.sh");
        args.add(environmentTornadoVM.get(EnvironmentVariables.TORNADO_SDK) + "/bin/tornado");
        args.add(classpath);
        args.add(classFile);
        args.add(deviceDescriptionJsonFileName);
        args.add(parameterSizeJsonFileName);
        args.add(generatedKernelFileName);
        args.add(kernelName);
        return args.toArray(new String[args.size()]);
    }

    // TODO Deprecate
    private String convertToClassName(String inputClassName) {
        return inputClassName.replace("class ", "").replace(".", "/");
    }

    public void compileToBytecode(long id, String methodFileName, String functionName) throws IOException, InterruptedException {
        OperatorInfo operatorInfo = OperatorParser.parse(methodFileName, functionName);
        if (operatorInfo == null) {
            System.err.println("OperatorInfo object is null after parsing the input file.");
        }
        // 1. Replace input/output objects to TornadoVM objects
        OperatorParser.tornadifyIO(operatorInfo);
        // 2. Rewrite the operator function to be tornadified
        String classBody = assembleClassOfInputMethodToClassFile(methodFileName, functionName, operatorInfo);
        File file = createNewFileForGeneratedClass(id, classBody);
        writeGeneratedClassToFile(classBody, file, functionName);

        tornadoVMProcessBuilder.command(getCommandForCompileToBytecode(id, functionName));
        this.tornadoVMProcess = tornadoVMProcessBuilder.start();
        int exitCode = tornadoVMProcessWaitFor();

        printOutputOfProcess(tornadoVMProcess);
    }

    private static String assembleClassOfInputMethodToClassFile(String methodFileName, String functionName, OperatorInfo operatorInfo) {
        return ClassGenerator.generateBoilerplateCode(methodFileName, functionName, operatorInfo);
    }

    private static File createNewFileForGeneratedClass(long id, String classBody) {
        File idDirectory = new File(environmentTornadoVM.get(EnvironmentVariables.BOILERPLATE_DIR) + "/" + id);
        if (!idDirectory.exists()) {
            idDirectory.mkdirs();
        }
        return idDirectory;
    }

    private static void writeGeneratedClassToFile(String classBody, File file, String functionName) {
        ClassGenerator.writeClassToFile(classBody, file.getAbsolutePath() + File.separator + ClassGenerator.getVirtualClassFileName(functionName));
    }

    public void compileBytecodeToOpenCL(long id, String deviceDescriptionJsonFileName, String kernelName, String parameterSizeJsonFileName, String generatedKernelFileName)
            throws IOException, InterruptedException {

        tornadoVMProcessBuilder.command(getCommandForVirtualCompilation(id, deviceDescriptionJsonFileName, kernelName, parameterSizeJsonFileName, generatedKernelFileName));
        this.tornadoVMProcess = tornadoVMProcessBuilder.start();
        int exitCode = tornadoVMProcessWaitFor();

        printOutputOfProcess(tornadoVMProcess);
    }

    private void printOutputOfProcess(Process process) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int tornadoVMProcessWaitFor() throws InterruptedException {
        return this.tornadoVMProcess.waitFor();
    }
}
