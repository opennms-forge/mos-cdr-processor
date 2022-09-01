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

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * Implements the "basic" command functionality.
 */
public abstract class BasicCommand extends Command {
    @Option(name = "--config", usage = "Full file path to config file", required = true, metaVar = "<config>")
    protected String configFilePath;

    protected RunConfig runConfig;

    @Override
    protected void validate(CmdLineParser parser) throws CmdLineException {
    }

    @Override
    public void printUsage() {
        super.printUsage();
        LOG.info("");
        LOG.info("Examples:");
        LOG.info("  Specify a json config file: java -jar CdrProcessor.jar watch --config \"/usr/local/myconfig.json\"");
        LOG.info("");
        LOG.info("(replace CdrProcessor.jar with actual jar, e.g. moscdrprocessor-0.0.1-SNAPSHOT-onejar.jar)");
    }

    protected void parseRunConfig() throws CmdRunException {
        try {
            runConfig = parseConfig(configFilePath);
        } catch (IOException e) {
            throw new CmdRunException("Error parsing config file '" + configFilePath + "': " + e.getMessage(), e);
        }

        LOG.debug("Successfully parsed config file.");
    }
}
