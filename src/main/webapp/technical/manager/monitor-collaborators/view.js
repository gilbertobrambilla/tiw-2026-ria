
function MonitorCollaboratorsView() {
    let collaboratorListEmpty = document.getElementById("monitorCollaborators-collaboratorListEmpty");
    let collaboratorSelector = document.getElementById("monitorCollaborators-collaboratorSelector");
    let collaboratorSelect = document.getElementById("monitorCollaborators-collaboratorSelect");
    let loadReportButton = document.getElementById("monitorCollaborators-loadReportButton");
    let collaboratorListError = document.getElementById("monitorCollaborators-collaboratorListError");
    let collaboratorReport = document.getElementById("monitorCollaborators-collaboratorReport");
    let collaboratorReportTitle = collaboratorReport.querySelector("h3");
    let reportTableBox = document.getElementById("monitorCollaborators-reportTableBox");

    // view events are registered
    this.registerEvents = function(orchestrator) {
        loadReportButton.addEventListener("click", function() {
            if (!collaboratorSelector.checkValidity()) {
                collaboratorSelector.reportValidity();
                return;
            }

            orchestrator.loadCollaboratorReport(collaboratorSelect.value);
        }, false);
    };

    // the view is reset
    this.reset = function() {
        collaboratorSelect.innerHTML = "";
        reportTableBox.innerHTML = "";
        collaboratorReport.classList.add("hidden");
        collaboratorListEmpty.classList.add("hidden");
        collaboratorSelector.classList.add("hidden");
        this.hideError();
    };

    // collaborators are shown
    this.showCollaborators = function(collaborators) {
        let option;
        let i;

        collaboratorSelect.innerHTML = "";
        reportTableBox.innerHTML = "";
        collaboratorReport.classList.add("hidden");

        option = document.createElement("option");
        option.value = "";
        option.textContent = "-- Select a Collaborator --";
        option.disabled = true;
        option.selected = true;
        collaboratorSelect.appendChild(option);

        if (collaborators.length === 0) {
            collaboratorListEmpty.classList.remove("hidden");
            collaboratorSelector.classList.add("hidden");
            collaboratorReport.classList.add("hidden");
            return;
        }

        collaboratorListEmpty.classList.add("hidden");
        collaboratorSelector.classList.remove("hidden");

        for (i = 0; i < collaborators.length; i++) {
            option = document.createElement("option");
            option.value = collaborators[i].id;
            option.textContent = collaborators[i].firstName + " " + collaborators[i].lastName;
            collaboratorSelect.appendChild(option);
        }
    };

    // the collaborator report is shown
    this.showCollaboratorReport = function(report) {
        collaboratorReportTitle.textContent = "Hours Report: " + report.collaborator.firstName + " " + report.collaborator.lastName;
        reportTableBox.innerHTML = "";
        showProjectTables(report.projects);
        collaboratorReport.classList.remove("hidden");
    };

    // the collaborator report is hidden
    this.hideCollaboratorReport = function() {
        collaboratorReport.classList.add("hidden");
    };

    // the error is shown
    this.showError = function(message) {
        collaboratorListError.textContent = message;
        collaboratorListError.classList.remove("hidden");
    };

    // the error is hidden
    this.hideError = function() {
        collaboratorListError.textContent = "";
        collaboratorListError.classList.add("hidden");
    };

    // project tables are shown
    function showProjectTables(projectReports) {
        let table;
        let i;

        for (i = 0; i < projectReports.length; i++) {
            table = buildProjectTable(projectReports[i], i === 0);
            reportTableBox.appendChild(table);
        }
    }

    // a project table is built
    function buildProjectTable(projectReport, firstTable) {
        let table = document.createElement("table");
        let thead = document.createElement("thead");
        let tbody = document.createElement("tbody");

        table.className = "data-table";

        if (firstTable) {
            table.id = "monitorCollaborators-reportTable";
            tbody.id = "monitorCollaborators-reportTableBody";
        }

        appendProjectHeader(thead, projectReport.project);
        appendMonthHeader(thead, projectReport.project.durationMonths);
        appendReportRows(tbody, projectReport);

        table.appendChild(thead);
        table.appendChild(tbody);
        return table;
    }

    // the project header is appended
    function appendProjectHeader(thead, project) {
        let row = document.createElement("tr");
        let th = document.createElement("th");

        row.className = "row-project";
        th.colSpan = 2 + project.durationMonths;
        th.textContent = "PROJECT: " + project.title;
        row.appendChild(th);
        thead.appendChild(row);
    }

    // the month header is appended
    function appendMonthHeader(thead, durationMonths) {
        let firstRow = document.createElement("tr");
        let secondRow = document.createElement("tr");
        let th;
        let month;

        th = document.createElement("th");
        th.colSpan = 2;
        th.rowSpan = 2;
        th.textContent = "WP / Task";
        firstRow.appendChild(th);

        for (month = 1; month <= durationMonths; month++) {
            th = document.createElement("th");
            th.textContent = "M" + month;
            firstRow.appendChild(th);

            th = document.createElement("th");
            th.textContent = "Worked";
            secondRow.appendChild(th);
        }

        thead.appendChild(firstRow);
        thead.appendChild(secondRow);
    }

    // report rows are appended
    function appendReportRows(tbody, projectReport) {
        let wpReport;
        let taskReport;
        let i;
        let j;

        for (i = 0; i < projectReport.wpList.length; i++) {
            wpReport = projectReport.wpList[i];
            appendWPRow(tbody, projectReport.project.durationMonths, wpReport);

            for (j = 0; j < wpReport.tasks.length; j++) {
                taskReport = wpReport.tasks[j];
                appendTaskRow(tbody, projectReport.project.durationMonths, wpReport.wp.sequenceNumber, taskReport);
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
        cell.textContent = "T" + wpNumber + "." + taskReport.task.sequenceNumber;
        row.appendChild(cell);

        for (month = 1; month <= durationMonths; month++) {
            cell = document.createElement("td");
            cell.textContent = months[month] !== undefined ? months[month].workedHours : "0";
            row.appendChild(cell);
        }

        tbody.appendChild(row);
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
