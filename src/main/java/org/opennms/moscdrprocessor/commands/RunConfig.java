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

import java.util.ArrayList;
import java.util.List;

public class RunConfig {
    /** File path of CDR record to parse; only used in some commands. */
    public String filePath;

    /** File path of CDR header to use to determine column indices. */
    public String headerFilePath;

    public String outputFilePath;

    /** Enable saving parsed CDR output to 'outputFilePath'  */
    public boolean enableOutput;

    /** If true, do not send any Graphite messages. */
    public boolean suppressSendGraphite;

    /** Recipient list of OpenNMS or Graphite server instances to send Graphite messages to. */
    public List<RecipientInfo> recipients;

    /** Base path/prefix to use in Graphite message, default is 'mos-cdr'. Must be understood by receiver. */
    public String graphiteBasePath;

    /** Use Groovy script 'cdrParseScript' to perform the CDR file processing. */
    public boolean useScript;

    /** If 'useScript' set, the Groovy script file to use. Example in 'resources/cdr-basic-message-generator.groovy' */
    public String cdrParseScript;

    /** Enable directory watch. */
    public boolean enableWatch;

    /** Folder in which CDR records are being dropped. */
    public String dropFolder;

    /** Enable deleting of CDR record files after processing, use with caution. Prefer 'enableArchive' instead. */
    public boolean enableDelete;

    /** Enable archiving of CDR record files after processing. This moves the file drom 'dropFolder' to 'archiveFolder'. */
    public boolean enableArchive;

    /** Folder to archive processed CDR records, if 'enableArchive' is true. */
    public String archiveFolder;

    /**
     * How long to sleep in milliseconds after sending a Graphite message.
     * This allows OpenNMS to process all the records in timeseries order.
     * 500ms is a reasonable default.
     */
    public Long sleepMillis;

    /** Placeholder for any extra attributes. */
    public List<String> extra = new ArrayList<>();

    /** Graphite recipient server connection info. */
    public static class RecipientInfo {
        public String hostName;
        public Integer port;
    }
}
