package com.example.todoapp.store.services.impl;

import com.example.todoapp.api.controllers.helpers.ControllerHelper;
import com.example.todoapp.api.dto.AckDto;
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

    @Override
    @Transactional
    public TaskStateDto changeTaskStatePosition(Long taskStateId, Optional<Long> optionalLeftTaskStateId) {
        // taskStateId таска яку ми туда сюда хуярим
        // leftTaskStateId таска яка зліва нового місця taskStateId
        // получаємо правого старого сусіда leftTaskStateId і їбашим його правим сусідом taskStateId
        // робимо taskStateId новим правим сусідом leftTaskStateId
        // робимо leftTaskStateId новим лівом сусідом taskStateId

        TaskStateEntity changeTaskState = getTaskStateOrThrowException(taskStateId);

        ProjectEntity project = changeTaskState.getProject();

        Optional<Long> optionalOldLeftTaskStateId = changeTaskState
                .getLeftTaskState()
                .map(TaskStateEntity::getId);

        if (optionalOldLeftTaskStateId.equals(optionalLeftTaskStateId)) { // то коли користувач ніхуя не міняє позицію таски
            return taskStateDtoFactory.makeTaskStateDto(changeTaskState);
        }

        Optional<TaskStateEntity> optionalNewLeftTaskState;
        if (optionalLeftTaskStateId.isPresent()) {
            Long leftTaskStateId = optionalLeftTaskStateId.get();
            if (taskStateId.equals(leftTaskStateId)) {
                throw new BadRequestException("Left task state id equals changed task state");
            }

            TaskStateEntity leftTaskStateEntity = getTaskStateOrThrowException(leftTaskStateId);

            if (!project.getId().equals(leftTaskStateEntity.getProject().getId())) {
                throw new BadRequestException("Task state position can be changed within the same project");
            }

            optionalNewLeftTaskState = Optional.of(leftTaskStateEntity);
        } else {
            optionalNewLeftTaskState = Optional.empty();
        }

        Optional<TaskStateEntity> optionalNewRightTaskState;
        if (optionalNewLeftTaskState.isEmpty()) { // якщо лівого сусіда не існує тоді правий сусід є найправіша таска

            optionalNewRightTaskState = project
                    .getTaskStates()
                    .stream()
                    .filter(anotherTaskState -> anotherTaskState.getLeftTaskState().isEmpty())
                    .findAny();
        } else { // якщо лівий сусід існує тоді ми получаємо праву таску з лівої таски
            optionalNewRightTaskState = optionalNewLeftTaskState
                    .get()
                    .getRightTaskState();
        }

        replaceOldTaskStatePosition(changeTaskState);

        if (optionalNewLeftTaskState.isPresent()) {

            TaskStateEntity newLeftTaskState = optionalNewLeftTaskState.get();

            newLeftTaskState.setRightTaskState(changeTaskState);

            changeTaskState.setLeftTaskState(newLeftTaskState);
        } else {
            changeTaskState.setLeftTaskState(null);
        }

        if (optionalNewRightTaskState.isPresent()) {

            TaskStateEntity newRightTaskState = optionalNewRightTaskState.get();

            newRightTaskState.setLeftTaskState(changeTaskState);

            changeTaskState.setRightTaskState(newRightTaskState);
        } else {
            changeTaskState.setRightTaskState(null);
        }

        changeTaskState = taskStateRepository.saveAndFlush(changeTaskState);

        optionalNewLeftTaskState
                .ifPresent(taskStateRepository::saveAndFlush);

        optionalNewRightTaskState
                .ifPresent(taskStateRepository::saveAndFlush);

        return taskStateDtoFactory.makeTaskStateDto(changeTaskState);
    }

    @Override
    @Transactional
    public AckDto deleteTaskState(Long taskStateId) {
        TaskStateEntity changeTaskState = getTaskStateOrThrowException(taskStateId);

        replaceOldTaskStatePosition(changeTaskState);

        taskStateRepository.delete(changeTaskState);

        return AckDto.builder().answer(true).build();
    }

    private void replaceOldTaskStatePosition(TaskStateEntity changeTaskState) {
        Optional<TaskStateEntity> optionalOldLeftTaskState = changeTaskState.getLeftTaskState();
        Optional<TaskStateEntity> optionalOldRightTaskState = changeTaskState.getRightTaskState();

        optionalOldLeftTaskState // старому лівому переназначили правого сусіда старого правого
                .ifPresent(it -> {
                    it.setRightTaskState(optionalOldRightTaskState.orElse(null));

                    taskStateRepository.saveAndFlush(it);
                });

        optionalOldRightTaskState // старому правому переназначили лівого сусіда старого лівого
                .ifPresent(it -> {
                    it.setLeftTaskState(optionalOldLeftTaskState.orElse(null));

                    taskStateRepository.saveAndFlush(it);
                });
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
