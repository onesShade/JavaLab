package javalab.service;

import javalab.exception.BadRequestException;
import javalab.exception.InternalException;
import javalab.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogServiceTest {
    @Spy
    @InjectMocks
    private LogService logService;
    @Mock
    File mockFile;
    @Mock
    private BufferedReader mockReader;

    @Test
    void getLogsByDate_WhenLogsExist_ReturnsLogs() {
        // Arrange
        String date = "2025-04-07";
        List<String> mockLogs = List.of(
                "2025-04-07 10:00:00 [INFO] Test log 1",
                "2025-04-07 10:01:00 [INFO] Test log 2"
        );
        doReturn(mockLogs).when(logService).filterLogsByDate(any(File.class), eq(date));
        when(mockFile.exists()).thenReturn(true);

        String result = logService.getLogsByDate(date);

        assertEquals(String.join("\n", mockLogs), result);
    }

    @Test
    void getLogsByDate_WhenNoLogsFound_ThrowsNotFoundException() {
        String date = "2025-04-08";
        when(mockFile.exists()).thenReturn(true);
        doReturn(List.of()).when(logService).filterLogsByDate(any(File.class), eq(date));

        assertThrows(NotFoundException.class, () -> logService.getLogsByDate(date));
    }

    @Test
    void getLogsByDate_WhenLogFileMissing_ThrowsNotFoundException() {
        String date = "2025-04-07";
        when(mockFile.exists()).thenReturn(false);

        LogService service = new LogService(mockFile);

        assertThrows(NotFoundException.class, () -> service.getLogsByDate(date));
    }

    @Test
    void filterLogsByDate_InvalidDateFormat_ThrowsBadRequestException() {
        String invalidDate = "2025/04/07";

        assertThrows(BadRequestException.class, () ->
                logService.filterLogsByDate(mockFile, invalidDate));
    }

    @Test
    void filterLogsByDate_ValidDate_ReturnsMatchingLogs() throws Exception {
        // Arrange
        String date = "2025-04-07";

        when(mockReader.readLine())
                .thenReturn("2025-04-07 10:00:00 [INFO] Test log 1")
                .thenReturn("2025-04-08 11:00:00 [INFO] Wrong date")
                .thenReturn("2025-04-07 10:01:00 [INFO] Test log 2")
                .thenReturn(null);

        try (MockedConstruction<FileReader> ignored = mockConstruction(FileReader.class);
             MockedConstruction<BufferedReader> mockedBufferedReader = mockConstruction(
                     BufferedReader.class,
                     (mock, context) -> when(mock.readLine()).thenAnswer(inv -> mockReader.readLine())
             )) {

            List<String> result = logService.filterLogsByDate(mockFile, date);

            assertEquals(2, result.size());
            assertTrue(result.get(0).contains("Test log 1"));
            assertTrue(result.get(1).contains("Test log 2"));
        }
    }

    @Test
    void filterLogsByDate_IOException_ThrowsInternalException() {
        String date = "2025-04-07";

        try (MockedConstruction<FileReader> ignored = mockConstruction(FileReader.class);
             MockedConstruction<BufferedReader> mockedBufferedReader = mockConstruction(
                     BufferedReader.class,
                     (mock, context) -> when(mock.readLine()).thenThrow(new IOException("Test error"))
             )) {

            InternalException exception = assertThrows(InternalException.class,
                    () -> logService.filterLogsByDate(mockFile, date));
            assertTrue(exception.getMessage().contains("Test error"));
        }
    }

    @Test
    void constructorWithoutArgs_shouldNotBeNull() {
        LogService logServiceTest = new LogService();
        assertNotNull(logServiceTest);
    }
}