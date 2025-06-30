package org.eu.smileyik.luaInMinecraftBukkitII.api.lua.luaState;

import org.jetbrains.annotations.NotNull;

import java.io.*;

public interface LuaIOHelper {

    /**
     * 将输入流传输至输出流. 传输完成后会关闭流.
     * @param inputStream  输入流
     * @param outputStream 输出流
     * @param bufferSize   缓冲区大小
     * @throws IOException 错误时抛出
     */
    public static void transferAndClose(@NotNull InputStream inputStream,
                                        @NotNull OutputStream outputStream,
                                        int bufferSize) throws IOException {
        try {
            transfer(inputStream, outputStream, bufferSize);
        } finally {
            try {
                inputStream.close();
            } catch (IOException ignored) {
            }
            try {
                outputStream.close();
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * 将输入流传输至输出流. 传输完成后不会关闭流, 需要手动关闭.
     * @param inputStream  输入流
     * @param outputStream 输出流
     * @param bufferSize   缓冲区大小
     * @throws IOException 错误时抛出
     */
    public static void transfer(@NotNull InputStream inputStream,
                                @NotNull OutputStream outputStream,
                                int bufferSize) throws IOException {
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

    /**
     * 从输入流中读取所有字节. 不会关闭输入流
     * @param inputStream 输入流
     * @param bufferSize  缓冲大小
     * @return 读入的所有字节
     * @throws IOException 错误时抛出
     */
    public static byte[] readBytes(@NotNull InputStream inputStream, int bufferSize) throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            transfer(inputStream, output, bufferSize);
            return output.toByteArray();
        }
    }

    /**
     * 写入所有字节到输出流中. 不会关闭流.
     * @param outputStream 输出流
     * @param bytes        要写出的字节
     * @throws IOException 错误时抛出
     */
    public static void writeBytes(@NotNull OutputStream outputStream, byte[] bytes) throws IOException {
        outputStream.write(bytes);
        outputStream.flush();
    }
}
