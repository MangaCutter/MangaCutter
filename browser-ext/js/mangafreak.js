console.log("execution started");
let urls = [];
let query = document.querySelectorAll("img#gohere");
if (query.length === 0) {
    alert("This page is not a chapter");
} else {
    query.forEach(function (item) {
        urls.push(item.src);
    });
    browser.runtime.sendMessage({
        "type": "download-chapter",
        "data": urls
    });
}
/*
(function () {
    let img = document.querySelector('img');
    console.log(img);
    img.setAttribute('crossOrigin', 'anonymous');

// make <canvas> of the same size
    let canvas = document.createElement('canvas');
    canvas.width = img.clientWidth;
    canvas.height = img.clientHeight;

    let context = canvas.getContext('2d');

// copy image to it (this method allows to cut image)
    context.drawImage(img, 0, 0);
// we can context.rotate(), and do many other things on canvas

// toBlob is async opereation, callback is called when done
    canvas.toBlob(function (blob) {
        // blob ready, download it
        let link = document.createElement('a');
        link.download = 'example.png';

        link.href = URL.createObjectURL(blob);
        link.click();

        // delete the internal blob reference, to let the browser clear memory from it
        URL.revokeObjectURL(link.href);
    }, 'image/png');
})();
*/
