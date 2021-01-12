let downloadChapterButton = document.getElementById("downloadChapter");
let downloadImagesButton = document.getElementById("downloadImages");

function supportedService(srvc) {
    let serviceURL = new URL(srvc);
    if (serviceURL.host.includes("w11.mangafreak.net"))
        return "/js/mangafreak.js";
    return "";
}

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

function onError(event) {
    console.log(event);
}

function loadConnectionStatus() {
    chrome.storage.local.get(['connectionStatus'], (item) => {
        setConnectionLabelStatus(item.connectionStatus);
        if (item.connectionStatus) {
            chrome.tabs.query({currentWindow: true, active: true}, (tabs) => {
                let tab = tabs[0];
                let serviceUrl = tab.url;
                let serviceCSFilepath = supportedService(serviceUrl);
                if (serviceCSFilepath !== "") {
                    downloadChapterButton.onclick = () => chrome.runtime.sendMessage({
                        type: "dc",
                        tab: tab.id,
                        file: serviceCSFilepath
                    });
                    downloadChapterButton.removeAttribute("disabled");
                } else {
                    downloadChapterButton.setAttribute("disabled", "");
                }
                downloadImagesButton.onclick = () => chrome.runtime.sendMessage({
                    type: "dc",
                    tab: tab.id,
                    file: "/js/image_injector.js"
                });
                downloadImagesButton.removeAttribute("disabled");
            });
        } else {
            downloadChapterButton.setAttribute("disabled", "");
            downloadImagesButton.setAttribute("disabled", "");
        }
    });
}

loadConnectionStatus();
setInterval(loadConnectionStatus, 2000);