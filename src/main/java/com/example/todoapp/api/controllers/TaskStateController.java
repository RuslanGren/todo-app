package com.example.todoapp.api.controllers;

import com.example.todoapp.api.dto.TaskStateDto;
import com.example.todoapp.store.services.TaskStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TaskStateController {
    private final TaskStateService taskStateService;

    public static final String GET_TASK_STATES = "/api/projects/{project_id}/task-states";
    public static final String CREATE_TASK_STATE = "/api/projects/{project_id}/task-states";

    @GetMapping(GET_TASK_STATES)
    public List<TaskStateDto> getTaskStates(@PathVariable(name = "project_id") Long projectId) {
        return taskStateService.getTaskStates(projectId);
    }

    @PostMapping(CREATE_TASK_STATE)
    public TaskStateDto createTaskState(
            @PathVariable(name = "project_id") Long projectId,
            @RequestParam(name = "task_state_name") String taskStateName) {
        return taskStateService.createTaskState(projectId, taskStateName);
    }
}
