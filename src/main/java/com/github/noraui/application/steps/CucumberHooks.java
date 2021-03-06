/**
 * NoraUi is licensed under the license GNU AFFERO GENERAL PUBLIC LICENSE
 * 
 * @author Nicolas HALLOUIN
 * @author Stéphane GRILLON
 */
package com.github.noraui.application.steps;

import java.util.Collection;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.noraui.browser.Auth;
import com.github.noraui.exception.Result;
import com.github.noraui.exception.TechnicalException;
import com.github.noraui.utils.Constants;
import com.github.noraui.utils.Context;
import com.github.noraui.utils.Messages;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;

public class CucumberHooks {

    /**
     * Specific logger
     */
    private static final Logger logger = LoggerFactory.getLogger(CucumberHooks.class);
    private static final String PROGRESS_MESSAGE = "PROGRESS_MESSAGE";
    private static final String SUCCESS_MESSAGE_BY_DEFAULT = "SUCCESS_MESSAGE_BY_DEFAULT";

    @Before()
    public static void setUpScenario(Scenario scenario) throws TechnicalException {
        logger.debug("setUpScenario {} scenario.", scenario.getName());
        if (Context.getCurrentScenarioData() == 0) {
            // Retrieve input data provider (by scenario name) to read
            String scenarioName = System.getProperty("scenario.name") != null ? System.getProperty("scenario.name") : getFirstNonEnvironmentTag(scenario.getSourceTagNames());
            Context.setScenarioName(scenarioName);
            Context.getDataInputProvider().prepare(Context.getScenarioName());
            Context.getDataOutputProvider().prepare(Context.getScenarioName());
            Context.startCurrentScenario();
        }
        // Increment current Excel file line to read
        Context.goToNextData();
        Context.emptyScenarioRegistry();
        Context.saveValue(Constants.IS_CONNECTED_REGISTRY_KEY, String.valueOf(Auth.isConnected()));
        Context.setCurrentScenario(scenario);
        new Result.Success<>(Context.getScenarioName(), Messages.getMessage(SUCCESS_MESSAGE_BY_DEFAULT));
    }

    @After()
    public static void tearDown(Scenario scenario) {
        logger.debug("tearDown {} scenario.", scenario.getName());
        logger.debug("Context.getCurrentScenarioData()={}", Context.getCurrentScenarioData());
        logger.debug("ExcelFactory.getNbLines()={}", Context.getDataInputProvider().getNbGherkinExample());
        printProgressBuild(scenario);
        if (Context.getCurrentScenarioData() >= Context.getDataInputProvider().getNbGherkinExample()) {
            logger.debug("Go to next feature");
            Context.goToNextFeature();
        } else {
            logger.debug("No go to next feature");
        }
    }

    private static void printProgressBuild(Scenario scenario) {
        int remainingTime = getRemainingTime();
        StringBuilder star = new StringBuilder();
        StringBuilder postStar = new StringBuilder();
        int width = scenario.getSourceTagNames().toString().length() + String.valueOf(Context.getCurrentScenarioData()).length()
                + String.valueOf(Context.getDataInputProvider().getNbGherkinExample()).length() + String.valueOf(Context.getNbFailure()).length() + String.valueOf(Context.getNbWarning()).length()
                + String.valueOf(remainingTime).length();

        String message = Messages.getMessage(PROGRESS_MESSAGE);
        for (int i = 0; i < message.length() - 12 + width; i++) {
            star.append("*");
        }
        postStar.append("*");
        for (int i = 0; i < message.length() - 14 + width; i++) {
            postStar.append(" ");
        }
        postStar.append("*");

        logger.info("{}", star);
        logger.info("{}", postStar);
        logger.info(message, scenario.getSourceTagNames(), Context.getCurrentScenarioData(), Context.getDataInputProvider().getNbGherkinExample(), Context.getNbFailure(), Context.getNbWarning(),
                remainingTime);
        logger.info("{}", postStar);
        logger.info("{}", star);
    }

    protected static int getRemainingTime() {
        DateTime now = DateTime.now();
        Seconds pastTime = Seconds.secondsBetween(Context.getStartCurrentScenario(), now);
        int totalTimecalculated = pastTime.getSeconds() * Context.getDataInputProvider().getNbGherkinExample() / Context.getCurrentScenarioData();
        return totalTimecalculated - pastTime.getSeconds();
    }

    private static String getFirstNonEnvironmentTag(Collection<String> collection) {
        for (String tag : collection) {
            if (!tag.startsWith("@~")) {
                return tag.replaceAll("@", "");
            }
        }
        return null;
    }

}
