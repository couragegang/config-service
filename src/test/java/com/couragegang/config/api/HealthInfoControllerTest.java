package com.couragegang.config.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HealthInfoControllerTest {

    @Test
    void rootListsWorkspacesPath() {
        assertThat(new HealthInfoController().root().get("workspaces"))
                .contains("workspaces");
    }
}
