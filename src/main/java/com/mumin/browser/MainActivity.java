package com.mumin.browser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import java.util.ArrayList;

public class MainActivity extends Activity {
    static final String BG = "#020C1A";
    static final String AC = "#14C8B4";
    static final String HOME = "mumin:home";
    static final String HOME_HTML =
        "<!doctype html><html><head><meta charset='utf-8'>"
        + "<meta name='viewport' content='width=device-width,initial-scale=1'>"
        + "<title>Mumin Browser</title><style>"
        + "body{margin:0;background:#020c1a;color:#e8f6f4;font-family:sans-serif;text-align:center;padding:36px 18px}"
        + "svg{width:140px;height:140px;margin-top:16px}"
        + "h1{margin:6px 0 4px;font-size:24px;letter-spacing:2px;color:#14c8b4}"
        + "p{margin:0 0 22px;color:#7fa5a0;font-size:14px}"
        + "input{width:100%;max-width:520px;box-sizing:border-box;border:1px solid #1d3b52;background:#0b1d31;color:#fff;border-radius:28px;padding:14px 20px;font-size:16px;outline:none}"
        + ".g{display:grid;grid-template-columns:repeat(3,1fr);gap:12px;max-width:520px;margin:26px auto 0}"
        + ".g a{background:#0b1d31;border:1px solid #1d3b52;border-radius:14px;padding:14px 4px;color:#e8f6f4;text-decoration:none;font-size:13px}"
        + "</style></head><body>"
        + "<svg viewBox='0 0 100 100'><g fill='none' stroke='#14c8b4' stroke-width='4' stroke-linecap='round'>"
        + "<path d='M16 94V58C16 40 36 32 50 16C64 32 84 40 84 58V94'/>"
        + "<circle cx='48' cy='64' r='14'/><path d='M58 74L70 86'/></g>"
        + "<path fill='#14c8b4' d='M52 3a8 8 0 1 0 6 12a6.5 6.5 0 0 1-6-12z'/></svg>"
        + "<h1>MUMIN BROWSER</h1><p>দ্রুত, সহজ, আপনার নিজের ব্রাউজার</p>"
        + "<form action='https://www.google.com/search' method='get'>"
        + "<input name='q' placeholder='Google-এ সার্চ করুন' autocomplete='off'></form>"
        + "<div class='g'>"
        + "<a href='https://www.youtube.com'>▶️<br>YouTube</a>"
        + "<a href='https://www.google.com'>🔍<br>Google</a>"
        + "<a href='https://bn.wikipedia.org'>📖<br>উইকিপিডিয়া</a>"
        + "<a href='https://www.facebook.com'>👍<br>Facebook</a>"
        + "<a href='https://github.com'>💻<br>GitHub</a>"
        + "<a href='https://www.prothomalo.com'>📰<br>প্রথম আলো</a>"
        + "</div></body></html>";

    final ArrayList<WebView> tabs = new ArrayList<WebView>();
    int ci = -1;
    FrameLayout root, holder;
    EditText url;
    ProgressBar bar;
    Button tabBtn;
    View customView;
    WebChromeClient.CustomViewCallback customCb;

    int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    WebView cur() {
        return tabs.get(ci);
    }

    Button btn(String t, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(t);
        b.setTextColor(Color.WHITE);
        b.setTextSize(20);
        b.setAllCaps(false);
        b.setBackgroundColor(Color.TRANSPARENT);
        b.setStateListAnimator(null);
        b.setMinWidth(0);
        b.setMinimumWidth(0);
        b.setMinHeight(0);
        b.setMinimumHeight(0);
        b.setPadding(0, 0, 0, 0);
        b.setOnClickListener(l);
        return b;
    }

    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        getWindow().setStatusBarColor(Color.parseColor(BG));
        root = new FrameLayout(this);
        root.setBackgroundColor(Color.parseColor(BG));

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(dp(4), dp(4), dp(4), dp(4));
        top.setBackgroundColor(Color.parseColor(BG));

