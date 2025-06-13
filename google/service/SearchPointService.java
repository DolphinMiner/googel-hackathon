package com.ctrip.flight.intl.exchange.hackathonnfc.service;

import com.ctrip.flight.intl.exchange.hackathonnfc.ai.google.GoogleCloudVertexAIServiceFacade;
import com.ctrip.flight.intl.exchange.hackathonnfc.dal.FltintlGoogleAccessiblePointsDao;
import com.ctrip.flight.intl.exchange.hackathonnfc.dal.FltintlGoogleCommentDao;
import com.ctrip.flight.intl.exchange.hackathonnfc.dal.entity.FltintlGoogleAccessiblePoints;
import com.ctrip.flight.intl.exchange.hackathonnfc.dal.entity.FltintlGoogleComment;
import com.ctrip.flight.intl.exchange.hackathonnfc.dto.Point;
import com.ctrip.flight.intl.exchange.hackathonnfc.dto.SearchAccessiblePointRequest;
import com.ctrip.flight.intl.exchange.hackathonnfc.dto.SearchAccessiblePointResponse;
import com.ctrip.platform.dal.dao.DalHints;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchPointService {

    private final FltintlGoogleAccessiblePointsDao fltintlGoogleAccessiblePointsDao;
    private final FltintlGoogleCommentDao fltintlGoogleCommentDao;

    private final GoogleCloudVertexAIServiceFacade googleCloudVertexAIServiceFacade;

    private String PROMPT = "请基于以上所有的用户评论内容，总结提炼出这个无障碍点的概要描述,生成的结果格式：简要描述：xxxx, 用户注意事项：，用户评论如下: ";

    public SearchAccessiblePointResponse process(SearchAccessiblePointRequest request) {
        SearchAccessiblePointResponse res = new SearchAccessiblePointResponse();
        try {
            // validate request

            // search point table
            FltintlGoogleAccessiblePoints fltintlGoogleAccessiblePoint = new FltintlGoogleAccessiblePoints();
            fltintlGoogleAccessiblePoint.setScenicAreaId(request.getScenicAreaId());
            // 查询景点下的所有无障碍点
            List<FltintlGoogleAccessiblePoints> points = fltintlGoogleAccessiblePointsDao.queryBy(fltintlGoogleAccessiblePoint, new DalHints());

            List<Point> pointList = points.stream().map(point -> {
                Point pointDto = new Point();
                pointDto.setPointId(point.getPointId());
                pointDto.setType(point.getType());
                pointDto.setLongitude(Double.valueOf(point.getLongitude().toString()));
                pointDto.setLatitude(Double.valueOf(point.getLatitude().toString()));
                return pointDto;
            }).toList();

            // 查询每个无障碍点下的评论
            // FltintlGoogleComment fltintlGoogleComment = new FltintlGoogleComment();
            List<Long> pointIdList = pointList.stream().map(Point::getPointId).toList();
            String idList = Strings.join(pointIdList, ',');
            List<FltintlGoogleComment> comments = fltintlGoogleCommentDao.query("select * from fltintl_google_comment where point_id in (" + idList + ");", new DalHints());
            // pointId, comments
            Map<Long, List<FltintlGoogleComment>> pointCommentMap = comments.stream()
                    .collect(Collectors.groupingBy(FltintlGoogleComment::getPointId));
            // 赋值评论
            pointList.forEach(pointDto -> {
                List<FltintlGoogleComment> commentList = pointCommentMap.get(pointDto.getPointId());
                if (CollectionUtils.isEmpty(commentList)) {
                    return;
                }
                List<String> curPointComments = pointCommentMap.get(pointDto.getPointId()).stream().map(FltintlGoogleComment::getDescription).toList();
                // 赋值当前point下的所有评论
                pointDto.setComments(curPointComments);
                String commentsJson = new Gson().toJson(curPointComments);
                // AI 基于评论生成描述
                String aiDescription = googleCloudVertexAIServiceFacade.chatAboutQuestion(PROMPT + commentsJson);
                pointDto.setAiDescription(aiDescription);
            });

            res.setPoints(pointList);
            return buildSuccessRes(res);

        } catch (Exception e) {
            return buildFailedRes(e.getMessage());
        }
    }

    private SearchAccessiblePointResponse buildFailedRes(String errorMsg) {
        SearchAccessiblePointResponse response = new SearchAccessiblePointResponse();
        response.setErrorCode(1001);
        response.setErrorMsg(errorMsg);
        return response;
    }


    private SearchAccessiblePointResponse buildSuccessRes(SearchAccessiblePointResponse res) {
        res.setErrorCode(0);
        res.setErrorMsg("successful");
        return res;
    }
}
