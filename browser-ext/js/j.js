var socke = new WebSocket("ws://127.0.0.1:50000");
try {
    console.log(socke.readyState);
    socke.onopen = function (event) {
        console.log("connected");
        socke.send("1");
        // socke.close();
    }
    // while()

    // socke.send("1");
    // socke.close();
} catch (ex) {
    console.log(ex);
}

function onclickf() {
    socke.send("1");
}