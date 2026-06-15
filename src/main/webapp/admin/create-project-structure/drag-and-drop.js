
function CreateProjectStructureDragAndDrop(container) {
    let sourceWPIndex = null;
    let sourceTaskIndex = null;

    // drag and drop events are registered
    this.registerEvents = function(orchestrator) {
        container.addEventListener("dragstart", function(event) {
            let target = event.target;
            let taskBox;

            if (target.classList === undefined || !target.classList.contains("task-drag-handle")) {
                return;
            }

            sourceWPIndex = parseInt(target.getAttribute("data-wp-index"), 10);
            sourceTaskIndex = parseInt(target.getAttribute("data-task-index"), 10);
            event.dataTransfer.effectAllowed = "move";
            event.dataTransfer.setData("text/plain", sourceWPIndex + ":" + sourceTaskIndex);

            taskBox = target.closest(".task-box");
            if (taskBox !== null) {
                taskBox.classList.add("dragging");
            }
        }, false);

        container.addEventListener("dragover", function(event) {
            if (sourceWPIndex === null || getDropTarget(event.target, event.clientY) === null) {
                return;
            }

            event.preventDefault();
            event.dataTransfer.dropEffect = "move";
        }, false);

        container.addEventListener("drop", function(event) {
            let dropTarget;

            if (sourceWPIndex === null) {
                return;
            }

            dropTarget = getDropTarget(event.target, event.clientY);
            if (dropTarget === null) {
                return;
            }

            event.preventDefault();
            orchestrator.moveTask(sourceWPIndex, sourceTaskIndex, dropTarget.wpIndex, dropTarget.taskIndex);
            sourceWPIndex = null;
            sourceTaskIndex = null;
        }, false);

        container.addEventListener("dragend", function() {
            let draggingTask = container.querySelector(".dragging");

            if (draggingTask !== null) {
                draggingTask.classList.remove("dragging");
            }

            sourceWPIndex = null;
            sourceTaskIndex = null;
        }, false);
    };

    // a task drop target is detected
    function getDropTarget(target, mouseY) {
        if (target.closest === undefined) {
            return null;
        }

        let taskBox = target.closest(".task-box");
        let taskList = target.closest(".task-list");
        let taskIndex;
        let taskMiddle;

        if (taskBox !== null && container.contains(taskBox)) {
            taskIndex = parseInt(taskBox.getAttribute("data-task-index"), 10);
            taskMiddle = taskBox.getBoundingClientRect().top + taskBox.offsetHeight / 2;
            if (mouseY > taskMiddle) {
                taskIndex++;
            }

            return {
                wpIndex: parseInt(taskBox.getAttribute("data-wp-index"), 10),
                taskIndex: taskIndex
            };
        }

        if (taskList !== null && container.contains(taskList)) {
            return {
                wpIndex: parseInt(taskList.getAttribute("data-wp-index"), 10),
                taskIndex: parseInt(taskList.getAttribute("data-task-count"), 10)
            };
        }

        return null;
    }
}
