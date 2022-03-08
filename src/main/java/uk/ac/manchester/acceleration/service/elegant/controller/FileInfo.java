package uk.ac.manchester.acceleration.service.elegant.controller;

public class FileInfo {
    private String functionName;
    private String directory;
    private String programmingLanguage;

    public FileInfo() {
    }

    public FileInfo(String functionCode, String directory, String programmingLanguage) {
        this.functionName = functionCode;
        this.directory = directory;
        this.programmingLanguage = programmingLanguage;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getProgrammingLanguage() {
        return programmingLanguage;
    }

    public void setProgrammingLanguage(String programmingLanguage) {
        this.programmingLanguage = programmingLanguage;
    }
}
