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

import com.fasterxml.jackson.annotation.JsonSetter;

public class CdrRecordItem {
    public long acctStatusType;
    public long acctSessionTime;
    public String nasIpAddress;
    public String h323SetupTime;
    public String h323ConnectTime;
    public String h323DisconnectTime;
    public String acmeSessionEgressRealm;
    public String acmeSessionIngressRealm;
    public String acmeFlowInSrcAddrFS1F;
    public String acmeFlowInDstAddrFS1F;
    public String acmeFlowOutSrcAddrFS1F;
    public String acmeFlowOutDstAddrFS1F;
    public long acmeCallingRTCPPacketsLostFS1;
    public long acmeCallingRTCPAvgJitterFS1;
    public long acmeCallingRTCPAvgLatencyFS1;
    public long acmeCallingRTCPMaxJitterFS1;
    public long acmeCallingRTCPMaxLatencyFS1;
    public long acmeCallingRTPPacketsLostFS1;
    public long acmeCallingRTPAvgJitterFS1;
    public long acmeCallingRTPMaxJitterFS1;
    public long acmeCallingRFactor;
    public long acmeCallingMOS;
    public String acmeFlowTypeFS1R;
    public String acmeFlowInSrcAddrFS1R;
    public String acmeFlowInDstAddrFS1R;
    public String acmeFlowOutSrcAddrFS1R;
    public String acmeFlowOutDstAddrFS1R;
    public long acmeCalledRTCPPacketsLostFS1;
    public long acmeCalledRTCPAvgJitterFS1;
    public long acmeCalledRTCPAvgLatencyFS1;
    public long acmeCalledRTCPMaxJitterFS1;
    public long acmeCalledRTCPMaxLatencyFS1;
    public long acmeCalledRTPPacketsLostFS1;
    public long acmeCalledRTPAvgJitterFS1;
    public long acmeCalledRTPMaxJitterFS1;
    public long acmeCalledRFactor;
    public long acmeCalledMOS;

    @JsonSetter("Acct-Status-Type")
    public void setAcctStatusType(long value) {
        this.acctStatusType = value;
    }

    @JsonSetter("Acct-Session-Time")
    public void setAcctSessionTime(long value) {
        this.acctSessionTime = value;
    }

    @JsonSetter("NAS-IP-Address")
    public void setNasIpAddress(String value) {
        this.nasIpAddress = value;
    }

    @JsonSetter("h323-setup-time")
    public void setH323SetupTime(String value) {
        this.h323SetupTime = value;
    }

    @JsonSetter("h323-connect-time")
    public void setH323ConnectTime(String value) {
        this.h323ConnectTime = value;
    }

    @JsonSetter("h323-disconnect-time")
    public void setH323DisconnectTime(String value) {
        this.h323DisconnectTime = value;
    }

    @JsonSetter("Acme-Session-Egress-Realm")
    public void setAcmeSessionEgressRealm(String value) {
        this.acmeSessionEgressRealm = value;
    }

    @JsonSetter("Acme-Session-Ingress-Realm")
    public void setAcmeSessionIngressRealm(String value) {
        this.acmeSessionIngressRealm = value;
    }

    @JsonSetter("Acme-Flow-In-Src-Addr_FS1_F")
    public void setAcmeFlowInSrcAddrFS1F(String value) {
        this.acmeFlowInSrcAddrFS1F = value;
    }

    @JsonSetter("Acme-Flow-In-Dst-Addr_FS1_F")
    public void setAcmeFlowInDstAddrFS1F(String value) {
        this.acmeFlowInDstAddrFS1F = value;
    }

    @JsonSetter("Acme-Flow-Out-Src-Addr_FS1_F")
    public void setAcmeFlowOutSrcAddrFS1F(String value) {
        this.acmeFlowOutSrcAddrFS1F = value;
    }

    @JsonSetter("Acme-Flow-Out-Dst-Addr_FS1_F")
    public void setAcmeFlowOutDstAddrFS1F(String value) {
        this.acmeFlowOutDstAddrFS1F = value;
    }

    @JsonSetter("Acme-Calling-RTCP-Packets-Lost_FS1")
    public void setAcmeCallingRTCPPacketsLostFS1(long value) {
        this.acmeCallingRTCPPacketsLostFS1 = value;
    }

    @JsonSetter("Acme-Calling-RTCP-Avg-Jitter_FS1")
    public void setAcmeCallingRTCPAvgJitterFS1(long value) {
        this.acmeCallingRTCPAvgJitterFS1 = value;
    }

    @JsonSetter("Acme-Calling-RTCP-Avg-Latency_FS1")
    public void setAcmeCallingRTCPAvgLatencyFS1(long value) {
        this.acmeCallingRTCPAvgLatencyFS1 = value;
    }

