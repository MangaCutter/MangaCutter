function supportedService(srvc) {
    return srvc.includes("w11.mangafreak.net");
}

browser.tabs.query({currentWindow: true, active: true}).then(function (tabs) {
    let tab = tabs[0];
    let serviceUrl = tab.url;
    if (supportedService(serviceUrl)) {
        let b = document.createElement("button");
        b.innerText = "Download chapter";
        b.onclick = async function () {
            browser.tabs.executeScript(tab.id, {file: "/js/mangafreak.js"});
        }
        document.body.appendChild(b);
    } else {
        let a = document.createElement("h3");
        a.innerText = serviceUrl;
        document.body.appendChild(a);
    }
});


