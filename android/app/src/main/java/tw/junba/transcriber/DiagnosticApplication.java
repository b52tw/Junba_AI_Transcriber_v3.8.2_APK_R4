package tw.junba.transcriber;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Process;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Installs the crash recorder before any Activity is created.  The full
 * transcriber runs in a separate process so a crash there does not take down
 * the diagnostic launcher process.
 */
public final class DiagnosticApplication extends Application {
    public static final String PREFS = "junba_r4_diag";
    public static final String KEY_LAST_CRASH = "last_crash";
    public static final String KEY_LAST_CRASH_AT = "last_crash_at";

    @Override
    public void onCreate() {
        super.onCreate();
        final Thread.UncaughtExceptionHandler previous = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                pw.println("Junba R4 Diagnostic crash report");
                pw.println("time=" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ROOT).format(new Date()));
                pw.println("process=" + Process.myPid() + " thread=" + thread.getName());
                pw.println("device=" + Build.MANUFACTURER + " " + Build.MODEL);
                pw.println("android=" + Build.VERSION.RELEASE + " sdk=" + Build.VERSION.SDK_INT);
                pw.println("abis=" + String.join(",", Build.SUPPORTED_ABIS));
                pw.println();
                throwable.printStackTrace(pw);
                pw.flush();
                String report = sw.toString();

                SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
                prefs.edit()
                        .putString(KEY_LAST_CRASH, report)
                        .putLong(KEY_LAST_CRASH_AT, System.currentTimeMillis())
                        .commit();

                File f = new File(getFilesDir(), "r4_last_crash.txt");
                try (FileOutputStream out = new FileOutputStream(f, false)) {
                    out.write(report.getBytes(StandardCharsets.UTF_8));
                    out.flush();
                }
            } catch (Throwable ignored) {
                // Never let the crash recorder mask the original exception.
            }

            if (previous != null) {
                previous.uncaughtException(thread, throwable);
            } else {
                Process.killProcess(Process.myPid());
                System.exit(10);
            }
        });
    }
}
