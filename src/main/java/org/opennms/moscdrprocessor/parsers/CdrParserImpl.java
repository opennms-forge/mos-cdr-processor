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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONObject;

import org.opennms.moscdrprocessor.model.CdrFieldMap;
import org.opennms.moscdrprocessor.model.CdrHeader;
import org.opennms.moscdrprocessor.model.CdrRecord;
import org.opennms.moscdrprocessor.model.CdrRecordItem;

public class CdrParserImpl implements CdrParser {
    public CdrHeader parseCdrHeader(String filePath) throws CdrParserException {

        CdrHeader cdrHeader = new CdrHeader();

        try (Reader reader = new FileReader(filePath);
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withTrim())) {

            // Just read a single line which is the header column names
            for (CSVRecord record : csvParser) {
                int size = record.size();

                for (int colIndex = 0; colIndex < size; colIndex++) {
                    String colName = record.get(colIndex);

                    cdrHeader.getColumnNameToIndexMap().put(colName, colIndex);
                    cdrHeader.getIndexToColumnNameMap().put(colIndex, colName);
                }

                // break - should only be 1 line
                break;
            }
        } catch (FileNotFoundException fnfe) {
            throw new CdrParserException("Could not find file '" + filePath + "'", fnfe);
        } catch (IOException ioex) {
            throw new CdrParserException("IO error for file '" + filePath + "'", ioex);
        }

        return cdrHeader;
    }

    public CdrRecord parseCdrRecord(String filePath, CdrHeader cdrHeader) throws CdrParserException {
        CdrRecord cdrRecord = new CdrRecord();

        parseCdrRecordMetadata(cdrRecord, filePath);

        // This takes some time, probably best to initialize early
        ObjectMapper mapper = new ObjectMapper();

        CdrFieldMap.initialize();

        // withQuote(null) may solve issue with embedded quote characters
        try (Reader reader = new FileReader(filePath);
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withQuote(null))) {

            List<CSVRecord> csvRecords = csvParser.getRecords();
            int rowIndex = 0;

            for (CSVRecord record : csvRecords) {
                // go through each column of this CSV row
                int recordSize = record.size();

                // We only want items having "Acct-Status-Type" of 2
                // This should be the first column
                // Other types seem to actually have different fields and CSV won't parse correctly
                boolean shouldProcess = recordSize > 0 &&
                    record.get(0) != null &&
                    record.get(0).equals("2");

                if (!shouldProcess) {
                    rowIndex++;
                    continue;
                }

                JSONObject jsonObj = new JSONObject();

                for (int colIndex = 0; colIndex < recordSize; colIndex++) {
                    String fieldData = record.get(colIndex);

                    // only map items we know/care about
                    if (cdrHeader.getIndexToColumnNameMap().containsKey(colIndex)) {
                        String columnName = cdrHeader.getIndexToColumnNameMap().get(colIndex);

                        if (CdrFieldMap.containsField(columnName)) {
                            // this is a field we map
                            String fieldType = CdrFieldMap.getFieldType(columnName);

                            // map to String or long
                            if (fieldType.equals("string") ||
                                fieldType.equals("ipaddr")) {
                                    jsonObj.put(columnName, fieldData);
                            } else if (fieldType.equals("integer")) {
                                long longValue = 0L;

                                try {
                                    longValue = Long.parseLong(fieldData, 10);
                                } catch (NumberFormatException nfe) {
                                    throw new CdrParserException(
                                        String.format("Error converting value to integer. Field: %s, rowIndex: %d, value: %s",
                                            columnName, rowIndex, fieldData), nfe);
                                }

                                jsonObj.put(columnName, longValue);
                            }
                        }
                    }
                }

                // map jsonObj to CdrRecordItem and add to cdrRecord
                String jsonString = jsonObj.toString();
                CdrRecordItem recordItem = mapper.readValue(jsonString, CdrRecordItem.class);

                cdrRecord.add(recordItem);

                rowIndex++;
            }
        } catch (FileNotFoundException fnfe) {
            throw new CdrParserException("Could not find file '" + filePath + "'", fnfe);
        } catch (IOException ioex) {
            throw new CdrParserException("IO error for file '" + filePath + "'", ioex);
        }

        return cdrRecord;
    }

    /**
     * Output CDR record as Json to a file.
     */
    public void outputCdrRecordJson(CdrRecord cdrRecord, String outputPath) throws CdrParserException {
        Gson gson = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .create();

        String json = gson.toJson(cdrRecord);

        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write(json);
        } catch (IOException ioex) {
            throw new CdrParserException("IO error for file '" + outputPath + "'", ioex);
        }
    }

    /**
     * Parse CDR record metadata from filename.
     */
    private void parseCdrRecordMetadata(CdrRecord cdrRecord, String cdrPath) throws CdrParserException {
        File file = new File(cdrPath);
        final String fileName = file.getName();

        // CDC_cdrYYYYMMDDHHmm[a-j]
        // HDC_cdrYYYYMMDDHHmm[a-j]
        final String CDR_REGEX_PATTERN = "([C|H]DC)_cdr(\\d{4})(\\d{2})(\\d{2})(\\d{2})(\\d{2})([a-j]?)(\\..+)?";

        Pattern pattern = Pattern.compile(CDR_REGEX_PATTERN);

        Matcher m = pattern.matcher(fileName);

        cdrRecord.fileName = fileName;

        if (m.find()) {
            String cdcOrHdc = m.group(1);
            String year = m.group(2);
            String month = m.group(3);
            String day = m.group(4);
            String hour = m.group(5);
            String minute = m.group(6);
            String suffix = m.group(7);

            if (cdcOrHdc.equals("CDC")) {
                cdrRecord.setFileType("CDC");
            } else if (cdcOrHdc.equals("HDC")) {
                cdrRecord.setFileType("HDC");
            }

            cdrRecord.fileTimeStamp = String.format("%s%s%s%s%s", year, month, day, hour, minute);

            try {
                Format dateFormatter = new SimpleDateFormat("yyyyMMddHHmm");
                Date dt = (Date) dateFormatter.parseObject(cdrRecord.fileTimeStamp);

                cdrRecord.fileTime = dt;
            } catch (ParseException pe) {
                throw new CdrParserException("Could not parse CDR record file time: " + pe.getMessage(), pe);
            }

            if (!Strings.isNullOrEmpty(suffix)) {
                cdrRecord.suffix = suffix;
            }
        }
    }
}
