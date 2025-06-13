package com.ctrip.flight.intl.exchange.hackathonnfc.service;

import com.ctrip.flight.intl.exchange.hackathonnfc.ai.google.GoogleCloudVertexAIServiceFacade;
import com.ctrip.flight.intl.exchange.hackathonnfc.dto.CommonResponse;
import com.ctrip.flight.intl.exchange.hackathonnfc.dto.FeedbackRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class TrainingService {

    private final GoogleCloudVertexAIServiceFacade googleCloudVertexAIServiceFacade;


    public CommonResponse process(FeedbackRequest request){
        String jpgStr = request.getImage();
        String prompt = request.getFreeText();
        String ans = googleCloudVertexAIServiceFacade.chatAboutAttachment(prompt, jpgStr);
        CommonResponse commonResponse = new CommonResponse();
        commonResponse.setErrorCode(0);
        commonResponse.setErrorMsg(ans);
        return commonResponse;
    }

}
