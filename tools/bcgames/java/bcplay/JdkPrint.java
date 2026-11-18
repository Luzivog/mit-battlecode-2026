package bcplay;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Prints a fingerprint of the JDK library code that bots can run: "jdk<feature>-<hash> <classes>".
 *
 * The engine's instrumenter gives each robot its own instrumented copy of the JDK classes under java/util, java/math
 * and jdk/internal/..util.. (ClassReferenceUtil.shouldAddInstrumentedPrefix), read from the running JDK, and counts
 * their bytecodes; so games depend on those classes' bytes. The hash covers them, except:
 * - the classes bots may not reference (the engine's DisallowedClasses.txt) and their inner classes;
 * - jdk/internal/util/Architecture and PlatformProps: platform descriptors that only Architecture, PlatformProps and
 *   OperatingSystem reference. Ubuntu's OpenJDK lists 7 more CPUs in them than Temurin's.
 * Ubuntu's 21.0.13 and Temurin's 21.0.13+9 differ in exactly those classes (and java/util/Properties, disallowed), so
 * they print the same line; remote games (Temurin) equal local ones (track C: 108 games, replays byte-identical).
 * Run with the engine jar on the classpath.
 */
public final class JdkPrint {
    static final Set<String> PLATFORM = Set.of("jdk/internal/util/Architecture", "jdk/internal/util/PlatformProps");

    public static void main(String[] args) throws Exception {
        Set<String> disallowed;
        try (InputStream in = JdkPrint.class.getResourceAsStream(
                "/battlecode/instrumenter/bytecode/resources/DisallowedClasses.txt")) {
            disallowed = new String(in.readAllBytes(), StandardCharsets.UTF_8).lines().map(String::trim)
                    .filter(s -> !s.isEmpty()).collect(Collectors.toSet());
        }
        Path base = FileSystems.getFileSystem(URI.create("jrt:/")).getPath("/modules/java.base");
        List<String> names;
        try (Stream<Path> s = Files.walk(base)) {
            names = s.map(p -> base.relativize(p).toString()).filter(n -> n.endsWith(".class"))
                    .map(n -> n.substring(0, n.length() - 6))
                    .filter(n -> instrumented(n) && !PLATFORM.contains(n)
                            && !disallowed.contains(n.contains("$") ? n.substring(0, n.indexOf('$')) : n))
                    .sorted().toList();
        }
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        for (String n : names) {
            md.update(n.getBytes(StandardCharsets.UTF_8));
            md.update((byte) 0);
            md.update(Files.readAllBytes(base.resolve(n + ".class")));
        }
        StringBuilder sb = new StringBuilder();
        byte[] d = md.digest();
        for (int i = 0; i < 8; i++) sb.append(String.format("%02x", d[i]));
        System.out.println("jdk" + Runtime.version().feature() + "-" + sb + " " + names.size());
    }

    /** ClassReferenceUtil.shouldAddInstrumentedPrefix, for JDK classes. */
    static boolean instrumented(String n) {
        if (n.startsWith("java/util/invoke") || n.startsWith("java/util/jar") || n.startsWith("java/util/zip")
                || n.equals("java/util/Iterator") || n.equals("java/util/concurrent/TimeUnit")
                || (n.startsWith("jdk/internal") && !n.contains("util"))) return false;
        return n.startsWith("java/util/") || n.startsWith("java/math/") || n.startsWith("jdk/internal/");
    }
}
