chrome.storage.local.set({connectionStatus: false});

let sock;
let sendQueue = [];
let busyTabs = [];
let proxyStatus = false;
let usersProxySettings;

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
    chrome.storage.local.set({connectionStatus: true});
    console.log("web socket opened");
    sendQueueToServer();
}

function onWebSocketClose() {
    chrome.storage.local.set({connectionStatus: false});
    disableProxy();
    setTimeout(createWebSocket, 2000);
}

function onWebSocketMessage(event) {
    let request = event.data;
    if (request.indexOf("open ") === 0) {
        chrome.tabs.create({url: request.substr(request.indexOf(" ") + 1)}).catch(onError);
    } else if (request.indexOf("completed ") === 0 || request.indexOf("canceled ") === 0) {
        console.log(request);
        let tab = request.substr(request.indexOf(" ") + 1);
        for (let i = 0; i < busyTabs.length; i++) {
            if ((busyTabs[i].tab + "") === tab) {
                busyTabs.splice(i, 1);
                break;
            }
        }
        if (busyTabs.length === 0) {
            disableProxy();
            sendMsgToServer("cl");
        }
    } else if (request.indexOf("refresh ") === 0) {
        let tab = request.substr(request.indexOf(" ") + 1);
        console.log("got refresh signal " + tab);
        for (let i = 0; i < busyTabs.length; i++) {
            if ((busyTabs[i].tab + "") === tab) {
                if (busyTabs[i].attempts < 5) {
                    chrome.tabs.reload(busyTabs[i].tab, {bypassCache: false}).then(() => {
                        chrome.tabs.executeScript(busyTabs[i].tab, {file: busyTabs[i].file});
                    });
                    busyTabs[i].attempts++;
                } else {
                    sendMsgToServer("tma " + tab);
                    busyTabs.splice(i, 1);
                    if (busyTabs.length === 0) {
                        disableProxy();
                        sendMsgToServer("cl");
                    }
                }
            }
        }
    }
}

function createWebSocket() {
    sock = new WebSocket("ws://localhost:50000");
    sock.onmessage = onWebSocketMessage;
    sock.onopen = onWebSocketOpen;
    sock.onclose = onWebSocketClose;
}

function handleRequest(requestInfo) {
    return {type: "http", host: "127.0.0.1", port: 50001};
}

async function enableProxy(after) {
    if (typeof browser !== 'undefined') {
        browser.proxy.onRequest.addListener(handleRequest, {urls: ["<all_urls>"]});
        console.log("proxy enabled")
        after();
    } else {
        await chrome.proxy.settings.get({}, async (settings) => {
            if (settings.levelOfControl === "controllable_by_this_extension" ||
                settings.levelOfControl === "controlled_by_this_extension") {
                usersProxySettings = settings.value;
                console.log(usersProxySettings);
                await chrome.proxy.settings.set({
                    scope: "regular",
                    value: {
                        mode: "fixed_servers",
                        rules: {
                            singleProxy: {
                                host: "127.0.0.1",
                                port: 50001,
                                scheme: "http"
                            }
                        }
                    }
                }, () => {
                    proxyStatus = true;
                    console.log("proxy enabled");
                    after();
                });
            }
        });
    }
}

function disableProxy() {
    if (browser !== "undefined") {
        browser.proxy.onRequest.removeListener(handleRequest);
        console.log("proxy disabled");
    } else {
        chrome.proxy.settings.clear({scope: "regular"}, () => {
            proxyStatus = false;
            console.log("proxy disabled");
        });
    }
}

function downloadChapter(message, sender) {
    for (let i = 0; i < busyTabs.length; i++) {
        if (busyTabs[i].tab === message.tab + "") {
            sendMsgToServer("cancel " + message.tab);
            console.log("cancel signal sent " + message.tab);
        }
        busyTabs.splice(i, 1);
        i--;
    }
    busyTabs.push({tab: message.tab, file: message.file, attempts: 0});
    chrome.tabs.reload(message.tab, {bypassCache: true}, () => chrome.tabs.executeScript(message.tab, {file: message.file}));
}

chrome.runtime.onMessage.addListener(async (message, sender) => {
    console.log(message);
    if (message.type === "dc") {
        if (!proxyStatus) {
            await enableProxy(() => downloadChapter(message, sender));
        }
        if (proxyStatus) {
            downloadChapter(message, sender);
        } //note: else branch is situated in enableProxy method
    }
    if (message.type === "su") {
        sendMsgToServer("su " + JSON.stringify({
            tabId: sender.tab.id + "",
            url: sender.tab.url + "",
            data: message.data
        }));
    }
});

createWebSocket();
