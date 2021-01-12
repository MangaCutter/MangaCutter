function getURLS() {
    let urls = [];
    let query = document.querySelectorAll("img");
    query.forEach(function (item) {
        if (item.src === "") {
            urls.push(item.getAttribute("data"))
        }
        urls.push(item.src);
    });
    return urls;
}

chrome.runtime.sendMessage({
    "type": "su",
    "data": getURLS()
});