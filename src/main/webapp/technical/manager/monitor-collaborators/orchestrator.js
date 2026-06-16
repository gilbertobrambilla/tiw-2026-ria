
function MonitorCollaboratorsOrchestrator() {
    const getManagedCollaboratorsUrl = "get-managed-collaborators";
    const getCollaboratorMonitoringUrl = "get-collaborator-monitoring";

    let view = new MonitorCollaboratorsView();

    // the orchestration is started
    this.start = function() {
        view.registerEvents(this);
        this.reloadCollaborators();
    };

    // managed collaborators are reloaded
    this.reloadCollaborators = function() {
        view.hideError();
        makeCall("GET", getManagedCollaboratorsUrl, null, function(request) {
            if (request.readyState === XMLHttpRequest.DONE) {
                if (request.status === 200) {
                    view.showCollaborators(JSON.parse(request.responseText));
                } else if (request.status === 403) {
                    redirectToLogin(request);
                } else {
                    view.showError(request.responseText);
                }
            }
        });
    };

    // the selected collaborator report is loaded
    this.loadCollaboratorReport = function(collaboratorId) {
        view.hideError();
        makeCall("GET", getCollaboratorMonitoringUrl + "?collaboratorId=" + encodeURIComponent(collaboratorId), null, function(request) {
            if (request.readyState === XMLHttpRequest.DONE) {
                if (request.status === 200) {
                    view.showCollaboratorReport(JSON.parse(request.responseText));
                } else if (request.status === 403) {
                    redirectToLogin(request);
                } else {
                    view.showError(request.responseText);
                }
            }
        });
    };
}
