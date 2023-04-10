package uk.ac.manchester.elegant.acceleration.service.tools;

import uk.ac.manchester.elegant.acceleration.service.controller.EnvironmentVariables;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
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
        environmentTornadoVM.put(EnvironmentVariables.TORNADO_SDK, System.getenv("TORNADOVM_ROOT")+"/bin/sdk");
        environmentTornadoVM.put(EnvironmentVariables.UPLOADED_DIR, System.getenv("SERVICE_HOME")+"/examples/uploaded");
        environmentTornadoVM.put(EnvironmentVariables.GENERATED_KERNELS_DIR, System.getenv("SERVICE_HOME")+"/examples/generated");
        environmentTornadoVM.put(EnvironmentVariables.BOILERPLATE_DIR, System.getenv("SERVICE_HOME")+"/examples/boilerplate");
    }

    @Override
    public int getExitCode() {
        return 0;
    }

    private String[] getCommandForCompileToBytecode(String methodFileName, long id) {
        ArrayList<String> args = new ArrayList<>();
        args.add(environmentTornadoVM.get(EnvironmentVariables.JAVA_HOME) + "/bin/javac");
        args.add("-cp");
        args.add(environmentTornadoVM.get(EnvironmentVariables.TORNADOVM_ROOT) + "/dist/tornado-sdk/tornado-sdk-0.16-dev-eb8f7ad/share/java/tornado/tornado-api-0.16-dev.jar");
        args.add("-g:vars");
        args.add(environmentTornadoVM.get(EnvironmentVariables.BOILERPLATE_DIR) + "/" + id + "/" + ClassGenerator.getVirtualClassFileName(methodFileName));
        return args.toArray(new String[args.size()]);
    }

    private String[] getCommandForVirtualCompilation(long id, String methodFileName, String deviceDescriptionJsonFileName, String parameterSizeJsonFileName, String generatedKernelFileName) {
        ArrayList<String> args = new ArrayList<>();
        String classpath = environmentTornadoVM.get(EnvironmentVariables.BOILERPLATE_DIR) + "/" + id;
        String classFile = environmentTornadoVM.get(EnvironmentVariables.BOILERPLATE_DIR) + "/" + id + File.separator + ClassGenerator.getVirtualClassName(methodFileName) + ".class";
        args.add(environmentTornadoVM.get(EnvironmentVariables.SERVICE_HOME) + "/bin/runCompilation.sh");
        args.add(environmentTornadoVM.get(EnvironmentVariables.TORNADO_SDK) + "/bin/tornado");
        args.add(classpath);
        args.add(classFile);
        args.add(deviceDescriptionJsonFileName);
        args.add(parameterSizeJsonFileName);
        args.add(generatedKernelFileName);
        args.add(ClassGenerator.getMethodNameFromFileName(methodFileName));
        return args.toArray(new String[args.size()]);
    }

    public void compileToBytecode(long id, String methodFileName) throws IOException, InterruptedException {
        String classBody = ClassGenerator.generateBoilerplateCode(methodFileName);
        File idDirectory = new File(environmentTornadoVM.get(EnvironmentVariables.BOILERPLATE_DIR) + "/" + id);
        if (!idDirectory.exists()) {
            idDirectory.mkdirs();
        }
        ClassGenerator.writeClassToFile(classBody, idDirectory.getAbsolutePath() + File.separator + ClassGenerator.getVirtualClassFileName(methodFileName));

        tornadoVMProcessBuilder.command(getCommandForCompileToBytecode(methodFileName, id));
        this.tornadoVMProcess = tornadoVMProcessBuilder.start();
        int exitCode = tornadoVMProcessWaitFor();

        printOutputOfProcess(tornadoVMProcess);
    }

    public void compileBytecodeToOpenCL(long id, String methodFileName, String deviceDescriptionJsonFileName, String parameterSizeJsonFileName, String generatedKernelFileName)
            throws IOException, InterruptedException {

        tornadoVMProcessBuilder.command(getCommandForVirtualCompilation(id, methodFileName, deviceDescriptionJsonFileName, parameterSizeJsonFileName, generatedKernelFileName));
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
