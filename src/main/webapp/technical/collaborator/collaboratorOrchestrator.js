function CollaboratorOrchestrator() {
    const getTimesheetUrl = "get-timesheet";
    const updateWorkedHoursUrl = "update-worked-hours";

    let view = new CollaboratorView();
    let timesheet = null;
    let selectedTaskId = null;

    // the orchestration is started
    this.start = function() {
        view.registerEvents(this);
        this.reloadTimesheet();
    };

    // the collaborator timesheet is reloaded
    this.reloadTimesheet = function() {
        reloadTimesheet(null);
    };

    // the selected task timesheet is loaded
    this.loadTaskTimesheet = function(taskId) {
        let taskData;

        view.hideMessages();
        selectedTaskId = taskId;
        taskData = findTask(taskId);

        if (taskData === null) {
            view.hideCollaboratorTimesheet();
            view.showAssignedProjectsError("Unable to load the selected task timesheet.");
            return;
        }

        view.showAssignedProjects(timesheet.projects, selectedTaskId);
        view.showCollaboratorTimesheet(taskData);
    };

    // the worked-hours update request is handled
    this.updateWorkedHours = function(form) {
        view.hideMessages();
        makeCall("POST", updateWorkedHoursUrl, form, function(request) {
            if (request.readyState === XMLHttpRequest.DONE) {
                if (request.status === 200) {
                    reloadTimesheet(request.responseText);
                } else if (request.status === 403 && request.getResponseHeader("Location") !== null) {
                    redirectToLogin(request);
                } else {
                    view.showTimesheetError(request.responseText);
                }
            }
        }, false);
    };

    // the collaborator timesheet data is loaded
    function reloadTimesheet(successMessage) {
        let taskData;

        view.hideMessages();
        makeCall("GET", getTimesheetUrl, null, function(request) {
            if (request.readyState === XMLHttpRequest.DONE) {
                if (request.status === 200) {
                    timesheet = JSON.parse(request.responseText);
                    view.showAssignedProjects(timesheet.projects, selectedTaskId);

                    if (selectedTaskId !== null) {
                        taskData = findTask(selectedTaskId);
                        if (taskData !== null) {
                            view.showCollaboratorTimesheet(taskData);
                        } else {
                            selectedTaskId = null;
                            view.hideCollaboratorTimesheet();
                        }
                    }

                    if (successMessage !== null) {
                        view.showTimesheetSuccess(successMessage);
                    }
                } else if (request.status === 403 && request.getResponseHeader("Location") !== null) {
                    redirectToLogin(request);
                } else {
                    view.showAssignedProjectsError(request.responseText);
                }
            }
        });
    }

    // a task is searched
    function findTask(taskId) {
        let project;
        let wp;
        let task;
        let i;
        let j;
        let k;

        if (timesheet === null) {
            return null;
        }

        for (i = 0; i < timesheet.projects.length; i++) {
            project = timesheet.projects[i];
            for (j = 0; j < project.workPackages.length; j++) {
                wp = project.workPackages[j];
                for (k = 0; k < wp.tasks.length; k++) {
                    task = wp.tasks[k];
                    if (String(task.id) === String(taskId)) {
                        return {
                            project: project,
                            wp: wp,
                            task: task
                        };
                    }
                }
            }
        }

        return null;
    }
}

(function() {
    let collaboratorOrchestrator;

    // the page is initialized after loading
    window.addEventListener("load", function() {
        collaboratorOrchestrator = new CollaboratorOrchestrator();
        collaboratorOrchestrator.start();
    }, false);
})();
