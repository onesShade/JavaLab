package javalab.service;

import javalab.exception.BadRequestException;
import javalab.exception.InternalException;
import javalab.exception.NotFoundException;
import javalab.mapper.CommentMapper;
import javalab.repository.AuthorRepository;
import javalab.repository.BookRepository;
import javalab.repository.CommentRepository;
import javalab.repository.UserRepository;
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
    @Mock
    private AuthorRepository authorRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private CommentMapper commentMapper;
    @Mock
    private BookService bookService;
    @Mock
    private UserService userService;
    @Mock
    private CommentService commentService;
    @Mock
    private AuthorService authorService;
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

        // Stub filterLogsByDate() on the spy
        doReturn(mockLogs).when(logService).filterLogsByDate(any(File.class), eq(date));
        when(mockFile.exists()).thenReturn(true);
        // Act
        String result = logService.getLogsByDate(date);

        // Assert
        assertEquals(String.join("\n", mockLogs), result);
    }

    @Test
    void getLogsByDate_WhenNoLogsFound_ThrowsNotFoundException() {
        // Arrange
        String date = "2025-04-08";
        when(mockFile.exists()).thenReturn(true);
        doReturn(List.of()).when(logService).filterLogsByDate(any(File.class), eq(date));

        // Act & Assert
        assertThrows(NotFoundException.class, () -> logService.getLogsByDate(date));
    }

    @Test
    void getLogsByDate_WhenLogFileMissing_ThrowsNotFoundException() {
        // Arrange
        String date = "2025-04-07";
        when(mockFile.exists()).thenReturn(false);

        // Create service with mock file
        LogService service = new LogService(mockFile);

        // Act & Assert
        assertThrows(NotFoundException.class, () -> service.getLogsByDate(date));
    }

    @Test
    void filterLogsByDate_InvalidDateFormat_ThrowsBadRequestException() {
        // Arrange
        String invalidDate = "2025/04/07";

        // Act & Assert
        assertThrows(BadRequestException.class, () ->
                logService.filterLogsByDate(mockFile, invalidDate));
    }

    @Test
    void filterLogsByDate_ValidDate_ReturnsMatchingLogs() throws Exception {
        // Arrange
        String date = "2025-04-07";


        // Mock reader behavior
        when(mockReader.readLine())
                .thenReturn("2025-04-07 10:00:00 [INFO] Test log 1")
                .thenReturn("2025-04-08 11:00:00 [INFO] Wrong date")
                .thenReturn("2025-04-07 10:01:00 [INFO] Test log 2")
                .thenReturn(null); // Signals end of file

        // Inject our mock reader
        try (MockedConstruction<FileReader> ignored = mockConstruction(FileReader.class);
             MockedConstruction<BufferedReader> mockedBufferedReader = mockConstruction(
                     BufferedReader.class,
                     (mock, context) -> when(mock.readLine()).thenAnswer(inv -> mockReader.readLine())
             )) {

            // Act
            List<String> result = logService.filterLogsByDate(mockFile, date);

            // Assert
            assertEquals(2, result.size());
            assertTrue(result.get(0).contains("Test log 1"));
            assertTrue(result.get(1).contains("Test log 2"));
        }
    }

    @Test
    void filterLogsByDate_IOException_ThrowsInternalException() {
        // Arrange
        String date = "2025-04-07";

        try (MockedConstruction<FileReader> ignored = mockConstruction(FileReader.class);
             MockedConstruction<BufferedReader> mockedBufferedReader = mockConstruction(
                     BufferedReader.class,
                     (mock, context) -> when(mock.readLine()).thenThrow(new IOException("Test error"))
             )) {

            // Act & Assert
            InternalException exception = assertThrows(InternalException.class,
                    () -> logService.filterLogsByDate(mockFile, date));
            assertTrue(exception.getMessage().contains("Test error"));
        }
    }


}