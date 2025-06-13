package com.ctrip.flight.intl.exchange.hackathonnfc.ai.google.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import qunar.tc.qconfig.client.spring.QMapConfig;

import java.util.Map;

/**
 * @author white
 * created at 2025-06-10 10:16
 * GoogleVertexAIModelConfiguration
 * @version 1.0
 */
@Component
@Slf4j
public class GoogleVertexAIModelConfig {

    @QMapConfig("google_vertex_ai_model_config.properties")
    private Map<String, String> configs;

    /**
     * 获取最大输出tokens
     * Output token limit determines the maximum amount of text output from one prompt.
     * A token is approximately four characters.
     *
     * @return 最大输出tokens 默认是8192
     */
    public Integer getMaxOutputTokens() {
        final Integer defaultValue = 8192;
        if (null == configs || configs.isEmpty()) {
            return defaultValue;
        }
        final String value = configs.getOrDefault("maxOutputTokens", String.valueOf(defaultValue));
        return Integer.parseInt(value);
    }

    /**
     * 获取Temperature
     * Temperature controls the randomness in token selection
     * <p>
     * 1、A lower temperature is good when you expect a true or correct response. A temperature of 0 means the highest probability token is always selected.
     * 2、A higher temperature can lead to diverse or unexpected results. Some models have a higher temperature max to encourage more random responses.
     * The selected model gemini-1.5-pro-001 has a temperature range of 0 - 2 and a default of 1
     *
     * @return Temperature 默认是1.0
     */
    public Float getTemperature() {
        final Float defaultValue = 1.0F;
        if (null == configs || configs.isEmpty()) {
            return defaultValue;
        }
        final String value = configs.getOrDefault("temperature", String.valueOf(defaultValue));
        return Float.parseFloat(value);
    }

    /**
     * 获取top-P配置
     * Top-p changes how the model selects tokens for output.
     * Tokens are selected from most probable to least until the sum of their probabilities equals the top-p value.
     * For example, if tokens A, B, and C have a probability of .3, .2, and .1 and the top-p value is .5,
     * then the model will select either A or B as the next token (using temperature).
     *
     * @return topP 默认是1.0
     */
    public Float getTopP() {
        final Float defaultValue = 0.95F;
        if (null == configs || configs.isEmpty()) {
            return defaultValue;
        }
        final String value = configs.getOrDefault("topP", String.valueOf(defaultValue));
        return Float.parseFloat(value);
    }

    /**
     * 模型名称
     * The maximum lifespan for a custom model is 18 months as of the GA release.
     * You must create and train a new model to continue classifying content after that amount of time.
     * @return 模型名称，默认是gemini-1.5-pro-001
     */
    public String getModelName() {
        final String defaultValue = "gemini-1.5-pro-001";
        if (null == configs || configs.isEmpty()) {
            return defaultValue;
        }
        return configs.getOrDefault("modelName", defaultValue);
    }

    /**
     * 服务部署区域
     * @return 服务部署区域，默认是us-central1
     */
    public String getVertexAiLocation() {
        final String defaultValue = "us-central1";
        if (null == configs || configs.isEmpty()) {
            return defaultValue;
        }
        return configs.getOrDefault("vertexAiLocation", defaultValue);
    }

    /**
     * 获取认证文件名称
     * @return 认证文件名称
     */
    public String getCredentialsFileName() {
        final String defaultValue = "sandbox_cred.txt";
        if (null == configs || configs.isEmpty()) {
            return defaultValue;
        }
        return configs.getOrDefault("credentialsFileName", defaultValue);
    }

    /**
     * 获取项目ID
     * @return 项目ID
     */
    public String getProjectId() {
        final String defaultValue = "striking-canyon-400403";
        if (null == configs || configs.isEmpty()) {
            return defaultValue;
        }
        return configs.getOrDefault("projectId", defaultValue);
    }

}
