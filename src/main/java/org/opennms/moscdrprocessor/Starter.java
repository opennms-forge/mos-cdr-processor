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

package org.opennms.moscdrprocessor;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.ParserProperties;
import org.kohsuke.args4j.spi.SubCommand;
import org.kohsuke.args4j.spi.SubCommands;
import org.kohsuke.args4j.spi.SubCommandHandler;

import org.opennms.moscdrprocessor.commands.CmdRunException;
import org.opennms.moscdrprocessor.commands.Command;
import org.opennms.moscdrprocessor.commands.ParseCdrCommand;
import org.opennms.moscdrprocessor.commands.ProcessCdrCommand;
import org.opennms.moscdrprocessor.commands.WatchFolderCommand;
import org.opennms.moscdrprocessor.commands.BasicCommand;
import org.opennms.moscdrprocessor.log.ConsoleLogAdapter;

import java.io.PrintStream;

/**
 * @author Scott Theleman <stheleman@opennms.com>
 */
public class Starter extends Command {
    @Argument(
            handler=SubCommandHandler.class,
            usage = "These are the supported commands. Type MosCdrProcessor <command> --help for more details.",
            metaVar = "<command>",
            required=false)
    @SubCommands({
            @SubCommand(name="basic", impl=BasicCommand.class),
            @SubCommand(name="parse", impl=ParseCdrCommand.class),
            @SubCommand(name="process", impl=ProcessCdrCommand.class),
            @SubCommand(name="watch", impl=WatchFolderCommand.class)})
    private Command cmd;

    public static void main(String[] args) {
        Starter starter = new Starter();

        CmdLineParser parser = new CmdLineParser(starter, ParserProperties.defaults().withUsageWidth(120));

        try {
            parser.parseArgument(args);
            if (starter.cmd == null) { // no command specified, we have to use StarterCommand
                starter.cmd = starter;
            }

            starter.cmd.run(parser);
        } catch (CmdLineException e) {
            starter.LOG.error(e.getMessage());
            starter.LOG.error("");
            starter.cmd.printUsage();
            System.exit(1);
        } catch (CmdRunException ex) {
            starter.LOG.error("Got CmdRunException: {}", ex.getMessage());
            handleException(ex, starter.LOG);
            System.exit(2);
        } catch (Exception e) {
            starter.LOG.error("Got Exception: {}", e.getMessage());
            handleException(e, starter.LOG);
            System.exit(3);
        }
    }

    protected static void handleException(Exception ex, ConsoleLogAdapter logAdapter) {
        logAdapter.error("An unexpected error occurred.");
        logAdapter.error(ex.getMessage());

        if (logAdapter.isDebugEnabled()) {
            ex.printStackTrace(new PrintStream(logAdapter.getErrorOutputStream()));
        }

        if (ex.getCause() != null) {
            logAdapter.error("{}: {}", ex.getCause(), ex.getCause().getMessage());

            if (logAdapter.isDebugEnabled()) {
                ex.getCause().printStackTrace(new PrintStream(logAdapter.getErrorOutputStream()));
            }
        }
    }

    @Override
    protected void execute() throws CmdRunException, CmdLineException {
        // we print help, if we are Starter and no options are setj
        if (!isHelp() && cmd == this) {
            printUsage();
        }
    }

    @Override
    protected void validate(CmdLineParser parser) throws CmdLineException {
        // we do not need to do anything here
        LOG.info("Start.validate(), does nothing.");
    }

    @Override
    protected String getDescription() {
        return "MosCdrProcessor <command> [options...] [arguments...]";
    }

    @Override
    public void printUsage() {
        super.printUsage();
        LOG.info("");
        LOG.info("Examples: ");
        LOG.info(" (need examples.....)");
    }
}
