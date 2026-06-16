
function VerifyProjectsOrchestrator() {
    const getProjectsUrl = "get-projects";
    const getProjectStructureUrl = "get-project-structure";

    let view = new VerifyProjectsView();

    // the orchestration is started
    this.start = function() {
        view.registerEvents(this);
        this.reloadProjects();
    };

    // the project list is reloaded
    this.reloadProjects = function() {
        view.hideError();
        makeCall("GET", getProjectsUrl, null, function(request) {
            if (request.readyState === XMLHttpRequest.DONE) {
                if (request.status === 200) {
                    view.showProjects(JSON.parse(request.responseText));
                } else if (request.status === 403) {
                    redirectToLogin(request);
                } else {
                    view.showError(request.responseText);
                }
            }
        });
    };

    // the selected project structure is loaded
    this.loadProjectStructure = function(projectId) {
        view.hideError();
        makeCall("GET", getProjectStructureUrl + "?projectId=" + encodeURIComponent(projectId), null, function(request) {
            if (request.readyState === XMLHttpRequest.DONE) {
                if (request.status === 200) {
                    view.showProjectStructure(JSON.parse(request.responseText));
                } else if (request.status === 403) {
                    redirectToLogin(request);
                } else {
                    view.showError(request.responseText);
                }
            }
        });
    };
}
