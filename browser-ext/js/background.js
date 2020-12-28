browser.storage.local.set({connectionStatus: false});

let sock;
let sendQueue = [];
let busyTabs = [];
let proxyStatus = false;

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

function onWebSocketMessage(event) {
    let request = event.data;
    if (request.indexOf("open ") === 0) {
        browser.tabs.create({url: request.substr(5)}).catch(onError);
    } else if (request.indexOf("done ") === 0) {
        let tab = request.substr(5);
        for (let i = 0; i < busyTabs.length; i++) {
            if (busyTabs[i].tab === tab) {
                busyTabs.splice(i, 1);
                break;
            }
        }
        if (busyTabs.length === 0) {
            disableProxy();
            sendMsgToServer("cl");
        }
    } else if (request.indexOf("refresh ") === 0) {
        let tab = request.substr(8);
        for (let i = 0; i < busyTabs.length; i++) {
            if (busyTabs[i].tab === tab) {
                if (busyTabs[i].attempts < 5) {
                    browser.tabs.reload(busyTabs[i].tab, {bypassCache: true}).then(browser.tabs.executeScript(busyTabs[i].tab, {file: busyTabs[i].file}));
                    busyTabs[i].attempts++;
                } else {
                    sendMsgToServer("alert browser.plugin.BrowserPlugin.onMessage.too_many_attempts");
                    busyTabs.splice(i, 1);
                }
            }
        }
    }
}

function createWebSocket() {
    sock = new WebSocket("ws://localhost:50000");
    sock.onerror = onWebSocketClose;
    sock.onmessage = onWebSocketMessage;
    sock.onopen = onWebSocketOpen;
    sock.onclose = onWebSocketClose;
}

async function enableProxy() {
    await browser.proxy.settings.get({}).then(async (settings) => {
        if (settings.levelOfControl === "controllable_by_this_extension" ||
            settings.levelOfControl === "controlled_by_this_extension") {
            await browser.proxy.settings.set({
                value: {
                    proxyType: "manual",
                    http: "http://127.0.0.1:50001",
                    httpProxyAll: true
                }
            }).then(() => proxyStatus = true, (e) => console.log(e));
        }
    }, (e) => console.log(e));
}

function disableProxy() {
    browser.proxy.settings.clear({}).then(() => proxyStatus = false);
}

createWebSocket();

browser.runtime.onMessage.addListener(async (message, sender) => {
    if (message.type === "dc") {
        if (!proxyStatus)
            await enableProxy();
        if (proxyStatus) {
            busyTabs.push({tab: message.tab, file: message.file, attempts: 0});
            browser.tabs.reload(message.tab, {bypassCache: true}).then(browser.tabs.executeScript(message.tab, {file: message.file}));
        } //note: else branch is situated in enableProxy method
    }
    if (message.type === "su") {
        sendMsgToServer("su " + sender.tab + "\t" + message.data);
    }
});
