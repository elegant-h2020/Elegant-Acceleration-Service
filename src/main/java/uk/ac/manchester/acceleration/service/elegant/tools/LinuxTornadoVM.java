package uk.ac.manchester.acceleration.service.elegant.tools;

import uk.ac.manchester.acceleration.service.elegant.controller.EnvironmentVariables;

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
        tornadoVMProcessBuilder.directory(new File(environmentTornadoVM.get("SERVICE_DIR")));
    }

    public static String getEnvironmentVariable(String key) {
        return environmentTornadoVM.get(key);
    }

    @Override
    public void initializeEnvironment() {
        environmentTornadoVM = tornadoVMProcessBuilder.environment();
        environmentTornadoVM.put(EnvironmentVariables.JAVA_HOME, "/home/thanos/installation/graalvm-ce-java11-22.2.0");
        environmentTornadoVM.put(EnvironmentVariables.SERVICE_DIR, "/home/thanos/repositories/Elegant-Acceleration-Service");
        environmentTornadoVM.put(EnvironmentVariables.TORNADOVM_DIR, "/home/thanos/repositories/tornadoVM2");
        environmentTornadoVM.put(EnvironmentVariables.TORNADO_SDK, "/home/thanos/repositories/tornadoVM2/bin/sdk");
        environmentTornadoVM.put(EnvironmentVariables.GENERATED_KERNELS_DIR, "/home/thanos/repositories/Elegant-Acceleration-Service/examples/generated");
        environmentTornadoVM.put(EnvironmentVariables.BOILERPLATE_DIR, "/home/thanos/repositories/Elegant-Acceleration-Service/examples/boilerplate/");
    }

    @Override
    public int getExitCode() {
        return 0;
    }

    private String[] getCommandForCompileToBytecode(String methodFileName, long id) {
        ArrayList<String> args = new ArrayList<>();
        args.add(environmentTornadoVM.get(EnvironmentVariables.JAVA_HOME) + "/bin/javac");
        args.add("-cp");
        args.add(environmentTornadoVM.get(EnvironmentVariables.TORNADOVM_DIR) + "/dist/tornado-sdk/tornado-sdk-0.15-dev-afef0d5/share/java/tornado/tornado-api-0.15-dev.jar");
        args.add("-g:vars");
        args.add(environmentTornadoVM.get(EnvironmentVariables.BOILERPLATE_DIR) + id + "/" + ClassGenerator.getVirtualClassFileName(methodFileName));
        return args.toArray(new String[args.size()]);
    }

    private String[] getCommandForVirtualCompilation(long id, String methodFileName, String deviceDescriptionJsonFileName, String generatedKernelFileName) {
        ArrayList<String> args = new ArrayList<>();
        String classpath = environmentTornadoVM.get(EnvironmentVariables.BOILERPLATE_DIR) + id;
        String classFile = environmentTornadoVM.get(EnvironmentVariables.BOILERPLATE_DIR) + id + File.separator + ClassGenerator.getVirtualClassName(methodFileName) + ".class";
        String deviceJsonFile = deviceDescriptionJsonFileName;
        args.add(environmentTornadoVM.get(EnvironmentVariables.SERVICE_DIR) + "/bin/runCompilation.sh");
        args.add(classpath);
        args.add(classFile);
        args.add(deviceJsonFile);
        args.add(generatedKernelFileName);
        args.add(ClassGenerator.getMethodNameFromFileName(methodFileName));
        return args.toArray(new String[args.size()]);
    }

    public void compileToBytecode(long id, String methodFileName) throws IOException, InterruptedException {
        String classBody = ClassGenerator.generateBoilerplateCode(methodFileName);
        File idDirectory = new File(environmentTornadoVM.get(EnvironmentVariables.BOILERPLATE_DIR) + id);
        if (!idDirectory.exists()) {
            idDirectory.mkdirs();
        }
        ClassGenerator.writeClassToFile(classBody, idDirectory.getAbsolutePath() + File.separator + ClassGenerator.getVirtualClassFileName(methodFileName));

        tornadoVMProcessBuilder.command(getCommandForCompileToBytecode(methodFileName, id));
        this.tornadoVMProcess = tornadoVMProcessBuilder.start();
        int exitCode = tornadoVMProcessWaitFor();

        printOutputOfProcess(tornadoVMProcess);
    }

    public void compileBytecodeToOpenCL(long id, String methodFileName, String deviceDescriptionJsonFileName, String generatedKernelFileName) throws IOException, InterruptedException {
        tornadoVMProcessBuilder.command(getCommandForVirtualCompilation(id, methodFileName, deviceDescriptionJsonFileName, generatedKernelFileName));
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
