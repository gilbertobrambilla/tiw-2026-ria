
function makeCall(method, url, formElement, callback, reset = true) {
    let request = new XMLHttpRequest();

    request.onreadystatechange = function() {
        callback(request);
    };

    request.open(method, url);

    if (formElement === null) {
        request.send();
    } else {
        request.send(new FormData(formElement));
        if (reset === true) {
            formElement.reset();
        }
    }
}

function makeJsonCall(method, url, data, callback) {
    let request = new XMLHttpRequest();

    request.onreadystatechange = function() {
        callback(request);
    };

    request.open(method, url);
    request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    request.send(JSON.stringify(data));
}
