package es.karmadev.api.web;

import es.karmadev.api.MemoryUnit;
import es.karmadev.api.file.util.PathUtilities;
import es.karmadev.api.schedule.runner.async.AsyncTaskExecutor;
import es.karmadev.api.schedule.task.completable.late.LateTask;
import lombok.Getter;

import javax.net.ssl.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Web downloader, to download files from
 * the web
 */
public class WebDownloader {

    private final static ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor();

    private final URL url;

    /**
     * -- GETTER --
     *  Get if the downloader is working
     */
    @Getter
    private boolean downloading = false;
    private int totalBytes;
    private long downloadStart;
    private long downloadEnd;
    private int downloadedBytes;

    /**
     * Initialize the web downloader
     *
     * @param targetURI the target URL
     * @throws MalformedURLException if the target URI is invalid
     */
    public WebDownloader(final URI targetURI) throws MalformedURLException {
        this(targetURI.toURL());
    }

    /**
     * Initialize the web downloader
     *
     * @param targetURL the target URL
     */
    public WebDownloader(final URL targetURL) {
        this.url = targetURL;
    }

    /**
     * Initialize the web downloader
     *
     * @param target the target URL
     * @throws MalformedURLException if the target URL is invalid
     */
    public WebDownloader(final String target) throws MalformedURLException {
        this(new URL(target));
    }

    /**
     * Download the file asynchronously
     *
     * @param target the target file
     * @return the download task
     */
    public LateTask<Boolean> downloadAsync(final Path target) {
        if (downloading) throw new IllegalStateException("Cannot download already downloading element");

        LateTask<Boolean> task = new LateTask<>();
        EXECUTOR.schedule(() -> {
            try {
                boolean download = download(target);
                task.complete(download);
            } catch (IOException | NoSuchAlgorithmException | KeyManagementException ex) {
                task.complete(false, ex);
            }
        }, 0, TimeUnit.SECONDS);

        return task;
    }

    /**
     * Download the file
     *
     * @param target the target file
     * @throws IOException if something bad happens
     * @throws NoSuchAlgorithmException if something bad happens
     * @throws KeyManagementException if something bad happens
     */
    public boolean download(final Path target) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        if (downloading) throw new IllegalStateException("Cannot download already downloading element");
        downloading = true;

        if (PathUtilities.createPath(target)) {
            try {
                URLConnection connection = url.openConnection();
                connection.connect();

                TrustManager[] trustManagers = new TrustManager[]{new NvbTrustManager()};
                SSLContext context = SSLContext.getInstance("TLSv1.3");
                context.init(null, trustManagers, null);

                HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
                HttpsURLConnection.setDefaultHostnameVerifier(new NvbHostnameVerifier());

                totalBytes = connection.getContentLength();
                try (InputStream stream = connection.getInputStream(); FileOutputStream fos = new FileOutputStream(target.toFile())) {
                    //totalBytes = stream.available() * 8192;
                    downloadStart = System.currentTimeMillis();
                    downloadEnd = 0;
                    downloadedBytes = 0;

                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = stream.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                        downloadedBytes += bytesRead;
                    }

                    downloadEnd = System.currentTimeMillis();
                    downloading = false;
                    return true;
                }
            } catch (IOException | NoSuchAlgorithmException | KeyManagementException ex) {
                downloading = false; //Set downloading status to false if any exception, then throw
                throw ex;
            }
        }

        downloading = false;
        return false;
    }

    /**
     * Get the amount of data downloaded
     *
     * @return the downloaded data
     */
    public long getDownloaded() {
        return downloadedBytes;
    }

    /**
     * Get the amount of data downloaded
     *
     * @param unit the unit to get the value
     *             as
     * @return the downloaded data
     */
    public long getDownloaded(final MemoryUnit unit) {
        return MemoryUnit.BYTES.to(downloadedBytes, unit);
    }

    /**
     * Get the remaining amount of data needed
     * in order to complete the download
     *
     * @return the remaining data
     */
    public long getRemaining() {
        return totalBytes - downloadedBytes;
    }

    /**
     * Get the remaining amount of data needed
     * in order to complete the download
     *
     * @param unit the unit to get the value
     *             as
     * @return the remaining data
     */
    public long getRemaining(final MemoryUnit unit) {
        long diff = (totalBytes - downloadedBytes);
        return MemoryUnit.BYTES.to(diff, unit);
    }

    /**
     * Get the download file size
     *
     * @return the download file size
     */
    public long getSize() {
        return totalBytes;
    }

    /**
     * Get the download file size
     *
     * @param unit the unit to get the value
     *             as
     * @return the download file size
     */
    public long getSize(final MemoryUnit unit) {
        return MemoryUnit.BYTES.to(totalBytes, unit);
    }

    /**
     * Get the download speed, based on
     * highest memory unit available per second
     *
     * @return the download speed
     */
    public double getDownloadSpeed() {
        double speed = getDownloadSpeed(MemoryUnit.BYTES, TimeUnit.SECONDS);
        MemoryUnit highest = MemoryUnit.highestAvailable((long) speed, MemoryUnit.BYTES);

        return getDownloadSpeed(highest, TimeUnit.SECONDS);
    }

    /**
     * Get the download speed, based on
     * the specified memory unit
     *
     * @param memory the memory unit to get speed
     *               for
     * @return the download speed
     */
    public double getDownloadSpeed(final MemoryUnit memory) {
        return getDownloadSpeed(memory, TimeUnit.SECONDS);
    }

    /**
     * Get the download speed
     *
     * @param memory the memory unit to get speed
     *             for ({memory}/{time})
     * @param time the time unit to get speed for ({memory}/{time})
     * @return the download speed
     */
    public double getDownloadSpeed(final MemoryUnit memory, final TimeUnit time) {
        long elapsed = time.convert(System.currentTimeMillis() - downloadStart, TimeUnit.MILLISECONDS);
        long downloaded = MemoryUnit.BYTES.to(downloadedBytes, memory);

        return (double) downloaded / elapsed;
    }

    /**
     * Get the file download remaining time
     *
     * @return the file remaining time
     */
    public long getRemainingTime() {
        return getRemainingTime(TimeUnit.SECONDS);
    }

    /**
     * Get the amount of time the downloader
     * took to download the file
     *
     * @return the download time
     */
    public long getDownloadTime() {
        if (downloadEnd != 0) {
            return downloadEnd - downloadStart;
        }

        return -1; //Still downloading
    }

    /**
     * Get the file download remaining time
     *
     * @return the file remaining time
     */
    public long getRemainingTime(final TimeUnit unit) {
        double speed = getDownloadSpeed(MemoryUnit.BYTES, unit);
        if (speed > 0) {
            long remaining = totalBytes - downloadedBytes;
            return (long) (remaining / speed);
        }

        return -1; //Unknown time
    }

    /**
     * Simple <code>TrustManager</code> that allows unsigned certificates.
     */
    private static final class NvbTrustManager implements TrustManager, X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

    /**
     * Simple <code>HostnameVerifier</code> that allows any hostname and session.
     */
    private static final class NvbHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
}
