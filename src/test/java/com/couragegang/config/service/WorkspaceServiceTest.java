package com.couragegang.config.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.couragegang.config.api.dto.WorkspaceModels.WorkspaceCreateRequest;
import com.couragegang.config.api.dto.WorkspaceModels.WorkspacePatchRequest;
import com.couragegang.config.error.ConfigApiException;
import com.couragegang.config.repo.WorkspaceRepository;
import com.couragegang.config.repo.WorkspaceRepository.WorkspaceRow;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkspaceServiceTest {

    @Mock
    WorkspaceRepository repo;

    WorkspaceService svc;

    UUID orgId = UUID.randomUUID();
    UUID groupId = UUID.randomUUID();
    UUID wsId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        svc = new WorkspaceService(repo);
    }

    @Test
    void bootstrapDefault_createsWhenMissing() throws SQLException {
        when(repo.findByGroupAndSlug(groupId, "default")).thenReturn(Optional.empty());
        when(repo.insert(eq(orgId), eq(groupId), any(), eq("default"))).thenReturn(wsId);
        when(repo.findById(wsId))
                .thenReturn(Optional.of(row(wsId, "Acme — Default workspace", "default")));

        var ws = svc.bootstrapDefault(orgId, groupId, "Acme");

        assertThat(ws.slug()).isEqualTo("default");
        verify(repo).insert(orgId, groupId, "Acme — Default workspace", "default");
    }

    @Test
    void bootstrapDefault_idempotent() throws SQLException {
        when(repo.findByGroupAndSlug(groupId, "default")).thenReturn(Optional.of(row(wsId, "Default workspace", "default")));

        var ws = svc.bootstrapDefault(orgId, groupId, "Acme");

        assertThat(ws.id()).isEqualTo(wsId);
    }

    @Test
    void listByOrg() throws SQLException {
        when(repo.listByOrg(orgId, Optional.empty(), 50)).thenReturn(List.of(row(wsId, "W", "default")));

        var page = svc.list(orgId, Optional.empty(), 50);
        assertThat(page.items()).hasSize(1);
    }

    @Test
    void createWorkspace() throws SQLException {
        when(repo.findByGroupAndSlug(groupId, "proj")).thenReturn(Optional.empty());
        when(repo.insert(orgId, groupId, "Proj", "proj")).thenReturn(wsId);
        when(repo.findById(wsId)).thenReturn(Optional.of(row(wsId, "Proj", "proj")));

        var ws = svc.create(orgId, new WorkspaceCreateRequest("Proj", "proj", groupId));
        assertThat(ws.name()).isEqualTo("Proj");
    }

    @Test
    void bootstrapUsesDefaultNameWhenOrgNameBlank() throws SQLException {
        when(repo.findByGroupAndSlug(groupId, "default")).thenReturn(Optional.empty());
        when(repo.insert(orgId, groupId, WorkspaceService.DEFAULT_WORKSPACE_NAME, "default")).thenReturn(wsId);
        when(repo.findById(wsId))
                .thenReturn(Optional.of(row(wsId, WorkspaceService.DEFAULT_WORKSPACE_NAME, "default")));

        var ws = svc.bootstrapDefault(orgId, groupId, "  ");

        assertThat(ws.name()).isEqualTo(WorkspaceService.DEFAULT_WORKSPACE_NAME);
    }

    @Test
    void getNotFound() throws SQLException {
        when(repo.findById(wsId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> svc.get(wsId)).isInstanceOf(ConfigApiException.class);
    }

    @Test
    void patchRejectsInvalidStatus() throws SQLException {
        when(repo.findById(wsId)).thenReturn(Optional.of(row(wsId, "W", "default")));

        assertThatThrownBy(() -> svc.patch(wsId, new WorkspacePatchRequest(null, "deleted")))
                .isInstanceOf(ConfigApiException.class);
    }

    @Test
    void createConflictWhenSlugTaken() throws SQLException {
        when(repo.findByGroupAndSlug(groupId, "proj")).thenReturn(Optional.of(row(wsId, "Proj", "proj")));

        assertThatThrownBy(() -> svc.create(orgId, new WorkspaceCreateRequest("Proj", "proj", groupId)))
                .isInstanceOf(ConfigApiException.class);
    }

    @Test
    void createUniqueViolationSql() throws SQLException {
        when(repo.findByGroupAndSlug(groupId, "proj")).thenReturn(Optional.empty());
        var sql = new SQLException("dup", "23505");
        when(repo.insert(orgId, groupId, "Proj", "proj")).thenThrow(sql);

        assertThatThrownBy(() -> svc.create(orgId, new WorkspaceCreateRequest("Proj", "proj", groupId)))
                .isInstanceOf(ConfigApiException.class);
    }

    @Test
    void bootstrapRetriesOnUniqueViolation() throws SQLException {
        when(repo.findByGroupAndSlug(groupId, "default"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(row(wsId, "Default workspace", "default")));
        var sql = new SQLException("dup", "23505");
        when(repo.insert(orgId, groupId, WorkspaceService.DEFAULT_WORKSPACE_NAME, "default")).thenThrow(sql);

        var ws = svc.bootstrapDefault(orgId, groupId, null);

        assertThat(ws.slug()).isEqualTo("default");
    }

    @Test
    void listWrapsSqlException() throws SQLException {
        when(repo.listByOrg(orgId, Optional.empty(), 5)).thenThrow(new SQLException("db"));

        assertThatThrownBy(() -> svc.list(orgId, Optional.empty(), 5)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void patchActiveStatusAllowed() throws SQLException {
        when(repo.findById(wsId)).thenReturn(Optional.of(row(wsId, "W", "default")), Optional.of(row(wsId, "W", "active")));

        svc.patch(wsId, new WorkspacePatchRequest(null, "active"));

        verify(repo).update(wsId, null, "active");
    }

    @Test
    void patchUpdatesWorkspace() throws SQLException {
        when(repo.findById(wsId)).thenReturn(Optional.of(row(wsId, "W", "default")));

        svc.patch(wsId, new WorkspacePatchRequest("New name", "archived"));

        verify(repo).update(wsId, "New name", "archived");
    }

    private WorkspaceRow row(UUID id, String name, String slug) {
        return new WorkspaceRow(id, orgId, groupId, name, slug, "active", Instant.parse("2026-01-01T00:00:00Z"));
    }
}
