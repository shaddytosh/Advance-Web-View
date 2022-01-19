package com.sam.afritech;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.material.navigation.NavigationView;


import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    WebView webView;
    TextView textView;
    DrawerLayout drawer;
    Button retrybtn;
    AdView adView;
    private InterstitialAd interstitialAd;
    private boolean exit = false;
    private  String url="https://www.afritechmedia.com/blog/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        webView = findViewById(R.id.webview);
        textView = findViewById(R.id.textview);
        retrybtn = findViewById(R.id.retrybtn);

        checkConnection(); //method to check connection

        //Runtime External storage permission for saving download files
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED) {
                Log.d("permission", "permission denied to WRITE_EXTERNAL_STORAGE - requesting it");
                String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissions, 1);
            }
        }

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront();

        retrybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkConnection();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_facebook) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/AfritechMedia")));

        }
        if (id == R.id.action_twitter) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.twitter.com/AfritechMedia")));

        }
        if (id == R.id.refresh) {
            if (isOnline()){
                webView.loadUrl(webView.getUrl());
        } else {
                Toast.makeText(this, "Can't Connect to Internet.", Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void openWhatsappHelp() {
        String number = getString(R.string.whatsapp_help_number);
        String message= "Hello Afritech Media Admin, please assist me with the following issue:";
        try{
            PackageManager packageManager = getApplicationContext().getPackageManager();
            Intent whatsappIntent = new Intent(Intent.ACTION_VIEW);
            String url = "https://api.whatsapp.com/send?phone="+ number +"&text=" + URLEncoder.encode(message, "UTF-8");
            whatsappIntent.setPackage("com.whatsapp");
            whatsappIntent.setData(Uri.parse(url));
            if (whatsappIntent.resolveActivity(packageManager) != null) {
                startActivity(whatsappIntent);
            }else {
                Toast.makeText(getApplicationContext(), R.string.please_install_whatsapp, Toast.LENGTH_SHORT).show();
            }
        } catch(Exception e) {
            Toast.makeText(getApplicationContext(), R.string.please_install_whatsapp, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
                if (id == R.id.nav_social_media) {
                    url="https://www.afritechmedia.com/category/social-media/";
                    checkConnection();
                } else if (id == R.id.nav_how_to) {
                    url="https://www.afritechmedia.com/category/how-to/";
                    checkConnection();
                } else if (id == R.id.nav_financial) {
                    url="https://www.afritechmedia.com/category/financial-tech/";
                    checkConnection();
                } else if (id == R.id.nav_gadget) {
                    url="https://www.afritechmedia.com/category/gadget-review/";
                    checkConnection();
                } else if (id == R.id.nav_afritech) {
                    url = "https://www.afritechmedia.com/our-services/";
                    checkConnection();
                }else if (id == R.id.nav_home) {
                        url="https://www.afritechmedia.com/blog/";
                        checkConnection();}

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(webView.canGoBack()) {
                webView.goBack();
            }else {
                if(exit) {
                    next();
                    finish();
                }else {
                    Toast.makeText(MainActivity.this, "Press again to exit.", Toast.LENGTH_SHORT).show();
                }
                Timer timer=new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        exit=false;
                    }
                },2000);
                exit=true;
            }
        }
    }

    protected boolean isOnline(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    private void checkConnection() {
        if (isOnline()){
            webView.loadUrl(url);
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webView.getSettings().setLoadsImagesAutomatically(true);
            webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
            webView.setWebViewClient(new WebViewClient());
            webView.setWebViewClient(new Browser_Home());
            webView.setWebChromeClient(new ChromeClient());
            webSettings.setAllowFileAccess(true);
            webSettings.setAppCacheEnabled(true);

            //handle downloading
            webView.setDownloadListener(new DownloadListener() {
                @Override
                public void onDownloadStart(String url, String userAgent,
                                            String contentDisposition, String mimeType,
                                            long contentLength) {
                    DownloadManager.Request request = new DownloadManager.Request(
                            Uri.parse(url));
                    request.setMimeType(mimeType);
                    String cookies = CookieManager.getInstance().getCookie(url);
                    request.addRequestHeader("cookie", cookies);
                    request.addRequestHeader("User-Agent", userAgent);
                    request.setDescription("Downloading File...");
                    request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType));
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(
                            Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(
                                    url, contentDisposition, mimeType));
                    DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    dm.enqueue(request);
                    Toast.makeText(getApplicationContext(), "Downloading File", Toast.LENGTH_LONG).show();
                }});

            adView=findViewById(R.id.adView);

            AdRequest adRequest= new AdRequest.Builder().build();
            adView.loadAd(adRequest);

            interstitialAd=new InterstitialAd(this);
            interstitialAd.setAdUnitId("https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js?client=ca-pub-9511103180749288"); //Enter your adunit ID here
            interstitialAd.loadAd(new AdRequest.Builder().build());
            interstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    finish();
                    interstitialAd.loadAd(new AdRequest.Builder().build());
                }
            });
            textView.setVisibility(View.INVISIBLE);
            retrybtn.setEnabled(false);
            retrybtn.setVisibility(View.INVISIBLE);
        }
        else {
            Toast.makeText(this, "Can't Connect to Internet.", Toast.LENGTH_SHORT).show();
            textView.setText("                      No Connection! \n Please check your internet connection.");
            retrybtn.setEnabled(true);
        }
    }

    public void next(){
        if(interstitialAd.isLoaded()) {
            interstitialAd.show();

        }else {
            finish();
        }
    }

    //fullscreen videos
    private static class Browser_Home extends WebViewClient {
        Browser_Home(){}
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }
    }

    private class ChromeClient extends WebChromeClient {
        private View customview;
        private WebChromeClient.CustomViewCallback customviewcallback;
        private int originalorientation;
        private int originalsystemvisibility;

        ChromeClient() {}

        public Bitmap getDefaultVideoPoster() {
            if (customview == null) {
                return null;
            }
            return BitmapFactory.decodeResource(getApplicationContext().getResources(), 2130837573);
        }

        public void onHideCustomView() {
            ((FrameLayout)getWindow().getDecorView()).removeView(this.customview);
            this.customview = null;
            getWindow().getDecorView().setSystemUiVisibility(this.originalsystemvisibility);
            setRequestedOrientation(this.originalorientation);
            this.customviewcallback.onCustomViewHidden();
            this.customviewcallback = null;
        }

        public void onShowCustomView(View paramView, WebChromeClient.CustomViewCallback paramCustomViewCallback) {
            if (this.customview != null){
                onHideCustomView();
                return;
            }
            this.customview = paramView;
            this.originalsystemvisibility = getWindow().getDecorView().getSystemUiVisibility();
            this.originalorientation = getRequestedOrientation();
            this.customviewcallback = paramCustomViewCallback;
            ((FrameLayout)getWindow().getDecorView()).addView(this.customview, new FrameLayout.LayoutParams(-1, -1));
            getWindow().getDecorView().setSystemUiVisibility(3846 | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }
}
