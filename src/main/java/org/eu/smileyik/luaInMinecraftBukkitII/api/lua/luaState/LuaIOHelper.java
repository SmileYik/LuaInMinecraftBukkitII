package org.eu.smileyik.luaInMinecraftBukkitII.api.lua.luaState;

import java.io.*;

public interface LuaIOHelper {
    public static void transfer(InputStream inputStream, OutputStream outputStream, int bufferSize) throws IOException {
        BufferedInputStream input;
        if (inputStream instanceof BufferedInputStream) {
            input = (BufferedInputStream) inputStream;
        } else {
            input = new BufferedInputStream(inputStream);
        }

        BufferedOutputStream output;
        if (outputStream instanceof BufferedOutputStream) {
            output = (BufferedOutputStream) outputStream;
        }  else {
            output = new BufferedOutputStream(outputStream);
        }

        byte[] buffer = new byte[bufferSize];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
        output.flush();
    }

    public static byte[] readBytes(InputStream inputStream, int bufferSize) throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            transfer(inputStream, output, bufferSize);
            return output.toByteArray();
        }
    }

    public static void writeBytes(OutputStream outputStream, byte[] bytes) throws IOException {
        outputStream.write(bytes);
        outputStream.flush();
    }
}
