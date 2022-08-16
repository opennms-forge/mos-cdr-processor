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

import com.google.common.base.Strings;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import org.opennms.moscdrprocessor.runners.ProcessRunner;
import org.opennms.moscdrprocessor.runners.ProcessRunnerImpl;
import org.opennms.moscdrprocessor.runners.ProcessScriptRunner;

/**
 * Command to parse a CDR file, then send MOS data via Graphite to a server.
 */
public class ProcessCdrCommand extends Command {
   
    @Option(name = "--config", usage = "Full file path to config file", required = true, metaVar = "<config>")
    private String configFilePath;

    private RunConfig runConfig;

    @Override
    protected void execute() throws CmdRunException {
        LOG.info("In ProcessCdrCommand.execute");

        // parse RunConfig. possibly push this up to base class
        try {
            runConfig = parseConfig(configFilePath);
        } catch (IOException e) {
            throw new CmdRunException("Error parsing config file '" + configFilePath + "': " + e.getMessage(), e);
        }

        LOG.debug("Successfully parsed config file.");

        boolean shouldRunScript = runConfig.useScript && !Strings.isNullOrEmpty(runConfig.cdrParseScript);

        if (shouldRunScript) {
            executeGroovy();
        } else {
            processOneFile(this.runConfig);;
        }

        LOG.info("ProcessCdrCommand completed.");
    }

    @Override
    protected void validate(CmdLineParser parser) throws CmdLineException {
    }

    @Override
    public void printUsage() {
        super.printUsage();
        LOG.info("");
        LOG.info("Examples:");
        LOG.info("  Specify a json config file: java -jar CdrProcessor.jar parse --config \"/usr/local/myconfig.json\"");
        LOG.info("");
        LOG.info("(replace CdrProcessor.jar with actual jar, e.g. moscdrprocessor-0.0.1-SNAPSHOT-onejar.jar)");
    }

    @Override
    protected String getDescription() {
        return "Process a CDR file and send Graphite messages.";
    }

    private void processOneFile(RunConfig config) throws CmdRunException {
        ProcessRunner runner = new ProcessRunnerImpl(config, LOG);

        try {
            runner.execute();
        } catch (CmdRunException e) {
            throw e;
        }
    }

    private void executeGroovy() throws CmdRunException {
        ProcessRunner runner = new ProcessScriptRunner(this.runConfig, this.LOG);

        try {
            runner.execute();
        } catch (CmdRunException e) {
            throw e;
        }
    }
}
