# cordova-plugin-inappbrowserxwalk
- forked from empower-healthcare/cordova-plugin-inappbrowserxwalk
  - forked from mcierzniak/cordova-plugin-inappbrowserxwalk
    - forked from EternallLight/cordova-plugin-inappbrowserxwalk
      - forked from Shoety/cordova-plugin-inappbrowserxwalk


## So, what does this fork change?
The biggest change (yet) is that instead of creating a java toolbar with very limited things
to customize it now loads a local navigation.html file where you can create a toolbar with
html, css & javascript which then can communicate with this plugin via the
`navigation` object available in its javascript context.
If you create a global `onNavigationEvent` function there, events
like `loadstart`, `loadstop` and `loadprogress` are fetchable there.
Also the fork adds a loadUrl method to the browser to change the URL from the outside and
removes the script injection possibility and some other specific code.

A working example making use of it can be found here: https://github.com/jonathan-reisdorf/bb10-minimalistic-browser

# General description

This plugin provides a web browser view, which is using the Crosswalk engine
to render pages.

##Requirements
This plugin requires Cordova build with Crosswalk.


## Installation

    $ cordova plugin add https://github.com/jonathan-reisdorf/cordova-plugin-inappbrowserxwalk

## Methods

### open
```js
const browser = window.inAppBrowserXwalk.open(url, options);
```
Opens a new Crosswalk Webview in a dialog. The options parameter is optional, possible parameters
are demonstrated under Examples. If you dont pass options, the browser will open with default options.

### close
```js
browser.close()
```
Closes the browser and destroys dialog and webview.

### hide
```js
browser.hide()
```
Hides the browser but does not destroy it. You can call the show() function to make the browser
visible again.

### show
```js
browser.show()
```
Will make a hidden browser visible. Use this after browser.hide() or if the browser was opened
with the openHidden : true option.

### addEventListener
```js
browser.addEventListener(eventname, callback)
```
Adds a listener for an event from the crosswalk browser.
Possible events: loadstart, loadstop, loadprogress, exit

### removeEventListener
```js
browser.removeEventListener(eventname)
```
Removes the eventlistener for an event.

### loadUrl
```js
browser.loadUrl(url);
```

## Examples
```js
const options = {
  navigationHeight: 40, // optional
  openHidden: false // optional
};

document.addEventListener('deviceready', () => {
  if (!window.cordova || !window.inAppBrowserXwalk || cordova.platformId !== 'android') {
    return window.open('https://google.de', '_blank');
  }

  const browser = window.inAppBrowserXwalk.open('https://google.de', options);

  browser.addEventListener('loadstart', data => {
    console.log(data);
  });

  browser.addEventListener('loadstop', data => {
    console.log(data);
  });

  browser.addEventListener('loadprogress', data => {
    console.log(data);
  });

  browser.addEventListener('exit', () => {
    console.log('browser closed');
  });
}, false);
```

`options` is an optional argument, leaving it out opens the browser with standard settings.
