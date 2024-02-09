package com.example.todoapp.store.services.impl;

import com.example.todoapp.api.controllers.helpers.ControllerHelper;
import com.example.todoapp.api.dto.TaskStateDto;
import com.example.todoapp.api.exceptions.BadRequestException;
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

        project
                .getTaskStates()
                .stream()
                .map(TaskStateEntity::getName)
                .filter(anotherTaskStateName -> anotherTaskStateName.equals(taskStateName))
                .findAny().ifPresent(it -> {
                    throw new BadRequestException(String.format("Task state with %s already exists", taskStateName));
                });

        Optional<TaskStateEntity> optionalAnotherTaskState = taskStateRepository
                .findTaskStateEntityByRightTaskStateIdIsNullAndProjectId(projectId);

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
}
