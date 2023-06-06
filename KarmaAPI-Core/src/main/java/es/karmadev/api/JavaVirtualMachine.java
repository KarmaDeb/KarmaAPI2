package es.karmadev.api;

import es.karmadev.api.core.ExceptionCollector;
import es.karmadev.api.version.Version;

import java.lang.management.*;
import java.nio.file.Path;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Java Virtual Machine information
 */
@SuppressWarnings("unused")
public class JavaVirtualMachine {

    public static MemoryUnit PREFERRED_UNIT = MemoryUnit.BYTES;

    private final static OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
    private final static ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    private final static RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
    private final static Runtime runtime = Runtime.getRuntime();

    private final static Lock lock = new ReentrantLock();

    /**
     * Pause the current thread
     *
     * @param time the time to pause
     * @param unit the time unit
     * @throws RuntimeException if the wait gets interrupted
     */
    public static void wait(final long time, final TimeUnit unit) throws RuntimeException {
        Condition condition = lock.newCondition();
        try {
            lock.lock();
            if (condition.await(time, unit)) {
                throw new RuntimeException("Interrupted JVM wait");
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get the JVM cores
     *
     * @return the JVM cores
     */
    public static int cores() {
        return runtime.availableProcessors();
    }

    /**
     * Get the JVM available memory
     *
     * @return the available memory
     */
    public static long availableMemory() {
        return availableMemory(PREFERRED_UNIT);
    }

    /**
     * Get the JVM available memory
     *
     * @param unit the memory unit to use
     * @return the available memory
     */
    public static long availableMemory(final MemoryUnit unit) {
        long free = runtime.freeMemory();
        switch (unit) {
            case BITS:
                return free * 8;
            case KILOBYTES:
                return free / 1024;
            case MEGABYTES:
                return (free / 1024) / 1024;
            case GIGABYTES:
               return ((free / 1024) / 1024) / 1024;
            case BYTES:
            default:
                return free;
        }
    }

    /**
     * Get the system total memory
     *
     * @return the total memory
     */
    public static long totalMemory() {
        return totalMemory(PREFERRED_UNIT);
    }

    /**
     * Get the system total memory
     *
     * @param unit the memory unit to use
     * @return the total memory
     */
    public static long totalMemory(final MemoryUnit unit) {
        long free = runtime.totalMemory();
        switch (unit) {
            case BITS:
                return free * 8;
            case KILOBYTES:
                return free / 1024;
            case MEGABYTES:
                return (free / 1024) / 1024;
            case GIGABYTES:
                return ((free / 1024) / 1024) / 1024;
            case BYTES:
            default:
                return free;
        }
    }

    /**
     * Get the JVM max memory
     *
     * @return the max memory
     */
    public static long allocatedMemory() {
        return allocatedMemory(PREFERRED_UNIT);
    }

    /**
     * Get the JVM max memory
     *
     * @param unit the memory unit to use
     * @return the max memory
     */
    public static long allocatedMemory(final MemoryUnit unit) {
        long free = runtime.maxMemory();
        switch (unit) {
            case BITS:
                return free * 8;
            case KILOBYTES:
                return free / 1024;
            case MEGABYTES:
                return (free / 1024) / 1024;
            case GIGABYTES:
                return ((free / 1024) / 1024) / 1024;
            case BYTES:
            default:
                return free;
        }
    }

    /**
     * Get the system average load charge
     *
     * @return the system average load
     */
    public static double systemLoad() {
        return osBean.getSystemLoadAverage();
    }

    /**
     * Get the system os name
     *
     * @return the system os name
     */
    public static String osName() {
        return osBean.getName();
    }

    /**
     * Get the system architecture
     *
     * @return the system architecture
     */
    public static String osArch() {
        return osBean.getArch();
    }

    /**
     * Get the system version
     *
     * @return the system version
     */
    public static Version osVersion() {
        String versionString = osBean.getVersion();
        int mayor = 0;
        int minor = 0;
        int patch = 0;
        String build = null;
        if (versionString.contains(".")) {
            String[] versionData = versionString.split("\\.");
            switch (versionData.length) {
                case 1:
                    try {
                        String mayorPart = versionData[0];
                        if (mayorPart.contains("-")) {
                            String[] part = mayorPart.split("-");
                            mayor = Integer.parseInt(part[0]);
                            build = mayorPart.replace(mayor + "-", "");
                        } else {
                            mayor = Integer.parseInt(mayorPart);
                        }
                    } catch (NumberFormatException ex) {
                        ExceptionCollector.catchException(JavaVirtualMachine.class, ex);
                    }
                    break;
                case 2:
                    try {
                        String mayorPart = versionData[0];
                        String minorPart = versionData[1];
                        if (mayorPart.contains("-")) {
                            String[] part = mayorPart.split("-");
                            mayor = Integer.parseInt(part[0]);
                            build = mayorPart.replace(mayor + "-", "");
                        } else {
                            mayor = Integer.parseInt(mayorPart);
                        }

                        if (minorPart.contains("-")) {
                            String[] part = minorPart.split("-");
                            minor = Integer.parseInt(part[0]);
                            build = minorPart.replace(minor + "-", "");
                        } else {
                            minor = Integer.parseInt(minorPart);
                        }
                    } catch (NumberFormatException ex) {
                        ExceptionCollector.catchException(JavaVirtualMachine.class, ex);
                    }
                    break;
                case 3:
                    try {
                        String mayorPart = versionData[0];
                        String minorPart = versionData[1];
                        String patchPart = versionData[2];

                        if (mayorPart.contains("-")) {
                            String[] part = mayorPart.split("-");
                            mayor = Integer.parseInt(part[0]);
                            build = mayorPart.replace(mayor + "-", "");
                        } else {
                            mayor = Integer.parseInt(mayorPart);
                        }

                        if (minorPart.contains("-")) {
                            String[] part = minorPart.split("-");
                            minor = Integer.parseInt(part[0]);
                            build = minorPart.replace(minor + "-", "");
                        } else {
                            minor = Integer.parseInt(versionData[1]);
                        }

                        if (patchPart.contains("-")) {
                            String[] part = patchPart.split("-");
                            patch = Integer.parseInt(part[0]);
                            build = patchPart.replace(mayor + "-", "");
                        } else {
                            patch = Integer.parseInt(patchPart);
                        }
                    } catch (NumberFormatException ex) {
                        ExceptionCollector.catchException(JavaVirtualMachine.class, ex);
                    }
                    break;
                case 0:
                default:
                    break;
            }
        }

        return Version.of(mayor, minor, patch, build);
    }

    /**
     * Get the JVM name
     *
     * @return the JVM name
     */
    public static String jvmName() {
        return runtimeBean.getVmName();
    }

    /**
     * Get the JVM version
     *
     * @return the JVM version
     */
    public static Version jvmVersion() {
        String versionString = runtimeBean.getVmVersion();
        int mayor = 0;
        int minor = 0;
        int patch = 0;
        String build = null;
        if (versionString.contains(".")) {
            String[] versionData = versionString.split("\\.");
            switch (versionData.length) {
                case 1:
                    try {
                        String mayorPart = versionData[0];
                        if (mayorPart.contains("-")) {
                            String[] part = mayorPart.split("-");
                            mayor = Integer.parseInt(part[0]);
                            build = mayorPart.replace(mayor + "-", "");
                        } else {
                            mayor = Integer.parseInt(mayorPart);
                        }
                    } catch (NumberFormatException ex) {
                        ExceptionCollector.catchException(JavaVirtualMachine.class, ex);
                    }
                    break;
                case 2:
                    try {
                        String mayorPart = versionData[0];
                        String minorPart = versionData[1];
                        if (mayorPart.contains("-")) {
                            String[] part = mayorPart.split("-");
                            mayor = Integer.parseInt(part[0]);
                            build = mayorPart.replace(mayor + "-", "");
                        } else {
                            mayor = Integer.parseInt(mayorPart);
                        }

                        if (minorPart.contains("-")) {
                            String[] part = minorPart.split("-");
                            minor = Integer.parseInt(part[0]);
                            build = minorPart.replace(minor + "-", "");
                        } else {
                            minor = Integer.parseInt(minorPart);
                        }
                    } catch (NumberFormatException ex) {
                        ExceptionCollector.catchException(JavaVirtualMachine.class, ex);
                    }
                    break;
                case 3:
                    try {
                        String mayorPart = versionData[0];
                        String minorPart = versionData[1];
                        String patchPart = versionData[2];

                        if (mayorPart.contains("-")) {
                            String[] part = mayorPart.split("-");
                            mayor = Integer.parseInt(part[0]);
                            build = mayorPart.replace(mayor + "-", "");
                        } else {
                            mayor = Integer.parseInt(mayorPart);
                        }

                        if (minorPart.contains("-")) {
                            String[] part = minorPart.split("-");
                            minor = Integer.parseInt(part[0]);
                            build = minorPart.replace(minor + "-", "");
                        } else {
                            minor = Integer.parseInt(versionData[1]);
                        }

                        if (patchPart.contains("-")) {
                            String[] part = patchPart.split("-");
                            patch = Integer.parseInt(part[0]);
                            build = patchPart.replace(mayor + "-", "");
                        } else {
                            patch = Integer.parseInt(patchPart);
                        }
                    } catch (NumberFormatException ex) {
                        ExceptionCollector.catchException(JavaVirtualMachine.class, ex);
                    }
                    break;
                case 0:
                default:
                    break;
            }
        }

        return Version.of(mayor, minor, patch, build);
    }

    /**
     * Get the JVM uptime
     *
     * @return the JVM uptime
     */
    public static long upTime() {
        return runtimeBean.getUptime();
    }

    /**
     * Get the JVM uptime
     *
     * @param unit the unit to return the time
     *             as
     * @return the JVM uptime
     */
    public static long upTime(final TimeUnit unit) {
        long time = runtimeBean.getUptime();

        switch (unit) {
            case MICROSECONDS:
                return TimeUnit.MILLISECONDS.toMicros(time);
            case NANOSECONDS:
                return TimeUnit.MILLISECONDS.toNanos(time);
            case SECONDS:
                return TimeUnit.MILLISECONDS.toSeconds(time);
            case MINUTES:
                return TimeUnit.MILLISECONDS.toMinutes(time);
            case HOURS:
                return TimeUnit.MILLISECONDS.toHours(time);
            case DAYS:
                return TimeUnit.MILLISECONDS.toDays(time);
            case MILLISECONDS:
            default:
                return time;
        }
    }

    /**
     * Get when the JVM was started
     *
     * @return the JVM start time
     */
    public static Instant startTime() {
        return Instant.ofEpochMilli(runtimeBean.getStartTime());
    }

    /**
     * Get all the threads started by the JVM
     *
     * @return the created threads
     */
    public static long startedThreads() {
        return threadBean.getTotalStartedThreadCount();
    }

    /**
     * Get the JVM thread count
     *
     * @return the thread count
     */
    public static int threadCount() {
        return threadBean.getThreadCount();
    }

    /**
     * Get the JVM daemon thread count
     *
     * @return the daemon count
     */
    public static int daemonCount() {
        return threadBean.getDaemonThreadCount();
    }

    /**
     * Get the JVM thread peak count
     *
     * @return the thread peak count
     */
    public static int peakCount() {
        return threadBean.getPeakThreadCount();
    }

    /**
     * Get a thread by its ID
     *
     * @param id the thread ID
     * @return the thread
     */
    public static ThreadInfo getThread(final long id) {
        return threadBean.getThreadInfo(id);
    }

    /**
     * Get all the threads
     *
     * @return the JVM threads
     */
    public static long[] getThreads() {
        return threadBean.getAllThreadIds();
    }

    /**
     * Get the current thread CPU usage time
     *
     * @return the current thread CPU time
     */
    public static long getCPUTime() {
        return threadBean.getCurrentThreadCpuTime();
    }

    /**
     * Get the thread CPU usage time
     *
     * @param id the thread id
     * @return the thread CPU time
     */
    public static long getCPUTime(final long id) {
        return threadBean.getThreadCpuTime(id);
    }

    /**
     * Get the current thread usage time
     *
     * @return the current thread time
     */
    public static long getUsageTime() {
        return threadBean.getCurrentThreadUserTime();
    }

    /**
     * Get the thread usage time
     *
     * @param id the thread id
     * @return the thread time
     */
    public static long getUsageTime(final long id) {
        return threadBean.getThreadUserTime(id);
    }

    /**
     * Append a jarfile into the current
     * class loader
     *
     * @param jarFile the jarfile to append
     */
    public static void append(final Path jarFile) {
        /*List<String> modules = new ArrayList<>();
        ModuleLayer.boot().modules().forEach(module -> modules.add(module.getName()));

        ModuleLayer parentLayer = ModuleLayer.boot();
        ModuleFinder finder = ModuleFinder.of(jarFile);
        ModuleReference reference = finder.findAll().iterator().next();
        ModuleDescriptor descriptor = reference.descriptor();
        ModuleLayer.Controller controller = parentLayer.controller().newModuleLayer(
                parentLayer.configuration().resolveAndBind(ModuleFinder.of(), ModuleFinder.of(), ModuleFinder.of()),
                List.of(parentLayer),
                ClassLoader.getSystemClassLoader()
        );
        ClassLoader customClassLoader = controller.layer().findLoader(descriptor.name());*/
    }
}