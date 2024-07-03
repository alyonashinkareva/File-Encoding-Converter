package info.kgeorgiy.ja.shinkareva;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FileEncodingConverter {
    private static final int BUFFER_SIZE = 1024;
    private static final Set<String> encodingVariants = Set.of("KOI8-R", "CP1251", "CP866", "UTF8", "UTF16");
    private static final Map<String, Charset> encodingsMap = Map.of(
            "UTF8",
            StandardCharsets.UTF_8,
            "UTF16",
            StandardCharsets.UTF_16,
            "KOI8-R",
            Charset.forName("KOI8-R"),
            "CP1251",
            Charset.forName("windows-1251"),
            "CP866",
            Charset.forName("IBM866")
    );

    public static void main(String[] args) {
        ResourceBundle messages = ResourceBundle.getBundle("info/kgeorgiy/ja/shinkareva/MessagesResourceBundle", Locale.of(args[0]));
        String inputEncoding = enteringEncoding("input_encoding_prompt", "invalid_encoding", messages);
        if (isCorrectEncoding(inputEncoding)) {
            String outputEncoding = enteringEncoding("output_encoding_prompt", "invalid_encoding", messages);
            if (isCorrectEncoding(outputEncoding)) {
                String inputFile = enteringFile("input_file_prompt", "file_not_found", true, messages);
                if (Files.exists(Paths.get(inputFile))) {
                    String outputFile = enteringFile("output_file_prompt", "file_exists_overwrite", false, messages);
                    convert(inputEncoding, outputEncoding, inputFile, outputFile, messages);
                }
            }
        }

    }

    private static String enteringFile(String firstMessage, String errorMessage, boolean mustExist, ResourceBundle messages) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String result;
        while (true) {
            try {
                System.out.println(messages.getString(firstMessage));
                result = reader.readLine();
                if (doesFileExist(mustExist, result)) {
                    break;
                } else {
                    System.out.println(messages.getString(errorMessage));
                    String response = reader.readLine();
                    if (!(response.toLowerCase(Locale.ROOT).equals(messages.getString("yes")))) {
                        break;
                    }
                }
            } catch (IOException e) {
                System.out.println("IO exception: " + e.getMessage());
            }
        }
        return result;
    }

    private static String enteringEncoding(String firstMessage, String errorMessage, ResourceBundle messages) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String result;
        while (true) {
            try {
                System.out.println(messages.getString(firstMessage));
                result = reader.readLine();
                if (isCorrectEncoding(result)) {
                    break;
                } else {
                    System.out.println(messages.getString(errorMessage));
                    String response = reader.readLine();
                    if (!(response.toLowerCase(Locale.ROOT).equals(messages.getString("yes")))) {
                        break;
                    }
                }
            } catch (IOException e) {
                System.out.println("IO exception: " + e.getMessage());
            }
        }
        return result;
    }

    private static void convert(String inputEncoding, String outputEncoding, String inputFile, String outputFile, ResourceBundle messages) {
        char[] buffer = new char[BUFFER_SIZE];
        try (final BufferedReader reader = Files.newBufferedReader(Path.of(inputFile), encodingsMap.get(inputEncoding))) {
            try (BufferedWriter writer = Files.newBufferedWriter(Path.of(outputFile), encodingsMap.get(outputEncoding))) {
                while (true) {
                    int sz = reader.read(buffer);
                    if (sz == -1) {
                        break;
                    }
                    writer.write(buffer, 0, sz);
                    System.out.println(messages.getString("success"));
                }
            } catch (IOException e) {
                System.out.println("IO exception: " + e.getMessage());
            }
        } catch (IOException e) {
            System.out.println("IO exception: " + e.getMessage());
        }
    }

    private static boolean isCorrectEncoding(String encoding) {
        return encodingVariants.contains(encoding.toUpperCase());
    }

    private static boolean doesFileExist(boolean mustExist, String result) {
        return mustExist == Files.exists(Paths.get(result));
    }
}