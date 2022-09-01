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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.lang.Thread;

import com.google.common.base.Strings;
import org.opennms.moscdrprocessor.runners.SingleCdrFileProcessor;

/**
 * Command to process all CDR records in a folder in timestamp order.
 */
public class ProcessFolderCommand extends BasicCommand {

    @Override
    protected void execute() throws CmdRunException {
        LOG.info("In ProcessFolderCommand.execute");
        parseRunConfig();

        if (Strings.isNullOrEmpty(runConfig.dropFolder)) {
            throw new CmdRunException("Process Folder command called without specifying a dropFolder");
        }
    
        List<File> files = Collections.emptyList();

        try {
            files = Files.list(Paths.get(runConfig.dropFolder))
                .map(Path::toFile)
                .filter(File::isFile)
                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new CmdRunException("Process Folder command error getting files: " + e.getMessage(), e);
        }
    
        // Sort by filename, should be in ascending timestamp order if CDR files are named correctly
        files.sort((a, b) -> a.getName().compareTo(b.getName()));
        
        var processor = new SingleCdrFileProcessor();

        for (File f : files) {
            RunConfig clonedConfig = cloneRunConfig(this.runConfig);
            clonedConfig.filePath = f.getAbsolutePath();

            processor.process(clonedConfig, f.getAbsolutePath(), LOG);

            // Sleep so receiver has chance to process these messages in order
            if (clonedConfig.sleepMillis > 0) {
                try {
                    LOG.debug("Sleeping for {} ms...", clonedConfig.sleepMillis);
                    Thread.sleep(clonedConfig.sleepMillis);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    @Override
    protected String getDescription() {
        return "Process all CDR files in the dropFolder in timestamp order and send Graphite messages.";
    }
}
