browser.runtime.sendMessage({
    "type": "download-chapter",
    "data": getURLS()
});

function getURLS() {
    let urls = [];
    let query = document.querySelectorAll("img#gohere");
    if (query.length === 0) {
        alert("This page is not a chapter");
    } else {
        query.forEach(function (item) {
            urls.push(item.src);
        });
    }
    return urls;
}