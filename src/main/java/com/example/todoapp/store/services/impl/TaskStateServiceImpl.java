package com.example.todoapp.store.services.impl;

import com.example.todoapp.api.controllers.helpers.ControllerHelper;
import com.example.todoapp.api.dto.TaskStateDto;
import com.example.todoapp.api.exceptions.BadRequestException;
import com.example.todoapp.api.exceptions.NotFoundException;
import com.example.todoapp.api.factories.TaskStateDtoFactory;
import com.example.todoapp.store.entities.ProjectEntity;
import com.example.todoapp.store.entities.TaskStateEntity;
import com.example.todoapp.store.repositories.TaskStateRepository;
import com.example.todoapp.store.services.TaskStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskStateServiceImpl implements TaskStateService {

    private final TaskStateRepository taskStateRepository;

    private final TaskStateDtoFactory taskStateDtoFactory;

    private final ControllerHelper controllerHelper;

    @Override
    @Transactional(readOnly = true)
    public List<TaskStateDto> getTaskStates(Long projectId) {
        ProjectEntity project = controllerHelper.getProjectOrThrowException(projectId);

        return project
                .getTaskStates()
                .stream()
                .map(taskStateDtoFactory::makeTaskStateDto)
                .collect(Collectors.toList());

    }

    @Override
    @Transactional
    public TaskStateDto createTaskState(Long projectId, String taskStateName) {

        if (taskStateName.isBlank()) {
            throw new BadRequestException("Task state name can't be empty");
        }

        ProjectEntity project = controllerHelper.getProjectOrThrowException(projectId);

        Optional<TaskStateEntity> optionalAnotherTaskState = Optional.empty();

        for (TaskStateEntity taskState: project.getTaskStates()) {
            if (taskState.getName().equalsIgnoreCase(taskStateName)) {
                throw new BadRequestException(String.format("Task state %s already exists", taskStateName));
            }

            if (taskState.getRightTaskState().isEmpty()) {
                optionalAnotherTaskState = Optional.of(taskState);
                break;
            }
        }

        TaskStateEntity taskState = taskStateRepository.saveAndFlush(
                TaskStateEntity.builder()
                        .name(taskStateName)
                        .project(project)
                        .build()
        );

        optionalAnotherTaskState
                .ifPresent(anotherTaskState -> {
                    taskState.setLeftTaskState(anotherTaskState);

                    anotherTaskState.setRightTaskState(taskState);

                    taskStateRepository.saveAndFlush(anotherTaskState);
                });

        final TaskStateEntity savedTaskState = taskStateRepository.saveAndFlush(taskState);

        return taskStateDtoFactory.makeTaskStateDto(savedTaskState);

    }

    @Override
    @Transactional
    public TaskStateDto updateTaskState(Long taskStateId, String taskStateName) {

        if (taskStateName.trim().isEmpty()) {
            throw new BadRequestException("Task state name can't be empty.");
        }

        TaskStateEntity taskState = getTaskStateOrThrowException(taskStateId);

        taskStateRepository
                .findTaskStateEntityByProjectIdAndNameContainsIgnoreCase(
                        taskState.getProject().getId(),
                        taskStateName
                )
                .filter(anotherTaskState -> !anotherTaskState.getId().equals(taskStateId))
                .ifPresent(anotherTaskState -> {
                    throw new BadRequestException(String.format("Task state %s already exists.", taskStateName));
                });

        taskState.setName(taskStateName);

        taskState = taskStateRepository.saveAndFlush(taskState);

        return taskStateDtoFactory.makeTaskStateDto(taskState);
    }

    private TaskStateEntity getTaskStateOrThrowException(Long taskStateId) {
        return taskStateRepository
                .findById(taskStateId)
                .orElseThrow(() ->
                        new NotFoundException(
                                String.format(
                                "Task state with %s id doesn't exist."
                                , taskStateId
                                )
                        )
                );
    }
}
