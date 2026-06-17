
(function() {
    let createProjectStructureOrchestrator;
    let verifyProjectsOrchestrator;

    // the page is initialized after loading
    window.addEventListener("load", function() {
        showStoredUserGreeting("adminGreeting");

        createProjectStructureOrchestrator = new CreateProjectStructureOrchestrator(projectSaved);
        verifyProjectsOrchestrator = new VerifyProjectsOrchestrator();

        createProjectStructureOrchestrator.start();
        verifyProjectsOrchestrator.start();
    }, false);

    // a saved project event is received
    function projectSaved() {
        verifyProjectsOrchestrator.reloadProjects();
    }
})();
