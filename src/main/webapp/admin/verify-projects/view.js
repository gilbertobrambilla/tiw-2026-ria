
function VerifyProjectsView() {
    let projectList = document.getElementById("verifyProjects-projectList");
    let errorContainer = document.getElementById("verifyProjects-error");
    let projectViewer = document.getElementById("verifyProjects-projectViewer");
    let projectTree = document.getElementById("verifyProjects-projectTree");
    let selectedProjectId = null;

    // view events are registered
    this.registerEvents = function(orchestrator) {
        projectList.addEventListener("click", function(event) {
            let target = event.target;

            if (target.tagName !== "BUTTON" || !target.hasAttribute("data-project-id")) {
                return;
            }

            selectedProjectId = target.getAttribute("data-project-id");
            orchestrator.loadProjectStructure(selectedProjectId);
        }, false);
    };

    // the view is reset
    this.reset = function() {
        selectedProjectId = null;
        projectList.innerHTML = "";
        projectTree.innerHTML = "";
        projectViewer.classList.add("hidden");
        this.hideError();
    };

    // the project list is shown
    this.showProjects = function(projects) {
        let button;
        let i;

        selectedProjectId = null;
        projectList.innerHTML = "";
        projectTree.innerHTML = "";
        projectViewer.classList.add("hidden");

        if (projects.length === 0) {
            button = document.createElement("button");
            button.type = "button";
            button.textContent = "No projects available";
            button.disabled = true;
            projectList.appendChild(button);
            return;
        }

        for (i = 0; i < projects.length; i++) {
            button = document.createElement("button");
            button.type = "button";
            button.textContent = projects[i].title;
            button.setAttribute("data-project-id", projects[i].id);
            projectList.appendChild(button);
        }
    };

    // the project structure is shown
    this.showProjectStructure = function(projectStructure) {
        let project = projectStructure.project;
        let wpList = projectStructure.wpList;
        let projectBox;
        let projectTitle;
        let wpBox;
        let wpHeader;
        let wpTitle;
        let wpMeta;
        let taskBox;
        let taskCode;
        let taskTitle;
        let taskDescription;
        let taskDetails;
        let buttons;
        let wpNumber;
        let taskNumber;
        let i;
        let j;

        buttons = projectList.querySelectorAll("button[data-project-id]");
        for (i = 0; i < buttons.length; i++) {
            if (buttons[i].getAttribute("data-project-id") === selectedProjectId) {
                buttons[i].classList.add("active-link");
            } else {
                buttons[i].classList.remove("active-link");
            }
        }

        projectTree.innerHTML = "";
        projectViewer.classList.remove("hidden");

        projectBox = document.createElement("div");
        projectBox.className = "tree-project";

        projectTitle = document.createElement("span");
        projectTitle.className = "tree-project-title";
        projectTitle.textContent = "PROJECT: " + project.title;
        projectBox.appendChild(projectTitle);

        for (i = 0; i < wpList.length; i++) {
            wpNumber = wpList[i].wp.sequenceNumber;

            wpBox = document.createElement("div");
            wpBox.className = "tree-wp";

            wpHeader = document.createElement("div");

            wpTitle = document.createElement("span");
            wpTitle.className = "node-title";
            wpTitle.textContent = "WP" + wpNumber + ": " + wpList[i].wp.title;
            wpHeader.appendChild(wpTitle);

            wpMeta = document.createElement("span");
            wpMeta.className = "node-meta";
            wpMeta.textContent = "(Start Month: M" + wpList[i].wp.startMonth + ", End Month: M" + wpList[i].wp.endMonth + ")";
            wpHeader.appendChild(wpMeta);

            wpBox.appendChild(wpHeader);

            for (j = 0; j < wpList[i].tasks.length; j++) {
                taskNumber = wpList[i].tasks[j].task.sequenceNumber;

                taskBox = document.createElement("div");
                taskBox.className = "tree-task";

                taskCode = document.createElement("span");
                taskCode.className = "task-code";
                taskCode.textContent = "T" + wpNumber + "." + taskNumber + ": ";
                taskBox.appendChild(taskCode);

                taskTitle = document.createElement("span");
                taskTitle.textContent = wpList[i].tasks[j].task.title;
                taskBox.appendChild(taskTitle);

                taskDescription = document.createElement("span");
                taskDescription.className = "task-desc-text";
                taskDescription.textContent = wpList[i].tasks[j].task.description;
                taskBox.appendChild(taskDescription);

                taskDetails = document.createElement("span");
                taskDetails.className = "task-details";
                taskDetails.textContent = "Period: M" + wpList[i].tasks[j].task.startMonth + " - M" + wpList[i].tasks[j].task.endMonth +
                    " | Total planned hours: " + wpList[i].tasks[j].totalExpectedHours +
                    " | Total worked hours: " + wpList[i].tasks[j].totalWorkedHours;
                taskBox.appendChild(taskDetails);

                wpBox.appendChild(taskBox);
            }

            projectBox.appendChild(wpBox);
        }

        projectTree.appendChild(projectBox);
    };

    // the error is shown
    this.showError = function(message) {
        errorContainer.textContent = message;
        errorContainer.classList.remove("hidden");
    };

    // the error is hidden
    this.hideError = function() {
        errorContainer.textContent = "";
        errorContainer.classList.add("hidden");
    };
}
