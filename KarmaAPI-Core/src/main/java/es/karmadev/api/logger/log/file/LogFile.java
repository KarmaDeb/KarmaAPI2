package es.karmadev.api.logger.log.file;

import es.karmadev.api.JavaVirtualMachine;
import es.karmadev.api.MemoryUnit;
import es.karmadev.api.core.ExceptionCollector;
import es.karmadev.api.core.source.APISource;
import es.karmadev.api.file.util.PathUtilities;
import es.karmadev.api.file.util.StreamUtils;
import es.karmadev.api.logger.log.console.LogLevel;
import es.karmadev.api.logger.log.file.component.LogQueue;
import es.karmadev.api.logger.log.file.component.QuePair;
import es.karmadev.api.logger.log.file.component.header.HeaderLine;
import es.karmadev.api.logger.log.file.component.header.LogHeader;
import es.karmadev.api.schedule.runner.async.AsyncTaskExecutor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * KarmaAPI log file
 */
public class LogFile {

    private final LogHeader header = new LogHeader();
    private final LogQueue queue = new LogQueue();
    private final APISource source;
    private final AtomicBoolean writing = new AtomicBoolean(false);

    /**
     * Initialize the log file
     *
     * @param source the log file source
     */
    public LogFile(final APISource source) {
        this.source = source;
        header.add(new HeaderLine("# System information"));
        header.add(new HeaderLine(() -> "OS Name: " + JavaVirtualMachine.osName()));
        header.add(new HeaderLine(() -> "OS Version: " + JavaVirtualMachine.osVersion()));
        header.add(new HeaderLine(() -> "OS Model: " + JavaVirtualMachine.osArch()));
        header.add(new HeaderLine(() -> "OS Processors: " + JavaVirtualMachine.cores()));
        header.add(new HeaderLine(() -> "OS Memory: " + JavaVirtualMachine.totalMemory(MemoryUnit.GIGABYTES) + "GB"));
        header.add(new HeaderLine().lineBreak(false));
        header.add(new HeaderLine("# VM information"));
        header.add(new HeaderLine(() -> "VM Name: " + JavaVirtualMachine.jvmName()));
        header.add(new HeaderLine(() -> "VM Version: " + JavaVirtualMachine.jvmVersion()));
        header.add(new HeaderLine(() -> "VM Max Memory: " + JavaVirtualMachine.allocatedMemory(MemoryUnit.GIGABYTES) + "GB"));
        header.add(new HeaderLine(() -> "VM Free Memory: " + JavaVirtualMachine.availableMemory(MemoryUnit.KILOBYTES) + "KB"));
        header.add(new HeaderLine(() -> {
            Instant instant = JavaVirtualMachine.startTime();
            ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());

            String raw = String.format("%02d/%02d/%d %02d:%02d:%02d",
                    zdt.getDayOfMonth(),
                    zdt.getMonthValue() + 1,
                    zdt.getYear(),
                    zdt.getHour(),
                    zdt.getMinute(),
                    zdt.getSecond());

            return "VM Time: " + raw;
        }));
        header.add(new HeaderLine().lineBreak(false));
        header.add(new HeaderLine("# KarmaAPI information"));
        header.add(new HeaderLine(() -> "API Version: 2.0.0-SNAPSHOT"));
        header.add(new HeaderLine(() -> "API Build: 1"));
        header.add(new HeaderLine(() -> "API Compiler: 1.8.0_362"));
        header.add(new HeaderLine(() -> "API Date: 27-03-2023 19:20:40"));
        header.add(new HeaderLine().lineBreak(false));
        header.add(new HeaderLine("# Source information"));
        header.add(new HeaderLine(() -> "Name: " + source.sourceName()));
        header.add(new HeaderLine(() -> "Version: " + source.sourceVersion()));
        header.add(new HeaderLine(() -> "Description: " + source.sourceDescription()));

        Path fl = logFile();
        rebuildHeader(fl);

