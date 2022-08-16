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
import org.kohsuke.args4j.Option;

import org.opennms.moscdrprocessor.model.CdrHeader;
import org.opennms.moscdrprocessor.model.CdrRecord;
import org.opennms.moscdrprocessor.parsers.CdrParser;
import org.opennms.moscdrprocessor.parsers.CdrParserImpl;
import org.opennms.moscdrprocessor.parsers.CdrParserException;

/**
 * Command to parse a CDR file.
 */
public class ParseCdrCommand extends Command {
    @Option(name = "--file", usage = "Full file path to CDR file", metaVar = "<file>")
    private String filePath;

    @Option(name = "--header", usage = "Full file path to CDR header file", metaVar = "<header>")
    private String headerFilePath;
    
    @Option(name = "--output", usage = "Full file path to CDR output Json file", metaVar = "<output>")
    private String outputFilePath;
    
    @Override
    protected void execute() throws CmdRunException {
        LOG.info("In ParseCdrCommand.execute");

        CdrHeader cdrHeader;
        CdrRecord cdrRecord;

        try  {
            cdrHeader = parseCdrHeader();
        } catch (CdrParserException cpe) {
            throw new CmdRunException("Error parsing CDR header file: " + cpe.getMessage(), cpe);
        }

        try  {
            cdrRecord = parseCdrRecord(cdrHeader);
        } catch (CdrParserException cpe) {
            throw new CmdRunException("Error parsing CDR data file: " + cpe.getMessage(), cpe);
        }

        LOG.info("Done parsing CDR record");

        if (!Strings.isNullOrEmpty(outputFilePath)) {
            try {
                outputCdrRecord(cdrRecord);
            } catch (CdrParserException cpe) {
                throw new CmdRunException(
                    String.format("Error outputting CDR record to file '%s': %s", outputFilePath, cpe.getMessage()),
                    cpe);
            }
        }

        // create Graphite messages
        // send via Graphite
    }

    @Override
    protected void validate(CmdLineParser parser) throws CmdLineException {
        LOG.info("In ParseCdrCommand.validate");
        LOG.info("Currently does nothing ;)");
    }

    @Override
    public void printUsage() {
        super.printUsage();
        LOG.info("");
        LOG.info("Examples:");
        LOG.info("  Parse a CDR file: java -jar CdrProcessor.jar parse --file \"/usr/local/somefile.cdr\"");
        LOG.info("");
        LOG.info("(replace CdrProcessor.jar with actual jar, e.9. moscdrprocessor-0.0.1-SNAPSHOT-onejar.jar)");
    }

    @Override
    protected String getDescription() {
        return "Parse a CDR file";
    }

    private CdrHeader parseCdrHeader() throws CdrParserException {
        // TODO: Replace with DI
        CdrParser parser = new CdrParserImpl();

        CdrHeader cdrHeader = parser.parseCdrHeader(this.headerFilePath);

        return cdrHeader;
    }

    private CdrRecord parseCdrRecord(CdrHeader cdrHeader) throws CdrParserException {
        CdrParser parser = new CdrParserImpl();

        CdrRecord cdrRecord = parser.parseCdrRecord(this.filePath, cdrHeader);

        return cdrRecord;
    }

    private void outputCdrRecord(CdrRecord cdrRecord) throws CdrParserException {
        CdrParser parser = new CdrParserImpl();

        parser.outputCdrRecordJson(cdrRecord, this.outputFilePath);
    }
 }
