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

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a CDR record.
 */
public class CdrRecord implements Iterable<CdrRecordItem> {

    /** Filename of the CDR file. */
    public String fileName;

    /** File type, either "HDC" or "CDC". Unsure what these mean. */
    public String fileType;

    /** Timestamp as a String, from the CDR filename. */
    public String fileTimeStamp;
    
    /** Timestamp as a Date object, from the CDR filename. */
    public Date fileTime;

    /** File name suffix, a-j */
    public String suffix;

    public List<CdrRecordItem> items = new ArrayList<>();


    // GETTERS
    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public String getFileTimeStamp() {
        return this.fileTimeStamp;
    }

    public Date getFileTime() {
        return fileTime;
    }


    // SETTERS
    public void setFileName(String filename) {
        this.fileName = filename;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public void setFileTimeStamp(String timestamp) {
        this.fileTimeStamp = timestamp;
    }

    public void setFileTime(Date time) {
        this.fileTime = time;
    }

    // METHODS
    public void add(CdrRecordItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Item must not be null");
        }

        items.add(item);
    }

    public CdrRecordItem get(int index) {
        if (index < 0 || index >= items.size()) {
            throw new IndexOutOfBoundsException("Index out of range of record items.");
        }
    
        return items.get(index);
    }

    public Iterator<CdrRecordItem> iterator() {
        return items.iterator();
    }
}
