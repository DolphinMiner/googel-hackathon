package com.ctrip.flight.intl.exchange.hackathonnfc.ai.google;


import com.ctrip.flight.intl.exchange.hackathonnfc.ai.google.exception.GoogleCloudInteractionException;
import com.ctrip.flight.intl.exchange.hackathonnfc.ai.google.factory.GoogleVertexAIClientFactory;
import com.ctrip.flight.intl.exchange.hackathonnfc.util.FileUtils;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.PartMaker;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Base64;

/**
 * @author white
 * created at 2025-06-10 10:16
 * Vertex AI
 * @version 1.0
 */
@Component
@Slf4j

public class GoogleCloudVertexAIServiceFacade {

    private final static String MIME_TYPE = "application/pdf";
    private final static String PNG_TYPE = "image/png";
    private final static String JPG_TYPE = "image/jpeg";
    private final GoogleVertexAIClientFactory vertexAIModelFactory;

    private VertexAI vertexAI;

    GoogleCloudVertexAIServiceFacade(@Autowired GoogleVertexAIClientFactory vertexAIModelFactory) throws Exception {
        this.vertexAIModelFactory = vertexAIModelFactory;
        vertexAI = vertexAIModelFactory.getVertexAIClient();
    }


    /**
     * 询问VertexAI 问题
     *
     * @param prompt 问题
     * @return String 回答内容
     */
    public String chatAboutQuestion(final String prompt) {

        try {
            // 附件数据转换
            Content content = ContentMaker.fromString(prompt);
            // 创建模型
            final GenerativeModel model = vertexAIModelFactory.createGenerativeModel(vertexAI);
            // 开始问询
            final GenerateContentResponse generateContentResponse = model.generateContent(content);

            return ResponseHandler.getText(generateContentResponse);
        } catch (Throwable e) {
            throw new GoogleCloudInteractionException("GoogleCloudVertexAIServiceFacade chatAboutQuestion error", e);
        }
    }

    public String chatAboutAttachment(final String prompt, final String attachmentDataBase64) {
        try {
            // 附件数据转换
            final Part document = this.convertFileData2PartForJPG(attachmentDataBase64);
            final Content content = ContentMaker.fromMultiModalData(document, prompt);
            // 创建模型
            final GenerativeModel model = vertexAIModelFactory.createGenerativeModel(vertexAI);
            // 开始问询
            final GenerateContentResponse generateContentResponse = model.generateContent(content);
            return ResponseHandler.getText(generateContentResponse);
        } catch (Throwable e) {
            log.error("GoogleCloudVertexAIServiceFacade chatAboutAttachment error", e);
            throw new GoogleCloudInteractionException("GoogleCloudVertexAIServiceFacade chatAboutAttachment error", e);
        }
    }


    /**
     * 转换文件内容为Part
     *
     * @param attachmentDataBase64 base64编码后的文件内容
     * @return Part
     */
    private Part convertFileData2PartForPNG(final String attachmentDataBase64) {
        final byte[] dataBytes = Base64.getDecoder().decode(FileUtils.zipData2Base64(attachmentDataBase64));
        return PartMaker.fromMimeTypeAndData(PNG_TYPE, dataBytes);
    }


    private Part convertFileData2PartForJPG(final String attachmentDataBase64) {
        final byte[] dataBytes;

        dataBytes = jodd.util.Base64.decode(attachmentDataBase64);
        return PartMaker.fromMimeTypeAndData(JPG_TYPE, dataBytes);
    }

}
