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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.google.common.base.Throwables;
import org.junit.rules.TestName;
import org.opennms.moscdrprocessor.model.IpPatternFilter.IpPatternFilterOctet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class IpPatternFilterTest {

    @Rule
    public TestName testName = new TestName();
    
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testCreateFilter() {
        String filterPattern = "10.0-253.2-9.*";

        IpPatternFilter filter = null;
        
        try {
            filter = new IpPatternFilter(filterPattern);
        } catch (IllegalArgumentException e) {
            Assert.fail("IpPatternFilter ctor threw IllegalArgumentException: " + e.getMessage());
            return;
        }
        
        IpPatternFilterOctet[] octets = filter.getOctets();
        
        Assert.assertEquals(filterPattern, filter.getPattern());
        
        Assert.assertEquals(IpPatternFilter.FilterType.Single, octets[0].filterType);
        Assert.assertEquals(10, octets[0].singleNumber);

        Assert.assertEquals(IpPatternFilter.FilterType.Range, octets[1].filterType);
        Assert.assertEquals(0, octets[1].rangeStart);
        Assert.assertEquals(253, octets[1].rangeEnd);

        Assert.assertEquals(IpPatternFilter.FilterType.Range, octets[2].filterType);
        Assert.assertEquals(2, octets[2].rangeStart);
        Assert.assertEquals(9, octets[2].rangeEnd);

        Assert.assertEquals(IpPatternFilter.FilterType.Any, octets[3].filterType);
    }

    @Test
    public void testApplyFilter() {
        String filterPattern = "10.0-253.2-9.*";

        IpPatternFilter filter = null;
        
        try {
            filter = new IpPatternFilter(filterPattern);
        } catch (IllegalArgumentException e) {
            Assert.fail("IpPatternFilter ctor threw IllegalArgumentException: " + e.getMessage());
            return;
        }
 
        Assert.assertTrue(filter.isMatch("10.0.2.22"));
        Assert.assertTrue(filter.isMatch("10.253.9.88"));
        Assert.assertTrue(filter.isMatch("10.253.9.254"));
        Assert.assertFalse(filter.isMatch("10.254.9.88"));
        Assert.assertFalse(filter.isMatch("10.253.0.88"));
        Assert.assertFalse(filter.isMatch("10.253.1.88"));
        Assert.assertFalse(filter.isMatch("10.253.10.88"));
        Assert.assertFalse(filter.isMatch("10.253.100.88"));

        Assert.assertFalse(filter.isMatch("11.0.2.22"));
    }
}
