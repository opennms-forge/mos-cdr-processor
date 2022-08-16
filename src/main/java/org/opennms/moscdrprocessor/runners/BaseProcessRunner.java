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

package org.opennms.moscdrprocessor.runners;

import java.util.List;

import com.google.common.base.Strings;

import org.opennms.moscdrprocessor.client.GraphiteClient;
import org.opennms.moscdrprocessor.client.GraphiteClientImpl;
import org.opennms.moscdrprocessor.commands.CmdRunException;
import org.opennms.moscdrprocessor.commands.RunConfig;
import org.opennms.moscdrprocessor.log.LogAdapter;

public abstract class BaseProcessRunner implements ProcessRunner, Runnable {
    protected RunConfig runConfig;
    protected LogAdapter LOG;
    
    public BaseProcessRunner(RunConfig runConfig, LogAdapter logger) {
        this.runConfig = runConfig;
        this.LOG = logger;
    }

    @Override
    public void run() {
        try {
            execute();
        } catch (CmdRunException e) {
            LOG.error("Exception in BaseProcessRunner.run: " + e.getMessage(), e);
        }
    }

    public void execute() throws CmdRunException {
        LOG.info("BaseProcessRunner.execute started.");

        List<String> messages = parseFileToMessages();

        // send the messages
        boolean sendGraphite =
            !Strings.isNullOrEmpty(runConfig.hostName) &&
            runConfig.port > 0 &&
            !Strings.isNullOrEmpty(runConfig.graphiteBasePath);

        if (sendGraphite) {
            LOG.info("Sending Graphite messages for CDR record.");
            LOG.debug("host: {}, port: {}, Graphite base path: {}",
                runConfig.hostName, runConfig.port, runConfig.graphiteBasePath);

            sendGraphiteMessages(messages);
        }

        LOG.info("BaseProcessRunner.execute exiting.");
    }

    // Override this to create the Graphite messages from runConfig.filePath
    protected abstract List<String> parseFileToMessages() throws CmdRunException;

    protected void sendGraphiteMessages(List<String> messages) {
        GraphiteClient client = new GraphiteClientImpl(runConfig.hostName, runConfig.port, LOG.isDebugEnabled());

        client.sendMessages(messages);
    }
}
