package uk.ac.manchester.elegant.acceleration.service.tools;

import java.io.IOException;

public interface TornadoVMInterface {
    void initializeEnvironment() throws IOException, InterruptedException;

    int getExitCode();
}
