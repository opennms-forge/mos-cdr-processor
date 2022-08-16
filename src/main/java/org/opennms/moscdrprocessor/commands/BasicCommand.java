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

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

/**
 * Implements the "basic" command functionality.
 */
public class BasicCommand extends Command {
    @Override
    protected void execute() {
        LOG.info("In BasicCommand.execute");
        LOG.info("Currently does nothing ;)");
    }

    @Override
    protected void validate(CmdLineParser parser) throws CmdLineException {
        LOG.info("In BasicCommand.validate");
        LOG.info("Currently does nothing ;)");
    }

    @Override
    public void printUsage() {
        super.printUsage();
        LOG.info("");
        LOG.info("Examples:");
        LOG.info("(no examples yet....)");
    }

    @Override
    protected String getDescription() {
        return "Does basic CDR processing....";
    }
}
