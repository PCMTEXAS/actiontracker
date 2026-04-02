package com.pcmtexas.actiontracker.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AwsSecretsConfig implements EnvironmentPostProcessor, Ordered {

    private static final String SECRETS_ARN_PROPERTY = "aws.secrets.arn";
    private static final String AWS_REGION_PROPERTY = "aws.region";
    private static final String DEFAULT_REGION = "us-east-1";
    private static final String SOURCE_NAME = "awsSecretsManager";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String secretArn = environment.getProperty(SECRETS_ARN_PROPERTY);
        if (secretArn == null || secretArn.isBlank()) {
            log.debug("aws.secrets.arn not set — skipping AWS Secrets Manager loading");
            return;
        }

        String region = environment.getProperty(AWS_REGION_PROPERTY, DEFAULT_REGION);
        log.info("Loading secrets from AWS Secrets Manager: ARN={}, region={}", secretArn, region);

        try {
            SecretsManagerClient client = SecretsManagerClient.builder()
                    .region(Region.of(region))
                    .build();

            GetSecretValueResponse response = client.getSecretValue(
                    GetSecretValueRequest.builder()
                            .secretId(secretArn)
                            .build()
            );

            String secretJson = response.secretString();
            if (secretJson == null || secretJson.isBlank()) {
                log.warn("AWS Secrets Manager returned empty secret for ARN: {}", secretArn);
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> secrets = mapper.readValue(secretJson,
                    new TypeReference<Map<String, String>>() {});

            Map<String, Object> props = new HashMap<>(secrets);
            environment.getPropertySources().addFirst(
                    new MapPropertySource(SOURCE_NAME, props)
            );

            log.info("Successfully loaded {} secrets from AWS Secrets Manager", secrets.size());

        } catch (Exception e) {
            log.error("Failed to load secrets from AWS Secrets Manager (ARN: {}): {}",
                    secretArn, e.getMessage());
            // Intentionally not rethrowing — app can still start with env vars as fallback
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 10;
    }
}
