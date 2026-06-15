
function CreateProjectStructureView() {
    let projectHeaderView = new ProjectHeaderView(
        document.getElementById("createProjectStructure-projectHeaderForm"),
        document.getElementById("createProjectStructure-projectHeaderFields"),
        document.getElementById("createProjectStructure-projectTitleInput"),
        document.getElementById("createProjectStructure-projectDurationInput"),
        document.getElementById("createProjectStructure-projectManagerSelect"),
        document.getElementById("createProjectStructure-enterStructurePhaseButton"),
        document.getElementById("createProjectStructure-undoButton"),
        document.getElementById("createProjectStructure-redoButton"),
        document.getElementById("createProjectStructure-projectHeaderError"));

    let projectStructureView = new ProjectStructureView(
        document.getElementById("createProjectStructure-projectStructurePanel"),
        document.getElementById("createProjectStructure-projectStructureWPList"),
        document.getElementById("createProjectStructure-addWPButton"),
        document.getElementById("createProjectStructure-saveProjectStructureButton"),
        document.getElementById("createProjectStructure-projectStructureError"));

    let successContainer = document.getElementById("createProjectStructure-success");

    // view events are registered
    this.registerEvents = function(orchestrator) {
        projectHeaderView.registerEvents(orchestrator);
        projectStructureView.registerEvents(orchestrator);
    };

    // the view is reset
    this.reset = function() {
        projectHeaderView.reset();
        projectStructureView.reset();
    };

    // the view is shown
    this.show = function(state, technicalUsers, canUndo, canRedo) {
        this.update(state, technicalUsers, canUndo, canRedo);
    };

    // the view is updated
    this.update = function(state, technicalUsers, canUndo, canRedo) {
        projectHeaderView.show(state, technicalUsers, canUndo, canRedo);

        if (state.uiState.phase === 2) {
            projectStructureView.show(state);
        } else {
            projectStructureView.reset();
        }
    };

    // the header error is shown
    this.showHeaderError = function(errors) {
        projectHeaderView.showError(errors);
    };

    // the structure error is shown
    this.showStructureError = function(errors) {
        projectStructureView.showError(errors);
    };

    // the structure success is shown
    this.showStructureSuccess = function(message) {
        successContainer.textContent = message;
        successContainer.classList.remove("hidden");
    };

    // all messages are hidden
    this.hideMessages = function() {
        projectHeaderView.hideError();
        projectStructureView.hideMessages();
        successContainer.textContent = "";
        successContainer.classList.add("hidden");
    };
}

// the project header view is managed
function ProjectHeaderView(form, fields, titleInput, durationInput, managerSelect, enterButton, undoButton, redoButton, errorContainer) {
    let technicalUsers = [];

    // header events are registered
    this.registerEvents = function(orchestrator) {
        titleInput.addEventListener("blur", function() {
            orchestrator.updateProjectField("title", titleInput.value);
        }, false);

        durationInput.addEventListener("blur", function() {
            orchestrator.updateProjectField("durationMonths", durationInput.value);
        }, false);

        managerSelect.addEventListener("change", function() {
            orchestrator.updateProjectField("managedBy", managerSelect.value);
        }, false);

        enterButton.addEventListener("click", function() {
            orchestrator.enterStructurePhase();
        }, false);

        undoButton.addEventListener("click", function() {
            orchestrator.undoDraft();
        }, false);

        redoButton.addEventListener("click", function() {
            orchestrator.redoDraft();
        }, false);
    };

    // the header view is reset
    this.reset = function() {
        form.reset();
        fields.disabled = false;
        titleInput.disabled = false;
        durationInput.disabled = false;
        managerSelect.disabled = false;
        undoButton.disabled = true;
        redoButton.disabled = true;
        this.hideError();
    };

    // the header view is shown
    this.show = function(state, users, canUndo, canRedo) {
        this.update(state, users, canUndo, canRedo);
    };

    // the header view is updated
    this.update = function(state, users, canUndo, canRedo) {
        let option;
        let i;

        if (users !== undefined && users !== null) {
            technicalUsers = users;
        }

        managerSelect.innerHTML = "";
        option = document.createElement("option");
        option.value = "0";
        option.textContent = "-- Select Technical User --";
        option.disabled = true;
        managerSelect.appendChild(option);

        for (i = 0; i < technicalUsers.length; i++) {
            option = document.createElement("option");
            option.value = technicalUsers[i].id;
            option.textContent = technicalUsers[i].firstName + " " + technicalUsers[i].lastName + " (" + technicalUsers[i].username + ")";
            managerSelect.appendChild(option);
        }

        fields.disabled = state.uiState.phase === 2;
        undoButton.disabled = !canUndo;
        redoButton.disabled = !canRedo;

        titleInput.value = state.projectData.title;
        durationInput.value = state.projectData.durationMonths;
        managerSelect.value = state.projectData.managedBy;
    };

    // the header error is shown
    this.showError = function(errors) {
        errorContainer.textContent = Array.isArray(errors) ? errors.join("\n") : errors;
        errorContainer.classList.remove("hidden");
    };

    // the header error is hidden
    this.hideError = function() {
        errorContainer.textContent = "";
        errorContainer.classList.add("hidden");
    };
}

