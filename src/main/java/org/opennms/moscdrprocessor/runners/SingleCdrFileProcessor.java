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

package org.opennms.moscdrprocessor.runners;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import com.google.common.base.Strings;

import org.opennms.moscdrprocessor.commands.RunConfig;
import org.opennms.moscdrprocessor.log.LogAdapter;

/**
 * Utility class to process a single CDR File.
 */
public class SingleCdrFileProcessor {
    
    /**
     * Make sure to clone the RunConfig before passing in.
     */
    public void process(RunConfig runConfig, String filePathToProcess, LogAdapter LOG) {
        ProcessRunner runner = null;

        if (runConfig.useScript && !Strings.isNullOrEmpty(runConfig.cdrParseScript)) {
            LOG.info("Launching background script-based processor for file: {}", filePathToProcess);
            runner = new ProcessScriptRunner(runConfig, LOG);
        } else {
            LOG.info("Launching background processor for file: {}", filePathToProcess);
            runner = new ProcessRunnerImpl(runConfig, LOG);
        }

        // For now, blocking until processing current file is done, but could launch multiple threads here
        runner.run();

        // Archive or delete the file, removing it from drop folder so it's not reprocessed
        File f = new File(runConfig.filePath);
        Path source = Path.of(runConfig.filePath);

        if (runConfig.enableArchive && !Strings.isNullOrEmpty(runConfig.archiveFolder)) {
            try {
                Path dest = Path.of(runConfig.archiveFolder, f.getName());

                LOG.info("Archiving file '{}' to '{}'", source.toString(), dest.toString());

                Files.move(source, dest, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                LOG.error("Could not archive file '{}' to folder '{}': {}",
                    runConfig.filePath, runConfig.archiveFolder, e.getMessage());
            }
        } else if (runConfig.enableDelete) {
            try {
                LOG.info("Deleting file '{}'", source.toString());

                Files.delete(source);;
            } catch (IOException e) {
                LOG.error("Could not delete file '{}': {}", runConfig.filePath, e.getMessage());
            }
        }
    }
}
