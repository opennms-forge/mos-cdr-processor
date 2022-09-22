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

import groovy.util.logging.Slf4j
import java.net.InetAddress;
import java.util.List;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.RrdLabelUtils
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionAgentFactory;
import org.opennms.netmgt.collection.support.builder.InterfaceLevelResource
import org.opennms.netmgt.collection.support.builder.NodeLevelResource
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.telemetry.protocols.graphite.adapter.GraphiteMetric


/**
 * Simple graphite handler. No schema support, you'll have to parse/handle the message paths yourself.
 *
 * You can send test data by doing something like (using IPv4 and setting 'localhost' as the source):
 * <code>echo "mos-cdr:127.0.0.1:Acme_Calling_MOS 100 `date +%s`" | nc -u -w1 -s localhost localhost 2003</code>
 *
 * Note that log messages using 'log' will show up in etc/karaf.log. Depending on your Karaf log configuration
 * settings, you may need to use 'log.error()' for them to show up.
 * Some log messages are commented-out here and are provided as examples if you need additional
 * debug information.
 *
 * Scripts are cached but should automatically reload when updated and saved, however this may not occur if there
 * are compilation errors. If you want to force reload, you can run the following karaf command:
 *
 * <code>opennms:reload-daemon telemetryd</code>
 */
@Slf4j
class CollectionSetGenerator {
    static generate(agent, builder, graphiteMsg, factory, agentList, nodeDao) {
        log.debug("Generating collection set for message: {}", graphiteMsg)

        if (graphiteMsg.path.startsWith("mos-cdr:")) {
            log.debug("Received CDR message, path {}, value {}, timestamp: {}",
                graphiteMsg.getPath(), graphiteMsg.longValue(), graphiteMsg.getTimestamp());

            // message will be something like:
            // "mos-cdr:127.0.0.1:Acme_Called_MOS 123 1659916800000"
            String[] split = graphiteMsg.path.split(":");
            String ipAddr = split[1];
            String varName = split[2];

            // Parse IP address, see if this OpenNMS shard contains the corresponding node
            InetAddress inetAddress = InetAddress.getByName(ipAddr);

            // Assume all nodes/interfaces will have ICMP service
            // otherwise will need a different NodeDao call
            List<OnmsNode> nodeList = nodeDao.findByIpAddressAndService(inetAddress, "ICMP");

            // Either IP address does not exist, isn't tied to a node, or isn't in this OpenNMS shard
            if (nodeList.isEmpty()) {
                log.debug("Node not found for IP '{}', ignoring", ipAddr);
                return;
            }

            // First, build the top node-level resources, as other resource types
            // will depend on this one
            // This node should be the node this message and MOS score is correlated with
            int nodeId = nodeList.get(0).getId();

            log.debug("Found node: id {}, {}", nodeId, nodeList.get(0).toString());
            NodeLevelResource nodeLevelResource = new NodeLevelResource(nodeId);

            CollectionAgent overrideAgent = 
                factory.createCollectionAgent(Integer.toString(nodeId), inetAddress);

            agentList.add(overrideAgent);
            //log.debug("Created overridden collection agent");

            String ifaceLabel = RrdLabelUtils.computeLabelForRRD("mos_cdr_" + ipAddr, null, null);
            //log.debug("ifaceLabel: " + ifaceLabel);

            InterfaceLevelResource interfaceResource = new InterfaceLevelResource(nodeLevelResource, ifaceLabel);

            // NOTE: graphiteMsg timestamp is in ms since epoch. If seconds, we have to update
            Date d = new Date(graphiteMsg.getTimestamp());
            //log.debug("Date from timestamp: {}", d.toString());

            // MOS values are sent in as 100-500
            // We convert here to 1.0-5.0
            double doubleVal = ((double) graphiteMsg.longValue()) / 100.0;
            log.debug("Adding gauge value: {}", doubleVal);

            // will create a timeseries record such as:
            // Acme_Calling_MOS{location="Default", node="localhost", resourceId="snmp/1/127.0.0.1/mos-cdr"}
            interfaceResource.setTimestamp(d);
            builder.withTimestamp(d);
            builder.withGauge(interfaceResource, "mos-cdr", varName, doubleVal);
        } else {
            log.warn("Script does not know how to handle this message from graphite. :(  {}", graphiteMsg);
        }
    }
}

// The following variables are passed in as globals from the adapter:
// agent: the agent (or node) against which the metrics will be associated
//      This can be overridden below in agentList
// builder: a reference to a CollectionSetBuilder to which the resources/metrics should be added
// msg: the message from which to extract the metrics
// collectionAgentFactory: Factory class for creating a CollectionAgent to return in agentList
// agentList: script can add a CollectionAgent to this list to override the "agent" passed in.
//      GraphiteAdapter will use this CollectionAgent instead of the one it created, hence
//      can assign the OnmsNode id saved in the TS DB
// nodeDao: NodeDao for retrieving node information from OpenNMS

// In our case, the msg will a GraphiteMetric object
GraphiteMetric graphiteMsg = msg
CollectionAgentFactory factory = collectionAgentFactory
List<CollectionAgent> agents = agentList

// Generate the CollectionSet
CollectionSetGenerator.generate(agent, builder, graphiteMsg, collectionAgentFactory, agents, nodeDao)
