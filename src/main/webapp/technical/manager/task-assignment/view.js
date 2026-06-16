
function TaskAssignmentView() {
    let taskListEmpty = document.getElementById("taskAssignment-taskListEmpty");
    let taskSelector = document.getElementById("taskAssignment-taskSelector");
    let taskSelect = document.getElementById("taskAssignment-taskSelect");
    let loadTaskButton = document.getElementById("taskAssignment-loadTaskButton");
    let taskListError = document.getElementById("taskAssignment-taskListError");
    let assignmentForm = document.getElementById("taskAssignment-taskAssignmentForm");
    let taskSummary = document.getElementById("taskAssignment-taskSummary");
    let taskIdInput = document.getElementById("taskAssignment-taskIdInput");
    let projectIdInput = document.getElementById("taskAssignment-projectIdInput");
    let expectedHoursList = document.getElementById("taskAssignment-expectedHoursList");
    let collaboratorsList = document.getElementById("taskAssignment-collaboratorsList");
    let saveButton = document.getElementById("taskAssignment-saveButton");
    let assignProjectButton = document.getElementById("taskAssignment-assignProjectButton");
    let errorContainer = document.getElementById("taskAssignment-error");
    let successContainer = document.getElementById("taskAssignment-success");
    let projectAssignedSuccessContainer = document.getElementById("taskAssignment-projectAssignedSuccess");

    // view events are registered
    this.registerEvents = function(orchestrator) {
        loadTaskButton.addEventListener("click", function() {
            if (!taskSelector.checkValidity()) {
                taskSelector.reportValidity();
                return;
            }

            orchestrator.loadTaskAssignment(taskSelect.value);
        }, false);

        saveButton.addEventListener("click", function() {
            if (!assignmentForm.checkValidity()) {
                assignmentForm.reportValidity();
                return;
            }

            orchestrator.saveTaskAssignment(assignmentForm);
        }, false);

        assignProjectButton.addEventListener("click", function() {
            if (!assignmentForm.checkValidity()) {
                assignmentForm.reportValidity();
                return;
            }

            orchestrator.assignProject(assignmentForm);
        }, false);
    };

    // the view is reset
    this.reset = function() {
        taskSelect.innerHTML = "";
        assignmentForm.reset();
        taskSummary.textContent = "";
        expectedHoursList.innerHTML = "";
        collaboratorsList.innerHTML = "";
        taskListEmpty.classList.add("hidden");
        taskSelector.classList.add("hidden");
        assignmentForm.classList.add("hidden");
        this.hideMessages();
    };

    // tasks are shown
    this.showTasks = function(tasks) {
        let option;
        let i;

        taskSelect.innerHTML = "";
        assignmentForm.classList.add("hidden");

        option = document.createElement("option");
        option.value = "";
        option.textContent = "-- Select a Task --";
        taskSelect.appendChild(option);

        if (tasks.length === 0) {
            taskListEmpty.classList.remove("hidden");
            taskSelector.classList.add("hidden");
            return;
        }

        taskListEmpty.classList.add("hidden");
        taskSelector.classList.remove("hidden");

        for (i = 0; i < tasks.length; i++) {
            option = document.createElement("option");
            option.value = tasks[i].task.id;
            option.textContent = buildTaskLabel(tasks[i]);
            taskSelect.appendChild(option);
        }
    };

    // the task assignment is shown
    this.showTaskAssignment = function(details) {
        let managedTask = details.managedTask;
        let task = managedTask.task;
        let project = managedTask.project;

        assignmentForm.reset();
        taskIdInput.value = task.id;
        projectIdInput.value = project.id;
        taskSummary.textContent = "Project: " + project.title + " | Task: " + task.title +
            " | Active Phase: Month " + task.startMonth + " to Month " + task.endMonth;

        showExpectedHours(task, details.expectedHours);
        showCollaborators(details.availableCollaborators, details.collaboratorIds);
        assignmentForm.classList.remove("hidden");
    };

    // the task assignment is hidden
    this.hideTaskAssignment = function() {
        assignmentForm.classList.add("hidden");
    };

    // the task list error is shown
    this.showTaskListError = function(message) {
        taskListError.textContent = message;
        taskListError.classList.remove("hidden");
    };

    // the assignment error is shown
    this.showAssignmentError = function(message) {
        errorContainer.textContent = message;
        errorContainer.classList.remove("hidden");
    };

    // the assignment success is shown
    this.showAssignmentSuccess = function(message) {
        successContainer.textContent = message;
        successContainer.classList.remove("hidden");
    };

    // the project assignment success is shown
    this.showProjectAssignedSuccess = function(message) {
        projectAssignedSuccessContainer.textContent = message;
        projectAssignedSuccessContainer.classList.remove("hidden");
    };

    // all messages are hidden
    this.hideMessages = function() {
        taskListError.textContent = "";
        taskListError.classList.add("hidden");
        errorContainer.textContent = "";
        errorContainer.classList.add("hidden");
        successContainer.textContent = "";
        successContainer.classList.add("hidden");
        projectAssignedSuccessContainer.textContent = "";
        projectAssignedSuccessContainer.classList.add("hidden");
    };

    // the task label is built
    function buildTaskLabel(managedTask) {
        return managedTask.project.title + " - WP" + managedTask.wp.sequenceNumber +
            " - T" + managedTask.wp.sequenceNumber + "." + managedTask.task.sequenceNumber +
            " " + managedTask.task.title;
    }

    // expected hours are shown
    function showExpectedHours(task, expectedHours) {
        let expectedHoursByMonth = buildExpectedHoursByMonth(expectedHours);
        let line;
        let label;
        let input;
        let month;

        expectedHoursList.innerHTML = "";

        for (month = task.startMonth; month <= task.endMonth; month++) {
            line = document.createElement("div");
            line.className = "month-hour-line";

            label = document.createElement("label");
            label.className = "month-label";
            label.setAttribute("for", "taskAssignment-expectedHoursM" + month);
            label.textContent = "Month M" + month + ":";
            line.appendChild(label);

            input = document.createElement("input");
            input.id = "taskAssignment-expectedHoursM" + month;
            input.type = "number";
            input.name = "expectedHours_" + month;
            input.min = "0";
            input.required = true;
            input.value = expectedHoursByMonth[month] !== undefined ? expectedHoursByMonth[month] : "";
            line.appendChild(input);

            expectedHoursList.appendChild(line);
        }
    }

    // collaborators are shown
    function showCollaborators(collaborators, selectedCollaboratorIds) {
        let selectedIds = buildSelectedIds(selectedCollaboratorIds);
        let item;
        let input;
        let label;
        let i;

        collaboratorsList.innerHTML = "";

        for (i = 0; i < collaborators.length; i++) {
            item = document.createElement("div");
            item.className = "checkbox-item";

            input = document.createElement("input");
            input.id = "taskAssignment-collaboratorInput" + collaborators[i].id;
            input.type = "checkbox";
            input.name = "collaboratorIds";
            input.value = collaborators[i].id;
            input.checked = selectedIds[collaborators[i].id] === true;
            item.appendChild(input);

            label = document.createElement("label");
            label.setAttribute("for", input.id);
            label.textContent = collaborators[i].firstName + " " + collaborators[i].lastName;
            item.appendChild(label);

            collaboratorsList.appendChild(item);
        }
    }

    // expected hours are mapped by month
    function buildExpectedHoursByMonth(expectedHours) {
        let expectedHoursByMonth = {};
        let i;

        for (i = 0; i < expectedHours.length; i++) {
            expectedHoursByMonth[expectedHours[i].month] = expectedHours[i].expectedHours;
        }

        return expectedHoursByMonth;
    }

    // selected collaborator ids are mapped
    function buildSelectedIds(selectedCollaboratorIds) {
        let selectedIds = {};
        let i;

        for (i = 0; i < selectedCollaboratorIds.length; i++) {
            selectedIds[selectedCollaboratorIds[i]] = true;
        }

        return selectedIds;
    }
}
