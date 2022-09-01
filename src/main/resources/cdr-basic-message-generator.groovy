/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import groovy.util.logging.Slf4j;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import org.opennms.moscdrprocessor.commands.RunConfig;

/**
* Script to parse a CDR file (in CSV format, no header) into Graphite messages.
* The messages are returned to the mos-cdr-processor app and sent to OpenNMS via Graphite.
*/
@Slf4j
class CdrGenerator {
    private RunConfig config;

    private final String BASE_GRAPHITE_PATH = "mos-cdr";

    CdrGenerator(config) {
        this.config = config;
    }

    // returns a List<String> of Graphite messages
    def generate() {
        log.debug("DEBUG In cdr-basic-message-generator.groovy parse()");
        // can update this line to ensure the correct version of this script is being used by the application
        log.debug("version 1.0");
        log.debug("RunConfig filepath: " + this.config.filePath);
        log.debug("About to parse...");

        // get timestamp - either from CDR filename, or else just use current time
        Long timestamp = parseFileNameToTimestamp(config.filePath);

        if (timestamp == 0) {
            timestamp = (new Date()).getTime();
        }
        log.debug("Parsed filename, got timestamp: {}", timestamp);

        log.debug("Calling parseFile...");
        List<String> messages = parseFile(timestamp);

        log.debug("Exiting generate");
        return messages;
    }

    /**
    * Parse a CDR filename having "CDC_cdrYYYYMMDDHHmm[a-j]" pattern into a timestamp.
    */
    def parseFileNameToTimestamp(filePath) {
        File file = new File(filePath);
        final String fileName = file.getName();

        // CDC_cdrYYYYMMDDHHmm[a-j]
        // HDC_cdrYYYYMMDDHHmm[a-j]
        final String CDR_REGEX_PATTERN = "([C|H]DC)_cdr(\\d{4})(\\d{2})(\\d{2})(\\d{2})(\\d{2})([a-j]?)(\\..+)?";

        Pattern pattern = Pattern.compile(CDR_REGEX_PATTERN);

        Matcher m = pattern.matcher(fileName);

        if (m.find()) {
            String cdcOrHdc = m.group(1);
            String year = m.group(2);
            String month = m.group(3);
            String day = m.group(4);
            String hour = m.group(5);
            String minute = m.group(6);
            String suffix = m.group(7);

            String fileTimeStamp = String.format("%s%s%s%s%s", year, month, day, hour, minute);

            try {
                Format dateFormatter = new SimpleDateFormat("yyyyMMddHHmm");
                Date dt = (Date) dateFormatter.parseObject(fileTimeStamp);

                return dt.getTime(); // * 1000 ??
            } catch (ParseException pe) {
                throw pe;
            }
        }

        return 0;
    }

    /**
    * Parse a CDR CSV data file. See SBC_Headers.csv for headers; this file expected NOT to have a header.
    * This returns a List<String> of Graphite-style messages.
    * Only parses out "Acme_Calling_MOS" and "Acme_Called_MOS" values.
    */
    def parseFile(timestamp) {
        log.debug("In parseFile");

        // Parse CDR file
        // This is a CSV file with no header.
        // '.withQuote(null)' fixes some issues with embedded quotes in CSV data
        // We only want records with first field "Acct-Status-Type" of 2
        List<String> messages = new ArrayList<>();

        // 0-based index of specific fields we care about
        final int NAS_IP_ADDRESS_INDEX = 1;
        final int ACME_CALLING_MOS_INDEX = 46;
        final int ACME_CALLED_MOS_INDEX = 70;

        try (Reader reader = new FileReader(config.filePath);
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withQuote(null))) {

            int rowIndex = 0;

            List<CSVRecord> csvRecords = csvParser.getRecords();

            for (CSVRecord record : csvRecords) {
                log.error("parsing row {}", rowIndex);
                rowIndex++;

                // go through each column of this CSV row
                int recordSize = record.size();

                // skip records not having "Acct-Status-Type" of 2
                if (recordSize < 1 || !record.get(0).equals("2")) {
                    continue;
                }

                log.debug("Found record with status of 2");

                // Pick out just the columns we are interested in
                // Using "NAS-IP-Address" as an IP address to correlate CDR data with an OpenNMS node
                String ipAddress = record.get(NAS_IP_ADDRESS_INDEX).trim().replace("\"", "");

                String basePath = String.format("%s:%s", BASE_GRAPHITE_PATH, ipAddress);

                // these will be in format:
                // "mos-cdr:127.0.0.1:Acme_Calling_MOS 123 1660003200000"
                addValueIfPresent(messages, record, String.format("%s:Acme_Calling_MOS", basePath), ACME_CALLING_MOS_INDEX, timestamp);
                addValueIfPresent(messages, record, String.format("%s:Acme_Called_MOS", basePath), ACME_CALLED_MOS_INDEX, timestamp);
            }
        } catch (Exception e) {
            throw e;
        }

        log.debug("Exiting parse, there are {} messages", messages.size());
        return messages;
    }

    def addValueIfPresent(List<String> messages, CSVRecord record, String path, int index, long timestamp) {
        log.debug("addValueIfPresent...index: {}", index);

        Long value = getLongValue(record, index);
        log.debug("value: {}", value);
        
        if (value != null) {
            log.debug("...value not null...");
            String msg = createGraphiteMessage(path, value, timestamp);
            log.debug("...adding msg: {}", msg);
            messages.add(msg);
        }
    }

    // Returns a Long if there is a value at that index, or null
    def getLongValue(CSVRecord record, int index) {
        if (record.size() > index) {
            String fieldData = record.get(index);

            try {
                return Long.parseLong(fieldData, 10);
            } catch (NumberFormatException nfe) {
                return null;
            }
        }

        return null;
    }

    def createGraphiteMessage(String path, long value, long timestamp) {
        return String.format("%s %d %d", path, value, timestamp);
    }
}

var generator = new CdrGenerator(runConfig);

// result is returned to caller, should be a list<string>
var result = generator.generate();

