
(function() {
    let taskAssignmentOrchestrator;
    let monitorProjectsOrchestrator;
    let monitorCollaboratorsOrchestrator;

    // the page is initialized after loading
    window.addEventListener("load", function() {
        showStoredUserGreeting("managerPage-greeting");

        taskAssignmentOrchestrator = new TaskAssignmentOrchestrator(projectAssigned);
        monitorProjectsOrchestrator = new MonitorProjectsOrchestrator(projectConcluded);
        monitorCollaboratorsOrchestrator = new MonitorCollaboratorsOrchestrator();

        taskAssignmentOrchestrator.start();
        monitorProjectsOrchestrator.start();
        monitorCollaboratorsOrchestrator.start();
    }, false);

    // an assigned project event is received
    function projectAssigned() {
        taskAssignmentOrchestrator.reloadTasks();
        monitorProjectsOrchestrator.reloadProjects();
        monitorCollaboratorsOrchestrator.reloadCollaborators();
    }

    // a concluded project event is received
    function projectConcluded() {
        monitorProjectsOrchestrator.reloadProjects();
    }
})();
