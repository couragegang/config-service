package com.couragegang.config.api;

import com.couragegang.config.api.dto.WorkspaceModels.BootstrapDefaultWorkspaceRequest;
import com.couragegang.config.api.dto.WorkspaceModels.Workspace;
import com.couragegang.config.service.WorkspaceService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import jakarta.validation.Valid;
import java.util.UUID;

@Controller("/internal")
public class InternalController {

    private final WorkspaceService workspaces;

    public InternalController(WorkspaceService workspaces) {
        this.workspaces = workspaces;
    }

    @Post("/orgs/{orgId}/bootstrap-default-workspace")
    public HttpResponse<Workspace> bootstrapDefault(
            @PathVariable UUID orgId, @Body @Valid BootstrapDefaultWorkspaceRequest body) {
        return HttpResponse.created(
                workspaces.bootstrapDefault(orgId, body.defaultGroupId(), body.orgName()));
    }
}
