package uk.ac.manchester.acceleration.service.elegant.tools;

import uk.ac.manchester.acceleration.service.elegant.controller.EnvironmentVariables;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class LinuxTornadoVM implements TornadoVMInterface {
    ProcessBuilder tornadoVMProcessBuilder;

    static Map<String, String> environment;
    Process tornadoVMProcess;
    int exitCode;
    String output;

    public LinuxTornadoVM() {
        super();
        this.tornadoVMProcessBuilder = new ProcessBuilder();
        initializeEnvironment();
        // tornadoVMProcessBuilder.directory(new File(environment.get("SERVICE_DIR")));
    }

    public static String getEnvironmentVariable(String key) {
        return environment.get(key);
    }

    @Override
    public void initializeEnvironment() {
        environment = tornadoVMProcessBuilder.environment();
        environment.put(EnvironmentVariables.JAVA_HOME, "/home/thanos/installation/graalvm-ce-java11-22.2.0");
        environment.put(EnvironmentVariables.SERVICE_DIR, "/home/thanos/repositories/Elegant-Acceleration-Service");
        environment.put(EnvironmentVariables.TORNADOVM_DIR, "/home/thanos/repositories/tornadoVM2");
        environment.put(EnvironmentVariables.TORNADO_SDK, "/home/thanos/repositories/tornadoVM2/bin/sdk");
        environment.put(EnvironmentVariables.GENERATED_KERNELS_DIR, "/home/thanos/repositories/Elegant-Acceleration-Service/examples/generated");
        environment.put(EnvironmentVariables.BOILERPLATE_DIR, "/home/thanos/repositories/Elegant-Acceleration-Service/examples/boilerplate/");
    }

    @Override
    public int getExitCode() {
        return 0;
    }

    private String[] getCommandForCompileToBytecode(String methodFileName, long id) {
        ArrayList<String> args = new ArrayList<>();
        args.add(environment.get(EnvironmentVariables.JAVA_HOME) + "/bin/javac");
        args.add("-cp");
        args.add(environment.get(EnvironmentVariables.TORNADOVM_DIR) + "/dist/tornado-sdk/tornado-sdk-0.15-dev-afef0d5/share/java/tornado/tornado-api-0.15-dev.jar");
        args.add("-g:vars");
        args.add(environment.get(EnvironmentVariables.BOILERPLATE_DIR) + ClassGenerator.getVirtualClassFileName(methodFileName, id));
        return args.toArray(new String[args.size()]);
    }

    private String[] getCommandForVirtualCompilation(long id, String methodFileName, String deviceDescriptionJsonFileName, String generatedKernelFileName) {
        ArrayList<String> args = new ArrayList<>();
        args.add(environment.get(EnvironmentVariables.TORNADO_SDK) + "/bin/tornado");
        args.add("-cp");
        args.add(":" + environment.get(EnvironmentVariables.BOILERPLATE_DIR));
        args.add("--jvm=\"-Dtornado.input.classfile.dir=" + environment.get(EnvironmentVariables.BOILERPLATE_DIR) + ClassGenerator.getVirtualClassName(methodFileName, id) + ".class");
        args.add("-Dtornado.device.desc=" + deviceDescriptionJsonFileName);
        args.add("-Dtornado.virtual.device=True");
        args.add("-Dtornado.cim.mode=True");
        args.add("-Dtornado.print.kernel=True");
        args.add("-Dtornado.print.kernel.dir=" + generatedKernelFileName);
        args.add("\"");
        args.add("uk.ac.manchester.tornado.drivers.opencl.service.frontend.TestFrontEnd");
        args.add("--params");
        args.add(ClassGenerator.getMethodNameFromFileName(methodFileName));
        return args.toArray(new String[args.size()]);
    }

    public void compileToBytecode(long id, String methodFileName) throws IOException, InterruptedException {
        String classBody = ClassGenerator.generateBoilerplateCode(methodFileName, id);
        ClassGenerator.writeClassToFile(classBody, environment.get(EnvironmentVariables.BOILERPLATE_DIR) + ClassGenerator.getVirtualClassFileName(methodFileName, id));
        tornadoVMProcessBuilder.command(getCommandForCompileToBytecode(methodFileName, id));
        this.tornadoVMProcess = tornadoVMProcessBuilder.start();
        int exitCode = waitFor();

    }

    public void compileBytecodeToOpenCL(long id, String methodFileName, String deviceDescriptionJsonFileName, String generatedKernelFileName) throws IOException, InterruptedException {
        System.out.println("Command for virtual compilation:" + Arrays.toString(getCommandForVirtualCompilation(id, methodFileName, deviceDescriptionJsonFileName, generatedKernelFileName)));
        tornadoVMProcessBuilder.command(getCommandForVirtualCompilation(id, methodFileName, deviceDescriptionJsonFileName, generatedKernelFileName));
        this.tornadoVMProcess = tornadoVMProcessBuilder.start();
        int exitCode = waitFor();

    }

    public int waitFor() throws InterruptedException {
        return this.tornadoVMProcess.waitFor();
    }
}
