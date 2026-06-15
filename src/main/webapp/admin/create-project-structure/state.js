
function CreateProjectStructureState() {
    let state;
    let history;
    let historyIndex;

    // a new empty state is created
    this.reset = function() {
        state = {
            uiState: {
                phase: 1
            },
            projectData: {
                title: "",
                durationMonths: 0,
                managedBy: 0,
                workPackages: []
            }
        };
        history = [];
        historyIndex = -1;
        saveSnapshot();
    };

    // a copy of the state is returned
    this.getState = function() {
        return clone(state);
    };

    // undo availability is checked
    this.canUndo = function() {
        return historyIndex > 0;
    };

    // redo availability is checked
    this.canRedo = function() {
        return historyIndex < history.length - 1;
    };

    // the previous snapshot is restored
    this.undo = function() {
        if (historyIndex <= 0) {
            return false;
        }

        historyIndex--;
        state = clone(history[historyIndex]);
        return true;
    };

    // the next snapshot is restored
    this.redo = function() {
        if (historyIndex >= history.length - 1) {
            return false;
        }

        historyIndex++;
        state = clone(history[historyIndex]);
        return true;
    };

    // a project field is updated
    this.updateProjectField = function(fieldName, value) {
        let newValue;

        if (fieldName !== "title" && fieldName !== "durationMonths" && fieldName !== "managedBy") {
            return false;
        }

        if (state.uiState.phase === 2 && fieldName === "durationMonths") {
            return false;
        }

        if (fieldName === "durationMonths" || fieldName === "managedBy") {
            newValue = value;
        } else {
            newValue = String(value).trim();
        }

        if (state.projectData[fieldName] === newValue) {
            return false;
        }

        state.projectData[fieldName] = newValue;
        return saveSnapshot();
    };

    // the structure phase is entered when project data is valid
    this.enterStructurePhase = function() {
        let errors = [];
        let validationResult;

        if (isBlank(state.projectData.title)) {
            errors.push("Project title cannot be empty");
        }

        if (!isPositiveInteger(state.projectData.durationMonths)) {
            errors.push("Project duration must be greater than zero");
        }

        if (!isPositiveInteger(state.projectData.managedBy)) {
            errors.push("Technical manager must be selected");
        }

        validationResult = {
            valid: errors.length === 0,
            errors: errors
        };

        if (!validationResult.valid || state.uiState.phase === 2) {
            return validationResult;
        }

        state.uiState.phase = 2;
        saveSnapshot();
        return validationResult;
    };

    // a WP is added
    this.addWP = function() {
        if (state.uiState.phase !== 2 || !isPositiveInteger(state.projectData.durationMonths)) {
            return false;
        }

        state.projectData.workPackages.push({
            title: "WP title",
            startMonth: 1,
            endMonth: state.projectData.durationMonths,
            tasks: []
        });
        return saveSnapshot();
    };

    // a WP is removed
    this.removeWP = function(wpIndex) {
        if (state.uiState.phase !== 2 ||
            wpIndex < 0 ||
            wpIndex >= state.projectData.workPackages.length) {
            return false;
        }

        state.projectData.workPackages.splice(wpIndex, 1);
        return saveSnapshot();
    };

    // a WP field is updated
    this.updateWPField = function(wpIndex, fieldName, value) {
        let wp;
        let newValue;

        if (state.uiState.phase !== 2 ||
            wpIndex < 0 ||
            wpIndex >= state.projectData.workPackages.length ||
            (fieldName !== "title" && fieldName !== "startMonth" && fieldName !== "endMonth")) {
            return false;
        }

        wp = state.projectData.workPackages[wpIndex];
        if (fieldName === "startMonth" || fieldName === "endMonth") {
            newValue = value;
        } else {
            newValue = String(value).trim();
        }

        if (wp[fieldName] === newValue) {
            return false;
        }

        wp[fieldName] = newValue;
        return saveSnapshot();
    };

    // a task is added
    this.addTask = function(wpIndex) {
        let wp;

        if (state.uiState.phase !== 2 ||
            wpIndex < 0 ||
            wpIndex >= state.projectData.workPackages.length) {
            return false;
        }

        wp = state.projectData.workPackages[wpIndex];
        wp.tasks.push({
            title: "Task title",
            description: "Task description",
            startMonth: wp.startMonth,
            endMonth: wp.endMonth
        });
        return saveSnapshot();
    };

    // a task is removed
    this.removeTask = function(wpIndex, taskIndex) {
        let wp;

        if (state.uiState.phase !== 2 ||
            wpIndex < 0 ||
            wpIndex >= state.projectData.workPackages.length) {
            return false;
        }

        wp = state.projectData.workPackages[wpIndex];
        if (taskIndex < 0 || taskIndex >= wp.tasks.length) {
            return false;
        }

        wp.tasks.splice(taskIndex, 1);
        return saveSnapshot();
    };

    // a task field is updated
    this.updateTaskField = function(wpIndex, taskIndex, fieldName, value) {
        let wp;
        let task;
        let newValue;

        if (state.uiState.phase !== 2 ||
            wpIndex < 0 ||
            wpIndex >= state.projectData.workPackages.length) {
            return false;
        }

        wp = state.projectData.workPackages[wpIndex];
        if (taskIndex < 0 ||
            taskIndex >= wp.tasks.length ||
            (fieldName !== "title" && fieldName !== "description" && fieldName !== "startMonth" && fieldName !== "endMonth")) {
            return false;
        }

        task = wp.tasks[taskIndex];
        if (fieldName === "startMonth" || fieldName === "endMonth") {
            newValue = value;
        } else {
            newValue = String(value).trim();
        }

        if (task[fieldName] === newValue) {
            return false;
        }

        task[fieldName] = newValue;
        return saveSnapshot();
    };

    // the project structure is checked
    this.validateProjectStructure = function() {
        let errors = [];
        let wp;
        let task;
        let i;
        let j;

        if (isBlank(state.projectData.title)) {
            errors.push("Project title cannot be empty");
        }

        if (!isPositiveInteger(state.projectData.durationMonths)) {
            errors.push("Project duration must be greater than zero");
        }

        if (!isPositiveInteger(state.projectData.managedBy)) {
            errors.push("Technical manager must be selected");
        }

        if (state.projectData.workPackages.length === 0) {
            errors.push("Project must contain at least one WP");
        }

        for (i = 0; i < state.projectData.workPackages.length; i++) {
            wp = state.projectData.workPackages[i];

            if (isBlank(wp.title)) {
                errors.push("WP " + (i + 1) + " title cannot be empty");
            }

            if (!isPositiveInteger(wp.startMonth) || !isPositiveInteger(wp.endMonth)) {
                errors.push("WP " + (i + 1) + " months must be positive");
            } else if (Number(wp.startMonth) > Number(wp.endMonth)) {
                errors.push("WP " + (i + 1) + " end month cannot precede start month");
            } else if (Number(wp.endMonth) > Number(state.projectData.durationMonths)) {
                errors.push("WP " + (i + 1) + " duration cannot exceed project duration");
            }

            if (wp.tasks.length === 0) {
                errors.push("WP " + (i + 1) + " must contain at least one task");
            }

            for (j = 0; j < wp.tasks.length; j++) {
                task = wp.tasks[j];

                if (isBlank(task.title)) {
                    errors.push("Task " + (i + 1) + "." + (j + 1) + " title cannot be empty");
                }

                if (isBlank(task.description)) {
                    errors.push("Task " + (i + 1) + "." + (j + 1) + " description cannot be empty");
                }

                if (!isPositiveInteger(task.startMonth) || !isPositiveInteger(task.endMonth)) {
                    errors.push("Task " + (i + 1) + "." + (j + 1) + " months must be positive");
                } else if (Number(task.startMonth) > Number(task.endMonth)) {
                    errors.push("Task " + (i + 1) + "." + (j + 1) + " end month cannot precede start month");
                } else if (Number(task.startMonth) < Number(wp.startMonth) || Number(task.endMonth) > Number(wp.endMonth)) {
                    errors.push("Task " + (i + 1) + "." + (j + 1) + " duration must be contained in its WP duration");
                }
            }
        }

        return {
            valid: errors.length === 0,
            errors: errors
        };
    };

    // a JSON value is copied
    function clone(value) {
        return JSON.parse(JSON.stringify(value));
    }

    // the current state is stored
    function saveSnapshot() {
        let snapshot = clone(state);

        if (historyIndex >= 0 && JSON.stringify(history[historyIndex]) === JSON.stringify(snapshot)) {
            return false;
        }

        history = history.slice(0, historyIndex + 1);
        history.push(snapshot);

        if (history.length > 50) {
            history.shift();
        }

        historyIndex = history.length - 1;
        return true;
    }

    // a blank value is detected
    function isBlank(value) {
        return value === null || value === undefined || String(value).trim() === "";
    }

    // a positive integer is detected
    function isPositiveInteger(value) {
        return String(value).trim() !== "" && Number.isInteger(Number(value)) && Number(value) > 0;
    }

    this.reset();
}
