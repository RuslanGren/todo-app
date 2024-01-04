package com.example.todoapp.store.services;

import com.example.todoapp.api.dto.AckDto;
import com.example.todoapp.api.dto.ProjectDto;

import java.util.List;
import java.util.Optional;

public interface ProjectService {
    List<ProjectDto> fetchProjects(Optional<String> optionalPrefixName);

    AckDto deleteProject(Long projectId);

    ProjectDto createOrUpdateProject(Optional<Long> optionalProjectId, Optional<String> optionalProjectName);
}
