package com.example.todoapp.api.controllers;

import com.example.todoapp.api.dto.AckDto;
import com.example.todoapp.api.dto.TaskStateDto;
import com.example.todoapp.store.services.TaskStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class TaskStateController {
    private final TaskStateService taskStateService;

    public static final String GET_TASK_STATES = "/api/projects/{project_id}/task-states";
    public static final String CREATE_TASK_STATE = "/api/projects/{project_id}/task-states";
    public static final String UPDATE_TASK_STATE = "/api/task-states/{task_state_id}";
    public static final String CHANGE_TASK_STATE_POSITION = "/api/task-states/{task_state_id}/position/change";
    public static final String DELETE_TASK_STATE = "/api/task-states/{task_state_id}";

    @GetMapping(GET_TASK_STATES)
    public List<TaskStateDto> getTaskStates(
            @PathVariable(name = "project_id") Long projectId
    ) {
        return taskStateService.getTaskStates(projectId);
    }

    @PostMapping(CREATE_TASK_STATE)
    public TaskStateDto createTaskState(
            @PathVariable(name = "project_id") Long projectId,
            @RequestParam(name = "task_state_name") String taskStateName
    ) {
        return taskStateService.createTaskState(projectId, taskStateName);
    }

    @PatchMapping(UPDATE_TASK_STATE)
    public TaskStateDto updateTaskState(
            @PathVariable(name = "task_state_id") Long taskStateId,
            @RequestParam(name = "task_state_name") String taskStateName
    ) {
        return taskStateService.updateTaskState(taskStateId, taskStateName);
    }

    @PatchMapping(CHANGE_TASK_STATE_POSITION)
    public TaskStateDto changeTaskStatePosition(
            @PathVariable(name = "task_state_id") Long taskStateId,
            @RequestParam(name = "left_task_state_id", required = false) Optional<Long> optionalLeftTaskStateId
    ) {
        return taskStateService.changeTaskStatePosition(taskStateId, optionalLeftTaskStateId);
    }

    @DeleteMapping(DELETE_TASK_STATE)
    public AckDto deleteTaskState(
            @PathVariable(name = "task_state_id") Long taskStateId
    ) {
        return taskStateService.deleteTaskState(taskStateId);
    }
}
