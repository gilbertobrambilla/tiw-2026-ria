
function TaskAssignmentOrchestrator(projectAssignedCallback) {
    const getAssignableTasksUrl = "get-assignable-tasks";
    const getTaskAssignmentDetailsUrl = "get-task-assignment-details";
    const saveTaskAssignmentUrl = "save-task-assignment";
    const assignProjectUrl = "assign-project";

    let view = new TaskAssignmentView();

    // the orchestration is started
    this.start = function() {
        view.registerEvents(this);
        this.reloadTasks();
    };

    // assignable tasks are reloaded
    this.reloadTasks = function() {
        view.hideMessages();
        view.hideTaskAssignment();
        makeCall("GET", getAssignableTasksUrl, null, function(request) {
            if (request.readyState === XMLHttpRequest.DONE) {
                if (request.status === 200) {
                    view.showTasks(JSON.parse(request.responseText));
                } else if (request.status === 403) {
                    redirectToLogin(request);
                } else {
                    view.showTaskListError(request.responseText);
                }
            }
        });
    };

    // the selected task assignment is loaded
    this.loadTaskAssignment = function(taskId) {
        view.hideMessages();
        makeCall("GET", getTaskAssignmentDetailsUrl + "?taskId=" + encodeURIComponent(taskId), null, function(request) {
            if (request.readyState === XMLHttpRequest.DONE) {
                if (request.status === 200) {
                    view.showTaskAssignment(JSON.parse(request.responseText));
                } else if (request.status === 403) {
                    redirectToLogin(request);
                } else {
                    view.showTaskListError(request.responseText);
                }
            }
        });
    };

    // the task assignment save request is handled
    this.saveTaskAssignment = function(form) {
        view.hideMessages();
        makeCall("POST", saveTaskAssignmentUrl, form, function(request) {
            if (request.readyState === XMLHttpRequest.DONE) {
                if (request.status === 200) {
                    view.showAssignmentSuccess(request.responseText);
                } else if (request.status === 403) {
                    redirectToLogin(request);
                } else {
                    view.showAssignmentError(request.responseText);
                }
            }
        }, false);
    };

    // the project assignment request is handled
    this.assignProject = function(form) {
        view.hideMessages();
        makeCall("POST", assignProjectUrl, form, function(request) {
            if (request.readyState === XMLHttpRequest.DONE) {
                if (request.status === 200) {
                    if (projectAssignedCallback) {
                        projectAssignedCallback();
                    }
                    view.showProjectAssignedSuccess(request.responseText);
                } else if (request.status === 403) {
                    redirectToLogin(request);
                } else {
                    view.showAssignmentError(request.responseText);
                }
            }
        }, false);
    };
}
