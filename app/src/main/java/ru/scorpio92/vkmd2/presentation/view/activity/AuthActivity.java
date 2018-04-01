package ru.scorpio92.vkmd2.presentation.view.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.View;
import android.widget.ProgressBar;

import com.tbruyelle.rxpermissions2.RxPermissions;

import ru.scorpio92.vkmd2.App;
import ru.scorpio92.vkmd2.R;
import ru.scorpio92.vkmd2.presentation.view.activity.base.AbstractActivity;
import ru.scorpio92.vkmd2.presentation.view.activity.base.IAuthActivity;
import ru.scorpio92.vkmd2.presentation.view.webview.CustomWebView;
import ru.scorpio92.vkmd2.presentation.view.webview.CustomWebViewClient;
import ru.scorpio92.vkmd2.tools.LocalStorage;

import static ru.scorpio92.vkmd2.BuildConfig.AUDIO_URL;


public class AuthActivity extends AbstractActivity implements IAuthActivity {

    public static final String FORCE_SYNC_KEY = "force_sync";

    private CustomWebView webView;
    private ProgressBar progress;
    private LinearLayoutCompat errorContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        checkPermission();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        App.finish();
    }

    @Override
    public Context getViewContext() {
        return this;
    }

    @Override
    public void showWebView() {
        webView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showSyncActivity(String cookie) {
        if (LocalStorage.fileExist(this, LocalStorage.IS_NOT_FIRST_RUN))
            startActivity(new Intent(this, SyncActivity.class));
        else
            startActivity(new Intent(this, SyncActivity.class).putExtra(SyncActivity.AUTO_RUN_KEY, true));
        finish();
    }

    @Override
    public void showMusicActivity() {
        startActivity(new Intent(this, MusicActivity.class));
        finish();
    }

    @Override
    public void showProgress(boolean show) {
        webView.setVisibility(View.GONE);
        errorContainer.setVisibility(View.GONE);
        progress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private CustomWebViewClient.WebViewClientCallback webViewClientCallback = new CustomWebViewClient.WebViewClientCallback() {
        @Override
        public void onAuthPageLoaded() {
            showWebView();
        }

        @Override
        public void onPageBeginLoading() {
            showProgress(true);
        }

        @Override
        public void onAudioPageFinishLoad(String cookie) {
            try {
                LocalStorage.setDataInFile(AuthActivity.this, LocalStorage.COOKIE_STORAGE, cookie);
                showSyncActivity(cookie);
            } catch (Exception e) {
                onError();
            }
        }

        @Override
        public void onError() {
            showProgress(false);
            errorContainer.setVisibility(View.VISIBLE);
        }
    };

    private void initUI() {
        webView = findViewById(R.id.webView);
        webView.registerWebViewClientCallback(webViewClientCallback);

        progress = findViewById(R.id.progress);
        progress.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark), PorterDuff.Mode.MULTIPLY);

        errorContainer = findViewById(R.id.errorContainer);

        findViewById(R.id.retryBtn).setOnClickListener(v -> {
            showProgress(true);
            v.postDelayed(() -> webView.loadUrl(AUDIO_URL), 1000);
        });

        webView.setVisibility(View.GONE);
        errorContainer.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);

        if (needSync())
            webView.loadUrl(AUDIO_URL);
        else
            showMusicActivity();
    }

    private void checkPermission() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions
                .request(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        ((App) getApplication()).init();
                        initUI();
                    } else {
                        showToast("Для работы приложению необходимо предоставить нужные разрешения");
                        finish();
                    }
                });
    }

    private boolean needSync() {
        if (!LocalStorage.fileExist(this, LocalStorage.COOKIE_STORAGE))
            return true;

        boolean forceSync = getIntent().getBooleanExtra(FORCE_SYNC_KEY, false);
        boolean activeMode = PreferenceManager.getDefaultSharedPreferences(AuthActivity.this).getBoolean(SettingsActivity.ACTIVE_KEY, false);

        return forceSync || activeMode;
    }
}
