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

package org.opennms.moscdrprocessor.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Map of fields of CDR file that we care about to their type.  Used by CdrParser.
 */
public class CdrFieldMap {
    private static final Map<String,String> fieldTypeMap = new HashMap<>();

    private static boolean initialized;

    public static void initialize() {
        if (initialized) {
            return;
        }

        fieldTypeMap.put("Acct-Status-Type", "integer");
        fieldTypeMap.put("Acct-Session-Time", "integer");
        fieldTypeMap.put("NAS-IP-Address", "string");
        fieldTypeMap.put("h323-setup-time", "string");
        fieldTypeMap.put("h323-connect-time", "string");
        fieldTypeMap.put("h323-disconnect-time", "string");
        fieldTypeMap.put("Acme-Session-Egress-Realm", "string");
        fieldTypeMap.put("Acme-Session-Ingress-Realm", "string");
        fieldTypeMap.put("Acme-Flow-In-Src-Addr_FS1_F", "ipaddr");
        fieldTypeMap.put("Acme-Flow-In-Dst-Addr_FS1_F", "ipaddr");
        fieldTypeMap.put("Acme-Flow-Out-Src-Addr_FS1_F", "ipaddr");
        fieldTypeMap.put("Acme-Flow-Out-Dst-Addr_FS1_F", "ipaddr");
        fieldTypeMap.put("Acme-Calling-RTCP-Packets-Lost_FS1", "integer");
        fieldTypeMap.put("Acme-Calling-RTCP-Avg-Jitter_FS1", "integer");
        fieldTypeMap.put("Acme-Calling-RTCP-Avg-Latency_FS1", "integer");
        fieldTypeMap.put("Acme-Calling-RTCP-MaxJitter_FS1", "integer");
        fieldTypeMap.put("Acme-Calling-RTCP-MaxLatency_FS1", "integer");
        fieldTypeMap.put("Acme-Calling-RTP-Packets-Lost_FS1", "integer");
        fieldTypeMap.put("Acme-Calling-RTP-Avg-Jitter_FS1", "integer");
        fieldTypeMap.put("Acme-Calling-RTP-MaxJitter_FS1", "integer");
        fieldTypeMap.put("Acme-Calling-R-Factor", "integer");
        fieldTypeMap.put("Acme-Calling-MOS", "integer");
        fieldTypeMap.put("Acme-FlowType_FS1_R", "string");
        fieldTypeMap.put("Acme-Flow-In-Src-Addr_FS1_R", "ipaddr");
        fieldTypeMap.put("Acme-Flow-In-Dst-Addr_FS1_R", "ipaddr");
        fieldTypeMap.put("Acme-Flow-Out-Src-Addr_FS1_R", "ipaddr");
        fieldTypeMap.put("Acme-Flow-Out-Dst-Addr_FS1_R", "ipaddr");
        fieldTypeMap.put("Acme-Called-RTCP-Packets-Lost_FS1", "integer");
        fieldTypeMap.put("Acme-Called-RTCP-Avg-Jitter_FS1", "integer");
        fieldTypeMap.put("Acme-Called-RTCP-Avg-Latency_FS1", "integer");
        fieldTypeMap.put("Acme-Called-RTCP-MaxJitter_FS1", "integer");
        fieldTypeMap.put("Acme-Called-RTCP-MaxLatency_FS1", "integer");
        fieldTypeMap.put("Acme-Called-RTP-Packets-Lost_FS1", "integer");
        fieldTypeMap.put("Acme-Called-RTP-Avg-Jitter_FS1", "integer");
        fieldTypeMap.put("Acme-Called-RTP-MaxJitter_FS1", "integer");
        fieldTypeMap.put("Acme-Called-R-Factor", "integer");
        fieldTypeMap.put("Acme-Called-MOS", "integer");
    
        initialized = true;
    }

    public static boolean containsField(String field) {
        return fieldTypeMap.containsKey(field);
    }

    public static String getFieldType(String field) {
        return fieldTypeMap.get(field);
    }

    public static Map<String,String> getFieldTypeMap() {
        // TODO: Make read-only
        return fieldTypeMap;
    }
}
