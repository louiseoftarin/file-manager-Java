import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;


public class FileServiceTest {

    private final FileService fileService = new FileService();

    @TempDir
    Path tempDir;

    @Test
    public void testFormatBytes() {
        assertEquals("1.00 KB", FileService.formatBytes(1024));
        assertEquals("0 B", FileService.formatBytes(0));
        assertEquals("2.50 MB", FileService.formatBytes(2621440));
    }

    @Test
    public void testDirectoryExists() {
        assertTrue(fileService.directoryExists(tempDir));
        assertFalse(fileService.directoryExists(tempDir.resolve("nonexistent")));
    }

    @Test
    public void testFileExists() throws Exception {
        Path testFile = tempDir.resolve("test.txt");
        Files.createFile(testFile);
        assertTrue(fileService.fileExists(testFile));
        assertFalse(fileService.fileExists(tempDir.resolve("missing.txt")));
    }
}