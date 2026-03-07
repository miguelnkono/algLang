package io.dream.natives;

import io.dream.config.Messages;
import io.dream.error.RuntimeError;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Native File I/O operations for AlgoLang
 * Provides file reading and writing capabilities
 */
public class FileIO
{
    // File handle counter
    private static int nextHandle = 1;

    // Open file handles: handle -> FileDescriptor
    private static final Map<Integer, FileDescriptor> openFiles = new HashMap<>();

    /**
     * File descriptor holding reader/writer
     */
    private static class FileDescriptor
    {
        final String filename;
        final String mode; // "r" (read), "w" (write), "a" (append)
        BufferedReader reader;
        BufferedWriter writer;
        boolean closed;

        FileDescriptor(String filename, String mode)
        {
            this.filename = filename;
            this.mode = mode;
            this.closed = false;
        }
    }

    /**
     * Open a file for reading or writing
     *
     * @param filename Path to the file
     * @param mode "r" for read, "w" for write, "a" for append
     * @return File handle (integer)
     * @throws RuntimeError if file cannot be opened
     */
    public static int open(String filename, String mode) throws RuntimeError
    {
        try
        {
            FileDescriptor fd = new FileDescriptor(filename, mode);

            switch (mode)
            {
                case "r":
                    // Open for reading
                    fd.reader = new BufferedReader(
                            new InputStreamReader(
                                    new FileInputStream(filename),
                                    StandardCharsets.UTF_8
                            )
                    );
                    break;

                case "w":
                    // Open for writing (overwrite)
                    fd.writer = new BufferedWriter(
                            new OutputStreamWriter(
                                    new FileOutputStream(filename, false),
                                    StandardCharsets.UTF_8
                            )
                    );
                    break;

                case "a":
                    // Open for appending
                    fd.writer = new BufferedWriter(
                            new OutputStreamWriter(
                                    new FileOutputStream(filename, true),
                                    StandardCharsets.UTF_8
                            )
                    );
                    break;

                default:
                    throw new RuntimeError(null, Messages.invalidFileMode(mode));
            }

            int handle = nextHandle++;
            openFiles.put(handle, fd);
            return handle;
        }
        catch (FileNotFoundException e)
        {
            throw new RuntimeError(null, Messages.fileNotFound(filename));
        }
        catch (IOException e)
        {
            throw new RuntimeError(null, Messages.fileOpenError(filename, e.getMessage()));
        }
    }

    /**
     * Read a line from a file
     *
     * @param handle File handle
     * @return The line read, or null if EOF
     * @throws RuntimeError if file not open or cannot be read
     */
    public static String readLine(int handle) throws RuntimeError
    {
        FileDescriptor fd = getFileDescriptor(handle);

        if (fd.reader == null)
        {
            throw new RuntimeError(null, Messages.fileNotOpenForReading(fd.filename));
        }

        if (fd.closed)
        {
            throw new RuntimeError(null, Messages.fileAlreadyClosed(fd.filename));
        }

        try
        {
            return fd.reader.readLine();
        }
        catch (IOException e)
        {
            throw new RuntimeError(null, Messages.fileReadError(fd.filename, e.getMessage()));
        }
    }

