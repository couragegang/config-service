package com.couragegang.config.api;

import com.couragegang.config.api.dto.WorkspaceModels.Workspace;
import com.couragegang.config.api.dto.WorkspaceModels.WorkspaceCreateRequest;
import com.couragegang.config.api.dto.WorkspaceModels.WorkspaceListResponse;
import com.couragegang.config.api.dto.WorkspaceModels.WorkspacePatchRequest;
import com.couragegang.config.service.WorkspaceService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Patch;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import java.util.Optional;
import java.util.UUID;

@Controller
public final class WorkspacesController {

    private final WorkspaceService workspaces;

    public WorkspacesController(WorkspaceService workspaces) {
        this.workspaces = workspaces;
    }

    @Get("/orgs/{orgId}/workspaces")
    public HttpResponse<WorkspaceListResponse> list(
            @PathVariable UUID orgId,
            @Nullable @QueryValue("group_id") UUID groupId,
            @QueryValue(defaultValue = "50") int limit) {
        return HttpResponse.ok(workspaces.list(orgId, Optional.ofNullable(groupId), limit));
    }

    @Post("/orgs/{orgId}/workspaces")
    public HttpResponse<Workspace> create(
            @PathVariable UUID orgId, @Body @Valid WorkspaceCreateRequest body) {
        return HttpResponse.created(workspaces.create(orgId, body));
    }

    @Get("/workspaces/{workspaceId}")
    public HttpResponse<Workspace> get(@PathVariable UUID workspaceId) {
        return HttpResponse.ok(workspaces.get(workspaceId));
    }

    @Patch("/workspaces/{workspaceId}")
    public HttpResponse<Workspace> patch(
            @PathVariable UUID workspaceId, @Body @Valid WorkspacePatchRequest body) {
        return HttpResponse.ok(workspaces.patch(workspaceId, body));
    }
}
