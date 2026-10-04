package tw.junba.transcriber;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Minimal safe launcher.  This Activity intentionally does not reference the
 * Whisper library, Gemini client code, SecureKeyStore, MediaCodec or the full
 * TranscriberActivity class directly.  Its job is to prove that the APK can
 * start on the phone and to show the real stack trace if the full feature
 * process crashes.
 */
public final class MainActivity extends Activity {
    private EditText crashBox;
    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildUi());
        refreshCrashReport();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCrashReport();
    }

    private ScrollView buildUi() {
        int pad = dp(16);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, dp(28));
        scroll.addView(root, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView title = text("峻爸 AI Transcriber v3.8.2｜R4 Diagnostic Safe Launcher", 21, true);
        root.addView(title, match());

        TextView intro = text(
                "這個首頁只使用 Android 原生元件，不載入 Whisper、Gemini、API Key 或音訊解碼器。" +
                "如果你能看到這個畫面，就代表 APK 安裝與手機基本啟動層正常。完整轉錄功能會在獨立程序開啟；若它崩潰，回到這裡會留下真正的錯誤堆疊。",
                14, false);
        intro.setPadding(0, dp(8), 0, dp(12));
        root.addView(intro, match());

        status = text("R4 安全啟動層：正常", 17, true);
        status.setTextColor(0xFF0B7A34);
        root.addView(status, match());

        TextView device = text(
                "裝置：" + Build.MANUFACTURER + " " + Build.MODEL +
                "\nAndroid：" + Build.VERSION.RELEASE + "（SDK " + Build.VERSION.SDK_INT + ")" +
                "\nABI：" + String.join(", ", Build.SUPPORTED_ABIS),
                13, false);
        device.setPadding(0, dp(6), 0, dp(12));
        root.addView(device, match());

        Button open = button("開啟完整轉錄功能（獨立程序）");
        open.setOnClickListener(v -> openFullTranscriber());
        root.addView(open, match());

        Button refresh = button("重新讀取上次錯誤");
        refresh.setOnClickListener(v -> refreshCrashReport());
        root.addView(refresh, match());

        Button copy = button("複製診斷內容");
        copy.setOnClickListener(v -> copyReport());
        root.addView(copy, match());

        Button clear = button("清除上次錯誤紀錄");
        clear.setOnClickListener(v -> clearReport());
        root.addView(clear, match());

        TextView label = text("上次完整功能程序的錯誤紀錄", 16, true);
        label.setPadding(0, dp(14), 0, dp(4));
        root.addView(label, match());

        crashBox = new EditText(this);
        crashBox.setGravity(Gravity.TOP | Gravity.START);
        crashBox.setTextSize(12);
        crashBox.setMinLines(16);
        crashBox.setHorizontallyScrolling(false);
        crashBox.setVerticalScrollBarEnabled(true);
        crashBox.setTextIsSelectable(true);
        crashBox.setFocusable(false);
        crashBox.setHint("目前沒有錯誤紀錄。\n\n按「開啟完整轉錄功能」後，如果完整功能閃退，回到這個頁面即可看到 Exception、class 與 stack trace。");
        root.addView(crashBox, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(420)));

        TextView footer = text(
                "判讀方式：\n" +
                "① 如果連本頁都看不到 → APK/Manifest/系統啟動層問題。\n" +
                "② 本頁可見、完整功能閃退且此處出現 stack trace → 直接依行號修程式。\n" +
                "③ 本頁可見、完整功能也可開 → 手機本身不是主因。",
                13, false);
        footer.setPadding(0, dp(12), 0, 0);
        root.addView(footer, match());

        return scroll;
    }

    private void openFullTranscriber() {
        try {
            // String class name avoids loading TranscriberActivity in this safe process.
            Intent intent = new Intent();
            intent.setClassName(getPackageName(), "tw.junba.transcriber.TranscriberActivity");
            startActivity(intent);
            toast("已啟動完整功能獨立程序；若閃退請回到 R4 首頁查看錯誤。 ");
        } catch (Throwable t) {
            String msg = "Launcher 無法啟動完整功能：" + t.getClass().getName() + ": " + t.getMessage();
            status.setText(msg);
            status.setTextColor(0xFFB71C1C);
            crashBox.setText(msg);
        }
    }

    private void refreshCrashReport() {
        if (crashBox == null || status == null) return;
        SharedPreferences p = getSharedPreferences(DiagnosticApplication.PREFS, MODE_PRIVATE);
        String report = p.getString(DiagnosticApplication.KEY_LAST_CRASH, "");
        long at = p.getLong(DiagnosticApplication.KEY_LAST_CRASH_AT, 0L);
        if (report == null || report.trim().isEmpty()) {
            crashBox.setText("");
            status.setText("R4 安全啟動層：正常｜目前沒有完整功能崩潰紀錄");
            status.setTextColor(0xFF0B7A34);
            return;
        }
        String when = at <= 0 ? "未知時間" : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT).format(new Date(at));
        status.setText("已捕捉完整功能崩潰｜" + when);
        status.setTextColor(0xFFB71C1C);
        crashBox.setText(report);
        crashBox.setSelection(0);
    }

    private void copyReport() {
        String s = crashBox == null ? "" : crashBox.getText().toString();
        if (s.trim().isEmpty()) {
            toast("目前沒有診斷內容可複製");
            return;
        }
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            cm.setPrimaryClip(ClipData.newPlainText("Junba R4 crash report", s));
            toast("診斷內容已複製");
        }
    }

    private void clearReport() {
        getSharedPreferences(DiagnosticApplication.PREFS, MODE_PRIVATE)
                .edit()
                .remove(DiagnosticApplication.KEY_LAST_CRASH)
                .remove(DiagnosticApplication.KEY_LAST_CRASH_AT)
                .apply();
        refreshCrashReport();
        toast("已清除錯誤紀錄");
    }

    private TextView text(String s, int sp, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(0xFF17202A);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private Button button(String s) {
        Button b = new Button(this);
        b.setText(s);
        b.setAllCaps(false);
        b.setMinHeight(dp(48));
        return b;
    }

    private LinearLayout.LayoutParams match() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int x) {
        return Math.round(x * getResources().getDisplayMetrics().density);
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
