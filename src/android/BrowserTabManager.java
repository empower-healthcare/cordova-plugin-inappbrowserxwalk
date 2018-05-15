package com.jonathanreisdorf.plugin.InAppBrowserXwalk;

import com.jonathanreisdorf.plugin.InAppBrowserXwalk.BrowserResourceClient;

import java.util.ArrayList;
import java.util.ListIterator;

import org.apache.cordova.CallbackContext;
import org.xwalk.core.XWalkView;

import android.app.Activity;
import android.graphics.Color;
import android.view.ViewParent;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;


public class BrowserTabManager {
    private ArrayList<XWalkView> tabs = new ArrayList<>();
    private ArrayList<BrowserResourceClient> resourceClients = new ArrayList<>();
    // private ListIterator<XWalkView> tabsIterator = tabs.listIterator();
    private XWalkView currentTab = null;
    private XWalkView previousTab = null;
    private BrowserResourceClient currentResourceClient = null;
    private BrowserResourceClient previousResourceClient = null;

    private Activity activity;
    private LinearLayout mainLayout;
    private CallbackContext callbackContext;
    private XWalkView navigationWebView;


    BrowserTabManager(Activity activity, LinearLayout mainLayout, CallbackContext callbackContext, XWalkView navigationWebView) {
        this.activity = activity;
        this.mainLayout = mainLayout;
        this.callbackContext = callbackContext;
        this.navigationWebView = navigationWebView;
    }

    public XWalkView initialize(String url) {
        return this.addTab(url, null, false, false);
    }

    public XWalkView addTab(String url) {
        return this.addTab(url, null, false, true);
    }

    public XWalkView addTab(String url, boolean systemTab) {
        return this.addTab(url, null, systemTab, true);
    }

    public XWalkView addTab(String url, String customUserAgentString, boolean systemTab, boolean openTab) {
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

        if (systemTab) {
            xWalkWebView.setBackgroundColor(Color.BLACK);
            browserResourceClient.isSystem = true;
        } else {
            this.tabs.add(xWalkWebView);
            this.resourceClients.add(browserResourceClient);
        }

        if (this.currentTab == null) {
            this.currentTab = xWalkWebView;
            this.currentResourceClient = browserResourceClient;
            browserResourceClient.isActive = true;
        }

        if (openTab) {
            this.openTab(xWalkWebView, browserResourceClient);
        }

        return xWalkWebView;
    }

    public void closeSystemTab() {
        this.openTab(this.previousTab, this.previousResourceClient);
        this.previousTab.onDestroy();
    }

    private void openTab(final XWalkView newTab, final BrowserResourceClient newResourceClient) {
        this.previousTab = this.currentTab;
        this.previousResourceClient = this.currentResourceClient;

        this.currentTab = newTab;
        this.currentResourceClient = newResourceClient;

        if (this.previousTab != null && this.previousResourceClient != null) {
            this.previousTab.stopLoading();
            this.previousResourceClient.isActive = false;
        }

        this.currentResourceClient.isActive = true;
        this.currentResourceClient.broadcastNavigationItemDetails(this.currentTab);

        this.mainLayout.removeViewAt(0);
        this.mainLayout.addView(this.currentTab, 0);
        this.mainLayout.invalidate();
    }

    /*private void openLastTab() {
        // TODO use or remove
        int tabsSize = this.tabs.size();
        this.currentTab = this.tabs.get(tabsSize - 1);
    }*/

    public void load(String url) {
        this.currentTab.load(url, "");
    }
}
