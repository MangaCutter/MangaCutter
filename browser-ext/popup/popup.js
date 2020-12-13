let downloadChapterButton = document.getElementById("downloadChapter");
let downloadImagesButton = document.getElementById("downloadImages");
let toggleProxyButton = document.getElementById("toggleProxy");
let clientConnected = false;
let proxyEnabled = false;

function setConnectionLabelStatus(status) {
    let connectionLabel = document.getElementById("clientStatus");
    if (status === true) {
        connectionLabel.className = "ok";
        connectionLabel.innerText = "Connected to client";
    } else {
        connectionLabel.className = "error";
        connectionLabel.innerText = "Can't connect to client";
    }
}

function setProxyLabelStatus(status) {
    let proxyLabel = document.getElementById("proxyStatus");
    if (status === true) {
        proxyLabel.className = "ok";
        proxyLabel.innerText = "Proxy enabled";
        toggleProxyButton.innerText = "Disable proxy";
    } else {
        proxyLabel.className = "error";
        proxyLabel.innerText = "Proxy disabled";
        toggleProxyButton.innerText = "Enable proxy";
    }
}

function gotProxyStatus(item) {
    setProxyLabelStatus(item.proxyStatus);
    if (!proxyEnabled && item.proxyStatus) {
        browser.tabs.query({currentWindow: true, active: true}).then(function (tabs) {
            let tab = tabs[0];
            let serviceUrl = tab.url;
            let ss = supportedService(serviceUrl);
            if (ss !== "") {
                downloadChapterButton.onclick = () => browser.tabs.executeScript(tab.id, {file: ss});
                downloadChapterButton.removeAttribute("disabled");
            } else {
                downloadChapterButton.setAttribute("disabled", "");
            }
            downloadImagesButton.removeAttribute("disabled");
        });
    }
    if (proxyEnabled && !item.proxyStatus) {
        downloadChapterButton.setAttribute("disabled", "");
        downloadImagesButton.setAttribute("disabled", "");
    }
    proxyEnabled = item.proxyStatus;
}

function gotConnectionStatus(item) {
    setConnectionLabelStatus(item.connectionStatus);
    clientConnected = item.connectionStatus;
    if (clientConnected) {
        toggleProxyButton.removeAttribute("disabled");
    } else {
        toggleProxyButton.setAttribute("disabled", "");
    }
}

function onError(event) {
    console.log(event);
}

function supportedService(srvc) {
    let serviceURL = new URL(srvc);
    if (serviceURL.host.includes("w11.mangafreak.net"))
        return "/js/mangafreak.js";
    return "";
}

function loadConnectionStatus() {
    browser.storage.local.get("connectionStatus").then(gotConnectionStatus, onError);
}

function loadProxyStatus() {
    browser.storage.local.get("proxyStatus").then(gotProxyStatus, onError);
}

loadConnectionStatus();
loadProxyStatus();
setInterval(loadConnectionStatus, 5000);
setInterval(loadProxyStatus, 5000);

browser.tabs.query({currentWindow: true, active: true}).then(function (tabs) {
    let tab = tabs[0];
    let serviceUrl = tab.url;
    let ss = supportedService(serviceUrl);
    if (ss !== "") {
        downloadChapterButton.onclick = () => browser.tabs.executeScript(tab.id, {file: ss});
        downloadChapterButton.removeAttribute("disabled");
        downloadImagesButton.removeAttribute("disabled");
    } else {
        downloadChapterButton.setAttribute("disabled", "");
    }
});

