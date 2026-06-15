(function() {
    const loginUrl = "login";
    const adminHomeUrl = "admin/home-admin.html";
    const roles = {
        admin: "admin",
        technical: "technical"
    };

    let loginPanel = document.getElementById("loginForm-box");
    let loginButton = document.getElementById("loginForm-loginButton");
    let technicalRoleSelector = document.getElementById("technicalRoleSelector-box");
    let errorContainer = document.getElementById("loginForm-error");

    loginButton.addEventListener("click", function(event) {
        let form = event.target.closest("form");
        errorContainer.textContent = "";
        errorContainer.classList.add("hidden");

        if (!form.checkValidity()) {
            form.reportValidity();
            return;
        }

        makeCall("POST", loginUrl, form, function(request) {
            if (request.readyState === 4) {
                if (request.status === 200) {
                    let user = JSON.parse(request.responseText);

                    sessionStorage.setItem("firstName", user.firstName);
                    sessionStorage.setItem("lastName", user.lastName);
                    form.reset();

                    if (user.role === roles.admin) {
                        window.location.href = adminHomeUrl;
                    } else if (user.role === roles.technical) {
                        loginPanel.classList.add("hidden");
                        technicalRoleSelector.classList.remove("hidden");
                    }
                } else {
                    errorContainer.textContent = request.responseText;
                    errorContainer.classList.remove("hidden");
                }
            }
        }, false);
    }, false);
})();
