browser.storage.local.set({connectionStatus: false});

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
    browser.storage.local.set({connectionStatus: true});
    console.log("web socket opened");
    sendQueueToServer();
}

function onWebSocketClose() {
    browser.storage.local.set({connectionStatus: false});
    disableProxy();
    setTimeout(createWebSocket, 2000);
}

function onWebSocketMessage(event) {
    let request = event.data;
    if (request.indexOf("open ") === 0) {
        browser.tabs.create({url: request.substr(request.indexOf(" ") + 1)}).catch(onError);
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
                    browser.tabs.reload(busyTabs[i].tab, {bypassCache: false}).then(() => {
                        browser.tabs.executeScript(busyTabs[i].tab, {file: busyTabs[i].file});
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

async function enableProxy() {
    await browser.proxy.settings.get({}).then(async (settings) => {
        if (settings.levelOfControl === "controllable_by_this_extension" ||
            settings.levelOfControl === "controlled_by_this_extension") {
            usersProxySettings = settings.value;
            console.log(usersProxySettings);
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
    // browser.proxy.settings.set({value: usersProxySettings}).then(
    //     () => {
    browser.proxy.settings.clear({}).then((e) => {
        proxyStatus = false;
        console.log("proxy disabled " + e);
    });
    // },
    // (ex) => {
    //     console.log(ex);
    //     browser.proxy.settings.clear({}).then((e) => {
    //         proxyStatus = false;
    //         console.log("proxy disabled " + e);
    //     });
    // }
    // );
}

browser.runtime.onMessage.addListener(async (message, sender) => {

    if (message.type === "dc") {
        if (!proxyStatus) {
            await enableProxy();
        }
        if (proxyStatus) {
            for (let i = 0; i < busyTabs.length; i++) {
                if (busyTabs[i].tab === message.tab + "") {
                    sendMsgToServer("cancel " + message.tab);
                    console.log("cancel signal sent " + message.tab);
                }
                busyTabs.splice(i, 1);
                i--;
            }
            busyTabs.push({tab: message.tab, file: message.file, attempts: 0});
            browser.tabs.reload(message.tab, {bypassCache: false}).then(browser.tabs.executeScript(message.tab, {file: message.file}));
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
