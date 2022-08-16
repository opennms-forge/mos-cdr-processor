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

import org.opennms.moscdrprocessor.commands.CmdRunException;
import org.opennms.moscdrprocessor.commands.RunConfig;
import org.opennms.moscdrprocessor.log.LogAdapter;
import org.opennms.moscdrprocessor.parsers.GroovyScriptParser;

public class ProcessScriptRunner extends BaseProcessRunner {
    
    public ProcessScriptRunner(RunConfig config, LogAdapter logger) {
        super(config, logger);
    }
    
    protected List<String> parseFileToMessages() throws CmdRunException {
        var parser = new GroovyScriptParser(this.runConfig, this.LOG);

        List<String> messages = parser.parseCdrToMessages();

        LOG.debug("Executed Groovy, got messages");

        return messages;
    }
}