    /**
     * Read entire file content
     *
     * @param handle File handle
     * @return File content as string
     * @throws RuntimeError if file not open or cannot be read
     */
    public static String readAll(int handle) throws RuntimeError
    {
        FileDescriptor fd = getFileDescriptor(handle);

        if (fd.reader == null)
        {
            throw new RuntimeError(null, Messages.fileNotOpenForReading(fd.filename));
        }

        if (fd.closed)
        {
            throw new RuntimeError(null, Messages.fileAlreadyClosed(fd.filename));
        }

        try
        {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = fd.reader.readLine()) != null)
            {
                content.append(line).append("\n");
            }
            return content.toString();
        }
        catch (IOException e)
        {
            throw new RuntimeError(null, Messages.fileReadError(fd.filename, e.getMessage()));
        }
    }

    /**
     * Read an integer from a file
     *
     * @param handle File handle
     * @return The integer read
     * @throws RuntimeError if file not open, cannot be read, or invalid format
     */
    public static int readInt(int handle) throws RuntimeError
    {
        String line = readLine(handle);

        if (line == null)
        {
            throw new RuntimeError(null, Messages.unexpectedEndOfFile());
        }

        try
        {
            return Integer.parseInt(line.trim());
        }
        catch (NumberFormatException e)
        {
            throw new RuntimeError(null, Messages.invalidIntegerFormat(line));
        }
    }

    /**
     * Read a real number from a file
     *
     * @param handle File handle
     * @return The real number read
     * @throws RuntimeError if file not open, cannot be read, or invalid format
     */
    public static double readReal(int handle) throws RuntimeError
    {
        String line = readLine(handle);

        if (line == null)
        {
            throw new RuntimeError(null, Messages.unexpectedEndOfFile());
        }

        try
        {
            return Double.parseDouble(line.trim());
        }
        catch (NumberFormatException e)
        {
            throw new RuntimeError(null, Messages.invalidRealFormat(line));
        }
    }

    /**
     * Write a string to a file
     *
     * @param handle File handle
     * @param content Content to write
     * @throws RuntimeError if file not open or cannot be written
     */
    public static void write(int handle, String content) throws RuntimeError
    {
        FileDescriptor fd = getFileDescriptor(handle);

        if (fd.writer == null)
        {
            throw new RuntimeError(null, Messages.fileNotOpenForWriting(fd.filename));
        }

        if (fd.closed)
        {
            throw new RuntimeError(null, Messages.fileAlreadyClosed(fd.filename));
        }

        try
        {
            fd.writer.write(content);
        }
        catch (IOException e)
        {
            throw new RuntimeError(null, Messages.fileWriteError(fd.filename, e.getMessage()));
        }
    }

    /**
     * Write a line to a file (with newline)
     *
     * @param handle File handle
     * @param content Content to write
     * @throws RuntimeError if file not open or cannot be written
     */
    public static void writeLine(int handle, String content) throws RuntimeError
    {
        FileDescriptor fd = getFileDescriptor(handle);

        if (fd.writer == null)
        {
            throw new RuntimeError(null, Messages.fileNotOpenForWriting(fd.filename));
        }

        if (fd.closed)
        {
            throw new RuntimeError(null, Messages.fileAlreadyClosed(fd.filename));
        }

        try
        {
            fd.writer.write(content);
            fd.writer.newLine();
        }
        catch (IOException e)
        {
            throw new RuntimeError(null, Messages.fileWriteError(fd.filename, e.getMessage()));
        }
    }

    /**
     * Close a file
     *
     * @param handle File handle
     * @throws RuntimeError if file not open or cannot be closed
     */
    public static void close(int handle) throws RuntimeError
    {
        FileDescriptor fd = getFileDescriptor(handle);

        if (fd.closed)
        {
            throw new RuntimeError(null, Messages.fileAlreadyClosed(fd.filename));
        }

        try
        {
            if (fd.reader != null)
            {
                fd.reader.close();
            }
            if (fd.writer != null)
            {
                fd.writer.flush();
                fd.writer.close();
            }
            fd.closed = true;
            openFiles.remove(handle);
        }
        catch (IOException e)
        {
            throw new RuntimeError(null, Messages.fileCloseError(fd.filename, e.getMessage()));
        }
    }

    /**
     * Check if end of file reached
     *
     * @param handle File handle
     * @return true if EOF, false otherwise
     * @throws RuntimeError if file not open or not readable
     */
    public static boolean eof(int handle) throws RuntimeError
    {
        FileDescriptor fd = getFileDescriptor(handle);

        if (fd.reader == null)
        {
            throw new RuntimeError(null, Messages.fileNotOpenForReading(fd.filename));
        }

        if (fd.closed)
        {
            return true;
        }

        try
        {
            fd.reader.mark(1);
            int c = fd.reader.read();
            if (c == -1)
            {
                return true;
            }
            fd.reader.reset();
            return false;
        }
        catch (IOException e)
        {
            throw new RuntimeError(null, Messages.fileReadError(fd.filename, e.getMessage()));
        }
    }

    /**
     * Check if a file exists
     *
     * @param filename Path to the file
     * @return true if file exists, false otherwise
     */
    public static boolean exists(String filename)
    {
        File file = new File(filename);
        return file.exists() && file.isFile();
    }

    /**
     * Delete a file
     *
     * @param filename Path to the file
     * @return true if deleted successfully, false otherwise
     */
    public static boolean delete(String filename)
    {
        File file = new File(filename);
        return file.delete();
    }

    /**
     * Close all open files (cleanup)
     */
    public static void closeAll()
    {
        for (FileDescriptor fd : openFiles.values())
        {
            try
            {
                if (fd.reader != null) fd.reader.close();
                if (fd.writer != null)
                {
                    fd.writer.flush();
                    fd.writer.close();
                }
            }
            catch (IOException e)
            {
                // Ignore errors during cleanup
            }
        }
        openFiles.clear();
    }

    /**
     * Get file descriptor by handle
     *
     * @param handle File handle
     * @return File descriptor
     * @throws RuntimeError if handle is invalid
     */
    private static FileDescriptor getFileDescriptor(int handle) throws RuntimeError
    {
        FileDescriptor fd = openFiles.get(handle);
        if (fd == null)
        {
            throw new RuntimeError(null, Messages.invalidFileHandle(handle));
        }
        return fd;
    }
}
