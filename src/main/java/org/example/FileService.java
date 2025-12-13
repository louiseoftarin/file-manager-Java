package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;


public class FileService {
    private static final Logger logger = LogManager.getLogger(FileService.class);


    public boolean directoryExists(Path path) {
        return Files.exists(path) && Files.isDirectory(path);
    }


    public boolean fileExists(Path path) {
        return Files.exists(path) && Files.isRegularFile(path);
    }


    public void ensureDirectoryExists(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
            logger.info("Создана директория: {}", dir.toAbsolutePath());
        }
    }


    public long getDirectorySize(Path dir) throws IOException {
        final long[] total = {0};
        Files.walkFileTree(dir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                total[0] += Files.size(file);
                return FileVisitResult.CONTINUE;
            }
        });
        return total[0];
    }


    public void copyFile(Path source, Path target) throws IOException {
        logger.info("Копирование: {} → {}", source, target);
        if (!Files.isReadable(source)) {
            throw new IOException("Файл недоступен для чтения: " + source);
        }
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        logger.info("Файл успешно скопирован");
    }


    public static String formatBytes(long bytes) {
        if (bytes <= 0) return "0 B";
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = (int) (Math.log(bytes) / Math.log(1024));
        if (unitIndex >= units.length) unitIndex = units.length - 1;
        double size = bytes / Math.pow(1024, unitIndex);
        return new DecimalFormat("#.##").format(size) + " " + units[unitIndex];
    }
}