package utils;
import config.Config;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class InputOutput {
    public static String read(String filepath){
        String content = "";
        try {
            content = Files.readString(Path.of(filepath));  // 读取整个文件为字符串
            System.out.println(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    public static void write(String content, String filepath){
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filepath, true))) {
            bw.write(content);
            bw.newLine();  // 写入一个换行符
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void write(String content){
        write(content, Config.outputPath);
    }

    public static void writeError(String error){
        write(error, Config.errorPath);
    }
}
