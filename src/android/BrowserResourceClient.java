package com.jonathanreisdorf.plugin.InAppBrowserXwalk;

import org.json.JSONObject;
import org.json.JSONException;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

import org.xwalk.core.XWalkView;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkNavigationHistory;
import org.xwalk.core.XWalkNavigationItem;



class BrowserResourceClient extends XWalkResourceClient {

    private CallbackContext callbackContext;
    private XWalkView navigationWebView;
    private String navigationFileUrl;

    BrowserResourceClient(XWalkView view, CallbackContext callbackContext, XWalkView navigationWebView) {
        super(view);

        this.callbackContext = callbackContext;
        this.navigationWebView = navigationWebView;
        this.navigationFileUrl = navigationWebView.getUrl();
    }

    @Override
    public void onLoadStarted(XWalkView view, String url) {
        if (url.equals(this.navigationFileUrl)) {
            return;
        }

        try {
            JSONObject obj = new JSONObject();
            this.addNavigationItemDetails(view, obj);
            obj.put("type", "loadstart");
            obj.put("url", url);
            PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
            result.setKeepCallback(true);
            this.callbackContext.sendPluginResult(result);

            this.onNavigationEvent(obj);
        } catch (JSONException ex) {
        }
    }

    @Override
    public void onLoadFinished(XWalkView view, String url) {
        if (url.equals(this.navigationFileUrl)) {
            return;
        }

        try {
            JSONObject obj = new JSONObject();
            this.addNavigationItemDetails(view, obj);
            obj.put("type", "loadstop");
            obj.put("url", url);
            PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
            result.setKeepCallback(true);
            this.callbackContext.sendPluginResult(result);

            this.onNavigationEvent(obj);
        } catch (JSONException ex) {
        }
    }

    @Override
    public void onProgressChanged(XWalkView view, int progressInPercent) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("type", "loadprogress");
            obj.put("progress", progressInPercent);
            PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
            result.setKeepCallback(true);
            this.callbackContext.sendPluginResult(result);

            this.onNavigationEvent(obj);
        } catch (JSONException ex) {
        }
    }

    public JSONObject addNavigationItemDetails(XWalkView view, JSONObject obj) {
        XWalkNavigationHistory navigationHistory = view.getNavigationHistory();

        if (navigationHistory.size() < 1) {
            return obj;
        }

        XWalkNavigationItem navigationItem = navigationHistory.getCurrentItem();

        try {
            obj.put("navigationUrl", navigationItem.getUrl());
            obj.put("navigationOriginalUrl", navigationItem.getOriginalUrl());
            obj.put("navigationTitle", navigationItem.getTitle());
        } catch (JSONException ex) {
        }

        return obj;
    }

    public void onNavigationEvent(JSONObject obj) {
        this.navigationWebView.evaluateJavascript("javascript:window.onNavigationEvent && window.onNavigationEvent(" + obj + ")", null);
    }
}