        top.addView(btn("‹", new View.OnClickListener() {
            public void onClick(View v) { if (cur().canGoBack()) cur().goBack(); }
        }), new LinearLayout.LayoutParams(dp(34), dp(42)));
        top.addView(btn("›", new View.OnClickListener() {
            public void onClick(View v) { if (cur().canGoForward()) cur().goForward(); }
        }), new LinearLayout.LayoutParams(dp(34), dp(42)));
        top.addView(btn("⟳", new View.OnClickListener() {
            public void onClick(View v) { cur().reload(); }
        }), new LinearLayout.LayoutParams(dp(34), dp(42)));
        top.addView(btn("⌂", new View.OnClickListener() {
            public void onClick(View v) { load(cur(), HOME); }
        }), new LinearLayout.LayoutParams(dp(34), dp(42)));

        url = new EditText(this);
        url.setSingleLine(true);
        url.setTextColor(Color.WHITE);
        url.setHintTextColor(Color.parseColor("#7FA5A0"));
        url.setHint("সার্চ করুন বা ঠিকানা লিখুন");
        url.setTextSize(14);
        url.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        url.setImeOptions(EditorInfo.IME_ACTION_GO);
        url.setSelectAllOnFocus(true);
        GradientDrawable g = new GradientDrawable();
        g.setColor(Color.parseColor("#0B1D31"));
        g.setCornerRadius(dp(21));
        url.setBackground(g);
        url.setPadding(dp(16), 0, dp(16), 0);
        url.setOnEditorActionListener(new android.widget.TextView.OnEditorActionListener() {
            public boolean onEditorAction(android.widget.TextView tv, int actionId, android.view.KeyEvent ev) {
                go(url.getText().toString());
                return true;
            }
        });
        LinearLayout.LayoutParams ulp = new LinearLayout.LayoutParams(0, dp(42), 1f);
        ulp.setMargins(dp(4), 0, dp(4), 0);
        top.addView(url, ulp);

        tabBtn = btn("1", new View.OnClickListener() {
            public void onClick(View v) { showTabs(); }
        });
        tabBtn.setTextSize(13);
        GradientDrawable tg = new GradientDrawable();
        tg.setColor(Color.TRANSPARENT);
        tg.setStroke(dp(2), Color.WHITE);
        tg.setCornerRadius(dp(6));
        tabBtn.setBackground(tg);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(dp(30), dp(28));
        tlp.setMargins(dp(4), 0, dp(6), 0);
        top.addView(tabBtn, tlp);

        bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(100);
        bar.setVisibility(View.GONE);
        bar.setProgressTintList(ColorStateList.valueOf(Color.parseColor(AC)));

        holder = new FrameLayout(this);

        col.addView(top, new LinearLayout.LayoutParams(-1, -2));
        col.addView(bar, new LinearLayout.LayoutParams(-1, dp(3)));
        col.addView(holder, new LinearLayout.LayoutParams(-1, 0, 1f));
        root.addView(col, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);

