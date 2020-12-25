browser.storage.local.set({connectionStatus: false});
browser.storage.local.set({proxyStatus: false});

let sock;
createWebSocket();
let sendQueue = [];
let interceptList = [];

function sendMsgToServer(msg) {
    sendQueue.push(msg);
    sendQueueToServer();
}

function sendQueueToServer() {
    if (sock.readyState === sock.OPEN) {
        for (let message of sendQueue) {
            sock.send(message);
        }
        sendQueue = [];
    }
}

function onError(error) {
    console.log(`Error: ${error}`);
}

function onWebSocketOpen() {
    browser.storage.local.set({connectionStatus: true});
    sendQueueToServer();
}

function onWebSocketClose() {
    browser.storage.local.set({connectionStatus: false});
    setTimeout(createWebSocket, 2000);
}

function onWebSocketError() {
    if (sock.readyState !== sock.OPEN) {
        browser.storage.local.set({connectionStatus: false});
        browser.storage.local.set({proxyStatus: false});
    }
    sock.close();
}

function onWebSocketMessage(event) {
    let request = event.data;
    if (request.indexOf("open ") === 0) {
        browser.tabs.create({url: request.substr(5)}).catch(onError);
    } else if (request.indexOf("ps ") === 0) {
        browser.storage.local.set({proxyStatus: (request.substr(3) === "true")}).catch(onError);
    } else if (request.indexOf("got ") === 0) {
        removeFromInterceptList(request.substr(4));
    }
}

function createWebSocket() {
    sock = new WebSocket("ws://localhost:50000");
    sock.onerror = onWebSocketError;
    sock.onmessage = onWebSocketMessage;
    sock.onopen = onWebSocketOpen;
    sock.onclose = onWebSocketClose;
}

browser.runtime.onMessage.addListener((message, sender) => {
    if (message.type === "send-to-server") {
        console.log("send to server");
        message.data.forEach(function (item) {
            sendMsgToServer(item);
        });
    }
    if (message.type === "download-chapter") {
        addToInterceptList(message.data);
        browser.tabs.reload(sender.tab, {bypassCache: false});
    }
    if (message.type === "download-images") {
        addToInterceptList(message.data);
        browser.tabs.reload(sender.tab, {bypassCache: false});
    }
});

function enableProxy() {
    browser.proxy.settings.get({}).then((settings) => {
        if (settings.levelOfControl === "controllable_by_this_extension" ||
            settings.levelOfControl === "controlled_by_this_extension") {
            browser.proxy.settings.set({
                value: {
                    proxyType: "manual",
                    http: "http://127.0.0.1:50001"
                }
            });
        } else {
            sendMsgToServer("alert browser.plugin.BrowserPlugin.onMessage.browser_proxy_failed_to_control")
        }
    });
}

function disableProxy() {
    browser.proxy.settings.clear({});
}

function addToInterceptList(urls) {
    enableProxy();
    for (const url in urls) {
        interceptList.push(url);
    }
}

function removeFromInterceptList(url) {
    interceptList = interceptList.filter(function (value) {
        return value !== url;
    });
    if (interceptList.length === 0) {
        disableProxy();
    }
}

function requestProxyStatus() {
    sendMsgToServer("ps");
}

setInterval(requestProxyStatus, 5000);