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
    private ListIterator<XWalkView> tabsIterator = tabs.listIterator();
    private XWalkView currentTab = null;

    private CallbackContext callbackContext;
    private XWalkView navigationWebView;

    private Activity activity;


    BrowserTabManager(Activity activity, CallbackContext callbackContext, XWalkView navigationWebView) {
        this.activity = activity;
        this.callbackContext = callbackContext;
        this.navigationWebView = navigationWebView;
    }

    public XWalkView addTab(String url, String customUserAgentString, boolean openHidden) {
        XWalkView xWalkWebView = new XWalkView(this.activity, this.activity);
        xWalkWebView.setResourceClient(new BrowserResourceClient(xWalkWebView, this.callbackContext, this.navigationWebView));
        xWalkWebView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, (float) 1));

        if (customUserAgentString != null && customUserAgentString != "") {
            xWalkWebView.setUserAgentString(customUserAgentString);
        }

        if (url != null && url != "") {
            xWalkWebView.load(url, "");
        }

        this.tabs.add(xWalkWebView);

        if (this.currentTab == null) {
            this.currentTab = this.tabsIterator.next();
        }

        if (openHidden == false) {

        }

        return xWalkWebView;
    }

    private void openTab(int index) {
        // this.currentTab = this.tabsIterator.get(index);
        // TODO attach view to main and remove old view
    }

    private void openLastTab() {
        int tabsSize = this.tabs.size();
        this.openTab(tabsSize - 1);
    }
}
