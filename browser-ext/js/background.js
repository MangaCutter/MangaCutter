let sock = createWebSocket();

function sendMsgToServer(msg) {
    if (sock.readyState !== sock.OPEN) {
        sock = createWebSocket();
    }
    sock.send(msg);
}

function onCreated(tab) {
    console.log(`Created new tab: ${tab.id}`)
}

function onError(error) {
    console.log(`Error: ${error}`);
    sendMsgToServer(error);
}

function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

async function onWebSocketError(event) {
    if (sock.readyState !== sock.OPEN) {
        await sleep(5000);
        sock = createWebSocket();
    }
}

function onWebSocketMessage(event) {
    let request = event.data;
    if (request.indexOf("open ") === 0) {
        browser.tabs.create({url: request.substr(5)}).then(onCreated, onError);
    }
}

function createWebSocket() {
    let sock = new WebSocket("ws://localhost:50000");
    sock.onerror = onWebSocketError;
    sock.onmessage = onWebSocketMessage;
    return sock;
}

browser.runtime.onMessage.addListener(function (message) {
    console.log("got message");
    console.log(message);
    if (message.type === "send-to-server") {
        console.log("send to server");
        message.data.forEach(function (item) {
            sendMsgToServer(item);
        });
    }
});

function logURL(requestDetails) {
    if (!requestDetails.url.includes("/?f6ae943355d1438bb867a5e9581eea13=")) {
        let url = new URL(requestDetails.url);
        console.log("Before: " + url);
        url = "http://" + url.hostname + "/?f6ae943355d1438bb867a5e9581eea13=" + btoa(requestDetails.url);
        console.log("Changed: " + url);
        return {redirectUrl: url};
    }
    // if (requestDetails.url.startsWith("https")) {
    //     let url = requestDetails.url;
    //     console.log("Before: " + url);
    //     url = url.replace("https", "http");
    //     console.log("Changed: " + url);
    //     return {redirectUrl: url};
    // }
    console.log("Pass unchanged: " + requestDetails.url);
    // url = "http://localhost:50001/?ref=" + btoa(url);
}

// browser.webRequest.onBeforeRequest.addListener(
//     logURL,
//     {urls: ["*://*.mangafreak.net/*"], types: ["image"]},
//     ["blocking"]
// );

function shouldProxyRequest(requestInfo) {
    return requestInfo.parentFrameId !== -1;
}

function handleProxyRequest(requestInfo) {
    // if (shouldProxyRequest(requestInfo)) {
    console.log(`Proxying: ${requestInfo.url}`);
    return {type: "http", host: "127.0.0.1", port: 50001};
    // }
    // return {type: "direct"};
}

browser.proxy.onRequest.addListener(handleProxyRequest, {urls: ["*://*.mangafreak.net/*"]/*, types: ["image"]*/});
// browser.runtime.onMessage.addListener(listener);
//
// function listener(data) {
//     console.log("1");
//     browser.tabs.create({url: data.url}).then(onCreated, onError);
// }