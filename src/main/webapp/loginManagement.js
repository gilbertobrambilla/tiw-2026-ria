/*
 * Login page management.
 */

(function() {
    const CONFIG = {
        loginUrl: "login",
        adminHomeUrl: "admin/home-admin.html",
        roles: {
            admin: "admin",
            technical: "technical"
        },
        cssClasses: {
            hidden: "hidden"
        },
        storageKeys: {
            firstName: "firstName",
            lastName: "lastName"
        }
    };

    let loginPanel = document.getElementById("loginForm");
    let loginButton = document.getElementById("loginButton");
    let technicalRoleSelector = document.getElementById("technicalRoleSelector");
    let errorContainer = document.getElementById("loginFormError");

    loginButton.addEventListener("click", function(event) {
        let form = event.target.closest("form");
        errorContainer.textContent = "";
        errorContainer.classList.add(CONFIG.cssClasses.hidden);

        if (!form.checkValidity()) {
            form.reportValidity();
            return;
        }

        makeCall("POST", CONFIG.loginUrl, form, function(request) {
            if (request.readyState === 4) {
                if (request.status === 200) {
                    let user = JSON.parse(request.responseText);

                    sessionStorage.setItem(CONFIG.storageKeys.firstName, user.firstName);
                    sessionStorage.setItem(CONFIG.storageKeys.lastName, user.lastName);
                    form.reset();

                    if (user.role === CONFIG.roles.admin) {
                        window.location.href = CONFIG.adminHomeUrl;
                    } else if (user.role === CONFIG.roles.technical) {
                        loginPanel.classList.add(CONFIG.cssClasses.hidden);
                        technicalRoleSelector.classList.remove(CONFIG.cssClasses.hidden);
                    }
                } else {
                    errorContainer.textContent = request.responseText;
                    errorContainer.classList.remove(CONFIG.cssClasses.hidden);
                }
            }
        }, false);
    }, false);
})();
