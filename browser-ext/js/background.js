browser.storage.local.set({connectionStatus: false});
browser.storage.local.set({proxyStatus: false});

let sock;
createWebSocket();
let sendQueue = [];

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
    }
}

function createWebSocket() {
    sock = new WebSocket("ws://localhost:50000");
    sock.onerror = onWebSocketError;
    sock.onmessage = onWebSocketMessage;
    sock.onopen = onWebSocketOpen;
    sock.onclose = onWebSocketClose;
}

browser.runtime.onMessage.addListener(function (message) {
    if (message.type === "send-to-server") {
        console.log("send to server");
        message.data.forEach(function (item) {
            sendMsgToServer(item);
        });
    }
    if (message.type === "download-chapter") {
        sendMsgToServer("dc " + JSON.stringify(message.data));
    }
});

function handleProxyRequest(requestInfo) {
    console.log(`Proxying: ${requestInfo.url}`);
    return {type: "http", host: "127.0.0.1", port: 50001};
}

browser.proxy.onRequest.addListener(handleProxyRequest, {urls: ["*://*.mangafreak.net/*"]/*, types: ["image"]*/});

function requestProxyStatus() {
    sendMsgToServer("ps");
}

setInterval(requestProxyStatus, 5000);

// browser.runtime.onMessage.addListener(listener);
//
// function listener(data) {
//     console.log("1");
//     browser.tabs.create({url: data.url}).then(onCreated, onError);
// }