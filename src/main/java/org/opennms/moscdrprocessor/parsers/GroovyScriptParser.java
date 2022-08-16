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

package org.opennms.moscdrprocessor.parsers;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.opennms.moscdrprocessor.commands.RunConfig;
import org.opennms.moscdrprocessor.log.LogAdapter;

public class GroovyScriptParser implements ScriptParser {
    private RunConfig runConfig;
    private LogAdapter logger;

    public GroovyScriptParser(RunConfig runConfig, LogAdapter logger) {
        this.runConfig = runConfig;
        this.logger = logger;
    }
    
    /**
     * Run a Groovy script to parse a CDR file and create Graphite-style messages.
     */
    public List<String> parseCdrToMessages() {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("groovy");
        List<String> messages = null;

        if (engine == null) {
            logger.error("Groovy script engine not found!");
            return null;
        }

        // Get back list of Strings, each is a Graphite message
        try (FileReader reader = new FileReader(runConfig.cdrParseScript)) {
            // pass in global values to the script
            engine.put("log", logger);
            engine.put("runConfig", runConfig);

            Object evalResult = engine.eval(reader);

            messages = (List<String>) evalResult;
        } catch (IOException ioe) {
            logger.debug("An IOException occurred: {}", ioe.getMessage());
        } catch (ScriptException e) {
            logger.debug("A ScriptException occurred: {}", e.getMessage());
        }

        return messages;
    }
}
