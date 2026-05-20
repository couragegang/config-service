package com.couragegang.config.api.dto;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class WorkspaceModels {

    private WorkspaceModels() {}

    @Serdeable
    public record Workspace(
            UUID id,
            UUID orgId,
            UUID groupId,
            String name,
            String slug,
            String status,
            Instant createdAt) {}

    @Serdeable
    public record WorkspaceCreateRequest(
            @NotBlank String name, @NotBlank String slug, @NotNull UUID groupId) {}

    @Serdeable
    public record WorkspacePatchRequest(String name, String status) {}

    @Serdeable
    public record WorkspaceListResponse(List<Workspace> items) {}

    @Serdeable
    public record BootstrapDefaultWorkspaceRequest(@NotNull UUID defaultGroupId, String orgName) {}
}
