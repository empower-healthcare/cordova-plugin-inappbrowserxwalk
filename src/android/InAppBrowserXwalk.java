package com.jonathanreisdorf.plugin.InAppBrowserXwalk;

import com.jonathanreisdorf.plugin.InAppBrowserXwalk.BrowserDialog;
import com.jonathanreisdorf.plugin.InAppBrowserXwalk.BrowserResourceClient;
import com.jonathanreisdorf.plugin.InAppBrowserXwalk.BrowserTabManager;

import android.content.res.Resources;

import org.apache.cordova.*;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import org.json.JSONStringer;
import org.xwalk.core.XWalkView;
import org.xwalk.core.XWalkCookieManager;
import org.xwalk.core.JavascriptInterface;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import android.webkit.ValueCallback;

public class InAppBrowserXwalk extends CordovaPlugin {

    private String navigationFileUrl = "file:///android_asset/www/navigation.html";

    private BrowserDialog dialog;
    private XWalkView xWalkWebView;
    private XWalkView navigationWebView;
    private CallbackContext callbackContext;
    private BrowserTabManager browserTabManager;

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
        if (action.equals("open")) {
            this.callbackContext = callbackContext;
            this.openBrowser(data);
        }

        if (action.equals("close")) {
            this.closeBrowser();
        }

        if (action.equals("show")) {
            this.showBrowser();
        }

        if (action.equals("hide")) {
            this.hideBrowser();
        }

        if (action.equals("loadUrl")) {
            this.loadUrl(data.getString(0));
        }

        return true;
    }

    class NavigationJsInterface {
        NavigationJsInterface() {}

        @JavascriptInterface
        public void openUrl(final String url) {
            loadUrl(url);
        }
    }

    private void openBrowser(final JSONArray data) throws JSONException {
        final String url = data.getString(0);
        final Activity activity = this.cordova.getActivity();

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog = new BrowserDialog(activity, android.R.style.Theme_NoTitleBar);
                navigationWebView = new XWalkView(activity, activity);
                browserTabManager = new BrowserTabManager(activity, callbackContext, navigationWebView);
                xWalkWebView = browserTabManager.addTab(url, null, true);

                XWalkCookieManager mCookieManager = new XWalkCookieManager();
                mCookieManager.setAcceptCookie(true);
                mCookieManager.setAcceptFileSchemeCookies(true);

                navigationWebView.setResourceClient(new BrowserResourceClient(navigationWebView, callbackContext, navigationWebView));
                navigationWebView.addJavascriptInterface(new NavigationJsInterface(), "navigation");
                navigationWebView.load(navigationFileUrl, "");

                int navigationHeight = 40;
                boolean openHidden = false;

                if (data != null && data.length() > 1) {
                    try {
                        JSONObject options = new JSONObject(data.getString(1));

                        if (!options.isNull("navigationHeight")) {
                            navigationHeight = options.getInt("navigationHeight");
                        }
                        if (!options.isNull("openHidden")) {
                            openHidden = options.getBoolean("openHidden");
                        }
                    } catch (JSONException ex) {
                    }
                }

                LinearLayout main = new LinearLayout(activity);
                main.setOrientation(LinearLayout.VERTICAL);

                navigationHeight = (int) (navigationHeight * Resources.getSystem().getDisplayMetrics().density);
                navigationWebView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, navigationHeight, (float) 0));

                main.addView(xWalkWebView);
                main.addView(navigationWebView);

                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(false);
                dialog.addContentView(main, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

                if (!openHidden) {
                    dialog.show();
                }
            }
        });
    }

    public void hideBrowser() {
        this.cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dialog != null) {
                    dialog.hide();
                }
            }
        });
    }

    public void showBrowser() {
        this.cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dialog != null) {
                    dialog.show();
                }
            }
        });
    }

    public void loadUrl(final String url) {
        this.cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                xWalkWebView.load(url, "");
            }
        });
    }

    public void closeBrowser() {
        this.cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                xWalkWebView.onDestroy();
                dialog.dismiss();
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("type", "exit");
                    PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
                    result.setKeepCallback(true);
                    callbackContext.sendPluginResult(result);
                } catch (JSONException ex) {
                }
            }
        });
    }

    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = new Bundle();
        return state;
    }

    @Override
    public void onRestoreStateForActivityResult(Bundle state, CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
    }
}
