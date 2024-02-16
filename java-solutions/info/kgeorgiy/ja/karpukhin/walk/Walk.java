package info.kgeorgiy.ja.karpukhin.walk;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class Walk {
    private static final Charset charset = StandardCharsets.UTF_8;

    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Usage: Walk <input file> <output file>");
            return;
        }

        String inputFile = args[0];
        String outputFile = args[1];

        walk(inputFile, outputFile);
    }

    public static void walk(String inputFile, String outputFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile, charset))) {
            Path pathOut = Path.of(outputFile);
            if (!Files.exists(pathOut) && pathOut.getParent() != null) {
                Files.createDirectories(pathOut.getParent());
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, charset))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    try (InputStream inputStream = new FileInputStream(Path.of(line).toFile())) {
                        byte[] buffer = new byte[512];
                        int hash = 0;
                        int read;
                        while ((read = (inputStream.read(buffer))) != -1) {
                            hash = jenkins(buffer, hash, read);
                        }
                        hash += hash << 3;
                        hash ^= hash >>> 11;
                        hash += hash << 15;
                        writer.write(String.format("%08x ", hash));
                    } catch (IOException | InvalidPathException e) {
                        writer.write("00000000 ");
                    }
                    writer.write(line + System.lineSeparator());
                }
            } catch (IOException e) {
                System.err.println("Error writing to output file: " + e.getMessage());
            } catch (InvalidPathException e) {
                System.err.println("Invalid path: " + e.getMessage());
            } catch (SecurityException e) {
                System.err.println("Security error: " + e.getMessage());
            }
        } catch (IOException e) {
            System.err.println("Error reading input file: " + e.getMessage());
        } catch (InvalidPathException e) {
            System.err.println("Invalid path: " + e.getMessage());
        } catch (SecurityException e) {
            System.err.println("Security error: " + e.getMessage());
        }
    }

    public static int jenkins(byte[] bytes, int currentHash, int length) {
        int hash = currentHash;
        if (bytes != null) {
            for (int i = 0; i < length; i++) {
                hash += (bytes[i] & 0xFF);
                hash += (hash << 10);
                hash ^= (hash >>> 6);
            }
        }
        return hash;
    }
}
