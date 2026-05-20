package com.couragegang.config.service;

import com.couragegang.config.api.dto.WorkspaceModels.Workspace;
import com.couragegang.config.api.dto.WorkspaceModels.WorkspaceCreateRequest;
import com.couragegang.config.api.dto.WorkspaceModels.WorkspaceListResponse;
import com.couragegang.config.api.dto.WorkspaceModels.WorkspacePatchRequest;
import com.couragegang.config.error.ConfigApiException;
import com.couragegang.config.repo.WorkspaceRepository;
import com.couragegang.config.repo.WorkspaceRepository.WorkspaceRow;
import io.micronaut.http.HttpStatus;
import jakarta.annotation.Nullable;
import jakarta.inject.Singleton;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Singleton
public final class WorkspaceService {

    public static final String DEFAULT_WORKSPACE_SLUG = "default";
    public static final String DEFAULT_WORKSPACE_NAME = "Default workspace";

    private final WorkspaceRepository workspaces;

    public WorkspaceService(WorkspaceRepository workspaces) {
        this.workspaces = workspaces;
    }

    public Workspace bootstrapDefault(UUID orgId, UUID defaultGroupId, @Nullable String orgName) {
        try {
            var existing = workspaces.findByGroupAndSlug(defaultGroupId, DEFAULT_WORKSPACE_SLUG);
            if (existing.isPresent()) {
                return toDto(existing.get());
            }
            var name = orgName != null && !orgName.isBlank()
                    ? orgName.trim() + " — " + DEFAULT_WORKSPACE_NAME
                    : DEFAULT_WORKSPACE_NAME;
            var id = workspaces.insert(orgId, defaultGroupId, name, DEFAULT_WORKSPACE_SLUG);
            return toDto(workspaces.findById(id).orElseThrow());
        } catch (SQLException e) {
            if (isUniqueViolation(e)) {
                return bootstrapDefault(orgId, defaultGroupId, orgName);
            }
            throw new IllegalStateException(e);
        }
    }

    public WorkspaceListResponse list(UUID orgId, Optional<UUID> groupId, int limit) {
        try {
            var rows = workspaces.listByOrg(orgId, groupId, limit);
            return new WorkspaceListResponse(rows.stream().map(WorkspaceService::toDto).toList());
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public Workspace create(UUID orgId, WorkspaceCreateRequest req) {
        try {
            var slug = normalizeSlug(req.slug());
            if (workspaces.findByGroupAndSlug(req.groupId(), slug).isPresent()) {
                throw new ConfigApiException(HttpStatus.CONFLICT, "CONFLICT", "workspace slug taken in group");
            }
            var id = workspaces.insert(orgId, req.groupId(), req.name().trim(), slug);
            return toDto(workspaces.findById(id).orElseThrow());
        } catch (SQLException e) {
            if (isUniqueViolation(e)) {
                throw new ConfigApiException(HttpStatus.CONFLICT, "CONFLICT", "workspace slug taken");
            }
            throw new IllegalStateException(e);
        }
    }

    public Workspace get(UUID workspaceId) {
        try {
            return toDto(workspaces.findById(workspaceId).orElseThrow(this::notFound));
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public Workspace patch(UUID workspaceId, WorkspacePatchRequest req) {
        try {
            workspaces.findById(workspaceId).orElseThrow(this::notFound);
            if (req.status() != null && !req.status().equals("active") && !req.status().equals("archived")) {
                throw new ConfigApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "invalid status");
            }
            workspaces.update(workspaceId, req.name(), req.status());
            return toDto(workspaces.findById(workspaceId).orElseThrow());
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String normalizeSlug(String slug) {
        return slug.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean isUniqueViolation(SQLException e) {
        return "23505".equals(e.getSQLState());
    }

    private ConfigApiException notFound() {
        return new ConfigApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "workspace not found");
    }

    private static Workspace toDto(WorkspaceRow r) {
        return new Workspace(r.id(), r.orgId(), r.groupId(), r.name(), r.slug(), r.status(), r.createdAt());
    }
}