    @JsonSetter("Acme-Calling-RTCP-MaxJitter_FS1")
    public void setAcmeCallingRTCPMaxJitterFS1(long value) {
        this.acmeCallingRTCPMaxJitterFS1 = value;
    }

    @JsonSetter("Acme-Calling-RTCP-MaxLatency_FS1")
    public void setAcmeCallingRTCPMaxLatencyFS1(long value) {
        this.acmeCallingRTCPMaxLatencyFS1 = value;
    }

    @JsonSetter("Acme-Calling-RTP-Packets-Lost_FS1")
    public void setAcmeCallingRTPPacketsLostFS1(long value) {
        this.acmeCallingRTPPacketsLostFS1 = value;
    }

    @JsonSetter("Acme-Calling-RTP-Avg-Jitter_FS1")
    public void setAcmeCallingRTPAvgJitterFS1(long value) {
        this.acmeCallingRTPAvgJitterFS1 = value;
    }

    @JsonSetter("Acme-Calling-RTP-MaxJitter_FS1")
    public void setAcmeCallingRTPMaxJitterFS1(long value) {
        this.acmeCallingRTPMaxJitterFS1 = value;
    }

    @JsonSetter("Acme-Calling-R-Factor")
    public void setAcmeCallingRFactor(long value) {
        this.acmeCallingRFactor = value;
    }

    @JsonSetter("Acme-Calling-MOS")
    public void setAcmeCallingMOS(long value) {
        this.acmeCallingMOS = value;
    }

    @JsonSetter("Acme-FlowType_FS1_R")
    public void setAcmeFlowTypeFS1R(String value) {
        this.acmeFlowTypeFS1R = value;
    }

    @JsonSetter("Acme-Flow-In-Src-Addr_FS1_R")
    public void setAcmeFlowInSrcAddrFS1R(String value) {
        this.acmeFlowInSrcAddrFS1R = value;
    }

    @JsonSetter("Acme-Flow-In-Dst-Addr_FS1_R")
    public void setAcmeFlowInDstAddrFS1R(String value) {
        this.acmeFlowInDstAddrFS1R = value;
    }

    @JsonSetter("Acme-Flow-Out-Src-Addr_FS1_R")
    public void setAcmeFlowOutSrcAddrFS1R(String value) {
        this.acmeFlowOutSrcAddrFS1R = value;
    }

    @JsonSetter("Acme-Flow-Out-Dst-Addr_FS1_R")
    public void setAcmeFlowOutDstAddrFS1R(String value) {
        this.acmeFlowOutDstAddrFS1R = value;
    }

    @JsonSetter("Acme-Called-RTCP-Packets-Lost_FS1")
    public void acmeCalledRTCPPacketsLostFS1(long value) {
        this.acmeCalledRTCPPacketsLostFS1 = value;
    }

    @JsonSetter("Acme-Called-RTCP-Avg-Jitter_FS1")
    public void setAcmeCalledRTCPAvgJitterFS1(long value) {
        this.acmeCalledRTCPAvgJitterFS1 = value;
    }

    @JsonSetter("Acme-Called-RTCP-Avg-Latency_FS1")
    public void setAcmeCalledRTCPAvgLatencyFS1(long value) {
        this.acmeCalledRTCPAvgLatencyFS1 = value;
    }

    @JsonSetter("Acme-Called-RTCP-MaxJitter_FS1")
    public void setAcmeCalledRTCPMaxJitterFS1(long value) {
        this.acmeCalledRTCPMaxJitterFS1 = value;
    }

    @JsonSetter("Acme-Called-RTCP-MaxLatency_FS1")
    public void setAcmeCalledRTCPMaxLatencyFS1(long value) {
        this.acmeCalledRTCPMaxLatencyFS1 = value;
    }

    @JsonSetter("Acme-Called-RTP-Packets-Lost_FS1")
    public void setAcmeCalledRTPPacketsLostFS1(long value) {
        this.acmeCalledRTPPacketsLostFS1 = value;
    }

    @JsonSetter("Acme-Called-RTP-Avg-Jitter_FS1")
    public void setAcmeCalledRTPAvgJitterFS1(long value) {
        this.acmeCalledRTPAvgJitterFS1 = value;
    }

    @JsonSetter("Acme-Called-RTP-MaxJitter_FS1")
    public void setAcmeCalledRTPMaxJitterFS1(long value) {
        this.acmeCalledRTPMaxJitterFS1 = value;
    }

    @JsonSetter("Acme-Called-R-Factor")
    public void setAcmeCalledRFactor(long value) {
        this.acmeCalledRFactor = value;
    }

    @JsonSetter("Acme-Called-MOS")
    public void setAcmeCalledMOS(long value) {
        this.acmeCalledMOS = value;
    }
}
