function CollaboratorView() {
    let assignedProjectsListEmpty = document.getElementById("assignedProjectsList-empty");
    let assignedProjectsList = document.getElementById("assignedProjectsList-list");
    let assignedProjectsListError = document.getElementById("assignedProjectsList-error");
    let timesheetBox = document.getElementById("collaboratorTimesheet-box");
    let timesheetTitle = document.getElementById("collaboratorTimesheet-title");
    let timesheetProjectTitle = document.getElementById("collaboratorTimesheet-projectTitle");
    let timesheetTaskDescription = document.getElementById("collaboratorTimesheet-taskDescription");
    let timesheetTaskPeriod = document.getElementById("collaboratorTimesheet-taskPeriod");
    let timesheetTotalExpectedHours = document.getElementById("collaboratorTimesheet-totalExpectedHours");
    let timesheetTotalWorkedHours = document.getElementById("collaboratorTimesheet-totalWorkedHours");
    let timesheetTableBody = document.getElementById("collaboratorTimesheet-tableBody");
    let updateForm = document.getElementById("collaboratorTimesheet-updateForm");
    let taskIdInput = document.getElementById("collaboratorTimesheet-taskIdInput");
    let monthInput = document.getElementById("collaboratorTimesheet-monthInput");
    let workedHoursInput = document.getElementById("collaboratorTimesheet-workedHoursInput");
    let timesheetError = document.getElementById("collaboratorTimesheet-error");
    let timesheetSuccess = document.getElementById("collaboratorTimesheet-success");

    // view events are registered
    this.registerEvents = function(orchestrator) {
        assignedProjectsList.addEventListener("click", function(event) {
            let target = event.target;

            if (target.tagName !== "BUTTON" || !target.hasAttribute("data-task-id")) {
                return;
            }

            orchestrator.loadTaskTimesheet(target.getAttribute("data-task-id"));
        }, false);

        timesheetTableBody.addEventListener("mouseout", function(event) {
            let target = event.target;

            if (target.tagName !== "INPUT" || !target.classList.contains("collaborator-worked-hours-input")) {
                return;
            }

            if (target.value === target.getAttribute("data-original-value")) {
                return;
            }

            this.hideMessages();

            if (!target.checkValidity()) {
                target.reportValidity();
                this.showTimesheetError("Worked hours must be a non-negative integer.");
                return;
            }

            if (!isNonNegativeInteger(target.value)) {
                this.showTimesheetError("Worked hours must be a non-negative integer.");
                return;
            }

            taskIdInput.value = target.getAttribute("data-task-id");
            monthInput.value = target.getAttribute("data-month");
            workedHoursInput.value = target.value;
            orchestrator.updateWorkedHours(updateForm);
        }.bind(this), false);
    };

    // the view is reset
    this.reset = function() {
        assignedProjectsList.innerHTML = "";
        timesheetTableBody.innerHTML = "";
        assignedProjectsListEmpty.classList.add("hidden");
        assignedProjectsList.classList.add("hidden");
        timesheetBox.classList.add("hidden");
        updateForm.reset();
        this.hideMessages();
    };

    // assigned projects are shown
    this.showAssignedProjects = function(projects, selectedTaskId) {
        let projectBox;
        let i;

        assignedProjectsList.innerHTML = "";
        timesheetBox.classList.add("hidden");

        if (!hasAssignedTasks(projects)) {
            assignedProjectsListEmpty.classList.remove("hidden");
            assignedProjectsList.classList.add("hidden");
            return;
        }

        assignedProjectsListEmpty.classList.add("hidden");
        assignedProjectsList.classList.remove("hidden");

        for (i = 0; i < projects.length; i++) {
            projectBox = buildProjectBox(projects[i], selectedTaskId);
            assignedProjectsList.appendChild(projectBox);
        }
    };

    // the collaborator timesheet is shown
    this.showCollaboratorTimesheet = function(taskData) {
        let task = taskData.task;

        timesheetTitle.textContent = "Task Timesheet: " + task.title;
        timesheetProjectTitle.textContent = taskData.project.title;
        timesheetTaskDescription.textContent = task.description;
        timesheetTaskPeriod.textContent = "Month M" + task.startMonth + " to Month M" + task.endMonth;
        timesheetTotalExpectedHours.textContent = task.taskTotalExpectedHours;
        timesheetTotalWorkedHours.textContent = task.collaboratorTotalWorkedHours;
        showTimesheetRows(task);
        timesheetBox.classList.remove("hidden");
    };

    // the collaborator timesheet is hidden
    this.hideCollaboratorTimesheet = function() {
        timesheetBox.classList.add("hidden");
    };

    // the assigned projects error is shown
    this.showAssignedProjectsError = function(message) {
        assignedProjectsListError.textContent = message;
        assignedProjectsListError.classList.remove("hidden");
    };

    // the timesheet error is shown
    this.showTimesheetError = function(message) {
        timesheetError.textContent = message;
        timesheetError.classList.remove("hidden");
    };

    // the timesheet success is shown
    this.showTimesheetSuccess = function(message) {
        timesheetSuccess.textContent = message;
        timesheetSuccess.classList.remove("hidden");
    };

    // all messages are hidden
    this.hideMessages = function() {
        assignedProjectsListError.textContent = "";
        assignedProjectsListError.classList.add("hidden");
        timesheetError.textContent = "";
        timesheetError.classList.add("hidden");
        timesheetSuccess.textContent = "";
        timesheetSuccess.classList.add("hidden");
    };

    // assigned tasks are checked
    function hasAssignedTasks(projects) {
        let i;
        let j;

        for (i = 0; i < projects.length; i++) {
            for (j = 0; j < projects[i].workPackages.length; j++) {
                if (projects[i].workPackages[j].tasks.length > 0) {
                    return true;
                }
            }
        }

        return false;
    }

    // a project box is built
    function buildProjectBox(project, selectedTaskId) {
        let projectBox = document.createElement("div");
        let projectTitle = document.createElement("span");
        let wpBox;
        let i;

        projectBox.className = "collaborator-project-item";

        projectTitle.className = "collaborator-project-title";
        projectTitle.textContent = project.title;
        projectBox.appendChild(projectTitle);

        for (i = 0; i < project.workPackages.length; i++) {
            wpBox = buildWPBox(project.workPackages[i], selectedTaskId);
            projectBox.appendChild(wpBox);
        }

        return projectBox;
    }

    // a work package box is built
    function buildWPBox(wp, selectedTaskId) {
        let wpBox = document.createElement("div");
        let wpTitle = document.createElement("span");
        let taskList = document.createElement("div");
        let taskButton;
        let i;

        wpBox.className = "collaborator-wp-item";

        wpTitle.className = "collaborator-wp-title";
        wpTitle.textContent = "WP" + wp.sequenceNumber + " - " + wp.title;
        wpBox.appendChild(wpTitle);

        taskList.className = "collaborator-task-list";
        for (i = 0; i < wp.tasks.length; i++) {
            taskButton = buildTaskButton(wp, wp.tasks[i], selectedTaskId);
            taskList.appendChild(taskButton);
        }

        wpBox.appendChild(taskList);
        return wpBox;
    }

    // a task button is built
    function buildTaskButton(wp, task, selectedTaskId) {
        let button = document.createElement("button");

        button.type = "button";
        button.className = "collaborator-task-link";
        button.textContent = "T" + wp.sequenceNumber + "." + task.sequenceNumber + " " + task.title;
        button.setAttribute("data-task-id", task.id);

        if (String(task.id) === String(selectedTaskId)) {
            button.classList.add("collaborator-active-task-link");
        }

        return button;
    }

    // timesheet rows are shown
    function showTimesheetRows(task) {
        let row;
        let monthCell;
        let expectedHoursCell;
        let workedHoursCell;
        let input;
        let i;

        timesheetTableBody.innerHTML = "";

        for (i = 0; i < task.months.length; i++) {
            row = document.createElement("tr");

            monthCell = document.createElement("td");
            monthCell.textContent = "M" + task.months[i].month;
            row.appendChild(monthCell);

            expectedHoursCell = document.createElement("td");
            expectedHoursCell.textContent = task.months[i].taskExpectedHours;
            row.appendChild(expectedHoursCell);

            workedHoursCell = document.createElement("td");
            input = document.createElement("input");
            input.className = "collaborator-worked-hours-input";
            input.type = "number";
            input.name = "workedHours";
            input.min = "0";
            input.step = "1";
            input.required = true;
            input.value = task.months[i].collaboratorWorkedHours;
            input.setAttribute("data-original-value", task.months[i].collaboratorWorkedHours);
            input.setAttribute("data-task-id", task.id);
            input.setAttribute("data-month", task.months[i].month);
            workedHoursCell.appendChild(input);
            row.appendChild(workedHoursCell);

            timesheetTableBody.appendChild(row);
        }
    }

    // a non-negative integer is detected
    function isNonNegativeInteger(value) {
        return String(value).trim() !== "" && Number.isInteger(Number(value)) && Number(value) >= 0;
    }
}
