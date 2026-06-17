
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

function redirectToLogin(request) {
    sessionStorage.removeItem("firstName");
    sessionStorage.removeItem("lastName");
    window.location.href = request.getResponseHeader("Location");
}

function showStoredUserGreeting(greetingElementId) {
    let greetingElement = document.getElementById(greetingElementId);
    let firstName = sessionStorage.getItem("firstName");
    let lastName = sessionStorage.getItem("lastName");

    if (greetingElement === null || firstName === null || lastName === null) {
        return;
    }

    firstName = firstName.trim();
    lastName = lastName.trim();
    if (firstName === "" || lastName === "") {
        return;
    }

    greetingElement.textContent = "Welcome, " + firstName + " " + lastName;
}
