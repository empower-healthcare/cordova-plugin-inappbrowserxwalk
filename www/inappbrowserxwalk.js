/*global cordova, module*/

function InAppBrowserXwalk() {
}

var callbacks = new Array ();

InAppBrowserXwalk.prototype = {
    close: function () {
        cordova.exec(null, null, "InAppBrowserXwalk", "close", []);
    },
    addEventListener: function (eventname, func) {
        callbacks[eventname] = func;
    },
    removeEventListener: function (eventname) {
        callbacks[eventname] = undefined;
    },
    show: function () {
        cordova.exec(null, null, "InAppBrowserXwalk", "show", []);
    },
    hide: function () {
        cordova.exec(null, null, "InAppBrowserXwalk", "hide", []);
    },
    executeScript: function(injectDetails, cb) {
        cordova.exec(cb, null, "InAppBrowserXwalk", "injectScriptCode", [injectDetails, !!cb]);
    }
}

var callback = function(event) {
    if (event.type === "loadstart" && callbacks['loadstart'] !== undefined) {
        callbacks['loadstart'](event);
    }
    if (event.type === "loadstop" && callbacks['loadstop'] !== undefined) {
        callbacks['loadstop'](event);
    }
    if (event.type === "exit" && callbacks['exit'] !== undefined) {
        callbacks['exit']();
    }
    if (event.type === "jsCallback" && callbacks['jsCallback'] !== undefined) {
        callbacks['jsCallback'](event);
    }
}

module.exports = {
    open: function (url, options) {
        options = (options === undefined) ? "{}" : JSON.stringify(options);
        cordova.exec(callback, null, "InAppBrowserXwalk", "open", [url, options]);
        return new InAppBrowserXwalk();
    }
};
