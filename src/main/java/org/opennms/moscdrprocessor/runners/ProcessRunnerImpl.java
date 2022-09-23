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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import com.google.common.base.Strings;

import org.opennms.moscdrprocessor.commands.CmdRunException;
import org.opennms.moscdrprocessor.commands.RunConfig;
import org.opennms.moscdrprocessor.log.LogAdapter;
import org.opennms.moscdrprocessor.model.CdrHeader;
import org.opennms.moscdrprocessor.model.CdrRecord;
import org.opennms.moscdrprocessor.model.CdrRecordItem;
import org.opennms.moscdrprocessor.model.IpPatternFilter;
import org.opennms.moscdrprocessor.parsers.CdrParser;
import org.opennms.moscdrprocessor.parsers.CdrParserImpl;
import org.opennms.moscdrprocessor.parsers.CdrParserException;

public class ProcessRunnerImpl extends BaseProcessRunner {
    private static Pattern PATTERN_IP_ADDRESS = Pattern.compile("^([0-9]{1,3}\\.){3}[0-9]{1,3}$");
    
    private final List<IpPatternFilter> filters;

    public ProcessRunnerImpl(RunConfig runConfig, LogAdapter logger) {
        super(runConfig, logger);

        filters = runConfig.sourceIpFiltersAnyOf.stream()
            .map(IpPatternFilter::new)
            .collect(Collectors.toList());
    }

    protected List<String> parseFileToMessages() throws CmdRunException {
        LOG.info("ProcessRunnerImpl.processFileToMessages started.");

        CdrRecord cdrRecord = parseFileToCdrRecord();

        if (runConfig.enableOutput && !Strings.isNullOrEmpty(runConfig.outputFilePath)) {
            // output json
            try {
                outputCdrRecord(cdrRecord);
            } catch (CdrParserException cpe) {
                throw new CmdRunException(
                    String.format("Error outputting CDR record to file '%s': %s", runConfig.outputFilePath, cpe.getMessage()),
                    cpe);
            }
        }

        List<String> messages = createGraphiteMessages(cdrRecord);
        LOG.info("ProcessRunnerImpl.processFileToMessages exiting with {} messages.", messages.size());

        return messages;
    }

    private CdrRecord parseFileToCdrRecord() throws CmdRunException {
        CdrParser parser = new CdrParserImpl();
        CdrHeader cdrHeader;
        CdrRecord cdrRecord;

        try {
            cdrHeader = parser.parseCdrHeader(runConfig.headerFilePath);
            cdrRecord = parser.parseCdrRecord(runConfig.filePath, cdrHeader);
        } catch (CdrParserException cpe) {
            throw new CmdRunException("Error parsing CDR header or data file: " + cpe.getMessage(), cpe);
        }

        LOG.info("Done parsing CDR record");

        return cdrRecord;
    }

    private void outputCdrRecord(CdrRecord cdrRecord) throws CdrParserException {
        CdrParser parser = new CdrParserImpl();

        parser.outputCdrRecordJson(cdrRecord, runConfig.outputFilePath);
    }

    private List<String> createGraphiteMessages(CdrRecord cdrRecord) {
        List<String> graphiteMessages = new ArrayList<>();

        final long timestamp = cdrRecord.getFileTime().getTime();

        // For now just emit MOS messages
        for (CdrRecordItem item : cdrRecord) {
            Optional<String> ipAddress = extractIpAddress(item);
            
            if (!ipAddress.isPresent()) {
                LOG.error("Could not find valid IP for record: {}", item.ipCandidateDiagnosticString());
            }
            
            LOG.info("DEBUG found IP {} for record, considered: {}", ipAddress.get(), item.ipCandidateDiagnosticString());
            
            graphiteMessages.add(addMetric(runConfig.graphiteBasePath, ipAddress.get(), "Acme_Calling_MOS", item.acmeCallingMOS, timestamp));
            graphiteMessages.add(addMetric(runConfig.graphiteBasePath, ipAddress.get(), "Acme_Called_MOS", item.acmeCalledMOS, timestamp));
        }
        
        return graphiteMessages;
    }

    /**
     * Get the possible IP addresses from the CDR record item.
     * Clean them and determine which one should be used as the one associated with the
     * OpenNMS node to correlate this record with.
     */
    private Optional<String> extractIpAddress(CdrRecordItem item) {
        // Possible fields which could contain IP address to use
        // These are in order of preference
        List<String> rawAddresses = List.of(
            item.acmeFlowOutSrcAddrFS1F,
            item.acmeFlowInDstAddrFS1F,
            item.acmeFlowInSrcAddrFS1F,
            item.acmeFlowOutDstAddrFS1F,
            item.acmeFlowInSrcAddrFS1R,
            item.acmeFlowInDstAddrFS1R,
            item.acmeFlowOutSrcAddrFS1R,
            item.acmeFlowOutDstAddrFS1R
        );

        // Clean up and filter the fields to ensure they are valid, cleaned IP addresses
        List<String> cleanedIpAddresses =
            rawAddresses.stream()
                .map(String::trim)
                .map(x -> x.replace("\"", ""))
                .filter(x -> !x.isEmpty())
                .filter(x -> PATTERN_IP_ADDRESS.matcher(x).find())
                .collect(Collectors.toList());

        // Filter based on configuration
        // A filter such as "10.0-253.*.*" means:
        // - literal 10 for first octet
        // - 2nd octet can be between 0-253 inclusive
        // - 3rd and 4th octets can be any number

        List<String> filteredAddresses =
            cleanedIpAddresses.stream()
            .filter(ipAddr -> this.filters.stream().anyMatch(f -> f.isMatch(ipAddr)))
            .collect(Collectors.toList());

        if (!filteredAddresses.isEmpty()) {
            return Optional.of(filteredAddresses.get(0));
        }

        return Optional.empty();
    }

    private String addMetric(String base, String ipAddress, String path, long value, long timestamp) {
        // these will be in format as follows. timestamp is in ms since epoch
        // "mos-cdr:127.0.0.1:Acme_Called_MOS 123 1660003200000"
        return String.format("%s:%s:%s %d %d", base, ipAddress, path, value, timestamp);
    }
}
