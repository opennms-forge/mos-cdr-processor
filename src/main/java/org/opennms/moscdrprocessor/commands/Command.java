/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.moscdrprocessor.commands;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.google.common.base.Throwables;
import com.google.gson.Gson;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionHandlerFilter;

import org.opennms.moscdrprocessor.log.ConsoleLogAdapter;

/**
 * Overall Command class to group the available commands and provide common methods.
 */
public abstract class Command {
    // NOTE: Forcing debug to true. Need to do this via configuration
    protected final ConsoleLogAdapter LOG = new ConsoleLogAdapter();

    @Option(name = "-h", usage = "Show help", aliases = {"--help"})
    private boolean help = false;

    @Option(name="-v", usage ="Verbose mode", aliases = {"--verbose"})
    private boolean debugFlag = false;

    /**
     * The args4j {@link CmdLineParser}.
     */
    private CmdLineParser parser;

    protected boolean isHelp() {
        return help;
    }

    /**
     * This method is invoked by the Main method to start the execution of the Command.
     * @param parser
     * @throws IOException
     * @throws CmdLineException
     */
    public void run(CmdLineParser parser) throws CmdRunException, CmdLineException {
        LOG.setDebug(debugFlag);
        LOG.debug("Running in verbose mode");
        printFieldValues();

        this.parser = parser;

        if (isHelp()) {
            printUsage();
        } else {
            validate(getParser());
            execute();
        }
    }

    private void printFieldValues() {
        if (!LOG.isDebugEnabled()) {
            return;
        }

        LOG.debug("Options/Arguments set for {}", getClass().getName());
        Class<?> currentClass = getClass();

        do {
            printFieldValues(currentClass);
            currentClass = currentClass.getSuperclass();
        } while (currentClass != Object.class);
    }

    private void printFieldValues(Class<?> clazz) {
        try {

            for (Field eachField : clazz.getDeclaredFields()) {
                eachField.setAccessible(true);

                if (eachField.getAnnotation(Option.class) != null) {
                    Option option = eachField.getAnnotation(Option.class);
                    LOG.debug("%tOption {} = {}", option.name(), eachField.get(this));
                }

                if (eachField.getAnnotation(Argument.class) != null) {
                    LOG.debug("%tArgument {}", eachField.get(this));
                }
            }
        } catch (IllegalAccessException e) {
            Throwables.propagate(e);
        }
    }

    /**
     * Each subclass should implement an execute method to implement its behaviour.
     *
     * @throws IOException
     * @throws CmdLineException
     */
    protected abstract void execute() throws CmdRunException, CmdLineException;

    protected abstract void validate(CmdLineParser parser) throws CmdLineException;

    /**
     * Prints the usage of the command.
     */
    public void printUsage() {
        CmdLineParser parser = new CmdLineParser(this);
        LOG.info(getDescription());
        LOG.info("");
        parser.printUsage(new OutputStreamWriter(LOG.getOutputStream()), null, OptionHandlerFilter.ALL);
    }

    protected CmdLineParser getParser() {
        return parser;
    }

    /**
     * Returns the description (used for the usage) of the command.
     * @return the command's description (used for the usage).
     */
    protected abstract String getDescription();

    protected RunConfig parseConfig(String filePath) throws IOException {
        RunConfig runConfig;

        try (Reader reader = Files.newBufferedReader(Paths.get(filePath))) {
            Gson gson = new Gson();
            runConfig = gson.fromJson(reader, RunConfig.class);
        } catch (IOException e) {
            throw e;
        }

        return runConfig;
    }

    protected RunConfig cloneRunConfig(RunConfig runConfig) {
        Gson gson = new Gson();

        String json = gson.toJson(runConfig);

        RunConfig clone = gson.fromJson(json, RunConfig.class);

        return clone;
    }
}
