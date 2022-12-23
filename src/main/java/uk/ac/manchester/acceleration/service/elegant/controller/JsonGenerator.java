package uk.ac.manchester.acceleration.service.elegant.controller;

import java.util.Arrays;

public class JsonGenerator {

    public static StringBuilder stringBuilder;

    private static void emitOpeningBrace(StringBuilder sb) {
        sb.append("{");
        sb.append("\n");
    }

    private static void emitClosingBrace(StringBuilder sb) {
        sb.append("}");
    }

    private static void emitKeyValuePair(StringBuilder sb, String key, String value, boolean isString, boolean isLastField) {
        sb.append("  \"");
        sb.append(key);
        sb.append("\"");
        sb.append(": ");
        if (isString) {
            sb.append("\"");
        }
        sb.append(value);
        if (isString) {
            sb.append("\"");
        }
        if (!isLastField) {
            sb.append(",");
        }
        sb.append("\n");
    }

    public static String createJsonContents(Object object) {
        stringBuilder = new StringBuilder();

        if (object instanceof DeviceInfo) {
            DeviceInfo deviceInfo = (DeviceInfo) object;
            emitOpeningBrace(stringBuilder);
            emitKeyValuePair(stringBuilder, "deviceName", deviceInfo.getDeviceName(), true, false);
            emitKeyValuePair(stringBuilder, "doubleFPSupport", String.valueOf(deviceInfo.getDoubleFPSupport()), false, false);
            emitKeyValuePair(stringBuilder, "maxWorkItemSizes", Arrays.toString(deviceInfo.getMaxWorkItemSizes()), false, false);
            emitKeyValuePair(stringBuilder, "deviceAddressBits", String.valueOf(deviceInfo.getDeviceAddressBits()), false, false);
            emitKeyValuePair(stringBuilder, "deviceType", deviceInfo.getDeviceType(), true, false);
            emitKeyValuePair(stringBuilder, "deviceExtensions", deviceInfo.getDeviceExtensions(), true, false);
            emitKeyValuePair(stringBuilder, "availableProcessors", String.valueOf(deviceInfo.getAvailableProcessors()), false, true);
            emitClosingBrace(stringBuilder);
        } else if (object instanceof FileInfo) {
            FileInfo fileInfo = (FileInfo) object;
            emitOpeningBrace(stringBuilder);
            emitKeyValuePair(stringBuilder, "functionName", fileInfo.getFunctionName(), true, false);
            emitKeyValuePair(stringBuilder, "programmingLanguage", fileInfo.getProgrammingLanguage(), true, true);
            emitClosingBrace(stringBuilder);
        } else if (object instanceof ParameterInfo) {
            ParameterInfo parameterInfo = (ParameterInfo) object;
            emitOpeningBrace(stringBuilder);
            for (int i = 0; i < parameterInfo.getKeys().length; i++) {
                String parameterString = parameterInfo.getKeys()[i];
                String sizeString = String.valueOf(parameterInfo.getValues()[i]);
                emitKeyValuePair(stringBuilder, parameterString, sizeString, false, (i == parameterInfo.getKeys().length - 1) ? true : false);
            }
            emitClosingBrace(stringBuilder);
        } else {
            throw new RuntimeException("Object is not recongized for the creation of JSON file in the ELEGANT Acceleration Service.");
        }
        return stringBuilder.toString();
    }
}
