package com.example.plugin.InAppBrowserXwalk;

import com.example.plugin.InAppBrowserXwalk.BrowserDialog;

import android.content.res.Resources;

import org.apache.cordova.*;
import org.apache.cordova.PluginManager;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import org.json.JSONStringer;
import org.xwalk.core.XWalkView;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkCookieManager;

import android.os.Bundle;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import android.webkit.ValueCallback;

import android.util.Log;

public class InAppBrowserXwalk extends CordovaPlugin {

    private BrowserDialog dialog;
    private XWalkView xWalkWebView;
    private XWalkView navigationWebView;
    private CallbackContext callbackContext;

    public static final String LOG_TAG = "InAppBrowserXwalk";

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        Log.d(LOG_TAG, "Action " + action);

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

        if (action.equals("injectScriptCode")) {
            this.injectJS(data.getString(0), callbackContext);
        }

        if (action.equals("loadUrl")) {
            this.loadUrl(data.getString(0));
        }

        return true;
    }

    class MyResourceClient extends XWalkResourceClient {
        MyResourceClient(XWalkView view) {
            super(view);
        }

        @Override
        public void onLoadStarted(XWalkView view, String url) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("type", "loadstart");
                obj.put("url", url);
                PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);
            } catch (JSONException ex) {
            }
        }

        @Override
        public void onLoadFinished(XWalkView view, String url) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("type", "loadstop");
                obj.put("url", url);
                PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);
            } catch (JSONException ex) {
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(XWalkView view, String url) {
            if (url.startsWith("bun2card:")) {
                onLoadStarted(view, url);
                return true;
            }
            return super.shouldOverrideUrlLoading(view, url);
        }
    }

    private void openBrowser(final JSONArray data) throws JSONException {
        final String url = data.getString(0);
        this.cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog = new BrowserDialog(cordova.getActivity(), android.R.style.Theme_NoTitleBar);
                xWalkWebView = new XWalkView(cordova.getActivity(), cordova.getActivity());
                navigationWebView = new XWalkView(cordova.getActivity(), cordova.getActivity());

                String overrideUserAgent = preferences.getString("OverrideUserAgent", null);
                String appendUserAgent = preferences.getString("AppendUserAgent", null);
                if (overrideUserAgent != null) {
                    xWalkWebView.setUserAgentString(overrideUserAgent);
                }
                if (appendUserAgent != null) {
                    xWalkWebView.setUserAgentString(xWalkWebView.getUserAgentString() + appendUserAgent);
                }

                XWalkCookieManager mCookieManager = new XWalkCookieManager();
                mCookieManager.setAcceptCookie(true);
                mCookieManager.setAcceptFileSchemeCookies(true);

                xWalkWebView.setResourceClient(new MyResourceClient(xWalkWebView));
                xWalkWebView.load(url, "");

                navigationWebView.setResourceClient(new MyResourceClient(navigationWebView));
                navigationWebView.load("file:///android_asset/www/navigation.html", "");


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

                LinearLayout main = new LinearLayout(cordova.getActivity());
                main.setOrientation(LinearLayout.VERTICAL);

                navigationHeight = (int) (navigationHeight * Resources.getSystem().getDisplayMetrics().density);
                xWalkWebView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, (float) 1));
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

    public void injectJS(final String source, CallbackContext callbackContext) {
        final CallbackContext cbContext = callbackContext;
        String jsWrapper = "(function(){return [eval(%s)];})()";
        JSONArray jsonEsc = new org.json.JSONArray();
        jsonEsc.put(source);
        String jsonRepr = jsonEsc.toString();
        String jsonSourceString = jsonRepr.substring(1, jsonRepr.length() - 1);
        final String finalScriptToInject = String.format(jsWrapper, jsonSourceString);

        Log.d(LOG_TAG, "Inject JS: " + finalScriptToInject);
        this.cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                xWalkWebView.evaluateJavascript(finalScriptToInject, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String scriptResult) {
                        Log.d(LOG_TAG, "Inject JS result: " + scriptResult);
                        PluginResult result;
                        try {
                            JSONArray jsonArray = new JSONArray(scriptResult);
                            result = new PluginResult(PluginResult.Status.OK, jsonArray);
                        } catch (JSONException e) {
                            result = new PluginResult(PluginResult.Status.OK, scriptResult);
                        }
                        result.setKeepCallback(true);
                        cbContext.sendPluginResult(result);
                    }
                });
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
