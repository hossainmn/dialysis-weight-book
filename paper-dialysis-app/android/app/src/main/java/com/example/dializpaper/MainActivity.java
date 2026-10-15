package com.example.dializpaper;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.webkit.WebView;
import com.getcapacitor.BridgeActivity;
import com.getcapacitor.WebViewListener;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class MainActivity extends BridgeActivity {

    private static final long MAX_IMPORT_BYTES = 5L * 1024L * 1024L;
    private String pendingDializContent = null;
    private String pendingDializName = null;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (bridge != null) {
                // No liquid stretch/bounce when dragging past edges (Android 12+ stretch overscroll).
                if (bridge.getWebView() != null) {
                    bridge.getWebView().setOverScrollMode(android.view.View.OVER_SCROLL_NEVER);
                }
                bridge.addWebViewListener(new WebViewListener() {
                    @Override
                    public void onPageLoaded(WebView webView) {
                        deliverPendingDializ();
                    }
                });
            }
        } catch (Exception ignored) {}
        handleDializIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleDializIntent(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        deliverPendingDializ();
    }

    @SuppressWarnings("deprecation")
    private void handleDializIntent(Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if (action == null) return;
        Uri uri = null;
        if (Intent.ACTION_VIEW.equals(action)) {
            uri = intent.getData();
        } else if (Intent.ACTION_SEND.equals(action)) {
            try {
                android.os.Parcelable extra = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (extra instanceof Uri) uri = (Uri) extra;
            } catch (Exception ignored) {}
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            try {
                ArrayList<android.os.Parcelable> list = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                if (list != null && !list.isEmpty() && list.get(0) instanceof Uri) uri = (Uri) list.get(0);
            } catch (Exception ignored) {}
        }
        if (uri == null) return;
        String content = readTextUri(uri);
        if (content == null) return;
        if (!content.isEmpty() && content.charAt(0) == '﻿') content = content.substring(1);
        String trimmed = content.trim();
        // Only react to plausible JSON backups; ignore photos / random shares.
        if (trimmed.length() < 2 || trimmed.charAt(0) != '{') return;
        if (trimmed.length() > MAX_IMPORT_BYTES) return;
        if (trimmed.indexOf("dialysis-paper-logbook") < 0
                && trimmed.indexOf("\"jy\"") < 0
                && trimmed.indexOf("\"before\"") < 0
                && trimmed.indexOf("\"entries\"") < 0) {
            return;
        }
        pendingDializContent = content;
        pendingDializName = getDisplayName(uri);
        deliverPendingDializ();
        scheduleDializRetries();
        scheduleDializExpiry();
    }

    private void scheduleDializRetries() {
        long[] delays = { 800L, 2000L, 4000L };
        for (long d : delays) {
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() { deliverPendingDializ(); }
            }, d);
        }
    }

    private void scheduleDializExpiry() {
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() { pendingDializContent = null; pendingDializName = null; }
        }, 12000L);
    }

    private void deliverPendingDializ() {
        if (pendingDializContent == null) return;
        if (bridge == null || bridge.getWebView() == null) return;
        // NOTE: pending is intentionally NOT cleared here. The WebView may not
        // have finished loading index.html yet (cold start via tap-to-open), so
        // the first eval can land on a blank page and get wiped by navigation.
        // Retries + onPageLoaded re-deliver; JS dedupes them; expiry clears.
        try {
            String quotedContent = JSONObject.quote(pendingDializContent);
            String quotedName = JSONObject.quote(pendingDializName != null ? pendingDializName : "");
            String js = "window.__dializPendingContent=" + quotedContent + ";"
                    + "window.__dializPendingName=" + quotedName + ";"
                    + "window.dispatchEvent(new CustomEvent('dializ-file-open',{detail:{content:window.__dializPendingContent,name:window.__dializPendingName}}));";
            bridge.eval(js, null);
        } catch (Exception ignored) {}
    }

    private String getDisplayName(Uri uri) {
        try {
            if ("content".equals(uri.getScheme())) {
                Cursor c = null;
                try {
                    c = getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);
                    if (c != null && c.moveToFirst()) {
                        String n = c.getString(0);
                        if (n != null && n.length() > 0) return n;
                    }
                } finally {
                    if (c != null) c.close();
                }
            }
            String seg = uri.getLastPathSegment();
            return seg != null ? seg : "";
        } catch (Exception ignored) {
            return "";
        }
    }

    private String readTextUri(Uri uri) {
        InputStream in = null;
        try {
            in = getContentResolver().openInputStream(uri);
            if (in == null) return null;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            long total = 0;
            int n;
            while ((n = in.read(buf)) != -1) {
                total += n;
                if (total > MAX_IMPORT_BYTES) return null;
                out.write(buf, 0, n);
            }
            return out.toString(StandardCharsets.UTF_8.name());
        } catch (Exception ignored) {
            return null;
        } finally {
            try { if (in != null) in.close(); } catch (Exception ignored) {}
        }
    }
}
