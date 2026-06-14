/*
 * Shared AJAX utilities.
 */

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
