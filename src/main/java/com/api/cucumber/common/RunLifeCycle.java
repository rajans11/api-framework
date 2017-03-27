package com.api.cucumber.common;

import com.api.session.ScenarioSession;
import com.api.session.SessionKey;
import com.api.utils.RequestsLog;
import com.api.utils.ServiceUtil;
import com.jayway.restassured.response.Response;
import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

@ContextConfiguration(locations = "classpath:cucumber.xml")
public class RunLifeCycle {
    Scenario scenario;
    @Autowired
    public ServiceUtil serviceUtil;
    @Autowired
    public ScenarioSession scenarioSession;
    @Autowired
    public RequestsLog requestsLog;
    @Autowired
    TestSetup testSetup;

    @Value("${logging.failed.request.size}")
    private Integer numberOfRequestsToPrint;

    private static final Logger LOGGER = LoggerFactory.getLogger(RunLifeCycle.class);


    @Before
    public void setUp(Scenario scenario) {
        this.scenario = scenario;
        scenarioSession.putData("scenario", scenario);
        testSetup.setLog4jLevel();
    }

    @After
    public void tearDown() {
        if (scenario.isFailed()) {
            printReport();
        }
        //clearing scenarios session at end of each scenario
        scenarioSession.cleanScenarioSession();
        //clearing janrain user at end of each test
    }

    private void printReport() {
//        final String requestsLogsStr = requestsLog.getRequestLogs().toString();
        List<String> requestsLogsList = getRequestLogs();
        Response response = (Response) scenarioSession.getData(SessionKey.LATEST_RESPONSE_LOG);

        //print failed scenario details
        StringBuilder reportString = new StringBuilder();
        reportString.append("\n\n################################Failed Scenario################################");
        reportString.append("\n\nFailed Scenario : " + scenario.getName() + "\nTags : " + scenario.getSourceTagNames());
        reportString.append("\n\nLast request sent:");
        reportString.append("\n\n" + requestsLog.getLastRequest());
        reportString.append("\n\nLast response details received:");
        reportString.append("\n\n" + ServiceUtil.getJsonPrettify(response));
        reportString.append("\n\nPrevious requests sent in scenario:");
        reportString.append("\n\n" + requestsLogsList.toString());

        //print report
        LOGGER.error(reportString.toString());
    }

    private List<String> getRequestLogs(){
        if(requestsLog.getRequestLogs().size() >= getNumberOfRequestsToPrint()) {
            return requestsLog.getRequestLogs().subList(0, getNumberOfRequestsToPrint() - 1);
        } else {
            return requestsLog.getRequestLogs();
        }
    }

    private int getNumberOfRequestsToPrint(){
        if(numberOfRequestsToPrint == null){
            setNumberOfRequestsToPrint(5);
            return 5;
        } else {
            return this.numberOfRequestsToPrint;
        }
    }

    private void setNumberOfRequestsToPrint(Integer numberOfRequestsToPrint) {
        this.numberOfRequestsToPrint = numberOfRequestsToPrint;
    }

}
