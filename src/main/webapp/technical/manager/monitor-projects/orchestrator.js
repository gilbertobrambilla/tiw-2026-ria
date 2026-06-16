
function MonitorProjectsOrchestrator(projectConcludedCallback) {
    const getMonitorableProjectsUrl = "get-monitorable-projects";
    const getProjectMonitoringUrl = "get-project-monitoring";
    const concludeProjectUrl = "conclude-project";

    let view = new MonitorProjectsView();

    // the orchestration is started
    this.start = function() {
        view.registerEvents(this);
        this.reloadProjects();
    };

    // monitorable projects are reloaded
    this.reloadProjects = function() {
        view.hideMessages();
        makeCall("GET", getMonitorableProjectsUrl, null, function(request) {
            if (request.readyState === XMLHttpRequest.DONE) {
                if (request.status === 200) {
                    view.showProjects(JSON.parse(request.responseText));
                } else if (request.status === 403) {
                    redirectToLogin(request);
                } else {
                    view.showProjectListError(request.responseText);
                }
            }
        });
    };

    // the selected project report is loaded
    this.loadProjectReport = function(projectId) {
        view.hideMessages();
        makeCall("GET", getProjectMonitoringUrl + "?projectId=" + encodeURIComponent(projectId), null, function(request) {
            if (request.readyState === XMLHttpRequest.DONE) {
                if (request.status === 200) {
                    view.showProjectReport(JSON.parse(request.responseText));
                } else if (request.status === 403) {
                    redirectToLogin(request);
                } else {
                    view.showProjectListError(request.responseText);
                }
            }
        });
    };

    // the project conclusion request is handled
    this.concludeProject = function(form) {
        view.hideMessages();
        makeCall("POST", concludeProjectUrl, form, function(request) {
            if (request.readyState === XMLHttpRequest.DONE) {
                if (request.status === 200) {
                    view.showConcludeSuccess(request.responseText);
                    if (projectConcludedCallback) {
                        projectConcludedCallback();
                    }
                } else if (request.status === 403) {
                    redirectToLogin(request);
                } else {
                    view.showConcludeError(request.responseText);
                }
            }
        }, false);
    };
}
