
function CreateProjectStructureOrchestrator(projectSavedCallback) {
    const getTechnicalUsersUrl = "get-technical-users";
    const createProjectStructureUrl = "create-project-structure";

    let state = new CreateProjectStructureState();
    let view = new CreateProjectStructureView();
    let technicalUsers = [];

    // the orchestration is started
    this.start = function() {
        view.registerEvents(this);
        refreshView();
        loadTechnicalUsers();
    };

    // a project field update is handled
    this.updateProjectField = function(fieldName, value) {
        if (state.updateProjectField(fieldName, value)) {
            view.hideMessages();
            refreshView();
        }
    };

    // the structure phase request is handled
    this.enterStructurePhase = function() {
        let validationResult = state.enterStructurePhase();

        view.hideMessages();
        if (!validationResult.valid) {
            view.showHeaderError(validationResult.errors);
        }
        refreshView();
    };

    // a WP creation request is handled
    this.addWP = function() {
        if (state.addWP()) {
            view.hideMessages();
            refreshView();
        }
    };

    // a WP removal request is handled
    this.removeWP = function(wpIndex) {
        if (state.removeWP(wpIndex)) {
            view.hideMessages();
            refreshView();
        }
    };

    // a WP field update is handled
    this.updateWPField = function(wpIndex, fieldName, value) {
        if (state.updateWPField(wpIndex, fieldName, value)) {
            view.hideMessages();
            refreshView();
        }
    };

    // a task creation request is handled
    this.addTask = function(wpIndex) {
        if (state.addTask(wpIndex)) {
            view.hideMessages();
            refreshView();
        }
    };

    // a task removal request is handled
    this.removeTask = function(wpIndex, taskIndex) {
        if (state.removeTask(wpIndex, taskIndex)) {
            view.hideMessages();
            refreshView();
        }
    };

    // a task move request is handled
    this.moveTask = function(sourceWPIndex, sourceTaskIndex, destinationWPIndex, destinationTaskIndex) {
        if (state.moveTask(sourceWPIndex, sourceTaskIndex, destinationWPIndex, destinationTaskIndex)) {
            view.hideMessages();
            refreshView();
        }
    };

    // a task field update is handled
    this.updateTaskField = function(wpIndex, taskIndex, fieldName, value) {
        if (state.updateTaskField(wpIndex, taskIndex, fieldName, value)) {
            view.hideMessages();
            refreshView();
        }
    };

    // an undo request is handled
    this.undoDraft = function() {
        if (state.undo()) {
            view.hideMessages();
            refreshView();
        }
    };

    // a redo request is handled
    this.redoDraft = function() {
        if (state.redo()) {
            view.hideMessages();
            refreshView();
        }
    };

    // the project structure save request is handled
    this.saveProjectStructure = function() {
        let validationResult = state.validateProjectStructure();
        let projectData;

        view.hideMessages();
        if (!validationResult.valid) {
            view.showStructureError(validationResult.errors);
            return;
        }

        projectData = buildProjectPayload(state.getState().projectData);
        makeJsonCall("POST", createProjectStructureUrl, projectData, function(request) {
            if (request.readyState === XMLHttpRequest.DONE) {
                if (request.status === 200) {
                    state.reset();
                    refreshView();
                    view.showStructureSuccess("Project structure saved successfully");
                    if (projectSavedCallback) {
                        projectSavedCallback(request.responseText);
                    }
                } else if (request.status === 403) {
                    window.location.href = request.getResponseHeader("Location");
                } else {
                    view.showStructureError(request.responseText);
                }
            }
        });
    };

    // the view is refreshed
    function refreshView() {
        view.show(state.getState(), technicalUsers, state.canUndo(), state.canRedo());
    }

    // technical users are loaded
    function loadTechnicalUsers() {
        makeCall("GET", getTechnicalUsersUrl, null, function(request) {
            if (request.readyState === XMLHttpRequest.DONE) {
                if (request.status === 200) {
                    technicalUsers = JSON.parse(request.responseText);
                    refreshView();
                } else if (request.status === 403) {
                    window.location.href = request.getResponseHeader("Location");
                } else {
                    view.showHeaderError(request.responseText);
                }
            }
        });
    }

    // the backend payload is built
    function buildProjectPayload(projectData) {
        let payload = {
            title: projectData.title,
            durationMonths: Number(projectData.durationMonths),
            managedBy: Number(projectData.managedBy),
            workPackages: []
        };
        let wp;
        let task;
        let i;
        let j;

        for (i = 0; i < projectData.workPackages.length; i++) {
            wp = projectData.workPackages[i];
            payload.workPackages.push({
                title: wp.title,
                startMonth: Number(wp.startMonth),
                endMonth: Number(wp.endMonth),
                tasks: []
            });

            for (j = 0; j < wp.tasks.length; j++) {
                task = wp.tasks[j];
                payload.workPackages[i].tasks.push({
                    title: task.title,
                    description: task.description,
                    startMonth: Number(task.startMonth),
                    endMonth: Number(task.endMonth)
                });
            }
        }

        return payload;
    }
}
