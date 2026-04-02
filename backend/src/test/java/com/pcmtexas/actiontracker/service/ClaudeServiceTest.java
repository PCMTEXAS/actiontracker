package com.pcmtexas.actiontracker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ClaudeServiceTest {

    @Test
    void contextLoads_claudeServiceInstantiates() {
        ClaudeService service = new ClaudeService();
        ReflectionTestUtils.setField(service, "apiKey", "test-key");
        ReflectionTestUtils.setField(service, "model", "claude-test-model");
        ReflectionTestUtils.setField(service, "baseUrl", "https://api.anthropic.com/v1");
        assertThat(service).isNotNull();
    }
}