        //ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        AsyncTaskExecutor.EXECUTOR.scheduleAtFixedRate(() -> {
            if (queue.hasItems() && !writing.get()) {
                writing.set(true);
                Path logFile = logFile();

                QuePair<LogLevel, Instant, Object> lateLogData = queue.next();
                LogLevel level = lateLogData.getFirst();

                Instant time = lateLogData.getSecond();
                ZonedDateTime zdt = ZonedDateTime.ofInstant(time, ZoneId.systemDefault());

                Object value = lateLogData.getThird();
                String rawValue;
                if (value instanceof Throwable) {
                    Throwable error = (Throwable) value;
                    rawValue = String.format("```java%n%s%n```%n", buildErrorMessage(error));
                } else {
                    rawValue = String.format("[%02d:%02d:%02d - %s] %s<br>%n",
                            zdt.getHour(),
                            zdt.getMinute(),
                            zdt.getSecond(),
                            level.getRaw(),
                            value);
                }

                try {
                    Files.write(logFile, rawValue.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                } catch (IOException ex) {
                    ExceptionCollector.catchException(LogFile.class, ex);
                } finally {
                    writing.set(false);
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    /**
     * Append the line
     *
     * @param level the log level
     * @param line the line to log
     */
    public void append(final LogLevel level, final String line) {
        this.append(level, null, line);
    }

    /**
     * Append the line and error
     *
     * @param level the log level
     * @param error the error to log
     * @param line the line to log
     */
    public void append(final LogLevel level, final Throwable error, final String line) {
        queue.append(level, line);
        if (error != null) queue.append(level, error);
    }

    /**
     * Get the current log file
     *
     * @return the current log file
     */
    public Path logFile() {
        Instant now = Instant.now();
        ZonedDateTime zdt = ZonedDateTime.ofInstant(now, ZoneId.systemDefault());

        String day = String.format("%02d", zdt.getDayOfMonth());
        String month = zdt.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()).toLowerCase();
        String year = String.valueOf(zdt.getYear());

        Path schema = source.workingDirectory().resolve("logs").resolve("year\\\\month\\\\day.md");
        PathUtilities.createPath(schema);

        Path file = source.workingDirectory().resolve("logs").resolve(year).resolve(month).resolve(day + ".md");
        boolean create = !Files.exists(file);
        PathUtilities.createPath(file);

        if (create) rebuildHeader(file);
        return file;
    }

    /**
     * Rebuild the file header
     *
     * @param file the file
     */
    private void rebuildHeader(final Path file) {
        try (InputStream stream = PathUtilities.toStream(file)) {
            String[] lines = StreamUtils.streamToString(stream).split("\n");

            String[] logContent = new String[0];
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (line.equals("# Beginning of log<br><br>")) {
                    logContent = Arrays.copyOfRange(lines, Math.min(i + 2, lines.length - 1), lines.length);
                    break;
                }
            }

            String rawHeader = header.build() + "\n# Beginning of log<br><br>\n\n";
            StringBuilder rawContentBuilder = new StringBuilder(rawHeader);
            for (String line : logContent) rawContentBuilder.append(line).append("\n");

            Files.write(file, rawContentBuilder.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException ex) {
            ExceptionCollector.catchException(LogFile.class, ex);
        }
    }

    private String buildErrorMessage(final Throwable throwable) {
        Throwable[] suppressed = throwable.getSuppressed();
        StringBuilder builder = new StringBuilder(throwable.fillInStackTrace().toString()).append("\n");
        StackTraceElement[] elements = throwable.getStackTrace();
        for (int i = 0; i < elements.length; i++) {
            StackTraceElement element = elements[i];
            String clazz = element.getClassName();
            String method = element.getMethodName();
            String file = element.getFileName();
            int line = element.getLineNumber();

            String raw;
            if (file == null) {
                raw = String.format("\t\t\t%s#%s (at line %d)", clazz, method, line);
            } else {
                file = file.replace(".java", "");
                raw = String.format("\t\t\t%s#%s (at %s:%d)", clazz, method, file, line);
            }

            builder.append(raw).append((i == elements.length - 1 ? "" : "\n"));
        }

        for (Throwable sub : suppressed) {
            String raw = buildErrorMessage(sub);
            builder.append("\n").append("-- INVOLVED,SUPPRESSED --").append("\n").append(raw);
        }

        return builder.toString();
    }
}
