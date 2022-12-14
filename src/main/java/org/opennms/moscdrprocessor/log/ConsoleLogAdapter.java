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

package org.opennms.moscdrprocessor.log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;

/**
 * The STDOUT/STERR implementation of the {@ink LogAdapter}.
 */
public class ConsoleLogAdapter implements LogAdapter {

    private boolean debug;

    @Override
    public void warn(String message, Object... args) {
        System.out.println(String.format("%s: %s", formatHeader("WARNING"), format(message, args)));
    }

    @Override
    public void error(String message, Object... args) {
        System.err.println(String.format("%s: %s", formatHeader("ERROR"), format(message, args)));
    }

    @Override
    public void debug(String message, Object... args) {
        if (debug) {
            System.out.println(String.format("%s: %s", formatHeader("DEBUG"), format(message, args)));
        }
    }

    @Override
    public void info(String message, Object... args) {
        System.out.println(String.format("%s: %s", formatHeader("INFO"), format(message, args)));
    }

    @Override
    public void info(InputStream inputStream) {
        try {
            ByteStreams.copy(inputStream, System.out);
        } catch (IOException e) {
            Throwables.propagate(e);
        }
    }

    @Override
    public boolean isDebugEnabled() {
        return debug;
    }

    public OutputStream getErrorOutputStream() {
        return System.err;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public OutputStream getOutputStream() {
        return System.out;
    }

    protected String formatHeader(String header) {
        String date = new Date().toInstant().toString();

        return String.format("[%s][%s]", date, header);
    }
}