        String d = getIntent() != null ? getIntent().getDataString() : null;
        newTab(d != null ? d : HOME);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String d = intent.getDataString();
        if (d != null) newTab(d);
    }

    void load(WebView w, String u) {
        if (HOME.equals(u)) {
            w.loadDataWithBaseURL("https://mumin.local/", HOME_HTML, "text/html", "utf-8", null);
        } else {
            w.loadUrl(u);
        }
    }

    void newTab(String u) {
        WebView w = new WebView(this);
        setup(w);
        tabs.add(w);
        holder.addView(w, new FrameLayout.LayoutParams(-1, -1));
        select(tabs.size() - 1);
        load(w, u);
    }

    void select(int i) {
        for (int j = 0; j < tabs.size(); j++) {
            tabs.get(j).setVisibility(j == i ? View.VISIBLE : View.GONE);
        }
        ci = i;
        WebView w = tabs.get(i);
        setUrlText(w.getUrl());
        tabBtn.setText(String.valueOf(tabs.size()));
        bar.setVisibility(w.getProgress() < 100 ? View.VISIBLE : View.GONE);
        bar.setProgress(w.getProgress());
        w.requestFocus();
    }

    void closeTab(int i) {
        if (tabs.size() == 1) {
            load(tabs.get(0), HOME);
            return;
        }
        WebView w = tabs.remove(i);
        holder.removeView(w);
        w.destroy();
        select(Math.min(i, tabs.size() - 1));
    }

    void showTabs() {
        ArrayList<String> items = new ArrayList<String>();
        for (int i = 0; i < tabs.size(); i++) {
            String t = tabs.get(i).getTitle();
            if (t == null || t.length() == 0) t = "নতুন ট্যাব";
            items.add((i == ci ? "● " : "○ ") + t);
        }
        items.add("＋ নতুন ট্যাব");
        new AlertDialog.Builder(this)
            .setTitle("ট্যাব")
            .setItems(items.toArray(new String[0]), new android.content.DialogInterface.OnClickListener() {
                public void onClick(android.content.DialogInterface dlg, int which) {
                    if (which == tabs.size()) newTab(HOME);
                    else select(which);
                }
            })
            .setNeutralButton("এই ট্যাব বন্ধ", new android.content.DialogInterface.OnClickListener() {
                public void onClick(android.content.DialogInterface dlg, int which) {
                    closeTab(ci);
                }
            })
            .show();
    }

    void setUrlText(String u) {
        if (url.hasFocus()) return;
        if (u == null || u.startsWith("https://mumin.local") || u.startsWith("data:") || u.equals("about:blank")) {
            url.setText("");
        } else {
            url.setText(u);
        }
    }

    void go(String q) {
        q = q.trim();
        if (q.length() == 0) return;
        String u;
        if (q.matches("^[a-zA-Z][a-zA-Z0-9+.-]*://.*")) u = q;
        else if (!q.contains(" ") && q.contains(".")) u = "https://" + q;
        else u = "https://www.google.com/search?q=" + Uri.encode(q);
        cur().loadUrl(u);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(url.getWindowToken(), 0);
        url.clearFocus();
        cur().requestFocus();
    }

    void hideCustom() {
        if (customView == null) return;
        root.removeView(customView);
        customView = null;
        if (customCb != null) customCb.onCustomViewHidden();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    void setup(final WebView w) {
        WebSettings s = w.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setSupportZoom(true);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setUserAgentString(s.getUserAgentString().replace("; wv", "").replace(" Version/4.0", ""));
        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.setAcceptThirdPartyCookies(w, true);

        w.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                String u = r.getUrl().toString();
                if (u.startsWith("http://") || u.startsWith("https://") || u.startsWith("about:") || u.startsWith("data:")) {
                    return false;
                }
                try {
                    startActivity(Intent.parseUri(u, Intent.URI_INTENT_SCHEME));
                } catch (Exception e) {
                    // কোনো অ্যাপ নেই
                }
                return true;
            }

            @Override
            public void onPageStarted(WebView v, String u, Bitmap f) {
                if (ci >= 0 && v == cur()) setUrlText(u);
            }
        });

        w.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView v, int p) {
                if (ci >= 0 && v == cur()) {
                    bar.setProgress(p);
                    bar.setVisibility(p >= 100 ? View.GONE : View.VISIBLE);
                }
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback cb) {
                if (customView != null) {
                    cb.onCustomViewHidden();
                    return;
                }
                customView = view;
                customCb = cb;
                root.addView(view, new FrameLayout.LayoutParams(-1, -1));
                getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            }

            @Override
            public void onHideCustomView() {
                hideCustom();
            }
        });

        w.setDownloadListener(new android.webkit.DownloadListener() {
            public void onDownloadStart(String u, String ua, String cd, String mime, long len) {
                try {
                    DownloadManager.Request r = new DownloadManager.Request(Uri.parse(u));
                    r.setMimeType(mime);
                    String ck = CookieManager.getInstance().getCookie(u);
                    if (ck != null) r.addRequestHeader("Cookie", ck);
                    if (ua != null) r.addRequestHeader("User-Agent", ua);
                    r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(u, cd, mime));
                    ((DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE)).enqueue(r);
                    Toast.makeText(MainActivity.this, "ডাউনলোড শুরু হয়েছে", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "ডাউনলোড করা যায়নি", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (customView != null) {
            hideCustom();
        } else if (cur().canGoBack()) {
            cur().goBack();
        } else if (tabs.size() > 1) {
            closeTab(ci);
        } else {
            super.onBackPressed();
        }
    }
}
