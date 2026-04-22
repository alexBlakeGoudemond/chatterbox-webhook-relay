package com.webhook.relay.architecture_rules.structure;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Stream;

public class ClasspathResourcesRules {

    private static final String APPLICATION_PATTERN = "classpath*:**/application*";

    private ClasspathResourcesRules() {
        // utility class
    }

    public static List<String> compiledCodeShouldNotContainPropertiesFiles(final List<String> allowedFilePaths) {
        Enumeration<URL> resources = getClassLoaderResources();
        return Collections.list(resources)
                .stream()
                .flatMap(url -> getInvalidPathNames(url, allowedFilePaths).stream())
                .toList();
    }

    /**
     * Scans the entire compiled classpath (including dependency JARs) and
     * returns any forbidden application-* resources (e.g. application-prod.yml).
     */
    public static List<String> compiledCodeShouldNotContainProdProfiles(List<String> allowedFilePaths) {
        List<String> violations = new ArrayList<>();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = resolver.getResources(APPLICATION_PATTERN);
            for (Resource resource : resources) {
                String location = resource.getDescription().toLowerCase();
                if (isApplicationFile(location)
                        && isProdProfile(location)
                        && notAllowed(location, allowedFilePaths)) {
                    violations.add(resource.getDescription());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to scan classpath for application configuration files", e);
        }
        return violations;
    }

    private static List<String> getInvalidPathNames(URL root, final List<String> allowedFilePaths) {
        if (!rootUrlIsAFile(root)) {
            return List.of();
        }
        if (!rootUrlIsADirectory(root)) {
            return List.of();
        }
        Path base = getAsPath(root);
        return getInvalidPathNames(base, allowedFilePaths);
    }

    private static List<String> getInvalidPathNames(Path base, List<String> allowedFilePaths) {
        try (Stream<Path> paths = Files.walk(base)) {
            return paths
                    .filter(path -> notOneOfTheAllowedFilePaths(path, allowedFilePaths))
                    .filter(ClasspathResourcesRules::pathContainsApplicationPropertiesFile)
                    .map(Path::toString)
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("Unexpected issue occurred when walking through the path", e);
        }
    }

    private static Enumeration<URL> getClassLoaderResources() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            return classLoader.getResources("");
        } catch (IOException e) {
            throw new RuntimeException("Unexpected issue occurred when trying to get Classloader resources", e);
        }
    }

    /**
     * Root URL may have a structure like this
     * <pre>
     * {@code
     *  file:/C:/myworkbench/company_github_workspace/<servicePackages>/target/test-classes/
     *
     *  file:/C:/myworkbench/company_github_workspace/<servicePackages>/target/classes/
     *
     *  jar:file:/C:/myworkbench/data/maven3_repo/ch/qos/logback/logback-core/1.5.18/logback-core-1.5.18.jar!/META-INF/versions/21/
     *
     *  ...
     * }
     * </pre>
     *
     * This method checks: is the URL Protocol a file?
     */
    private static boolean rootUrlIsAFile(URL root) {
        return root.getProtocol().equals("file");
    }

    private static boolean rootUrlIsADirectory(URL root) {
        Path base = getAsPath(root);
        return Files.isDirectory(base);
    }

    private static Path getAsPath(URL root) {
        try {
            return Paths.get(root.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Unexpected issue when trying to get URL as a Path", e);
        }
    }

    private static boolean pathContainsApplicationPropertiesFile(Path path) {
        String pathName = path.getFileName().toString().toLowerCase();
        return !pathName.endsWith(".class")
                && (pathName.contains("application-") || pathName.contains("application."));
    }

    private static boolean notOneOfTheAllowedFilePaths(Path path, List<String> allowedFilePaths) {
        for (String allowedFilePath : allowedFilePaths) {
            allowedFilePath = allowedFilePath.replace("/", "\\");
            if (path.toString().toLowerCase().contains(allowedFilePath.toLowerCase())) {
                return false;
            }
        }
        return true;
    }

    private static boolean isApplicationFile(String path) {
        return !path.endsWith(".class")
                && (path.contains("application-") || path.contains("application."));
    }

    private static boolean isProdProfile(String path) {
        return path.contains("application-prod") || path.contains("application.prod");
    }

    private static boolean notAllowed(String path, List<String> allowedFilePaths) {
        String normalizedPath = normalize(path);
        for (String allowed : allowedFilePaths) {
            if (normalizedPath.contains(normalize(allowed))) {
                return false;
            }
        }
        return true;
    }

    private static String normalize(String path) {
        return path.replace("\\", "/").toLowerCase();
    }

}
