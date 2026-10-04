Junba AI Transcriber v3.8.2 APK R4 Diagnostic
=============================================

目的
----
這一版不是再猜閃退原因，而是把「安全啟動頁」與「完整轉錄功能」拆成兩個 Android process。

R4 首頁只使用 Android 原生元件：
- 不載入 Whisper
- 不載入 Gemini
- 不讀 API Key
- 不初始化音訊轉碼
- 不呼叫完整轉錄 Activity 類別

只要 R4 首頁能打開，就代表：
APK、Manifest、手機基本 Android Activity 啟動層正常。

完整功能由 :transcriber 獨立 process 啟動。
若完整功能 Runtime Crash，DiagnosticApplication 會把 Java/Kotlin stack trace 寫到共用 App 資料；
回到 R4 首頁即可看到並可直接複製。

測試順序
--------
1. 請先完整解除安裝舊版「峻爸 AI Transcriber」。
2. 再安裝 Junba-R4-Diagnostic.apk。
3. 開啟 App。
4. 先確認是否看得到「R4 Diagnostic Safe Launcher」。
5. 再按「開啟完整轉錄功能（獨立程序）」。
6. 若完整功能閃退，回到首頁按「重新讀取上次錯誤」。
7. 按「複製診斷內容」，把完整內容貼回 ChatGPT。

版本
----
versionCode: 388
versionName: 3.8.2-apk-r4-diagnostic
