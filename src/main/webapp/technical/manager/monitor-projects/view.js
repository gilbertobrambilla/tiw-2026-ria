
function MonitorProjectsView() {
    let projectListEmpty = document.getElementById("monitorProjects-projectListEmpty");
    let projectSelector = document.getElementById("monitorProjects-projectSelector");
    let projectSelect = document.getElementById("monitorProjects-projectSelect");
    let loadReportButton = document.getElementById("monitorProjects-loadReportButton");
    let projectListError = document.getElementById("monitorProjects-projectListError");
    let projectReport = document.getElementById("monitorProjects-projectReport");
    let projectReportTitle = projectReport.querySelector("h3");
    let reportTable = document.getElementById("monitorProjects-reportTable");
    let completedSuccess = document.getElementById("monitorProjects-completedSuccess");
    let closableBox = document.getElementById("monitorProjects-closableBox");
    let closableSuccess = document.getElementById("monitorProjects-closableSuccess");
    let concludeForm = document.getElementById("monitorProjects-concludeForm");
    let projectIdInput = document.getElementById("monitorProjects-projectIdInput");
    let concludeButton = document.getElementById("monitorProjects-concludeButton");
    let concludeError = document.getElementById("monitorProjects-concludeError");
    let concludeSuccess = document.getElementById("monitorProjects-concludeSuccess");

    // view events are registered
    this.registerEvents = function(orchestrator) {
        loadReportButton.addEventListener("click", function() {
            if (!projectSelector.checkValidity()) {
                projectSelector.reportValidity();
                return;
            }

            orchestrator.loadProjectReport(projectSelect.value);
        }, false);

        concludeButton.addEventListener("click", function() {
            if (!concludeForm.checkValidity()) {
                concludeForm.reportValidity();
                return;
            }

            orchestrator.concludeProject(concludeForm);
        }, false);
    };

    // the view is reset
    this.reset = function() {
        projectSelect.innerHTML = "";
        reportTable.innerHTML = "";
        projectReport.classList.add("hidden");
        projectListEmpty.classList.add("hidden");
        projectSelector.classList.add("hidden");
        completedSuccess.classList.add("hidden");
        closableBox.classList.add("hidden");
        this.hideMessages();
    };

    // projects are shown
    this.showProjects = function(projects) {
        let option;
        let i;

        projectSelect.innerHTML = "";
        reportTable.innerHTML = "";
        projectReport.classList.add("hidden");
        completedSuccess.classList.add("hidden");
        closableBox.classList.add("hidden");
        concludeForm.reset();

        option = document.createElement("option");
        option.value = "";
        option.textContent = "-- Select a Project --";
        option.disabled = true;
        option.selected = true;
        projectSelect.appendChild(option);

        if (projects.length === 0) {
            projectListEmpty.classList.remove("hidden");
            projectSelector.classList.add("hidden");
            projectReport.classList.add("hidden");
            return;
        }

        projectListEmpty.classList.add("hidden");
        projectSelector.classList.remove("hidden");

        for (i = 0; i < projects.length; i++) {
            option = document.createElement("option");
            option.value = projects[i].id;
            option.textContent = projects[i].title + " [" + projects[i].status + "]";
            projectSelect.appendChild(option);
        }
    };

    // the project report is shown
    this.showProjectReport = function(report) {
        projectIdInput.value = report.project.id;
        projectReportTitle.textContent = "Project Report: " + report.project.title;
        showReportTable(report);
        showProjectActions(report);
        projectReport.classList.remove("hidden");
    };

    // the project report is hidden
    this.hideProjectReport = function() {
        projectReport.classList.add("hidden");
    };

    // the project list error is shown
    this.showProjectListError = function(message) {
        projectListError.textContent = message;
        projectListError.classList.remove("hidden");
    };

    // the conclude error is shown
    this.showConcludeError = function(message) {
        concludeError.textContent = message;
        concludeError.classList.remove("hidden");
    };

    // the conclude success is shown
    this.showConcludeSuccess = function(message) {
        closableBox.classList.add("hidden");
        completedSuccess.classList.remove("hidden");
        concludeSuccess.textContent = message;
        concludeSuccess.classList.remove("hidden");
    };

    // all messages are hidden
    this.hideMessages = function() {
        projectListError.textContent = "";
        projectListError.classList.add("hidden");
        concludeError.textContent = "";
        concludeError.classList.add("hidden");
        concludeSuccess.textContent = "";
        concludeSuccess.classList.add("hidden");
    };

    // the report table is shown
    function showReportTable(report) {
        let thead = document.createElement("thead");
        let tbody = document.createElement("tbody");
        let firstHeaderRow = document.createElement("tr");
        let secondHeaderRow = document.createElement("tr");
        let th;
        let month;

        reportTable.innerHTML = "";
        tbody.id = "monitorProjects-reportTableBody";

        th = document.createElement("th");
        th.colSpan = 2;
        th.rowSpan = 2;
        th.textContent = "WP / Task";
        firstHeaderRow.appendChild(th);

        for (month = 1; month <= report.project.durationMonths; month++) {
            th = document.createElement("th");
            th.colSpan = 2;
            th.textContent = "M" + month;
            firstHeaderRow.appendChild(th);

            th = document.createElement("th");
            th.textContent = "Planned";
            secondHeaderRow.appendChild(th);

            th = document.createElement("th");
            th.textContent = "Worked";
            secondHeaderRow.appendChild(th);
        }

        thead.appendChild(firstHeaderRow);
        thead.appendChild(secondHeaderRow);
        reportTable.appendChild(thead);
        showReportRows(tbody, report);
        reportTable.appendChild(tbody);
    }

    // report rows are shown
    function showReportRows(tbody, report) {
        let wpReport;
        let taskReport;
        let i;
        let j;

        tbody.innerHTML = "";

        for (i = 0; i < report.wpList.length; i++) {
            wpReport = report.wpList[i];
            appendWPRow(tbody, report.project.durationMonths, wpReport);

            for (j = 0; j < wpReport.tasks.length; j++) {
                taskReport = wpReport.tasks[j];
                appendTaskRow(tbody, report.project.durationMonths, wpReport.wp.sequenceNumber, taskReport);
            }
        }
    }

    // a work package row is appended
    function appendWPRow(tbody, durationMonths, wpReport) {
        let months = buildMonthMap(wpReport.months);
        let row = document.createElement("tr");
        let cell;
        let month;

        row.className = "row-wp-sub";

        cell = document.createElement("td");
        cell.colSpan = 2;
        cell.textContent = "WP" + wpReport.wp.sequenceNumber + " - " + wpReport.wp.title;
        row.appendChild(cell);

        for (month = 1; month <= durationMonths; month++) {
            cell = document.createElement("td");
            cell.textContent = months[month] !== undefined && months[month].totalExpectedHours !== null ? months[month].totalExpectedHours : "0";
            row.appendChild(cell);

            cell = document.createElement("td");
            cell.textContent = months[month] !== undefined ? months[month].totalWorkedHours : "0";
            row.appendChild(cell);
        }

        tbody.appendChild(row);
    }

    // a task row is appended
    function appendTaskRow(tbody, durationMonths, wpNumber, taskReport) {
        let months = buildMonthMap(taskReport.months);
        let row = document.createElement("tr");
        let cell;
        let month;

        row.className = "row-task";

        cell = document.createElement("td");
        row.appendChild(cell);

        cell = document.createElement("td");
        cell.className = "task-name";
        cell.textContent = "T" + wpNumber + "." + taskReport.task.sequenceNumber + " - " + taskReport.task.title;
        row.appendChild(cell);

        for (month = 1; month <= durationMonths; month++) {
            cell = document.createElement("td");
            cell.textContent = months[month] !== undefined && months[month].expectedHours !== null ? months[month].expectedHours : "0";
            row.appendChild(cell);

            cell = document.createElement("td");
            cell.textContent = months[month] !== undefined ? months[month].workedHours : "0";
            row.appendChild(cell);
        }

        tbody.appendChild(row);
    }

    // project actions are shown
    function showProjectActions(report) {
        completedSuccess.classList.add("hidden");
        closableBox.classList.add("hidden");
        concludeError.classList.add("hidden");
        concludeSuccess.classList.add("hidden");

        if (report.project.status === "completed") {
            completedSuccess.classList.remove("hidden");
        } else if (report.closable) {
            closableSuccess.textContent = "All task requirements have been successfully met. This project is eligible for closure.";
            closableBox.classList.remove("hidden");
        }
    }

    // months are mapped
    function buildMonthMap(months) {
        let monthMap = {};
        let i;

        for (i = 0; i < months.length; i++) {
            monthMap[months[i].month] = months[i];
        }

        return monthMap;
    }
}
