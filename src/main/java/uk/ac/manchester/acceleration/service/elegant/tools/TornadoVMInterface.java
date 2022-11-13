package uk.ac.manchester.acceleration.service.elegant.tools;

import java.io.IOException;

public interface TornadoVMInterface {
    void initializeEnvironment() throws IOException, InterruptedException;

    int getExitCode();
}
