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
import com.google.common.base.Strings;
import org.apache.commons.lang3.tuple.Pair;

import org.opennms.moscdrprocessor.commands.CmdRunException;
import org.opennms.moscdrprocessor.commands.RunConfig;
import org.opennms.moscdrprocessor.log.LogAdapter;
import org.opennms.moscdrprocessor.model.CdrHeader;
import org.opennms.moscdrprocessor.model.CdrRecord;
import org.opennms.moscdrprocessor.model.CdrRecordItem;
import org.opennms.moscdrprocessor.parsers.CdrParser;
import org.opennms.moscdrprocessor.parsers.CdrParserImpl;
import org.opennms.moscdrprocessor.parsers.CdrParserException;

public class ProcessRunnerImpl extends BaseProcessRunner {
    
    public ProcessRunnerImpl(RunConfig runConfig, LogAdapter logger) {
        super(runConfig, logger);
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
            List<Pair<String,Long>> pairs = List.of(
                Pair.of("Acme_Calling_MOS", item.acmeCallingMOS),
                Pair.of("Acme_Called_MOS", item.acmeCalledMOS)

                /*
                Pair.of("Acct_Status_Type", item.acctStatusType),
                Pair.of("Acct_Session_Time", item.acctSessionTime),
                Pair.of("Acme_Calling_RTCP_Packets_Lost_FS1", item.acmeCallingRTCPPacketsLostFS1),
                Pair.of("Acme_Calling_RTCP_Avg_Jitter_FS1", item.acmeCallingRTCPAvgJitterFS1),
                Pair.of("Acme_Calling_RTCP_Avg_Latency_FS1", item.acmeCallingRTCPAvgLatencyFS1),
                Pair.of("Acme_Calling_RTCP_MaxJitter_FS1", item.acmeCallingRTCPMaxJitterFS1),
                Pair.of("Acme_Calling_RTCP_MaxLatency_FS1", item.acmeCallingRTCPMaxLatencyFS1),
                Pair.of("Acme_Calling_RTP_Packets_Lost_FS1", item.acmeCallingRTPPacketsLostFS1),
                Pair.of("Acme_Calling_RTP_Avg_Jitter_FS1", item.acmeCallingRTPAvgJitterFS1),
                Pair.of("Acme_Calling_RTP_MaxJitter_FS1", item.acmeCallingRTPMaxJitterFS1),
                Pair.of("Acme_Calling_R_Factor", item.acmeCallingRFactor),
                Pair.of("Acme_Called_RTCP_Packets_Lost_FS1", item.acmeCalledRTCPPacketsLostFS1),
                Pair.of("Acme_Called_RTCP_Avg_Jitter_FS1", item.acmeCalledRTCPAvgJitterFS1),
                Pair.of("Acme_Called_RTCP_Avg_Latency_FS1", item.acmeCalledRTCPAvgLatencyFS1),
                Pair.of("Acme_Called_RTCP_MaxJitter_FS1", item.acmeCalledRTCPMaxJitterFS1),
                Pair.of("Acme_Called_RTCP_MaxLatency_FS1", item.acmeCalledRTCPMaxLatencyFS1),
                Pair.of("Acme_Called_RTP_Packets_Lost_FS1", item.acmeCalledRTPPacketsLostFS1),
                Pair.of("Acme_Called_RTP_Avg_Jitter_FS1", item.acmeCalledRTPAvgJitterFS1),
                Pair.of("Acme_Called_RTP_MaxJitter_FS1", item.acmeCalledRTPMaxJitterFS1),
                Pair.of("Acme_Called_R_Factor", item.acmeCalledRFactor),
                */
            );

            // For now, using "NAS-IP-Address" from the CDR record to correlate ip address -> node ID saved in RRD

            for (var pair : pairs) {
                graphiteMessages.add(addMetric(runConfig.graphiteBasePath, item.nasIpAddress, pair.getLeft(), pair.getRight(), timestamp));
            }
        }
        
        return graphiteMessages;
    }

    private String addMetric(String base, String nasIpAddress, String path, long value, long timestamp) {
        final String ipAddress = nasIpAddress.trim().replace("\"", "");

        // these will be in format as follows. timestamp is in ms since epoch
        // "mos-cdr:127.0.0.1:Acme_Called_MOS 123 1660003200000"
        return String.format("%s:%s:%s %d %d", base, ipAddress, path, value, timestamp);
    }
}
