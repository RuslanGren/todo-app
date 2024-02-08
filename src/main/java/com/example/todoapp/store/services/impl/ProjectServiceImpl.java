package com.example.todoapp.store.services.impl;

import com.example.todoapp.api.controllers.helpers.ControllerHelper;
import com.example.todoapp.api.dto.AckDto;
import com.example.todoapp.api.dto.ProjectDto;
import com.example.todoapp.api.exceptions.BadRequestException;
import com.example.todoapp.api.factories.ProjectDtoFactory;
import com.example.todoapp.store.entities.ProjectEntity;
import com.example.todoapp.store.repositories.ProjectRepository;
import com.example.todoapp.store.services.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;

    private final ProjectDtoFactory projectDtoFactory;

    private final ControllerHelper controllerHelper;

    @Transactional(readOnly = true)
    @Override
    public List<ProjectDto> fetchProjects(Optional<String> optionalPrefixName) {
        optionalPrefixName = optionalPrefixName.filter(prefixName -> !prefixName.trim().isEmpty());

        Stream<ProjectEntity> projectStream = optionalPrefixName
                .map(projectRepository::streamAllByNameStartsWithIgnoreCase)
                .orElseGet(projectRepository::streamAllBy);

        return projectStream
                .map(projectDtoFactory::makeProjectDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public AckDto deleteProject(Long projectId) {
        controllerHelper.getProjectOrThrowException(projectId);

        projectRepository.deleteById(projectId);

        return AckDto.makeDefault(true);
    }

    @Transactional
    @Override
    public ProjectDto createOrUpdateProject(Optional<Long> optionalProjectId, Optional<String> optionalProjectName) {
        optionalProjectName = optionalProjectName.filter(projectName -> !projectName.trim().isEmpty());

        boolean isCreate = optionalProjectId.isEmpty();

        if (isCreate && optionalProjectName.isEmpty()) {
            throw new BadRequestException("Project name can't be empty.");
        }

        final ProjectEntity project = optionalProjectId
                .map(controllerHelper::getProjectOrThrowException)
                .orElseGet(() -> ProjectEntity.builder().build());

        optionalProjectName
                .ifPresent(projectName -> {
                    projectRepository
                            .findByName(projectName)
                            .filter(anotherProject -> !Objects.equals(anotherProject.getId(), project.getId()))
                            .ifPresent(anotherProject -> {
                                throw new BadRequestException(String.format("Project %s already exists.", projectName));
                            });
                    project.setName(projectName);
                    project.setUpdatedAt(Instant.now());
                });
        final ProjectEntity savedProject = projectRepository.saveAndFlush(project);

        return projectDtoFactory.makeProjectDto(savedProject);
    }
}
