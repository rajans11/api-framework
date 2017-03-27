package com.api.cucumber.common;


import com.api.session.ScenarioSession;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestSetup {

    @Autowired
    ScenarioSession scenarioSession;

    public void setLog4jLevel(){

        String level;
        level = System.getProperty("cucumber.logging.level");

        if(level==null){
            //set default level to INFO if null or not been set
            LogManager.getLogger("com.api").setLevel(Level.ERROR);
//            System.out.println("Log4j level set to default of 'ERROR'");
        } else {
            switch (level){
                case "TRACE":
                    LogManager.getLogger("com.api").setLevel(Level.TRACE);
                    break;
                case "DEBUG":
                    LogManager.getLogger("com.api").setLevel(Level.DEBUG);
                    break;
                case "INFO":
                    LogManager.getLogger("com.api").setLevel(Level.INFO);
                    break;
                case "WARN":
                    LogManager.getLogger("com.api").setLevel(Level.WARN);
                    break;
                case "ERROR":
                    LogManager.getLogger("com.api").setLevel(Level.ERROR);
                    break;
                case "FATAL":
                    LogManager.getLogger("com.api").setLevel(Level.FATAL);
                    break;
                case "OFF":
                    LogManager.getLogger("com.api").setLevel(Level.OFF);
                    break;
                default:
                    throw new RuntimeException("Debug level '" + level + "' is not a valid logging level. Valid levels are TRACE, DEBUG, INFO, ERROR, WARN, FATAL");
            }
//            System.out.println("Log4j level set to '" + level + "'");
        }
    }
}
