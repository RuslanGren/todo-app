package com.example.todoapp.store.services.impl;

import com.example.todoapp.api.dto.ProjectDto;
import com.example.todoapp.api.exceptions.BadRequestException;
import com.example.todoapp.api.factories.ProjectDtoFactory;
import com.example.todoapp.store.repositories.ProjectRepository;
import com.example.todoapp.store.services.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectDtoFactory projectDtoFactory;

    @Transactional
    @Override
    public ProjectDto createProject(String name) {
        projectRepository
                .findByName(name)
                .ifPresent(project -> {
                   throw new BadRequestException(String.format("Project \"%s\" already exists.", name));
                });
    }

}
