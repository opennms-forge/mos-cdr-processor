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

import com.google.common.base.Strings;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import org.opennms.moscdrprocessor.runners.ProcessRunner;
import org.opennms.moscdrprocessor.runners.ProcessRunnerImpl;
import org.opennms.moscdrprocessor.runners.ProcessScriptRunner;

/**
 * Command to parse a CDR file, then send MOS data via Graphite to a server.
 */
public class ProcessCdrCommand extends BasicCommand {
   
    @Override
    protected void execute() throws CmdRunException {
        LOG.info("In ProcessCdrCommand.execute");
        parseRunConfig();

        boolean shouldRunScript = runConfig.useScript && !Strings.isNullOrEmpty(runConfig.cdrParseScript);

        if (shouldRunScript) {
            executeGroovy();
        } else {
            processOneFile(this.runConfig);;
        }

        LOG.info("ProcessCdrCommand completed.");
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
