/**
 * NoraUi is licensed under the license GNU AFFERO GENERAL PUBLIC LICENSE
 *
 * @author Nicolas HALLOUIN
 * @author Stéphane GRILLON
 */
package com.github.noraui.gherkin;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.noraui.utils.Constants;
import com.github.noraui.utils.Context;

public class GherkinFactory {

    /**
     * Specific logger
     */
    private static final Logger logger = LoggerFactory.getLogger(GherkinFactory.class);

    private static final String DATA = "#DATA";
    private static final String DATA_END = "#END";
    private static final String SCENARIO_OUTLINE_SPLIT = "Scenario Outline:";

    /**
     * Private constructor
     */
    private GherkinFactory() {
    }

    /**
     * @param filename
     *            name of input Gherkin file.
     * @param examplesTable
     *            is a table of data (line by line and without headers).
     */
    public static void injectDataInGherkinExamples(String filename, Hashtable<Integer, List<String[]>> examplesTable) {
        try {
            if (!examplesTable.isEmpty()) {
                final Path filePath = getFeaturePath(filename);
                final String fileContent = new String(Files.readAllBytes(filePath), Charset.forName(Constants.DEFAULT_ENDODING));
                StringBuilder examplesString;
                final String[] scenarioOutlines = fileContent.split(SCENARIO_OUTLINE_SPLIT);
                for (final Entry<Integer, List<String[]>> examples : examplesTable.entrySet()) {
                    examplesString = new StringBuilder();
                    examplesString.append("    ");
                    for (int j = 0; j < examples.getValue().size(); j++) {
                        examplesString.append("|");
                        examplesString.append(j + 1);
                        for (final String col : examples.getValue().get(j)) {
                            examplesString.append("|");
                            examplesString.append(col);
                        }
                        examplesString.append("|\n    ");
                    }

                    scenarioOutlines[examples.getKey()] = scenarioOutlines[examples.getKey()].replaceAll("(" + DATA + "\r?\n.*\r?\n)[\\s\\S]*(" + DATA_END + ")",
                            "$1" + examplesString.toString() + "$2");

                    // final Pattern pattern = Pattern.compile("(" + DATA + "\\r?\\n.*\\r?\\n)[\\s\\S]*(" + DATA_END + ")");
                    // final Matcher m = pattern.matcher(fileContent);
                    // System.err.println("Matches : " + m.find());
                    // for (int g = 0; g <= m.groupCount(); g++) {
                    // System.err.println("Groupe : " + g + " : " + m.group(g));
                    // }
                    // fileContent = fileContent.replaceAll("(" + DATA + "\r?\n.*\r?\n)[\\s\\S]*(" + DATA_END + ")", "$1" + examplesString.toString() + "$2");
                }

                try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath.toString()), Charset.forName(Constants.DEFAULT_ENDODING)));) {
                    int i = 0;
                    bw.write(scenarioOutlines[i]);
                    while (++i < scenarioOutlines.length) {
                        bw.write(SCENARIO_OUTLINE_SPLIT + scenarioOutlines[i]);
                    }
                }
            }
        } catch (final IOException e) {
            logger.error("error GherkinFactory.injectDataInGherkinExamples()", e);
        }
    }

    public static int getNumberOfGherkinExamples(String filename) {
        return getExamples(filename).length;
    }

    public static String[] getExamples(String filename) {
        try {
            final Path filePath = getFeaturePath(filename);
            final String fileContent = new String(Files.readAllBytes(filePath), Charset.forName(Constants.DEFAULT_ENDODING));
            final Pattern pattern = Pattern.compile(DATA + "([\\s\\S]*)" + DATA_END);
            final Matcher matcher = pattern.matcher(fileContent);
            String lines = "";
            if (matcher.find() && matcher.groupCount() == 1) {
                lines = matcher.group(0);
            }
            final String[] examples = lines.split("\\n");
            // Return lines - #DATA - #END
            return (examples.length > 2) ? Arrays.copyOfRange(examples, 1, examples.length - 1) : new String[] {};

        } catch (final IOException e) {
            logger.error("error GherkinFactory.getExamples()", e);
        }
        return new String[] {};
    }

    private static Path getFeaturePath(String filename) {
        final int indexOfUnderscore = filename.lastIndexOf('_');
        final String path = indexOfUnderscore != -1
                ? Context.getResourcesPath() + Context.getScenarioProperty(filename.substring(0, indexOfUnderscore)) + filename.substring(0, indexOfUnderscore) + ".feature"
                : Context.getResourcesPath() + Context.getScenarioProperty(filename) + filename + ".feature";
        return Paths.get(path);
    }
}