// the project structure view is managed
function ProjectStructureView(panel, wpList, addWPButton, saveButton, errorContainer) {
    let dragAndDrop = new CreateProjectStructureDragAndDrop(wpList);

    // structure events are registered
    this.registerEvents = function(orchestrator) {
        dragAndDrop.registerEvents(orchestrator);

        addWPButton.addEventListener("click", function() {
            orchestrator.addWP();
        }, false);

        saveButton.addEventListener("click", function() {
            orchestrator.saveProjectStructure();
        }, false);

        wpList.addEventListener("click", function(event) {
            let target = event.target;
            let wpIndex;
            let taskIndex;

            if (target.classList.contains("remove-wp-button")) {
                wpIndex = parseInt(target.getAttribute("data-wp-index"), 10);
                orchestrator.removeWP(wpIndex);
            } else if (target.classList.contains("add-task-button")) {
                wpIndex = parseInt(target.getAttribute("data-wp-index"), 10);
                orchestrator.addTask(wpIndex);
            } else if (target.classList.contains("remove-task-button")) {
                wpIndex = parseInt(target.getAttribute("data-wp-index"), 10);
                taskIndex = parseInt(target.getAttribute("data-task-index"), 10);
                orchestrator.removeTask(wpIndex, taskIndex);
            }
        }, false);

        wpList.addEventListener("focusout", function(event) {
            let target = event.target;
            let wpIndex;
            let taskIndex;
            let fieldName;

            if (!target.hasAttribute("data-field")) {
                return;
            }

            wpIndex = parseInt(target.getAttribute("data-wp-index"), 10);
            fieldName = target.getAttribute("data-field");

            if (target.getAttribute("data-level") === "wp") {
                orchestrator.updateWPField(wpIndex, fieldName, target.value);
            } else if (target.getAttribute("data-level") === "task") {
                taskIndex = parseInt(target.getAttribute("data-task-index"), 10);
                orchestrator.updateTaskField(wpIndex, taskIndex, fieldName, target.value);
            }
        }, false);
    };

    // the structure view is reset
    this.reset = function() {
        panel.classList.add("hidden");
        wpList.innerHTML = "";
        this.hideMessages();
    };

    // the structure view is shown
    this.show = function(state) {
        panel.classList.remove("hidden");
        this.update(state);
    };

    // the structure view is updated
    this.update = function(state) {
        let workPackages = state.projectData.workPackages;
        let wp;
        let task;
        let wpBox;
        let wpLine;
        let taskList;
        let taskBox;
        let taskLine;
        let actions;
        let span;
        let input;
        let button;
        let i;
        let j;

        wpList.innerHTML = "";

        for (i = 0; i < workPackages.length; i++) {
            wp = workPackages[i];

            wpBox = document.createElement("div");
            wpBox.className = "wp-box";

            wpLine = document.createElement("div");
            wpLine.className = "wp-line";

            span = document.createElement("span");
            span.className = "seq";
            span.textContent = "WP" + (i + 1);
            wpLine.appendChild(span);

            input = document.createElement("input");
            input.className = "wp-title-input wide";
            input.type = "text";
            input.value = wp.title;
            input.placeholder = "WP Title";
            input.maxLength = 100;
            input.required = true;
            input.setAttribute("data-level", "wp");
            input.setAttribute("data-wp-index", i);
            input.setAttribute("data-field", "title");
            wpLine.appendChild(input);

            input = document.createElement("input");
            input.className = "wp-start-month-input month";
            input.type = "text";
            input.value = wp.startMonth;
            input.placeholder = "Start Month";
            input.inputMode = "numeric";
            input.required = true;
            input.setAttribute("data-level", "wp");
            input.setAttribute("data-wp-index", i);
            input.setAttribute("data-field", "startMonth");
            wpLine.appendChild(input);

            input = document.createElement("input");
            input.className = "wp-end-month-input month";
            input.type = "text";
            input.value = wp.endMonth;
            input.placeholder = "End Month";
            input.inputMode = "numeric";
            input.required = true;
            input.setAttribute("data-level", "wp");
            input.setAttribute("data-wp-index", i);
            input.setAttribute("data-field", "endMonth");
            wpLine.appendChild(input);

            button = document.createElement("button");
            button.type = "button";
            button.className = "btn danger line-button remove-wp-button";
            button.textContent = "- WP";
            button.setAttribute("data-wp-index", i);
            wpLine.appendChild(button);

            wpBox.appendChild(wpLine);

            taskList = document.createElement("div");
            taskList.className = "task-list";
            taskList.setAttribute("data-wp-index", i);
            taskList.setAttribute("data-task-count", wp.tasks.length);

            for (j = 0; j < wp.tasks.length; j++) {
                task = wp.tasks[j];

                taskBox = document.createElement("div");
                taskBox.className = "task-box";
                taskBox.setAttribute("data-wp-index", i);
                taskBox.setAttribute("data-task-index", j);

                taskLine = document.createElement("div");
                taskLine.className = "task-line";

                span = document.createElement("span");
                span.className = "seq task-drag-handle";
                span.textContent = "T" + (i + 1) + "." + (j + 1);
                span.draggable = true;
                span.setAttribute("data-wp-index", i);
                span.setAttribute("data-task-index", j);
                taskLine.appendChild(span);

                input = document.createElement("input");
                input.className = "task-title-input title";
                input.type = "text";
                input.value = task.title;
                input.placeholder = "Task Title";
                input.maxLength = 100;
                input.required = true;
                input.setAttribute("data-level", "task");
                input.setAttribute("data-wp-index", i);
                input.setAttribute("data-task-index", j);
                input.setAttribute("data-field", "title");
                taskLine.appendChild(input);

                input = document.createElement("input");
                input.className = "task-description-input description";
                input.type = "text";
                input.value = task.description;
                input.placeholder = "Description";
                input.required = true;
                input.setAttribute("data-level", "task");
                input.setAttribute("data-wp-index", i);
                input.setAttribute("data-task-index", j);
                input.setAttribute("data-field", "description");
                taskLine.appendChild(input);

                input = document.createElement("input");
                input.className = "task-start-month-input month";
                input.type = "text";
                input.value = task.startMonth;
                input.placeholder = "Start Month";
                input.inputMode = "numeric";
                input.required = true;
                input.setAttribute("data-level", "task");
                input.setAttribute("data-wp-index", i);
                input.setAttribute("data-task-index", j);
                input.setAttribute("data-field", "startMonth");
                taskLine.appendChild(input);

                input = document.createElement("input");
                input.className = "task-end-month-input month";
                input.type = "text";
                input.value = task.endMonth;
                input.placeholder = "End Month";
                input.inputMode = "numeric";
                input.required = true;
                input.setAttribute("data-level", "task");
                input.setAttribute("data-wp-index", i);
                input.setAttribute("data-task-index", j);
                input.setAttribute("data-field", "endMonth");
                taskLine.appendChild(input);

                button = document.createElement("button");
                button.type = "button";
                button.className = "btn danger line-button remove-task-button";
                button.textContent = "- Task";
                button.setAttribute("data-wp-index", i);
                button.setAttribute("data-task-index", j);
                taskLine.appendChild(button);

                taskBox.appendChild(taskLine);
                taskList.appendChild(taskBox);
            }

            wpBox.appendChild(taskList);

            actions = document.createElement("div");
            actions.className = "right-actions";

            button = document.createElement("button");
            button.type = "button";
            button.className = "btn add-task-button";
            button.textContent = "+ Task";
            button.setAttribute("data-wp-index", i);
            actions.appendChild(button);

            wpBox.appendChild(actions);
            wpList.appendChild(wpBox);
        }
    };

    // the structure error is shown
    this.showError = function(errors) {
        errorContainer.textContent = Array.isArray(errors) ? errors.join("\n") : errors;
        errorContainer.classList.remove("hidden");
    };

    // structure messages are hidden
    this.hideMessages = function() {
        errorContainer.textContent = "";
        errorContainer.classList.add("hidden");
    };
}
