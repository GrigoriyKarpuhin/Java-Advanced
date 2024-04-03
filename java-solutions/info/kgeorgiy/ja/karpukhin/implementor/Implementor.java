package info.kgeorgiy.ja.karpukhin.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.*;
import java.util.zip.ZipEntry;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

/**
 * Class for implementing interfaces and creating JAR files with the implementation.
 * This class implements {@link info.kgeorgiy.java.advanced.implementor.JarImpler}.
 */
public class Implementor implements JarImpler {

    /**
     * Implements the given interface and writes the implementation to the specified directory.
     *
     * @param token type token to create implementation for.
     * @param root  root directory.
     * @throws ImplerException if an error occurred during the implementation.
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (!token.isInterface() || Modifier.isPrivate(token.getModifiers())) {
            throw new ImplerException("Class " + token.getCanonicalName() + " is not an interface or is private");
        }

        File file = root.resolve(token.getPackageName().replace('.', File.separatorChar))
                .resolve(token.getSimpleName() + "Impl.java").toFile();

        if (!file.getParentFile().exists()) {
            try {
                Files.createDirectories(file.getParentFile().toPath());
            } catch (IOException e) {
                throw new ImplerException("Error creating directories", e);
            }
        }

        StringBuilder classBuilder = new StringBuilder();
        if (!token.getPackageName().isEmpty()) {
            classBuilder.append("package ").append(token.getPackageName()).append(";").append(System.lineSeparator());
        }

        classBuilder.append("public class ").append(token.getSimpleName()).append("Impl implements ")
                .append(token.getCanonicalName()).append(" {").append(System.lineSeparator());

        for (Method method : token.getMethods()) {
            classBuilder.append("    ").append(Modifier.toString(method.getModifiers() & ~Modifier.ABSTRACT & ~Modifier.TRANSIENT))
                    .append(" ").append(method.getReturnType().getCanonicalName())
                    .append(" ").append(method.getName()).append(getMethodParameters(method)).append(" {").append(System.lineSeparator());

            if (!method.getReturnType().equals(Void.TYPE)) {
                classBuilder.append("        return ").append(getDefaultValue(method.getReturnType())).append(";").append(System.lineSeparator());
            }

            classBuilder.append("    }").append(System.lineSeparator());
        }

        classBuilder.append("}");

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(escapeNonAscii(classBuilder.toString()));
        } catch (IOException e) {
            throw new ImplerException("Error writing to file", e);
        }
    }

    /**
     * Escapes non-ASCII characters in the given string.
     *
     * @param str the string to escape.
     * @return the escaped string.
     */
    private String escapeNonAscii(String str) {
        StringBuilder builder = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (c < 128) {
                builder.append(c);
            } else {
                builder.append(String.format("\\u%04x", (int) c));
            }
        }
        return builder.toString();
    }

    /**
     * Returns the default value for the given type.
     *
     * @param type the type to get the default value for.
     * @return the default value for the given type.
     */
    private String getDefaultValue(Class<?> type) {
        if (type.equals(boolean.class)) {
            return "false";
        } else if (type.isPrimitive()) {
            return "0";
        } else {
            return "null";
        }
    }

    /**
     * Returns a string representation of the method parameters.
     *
     * @param method the method to get the parameters from.
     * @return a string representation of the method parameters.
     */
    private String getMethodParameters(Method method) {
        StringBuilder builder = new StringBuilder("(");
        boolean firstFlag = true;
        for (Class<?> parameterType : method.getParameterTypes()) {
            if (!firstFlag) {
                builder.append(", ");
            }
            builder.append(parameterType.getCanonicalName()).append(" arg");
            if (!firstFlag)
                builder.append(builder.toString().split(", ").length);
            firstFlag = false;
        }
        builder.append(")");
        return builder.toString();
    }

    /**
     * Implements the given interface and creates a JAR file with the implementation.
     *
     * @param token   type token to create implementation for.
     * @param jarFile target JAR file.
     * @throws ImplerException if an error occurred during the implementation.
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        Path root = jarFile.toAbsolutePath().getParent();
        Path tmp = root.resolve("tmp");
        implement(token, tmp);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new ImplerException("Java compiler not found");
        }
        String classpath;
        try {
            classpath = tmp + File.pathSeparator
                    + Path.of(token.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        int exitCode = compiler.run(null, null, null, "-cp", classpath, "-encoding", StandardCharsets.UTF_8.toString(),
                tmp.toString() + File.separatorChar +
                        token.getPackageName().replace('.', File.separatorChar) +
                        File.separatorChar + token.getSimpleName() + "Impl.java");
        if (exitCode != 0) {
            throw new ImplerException("Error compiling generated class");
        }
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, token.getCanonicalName() + "Impl");
        try (JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(jarFile), manifest)) {
            jarOutputStream.putNextEntry(new ZipEntry(token.getPackageName().replace('.', File.separatorChar) + File.separatorChar + token.getSimpleName() + "Impl.class"));
            Files.copy(tmp.resolve(token.getPackageName().replace('.', File.separatorChar)).resolve(token.getSimpleName() + "Impl.class"), jarOutputStream);
        } catch (IOException e) {
            throw new ImplerException("Error writing to jar file", e);
        }
    }

    /**
     * The main method for running the Implementor.
     *
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        if (args == null || args.length < 2 || args[0] == null || args[1] == null) {
            System.err.println("Usage: java Implementor [-jar] <class> <output>");
            return;
        }

        try {
            Implementor implementor = new Implementor();
            if ("-jar".equals(args[0])) {
                implementor.implementJar(Class.forName(args[1]), Path.of(args[2]));
            } else {
                implementor.implement(Class.forName(args[0]), Path.of(args[1]));
            }
        } catch (ImplerException e) {
            System.err.println("Error implementing class: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found: " + e.getMessage());
        }
    }
}