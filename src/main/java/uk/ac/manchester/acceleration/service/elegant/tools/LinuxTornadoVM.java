package uk.ac.manchester.acceleration.service.elegant.tools;

import uk.ac.manchester.acceleration.service.elegant.controller.ElegantRequestHandler;
import uk.ac.manchester.acceleration.service.elegant.controller.EnvironmentVariables;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
        // tornadoVMProcessBuilder.directory(new
        // File(environment.get("TORNADOVM_DIR")));
    }

    public static String getEnvironmentVariable(String key) {
        return environment.get(key);
    }

    @Override
    public void initializeEnvironment() {
        environment = tornadoVMProcessBuilder.environment();
        environment.put(EnvironmentVariables.TORNADOVM_DIR, "/home/thanos/repositories/tornadoVM2");
        environment.put(EnvironmentVariables.TORNADO_SDK, "/home/thanos/repositories/tornadoVM2/bin/sdk");
        environment.put(EnvironmentVariables.GENERATED_KERNELS_DIR, "/home/thanos/repositories/Elegant-Acceleration-Service/examples/generated");
        environment.put(EnvironmentVariables.BOILERPLATE_DIR, "/home/thanos/repositories/Elegant-Acceleration-Service/examples/boilerplate/");
    }

    @Override
    public int getExitCode() {
        return 0;
    }

    public void buildTornadoVM() throws IOException {
        StringBuilder buildCommand = new StringBuilder();
        buildCommand.append("source ");
        buildCommand.append(environment.get(EnvironmentVariables.TORNADOVM_DIR));
        buildCommand.append("/source.sh");
        // buildCommand.append(" && make");

        System.out.println("build command: ");
        System.out.println(buildCommand.toString());
        tornadoVMProcessBuilder.command(buildCommand.toString());
        this.tornadoVMProcess = tornadoVMProcessBuilder.start();
    }

    private String getCommandForBoilerPlateCode(String methodFileName) {
        StringBuilder boilerPlateCommand = new StringBuilder();
        boilerPlateCommand.append(environment.get(EnvironmentVariables.TORNADO_SDK));
        boilerPlateCommand.append("/bin/");
        boilerPlateCommand.append("tornado --jvm=\"-Dtornado.method.path=");
        boilerPlateCommand.append(methodFileName);
        // boilerPlateCommand.append(" -Dtornado.service.destination.dir=");
        // boilerPlateCommand.append(environment.get(EnvironmentVariables.BOILERPLATE_DIR));
        boilerPlateCommand.append("\" ");
        boilerPlateCommand.append("-m tornado.runtime/uk.ac.manchester.tornado.runtime.acceleration.service.Boilerplate");

        return boilerPlateCommand.toString();
    }

    private static String getMethodNameFromSignature(String signatureName) {
        String[] strings = signatureName.split("\\(");
        String[] subStrings = strings[0].split(" ");
        return subStrings[subStrings.length - 1];
    }

    private static String extractSignature(String line) {
        return line.replaceFirst(" \\{|\\{", ";");
    }

    private static String getSignatureOfMethodFile(String fileName) {
        FileReader fileReader;
        BufferedReader bufferedReader;
        String signatureOfMethod;
        try {
            fileReader = new FileReader(fileName);
            bufferedReader = new BufferedReader(fileReader);
            String line;
            line = bufferedReader.readLine();
            signatureOfMethod = extractSignature(line);
            return signatureOfMethod;
        } catch (IOException e) {
            System.err.println("Input fileName [" + fileName + "] failed to run." + e.getMessage());
            return null;
        }
    }

    private String getVirtualClassName(String methodFileName) {
        String methodName = getMethodNameFromSignature(getSignatureOfMethodFile(methodFileName));
        String className = "Test" + methodName.substring(0, 1).toUpperCase() + methodName.substring(1);
        return className;
    }

    private String getCommandForVirtualCompilation(String deviceDescriptionJsonFileName, String generatedKernelFileName, String virtualClassName) {
        StringBuilder boilerPlateCommand = new StringBuilder();
        boilerPlateCommand.append(environment.get(EnvironmentVariables.TORNADO_SDK));
        boilerPlateCommand.append("/bin/");
        boilerPlateCommand.append("tornado --jvm=\"-Dtornado.device.desc=");
        boilerPlateCommand.append("/home/thanos/repositories/Elegant-Acceleration-Service/examples/uploaded/virtual-device-GPU.json");
        // boilerPlateCommand.append(deviceDescriptionJsonFileName);// FIXME:
        // Parameterize Class name
        boilerPlateCommand.append(" -Dtornado.virtual.device=True -Dtornado.cim.mode=True -Dtornado.print.kernel=True -Dtornado.print.kernel.dir=");
        boilerPlateCommand.append(generatedKernelFileName);
        boilerPlateCommand.append("\" ");
        boilerPlateCommand.append("-m tornado.examples/uk.ac.manchester.tornado.examples.virtual.");
        boilerPlateCommand.append(virtualClassName);
        return boilerPlateCommand.toString();
    }

    public void compile(String methodFileName, String deviceDescriptionJsonFileName, String generatedKernelFileName) throws IOException, InterruptedException {
        // Process p =
        // Runtime.getRuntime().exec(getCommandForBoilerPlateCode(methodFileName));
        // p.waitFor();
        System.out.println("command: ");
        // 1. Boilerplate code generation
        System.out.println(getCommandForBoilerPlateCode(methodFileName));
        // tornadoVMProcessBuilder.command(getCommandForBoilerPlateCode(methodFileName));
        // this.tornadoVMProcess = tornadoVMProcessBuilder.start();
        // 2. Compile with Tornado the boilerplate class to be in classpath
        // 3. Compile for Virtual Device
        System.out.println(getCommandForVirtualCompilation(deviceDescriptionJsonFileName, generatedKernelFileName, getVirtualClassName(methodFileName)));

    }

    public int waitFor() throws InterruptedException {
        return this.tornadoVMProcess.waitFor();
    }
}
