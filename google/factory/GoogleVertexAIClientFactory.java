package com.ctrip.flight.intl.exchange.hackathonnfc.ai.google.factory;

import com.ctrip.flight.intl.exchange.hackathonnfc.ai.google.exception.GoogleCloudInteractionException;
import com.ctrip.flight.intl.exchange.hackathonnfc.ai.google.config.GoogleVertexAIModelConfig;
import com.ctrip.flight.intl.exchange.hackathonnfc.ai.google.config.HttpProxyConfig;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.core.GaxProperties;
import com.google.api.gax.grpc.InstantiatingGrpcChannelProvider;
import com.google.api.gax.rpc.FixedHeaderProvider;
import com.google.api.gax.rpc.HeaderProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vertexai.Constants;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.*;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.common.collect.Lists;
import io.grpc.HttpConnectProxiedSocketAddress;
import io.grpc.ProxiedSocketAddress;
import io.grpc.ProxyDetector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

/**
 * @author cj.huang
 * created at 2024-06-21 11:48
 * GoogleVertexAIClientFactory
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class GoogleVertexAIClientFactory {

    private final HttpProxyConfig httpProxyConfig;

    private final GoogleVertexAIModelConfig modelConfig;

    private final static String CREDENTIAL_DIR = "credential";

    /**
     * 获取Vertex AI客户端
     * @return VertexAI client
     */
    public VertexAI getVertexAIClient() throws IOException {
        // 加载认证文件信息
        final Credentials credentials = loadCredentials(modelConfig.getCredentialsFileName());
        final PredictionServiceSettings predictionServiceSettings = createPredictionServiceClient(credentials);
        return new VertexAI.Builder()
                .setProjectId(modelConfig.getProjectId())
                .setLocation(modelConfig.getVertexAiLocation())
                .setCredentials(credentials)
                .setPredictionClientSupplier(() -> {
                    try {
                        return PredictionServiceClient.create(predictionServiceSettings);
                    } catch (IOException e) {
                        log.error("vertex ai create PredictionServiceClient error", e);
                        throw new GoogleCloudInteractionException("vertex ai create PredictionServiceClient error", e);
                    }
                })
                .build();
    }

    /**
     * 获取大模型
     * @return 大模型，模型配置参看：GoogleVertexAIModelConfig
     */
    public GenerativeModel createGenerativeModel(final VertexAI vertexAI) throws IOException {
        // 获取Vertex AI Client
        final GenerationConfig generationConfig =
                GenerationConfig.newBuilder()
                        .setMaxOutputTokens(modelConfig.getMaxOutputTokens())
                        .setTemperature(modelConfig.getTemperature())
                        .setTopP(modelConfig.getTopP())
                        .build();

        final List<SafetySetting> safetySettings = Arrays.asList(
                SafetySetting.newBuilder()
                        .setCategory(HarmCategory.HARM_CATEGORY_HATE_SPEECH)
                        .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_ONLY_HIGH)
                        .build(),
                SafetySetting.newBuilder()
                        .setCategory(HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT)
                        .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_ONLY_HIGH)
                        .build(),
                SafetySetting.newBuilder()
                        .setCategory(HarmCategory.HARM_CATEGORY_SEXUALLY_EXPLICIT)
                        .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_ONLY_HIGH)
                        .build(),
                SafetySetting.newBuilder()
                        .setCategory(HarmCategory.HARM_CATEGORY_HARASSMENT)
                        .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_ONLY_HIGH)
                        .build()
        );
        return new GenerativeModel.Builder()
                .setModelName(modelConfig.getModelName())
                .setVertexAi(vertexAI)
                .setGenerationConfig(generationConfig)
                .setSafetySettings(safetySettings)
                .build();
    }

    /**
     * 自定义通信代理，PredictionServiceSettings
     * @param credentials 凭证
     * @return PredictionServiceSettings
     */
    private PredictionServiceSettings createPredictionServiceClient(final Credentials credentials) throws IOException {
        final PredictionServiceSettings.Builder builder = PredictionServiceSettings.newBuilder();
        builder.setTransportChannelProvider(InstantiatingGrpcChannelProvider.newBuilder()
                .setMaxInboundMessageSize(Integer.MAX_VALUE).setChannelConfigurator(managedChannelBuilder -> managedChannelBuilder.proxyDetector(
                        new ProxyDetector() {
                            @Nullable
                            @Override
                            public ProxiedSocketAddress proxyFor(SocketAddress socketAddress)
                                    throws IOException {
                                return HttpConnectProxiedSocketAddress.newBuilder()
                                        .setProxyAddress(new InetSocketAddress(httpProxyConfig.getHttpProxyHost(), httpProxyConfig.getHttpProxyPort()))
                                        .setTargetAddress((InetSocketAddress) socketAddress)
                                        .build();
                            }
                        })).build());

        builder.setEndpoint(String.format("%s:443", String.format("%s-aiplatform.googleapis.com", modelConfig.getVertexAiLocation())));
        builder.setCredentialsProvider(FixedCredentialsProvider.create(credentials));
        HeaderProvider headerProvider =
                FixedHeaderProvider.create(
                        "user-agent",
                        String.format(
                                "%s/%s",
                                Constants.USER_AGENT_HEADER,
                                GaxProperties.getLibraryVersion(PredictionServiceSettings.class)));
        builder.setHeaderProvider(headerProvider);
        return builder.build();
    }

    /**
     * 加载并创建凭证
     * @param credentialFileName 凭证文件名
     * @return Credentials
     */
    private Credentials loadCredentials(final String credentialFileName) {
        InputStream read = null;
        try {
            read = this.getClass().getClassLoader().getResourceAsStream(CREDENTIAL_DIR + "/" + credentialFileName);
            String credentialStr = IOUtils.toString(read);
            byte[] credentialByteAry = Base64.getDecoder().decode(credentialStr);
            read.close();
            InputStream in = new ByteArrayInputStream(credentialByteAry);
            return GoogleCredentials.fromStream(
                    Objects.requireNonNull(in),
                    new ProxiedHttpTransportFactory(httpProxyConfig.getHttpProxyHost(), httpProxyConfig.getHttpProxyPort())
            ).createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
        } catch (IOException e) {
            log.error("loadAndCreateCredentials error", e);
            throw new GoogleCloudInteractionException("loadAndCreateCredentials error", e);
        } finally {
            if (read != null) {
                try {
                    read.close();
                } catch (IOException e) {
                    log.error("google vertex ai load credential close read error", e);
                }
            }
        }
    }
}
