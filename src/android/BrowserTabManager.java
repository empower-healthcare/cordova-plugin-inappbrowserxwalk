package com.jonathanreisdorf.plugin.InAppBrowserXwalk;

import com.jonathanreisdorf.plugin.InAppBrowserXwalk.BrowserResourceClient;

import java.util.ArrayList;
import java.util.ListIterator;

import org.apache.cordova.CallbackContext;
import org.xwalk.core.XWalkView;

import android.app.Activity;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;


public class BrowserTabManager {
    private ArrayList<XWalkView> tabs = new ArrayList<>();
    private ArrayList<BrowserResourceClient> resourceClients = new ArrayList<>();
    // private ListIterator<XWalkView> tabsIterator = tabs.listIterator();
    private XWalkView currentTab = null;
    private BrowserResourceClient currentResourceClient = null;

    private CallbackContext callbackContext;
    private XWalkView navigationWebView;

    private Activity activity;


    BrowserTabManager(Activity activity, CallbackContext callbackContext, XWalkView navigationWebView) {
        this.activity = activity;
        this.callbackContext = callbackContext;
        this.navigationWebView = navigationWebView;
    }

    public XWalkView addTab(String url) {
        return this.addTab(url, null, true, true);
    }

    public XWalkView addTab(String url, boolean registerTab) {
        return this.addTab(url, null, registerTab, true);
    }

    public XWalkView addTab(String url, String customUserAgentString, boolean registerTab, boolean openTab) {
        XWalkView xWalkWebView = new XWalkView(this.activity, this.activity);
        BrowserResourceClient browserResourceClient = new BrowserResourceClient(xWalkWebView, this.callbackContext, this.navigationWebView);

        xWalkWebView.setResourceClient(browserResourceClient);
        xWalkWebView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, (float) 1));

        if (customUserAgentString != null && customUserAgentString != "") {
            xWalkWebView.setUserAgentString(customUserAgentString);
        }

        if (url != null && url != "") {
            xWalkWebView.load(url, "");
        }

        if (registerTab) {
            this.tabs.add(xWalkWebView);
            this.resourceClients.add(browserResourceClient);
        }

        if (this.currentResourceClient != null && openTab) {
            this.currentResourceClient.isActive = false;
        }

        if (this.currentTab != null && openTab) {
            this.currentTab.stopLoading();
        }

        if (this.currentTab == null || openTab) {
            this.currentTab = xWalkWebView;
            this.currentResourceClient = browserResourceClient;

            browserResourceClient.isActive = true;
        }

        if (openTab) {
            this.openTab();
        }

        return xWalkWebView;
    }

    private void openTab() {
        // TODO attach view to main and remove old view
    }

    private void openLastTab() {
        // TODO use or remove
        int tabsSize = this.tabs.size();
        this.currentTab = this.tabs.get(tabsSize - 1);
    }
}