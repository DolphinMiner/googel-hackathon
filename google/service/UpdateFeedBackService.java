package com.ctrip.flight.intl.exchange.hackathonnfc.service;

import com.ctrip.flight.intl.exchange.hackathonnfc.ai.google.GoogleCloudVertexAIServiceFacade;
import com.ctrip.flight.intl.exchange.hackathonnfc.dal.FltintlGoogleAccessiblePointsDao;
import com.ctrip.flight.intl.exchange.hackathonnfc.dal.FltintlGoogleCommentDao;
import com.ctrip.flight.intl.exchange.hackathonnfc.dal.entity.FltintlGoogleAccessiblePoints;
import com.ctrip.flight.intl.exchange.hackathonnfc.dal.entity.FltintlGoogleComment;
import com.ctrip.flight.intl.exchange.hackathonnfc.dto.AiReply;
import com.ctrip.flight.intl.exchange.hackathonnfc.dto.FeedbackRequest;
import com.ctrip.flight.intl.exchange.hackathonnfc.dto.FeedbackResponse;
import com.ctrip.platform.dal.dao.DalHints;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UpdateFeedBackService {

    private static final String prompt = "结合图片以及用户的评论信息，识别图片中的无障碍信息并返回一个json {type:无障碍类型, problem:用户潜在的问题点}";
    private final FltintlGoogleAccessiblePointsDao fltintlGoogleAccessiblePointsDao;
    private final FltintlGoogleCommentDao fltintlGoogleCommentDao;
    private final GoogleCloudVertexAIServiceFacade googleCloudVertexAIServiceFacade;

    public FeedbackResponse process(FeedbackRequest request) {
        FeedbackResponse res = new FeedbackResponse();
        try {
            // validate request
            // fixme AI描述信息 访问google api


            // update point table
            FltintlGoogleAccessiblePoints fltintlGoogleAccessiblePoint = new FltintlGoogleAccessiblePoints();
            // 访问AI
            String aiAnswer = googleCloudVertexAIServiceFacade.chatAboutAttachment(prompt, request.getImage());
            AiReply aiReply = parseAiAnswer(aiAnswer);
            if (request.getType() == null) {
                fltintlGoogleAccessiblePoint.setType(aiReply.getType());
            }
            fltintlGoogleAccessiblePoint.setType(request.getType());
            fltintlGoogleAccessiblePoint.setLatitude(BigDecimal.valueOf(request.getLatitude()));
            fltintlGoogleAccessiblePoint.setLongitude(BigDecimal.valueOf(request.getLongitude()));
            fltintlGoogleAccessiblePoint.setScenicAreaId(request.getScenicAreaId());
            fltintlGoogleAccessiblePoint.setType(request.getType());
            // update point table
            int insert1 = fltintlGoogleAccessiblePointsDao.insert(new DalHints(), fltintlGoogleAccessiblePoint);
            List<FltintlGoogleAccessiblePoints> fltintlGoogleAccessiblePoints = fltintlGoogleAccessiblePointsDao.queryBy(fltintlGoogleAccessiblePoint, new DalHints());
            Long pointId = fltintlGoogleAccessiblePoints.get(0).getPointId();
            if (insert1 == 0 || pointId == 0) {
                throw new RuntimeException("insert accessPoint table error, pointId ==0");
            }

            FltintlGoogleComment fltintlGoogleComment = new FltintlGoogleComment();
            fltintlGoogleComment.setInfo(request.getImage());
            fltintlGoogleComment.setFreeText(request.getFreeText());
            fltintlGoogleComment.setDescription(request.getDescription());
            fltintlGoogleComment.setFreeText(aiReply.getProblem());
            fltintlGoogleComment.setPointId(pointId);
            // update comment table
            int insert2 = fltintlGoogleCommentDao.insert(new DalHints(), fltintlGoogleComment);
            if (insert2 == 0) {
                throw new RuntimeException("insert comment table error, pointId ==0");
            }
            return buildSuccessRes(res);

        } catch (Exception e) {
            return buildFailedRes(e.getMessage());
        }
    }

    private FeedbackResponse buildFailedRes(String errorMsg) {
        FeedbackResponse feedbackResponse = new FeedbackResponse();
        feedbackResponse.setErrorCode(1001);
        feedbackResponse.setErrorMsg(errorMsg);
        return feedbackResponse;
    }


    private FeedbackResponse buildSuccessRes(FeedbackResponse res) {
        res.setErrorCode(0);
        res.setErrorMsg("successful");
        return res;
    }

    private AiReply parseAiAnswer(String aiAnswer){
        return new AiReply();
    }
}
