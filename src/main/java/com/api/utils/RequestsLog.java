package com.api.utils;

import com.api.service.RequestData;
import com.api.session.ScenarioSession;
import com.api.session.SessionKey;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RequestsLog {
    @Autowired
    public ScenarioSession scenarioSession;
    List<String> requestsList;

    public void setRequestsLog(RequestData requestData) {
        String requestDataStr = requestData.getRequestData(true);
        requestsList = (List<String>) scenarioSession.getData(SessionKey.REQUESTS_LOG);
        if (requestsList == null) {
            requestsList = new ArrayList<>();
        }
        int reqNo = requestsList.size() + 1;
        requestDataStr = "\n\n\nRequest No: " + reqNo + "  " + requestDataStr;
        requestsList.add(requestDataStr);
        scenarioSession.putData(SessionKey.REQUESTS_LOG, requestsList);
        setLatestRequestData(requestData);
    }

    public List<String> getRequestLogs() {
//        Scenario scenario = (Scenario) scenarioSession.getData("scenario");
//        String requestDataStr = "\n\n\nFailed Scenario : " + scenario.getName() + "\nTags : " + scenario.getSourceTagNames();
//        requestsList.add(requestDataStr);
//        requestsList = Lists.reverse(requestsList);
        return requestsList;
    }
    public void setLatestRequestData(RequestData requestData) {
     scenarioSession.putData(SessionKey.LATEST_RESPONSE_LOG,requestData.getResponse());
    }

    public String getLastRequest(){
        List<String> requestsLogsList = getRequestLogs();
        return Lists.reverse(requestsLogsList).get(0);
    }
}
