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

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class IpPatternFilter {
    final private static Pattern PATTERN_NUMBER = Pattern.compile("^[0-9]+$");
    final private static Pattern PATTERN_RANGE = Pattern.compile("^([0-9]+)-([0-9]+)$");

    final private String pattern;

    final private IpPatternFilterOctet[] octets;

    public enum FilterType {
        Single,
        Range,
        Any
    }

    public static class IpPatternFilterOctet {
        public FilterType filterType;
        public int singleNumber;
        public int rangeStart;
        public int rangeEnd;
    }

    public IpPatternFilter(String pattern) throws IllegalArgumentException {
        this.pattern = pattern;
        this.octets = IpPatternFilter.compileOctets(this.pattern);
    }

    public String getPattern() {
        return this.pattern;
    }

    public IpPatternFilterOctet[] getOctets() {
        return this.octets;
    }

    private static IpPatternFilterOctet[] compileOctets(String pattern) throws IllegalArgumentException {
        String[] patterns = pattern.split("\\.");

        if (patterns.length != 4) {
            throw new IllegalArgumentException("Invalid IpPatternFilter: " + pattern);
        }

        IpPatternFilterOctet[] octets = IntStream.rangeClosed(1, 4)
            .boxed()
            .map(n -> new IpPatternFilterOctet())
            .toArray(IpPatternFilterOctet[]::new);

        for (int i = 0; i < 4; i++) {
            String p = patterns[i];

            if (p.equals("*")) {
                octets[i].filterType = FilterType.Any;
            } else if (PATTERN_NUMBER.matcher(p).find()) {
                octets[i].filterType = FilterType.Single;
                octets[i].singleNumber = Integer.parseInt(p);
            } else {
                Matcher m = PATTERN_RANGE.matcher(p);
            
                if (!m.find()) {
                    throw new IllegalArgumentException("Ip Filter Pattern did not match any known types of filters.");
                }
            
                octets[i].filterType = FilterType.Range;
                octets[i].rangeStart = Integer.parseInt(m.group(1));
                octets[i].rangeEnd = Integer.parseInt(m.group(2));
            }
        }

        return octets;
    }

    public boolean isMatch(String ipAddress) {
        String[] ipValues = ipAddress.split("\\.");

        if (ipValues.length != 4) {
            throw new IllegalArgumentException("Invalid ipAddress: " + ipAddress);
        }

        List<Integer> ipOctets = Arrays.stream(ipValues)
            .map(Integer::parseInt)
            .collect(Collectors.toList());

        for (int i = 0; i < 4; i++) {
            int ipOctet = ipOctets.get(i).intValue();
            IpPatternFilterOctet filter = this.octets[i];

            if (!applyOctetFilter(ipOctet, filter)) {
                return false;
            }
        }

        return true;
    }

    private boolean applyOctetFilter(int ipOctet, IpPatternFilterOctet filter) {
        if (filter.filterType.equals(FilterType.Any)) {
            return true;
        } else if (filter.filterType.equals(FilterType.Single)) {
            return ipOctet == filter.singleNumber;
        } else if (filter.filterType.equals(FilterType.Range)) {
            return ipOctet >= filter.rangeStart &&
                ipOctet <= filter.rangeEnd;
        }

        return false;
    }
}
