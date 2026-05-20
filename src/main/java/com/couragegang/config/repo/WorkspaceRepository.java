package com.couragegang.config.repo;

import jakarta.inject.Singleton;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.sql.DataSource;

@Singleton
public final class WorkspaceRepository {

    private final DataSource dataSource;

    public WorkspaceRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public UUID insert(UUID orgId, UUID groupId, String name, String slug) throws SQLException {
        try (var c = dataSource.getConnection();
                var ps = c.prepareStatement(
                        """
                        INSERT INTO workspaces (org_id, group_id, name, slug, status)
                        VALUES (?, ?, ?, ?, 'active')
                        RETURNING id
                        """)) {
            ps.setObject(1, orgId);
            ps.setObject(2, groupId);
            ps.setString(3, name);
            ps.setString(4, slug);
            try (var rs = ps.executeQuery()) {
                rs.next();
                return rs.getObject(1, UUID.class);
            }
        }
    }

    public Optional<WorkspaceRow> findById(UUID id) throws SQLException {
        return queryOne("WHERE w.id = ?", id);
    }

    public Optional<WorkspaceRow> findByGroupAndSlug(UUID groupId, String slug) throws SQLException {
        try (var c = dataSource.getConnection();
                var ps = c.prepareStatement(
                        """
                        SELECT w.id, w.org_id, w.group_id, w.name, w.slug, w.status, w.created_at
                        FROM workspaces w
                        WHERE w.group_id = ? AND w.slug = ?
                        """)) {
            ps.setObject(1, groupId);
            ps.setString(2, slug);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<WorkspaceRow> listByOrg(UUID orgId, Optional<UUID> groupId, int limit) throws SQLException {
        var sql =
                """
                SELECT w.id, w.org_id, w.group_id, w.name, w.slug, w.status, w.created_at
                FROM workspaces w
                WHERE w.org_id = ?
                """;
        if (groupId.isPresent()) {
            sql += " AND w.group_id = ?";
        }
        sql += " ORDER BY w.created_at ASC LIMIT ?";
        try (var c = dataSource.getConnection();
                var ps = c.prepareStatement(sql)) {
            ps.setObject(1, orgId);
            var idx = 2;
            if (groupId.isPresent()) {
                ps.setObject(idx++, groupId.get());
            }
            ps.setInt(idx, Math.min(Math.max(limit, 1), 100));
            var out = new ArrayList<WorkspaceRow>();
            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(mapRow(rs));
                }
            }
            return out;
        }
    }

    public void update(UUID id, String name, String status) throws SQLException {
        try (var c = dataSource.getConnection();
                var ps = c.prepareStatement(
                        """
                        UPDATE workspaces SET
                          name = COALESCE(?, name),
                          status = COALESCE(?, status)
                        WHERE id = ?
                        """)) {
            ps.setString(1, name);
            ps.setString(2, status);
            ps.setObject(3, id);
            ps.executeUpdate();
        }
    }

    private Optional<WorkspaceRow> queryOne(String whereClause, UUID id) throws SQLException {
        try (var c = dataSource.getConnection();
                var ps = c.prepareStatement(
                        """
                        SELECT w.id, w.org_id, w.group_id, w.name, w.slug, w.status, w.created_at
                        FROM workspaces w
                        """
                                + whereClause)) {
            ps.setObject(1, id);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    private static WorkspaceRow mapRow(java.sql.ResultSet rs) throws SQLException {
        return new WorkspaceRow(
                rs.getObject(1, UUID.class),
                rs.getObject(2, UUID.class),
                rs.getObject(3, UUID.class),
                rs.getString(4),
                rs.getString(5),
                rs.getString(6),
                rs.getTimestamp(7).toInstant());
    }

    public record WorkspaceRow(
            UUID id, UUID orgId, UUID groupId, String name, String slug, String status, Instant createdAt) {}
}
