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

package org.opennms.moscdrprocessor.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opennms.moscdrprocessor.log.ConsoleLogAdapter;

public class GraphiteClientImpl implements GraphiteClient {
    private String host;
    private int port;

    protected final ConsoleLogAdapter LOG = new ConsoleLogAdapter();

    public GraphiteClientImpl(String host, int port, boolean debug) {
        this.host = host;
        this.port = port;
        LOG.setDebug(debug);
    }

    @Override
    public String getHostName() {
        return this.host;
    }

    @Override
    public int getPort() {
        return this.port;
    }

    @Override
    public void sendMetric(String key, Number value) {
        sendMetric(key, value, getCurrentTimestamp());
    }
     
    @Override
    public void sendMetrics(Map<String, List<Number>> metrics) {
        sendMetrics(metrics, getCurrentTimestamp());
    }

    @Override
    public void sendMetric(String key, Number value, long timestamp) {
        final var map = Map.of(key, List.of(value));

        sendMetrics(map, timestamp);
    }
  
    @Override
    public void sendMetrics(Map<String, List<Number>> metrics, long timeStamp) {
        sendMetricsImpl(metrics, timeStamp);
    }

    @Override
    public void sendMessages(List<String> messages) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Preparing {} messages to send", messages.size());

            for (String msg : messages) {
                LOG.debug("Message: {}", msg);
            }
        }

        LOG.debug("Creating UDP socket");

        try (DatagramSocket udpSocket = new DatagramSocket()) {
            // port is probably "-1"
            LOG.debug("UDP socket created on port {}", udpSocket.getPort());

            InetAddress serverAddress = InetAddress.getByName(host);

            for (String msg : messages) {
                LOG.debug("Sending message: {}", msg);

                DatagramPacket p = new DatagramPacket(msg.getBytes(), msg.getBytes().length, serverAddress, port);
                udpSocket.send(p);                    

                LOG.debug("...message sent");
            }
         } catch (UnknownHostException e) {
            throw new GraphiteClientException("Error: Unknown host: " + host);
         } catch (IOException e) {
            throw new GraphiteClientException("Error writing graphite data: " + e.getMessage(), e);
         }

        LOG.info("GraphiteClient.sendMessages completed.");
    }

    private void sendMetricsImpl(Map<String, List<Number>> metrics, long timeStamp) {
        LOG.info("GraphiteClient.sendMetrics start");
        LOG.debug("host: {}, port: {}", host, port);

        List<String> messages = createMessages(metrics, timeStamp);

        sendMessages(messages);;

        LOG.info("GraphiteClient.sendMetrics completed.");
    }

    private List<String> createMessages(Map<String, List<Number>> metrics, long timeStamp) {
        List<String> messages = new ArrayList<>();
            
        for (Map.Entry<String, List<Number>> metric : metrics.entrySet()) {
            final String path = metric.getKey();
            
            for (Number value : metric.getValue()) {
                String msg = String.format("%s %s %d%n", path, value, timeStamp);
                messages.add(msg);
            }
        }

        return messages;
    }

    private long getCurrentTimestamp() {
        return System.currentTimeMillis() / 1000;
    }
}
