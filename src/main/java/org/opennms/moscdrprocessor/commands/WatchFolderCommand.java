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

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Strings;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import org.opennms.moscdrprocessor.runners.ProcessRunner;
import org.opennms.moscdrprocessor.runners.ProcessRunnerImpl;
import org.opennms.moscdrprocessor.runners.ProcessScriptRunner;

/**
 * Command to watch a folder for CDR file drops, then process them.
 */
public class WatchFolderCommand extends Command {
    
    @Option(name = "--config", usage = "Full file path to config file", required = true, metaVar = "<config>")
    private String configFilePath;

    private RunConfig runConfig;

    private volatile boolean stopConsuming;

    private Object lock = new Object();

    private List<String> fileQueue = new ArrayList<>();

    @Override
    protected void execute() throws CmdRunException {
        LOG.info("In WatchFolderCommand.execute");

        // parse RunConfig. possibly push this up to base class
        try {
            runConfig = parseConfig(configFilePath);
        } catch (IOException e) {
            throw new CmdRunException("Error parsing config file '" + configFilePath + "': " + e.getMessage(), e);
        }

        LOG.debug("Successfully parsed config file.");

        if (!runConfig.enableWatch || Strings.isNullOrEmpty(runConfig.watchFolder)) {
            throw new CmdRunException("Watch command called without enabling watch or specifying a watchFolder");
        }

        // launch consumer
        Thread consumer = new Thread(() -> {
            try {
                consume();
            } catch (InterruptedException e) {
            }
        });
        consumer.setDaemon(true);
        consumer.start();

        Exception caughtException = null;

        try {
            startWatching();
        } catch (CmdRunException e) {
            LOG.error("Caught exception while watching. Signalling consumer to stop");
            stopConsuming = true;
            caughtException = e;
        }

        if (caughtException != null) {
            LOG.error("Caught exception: {}", caughtException.getMessage());
        }

        LOG.info("In finally, joining consumer thread");

        try {
            consumer.join();
        } catch (InterruptedException e) {
        }

        LOG.info("WatchFolderCommand completed.");
    }

    @Override
    protected void validate(CmdLineParser parser) throws CmdLineException {
    }

    @Override
    public void printUsage() {
        super.printUsage();
        LOG.info("");
        LOG.info("Examples:");
        LOG.info("  Specify a json config file: java -jar CdrProcessor.jar watch --config \"/usr/local/myconfig.json\"");
        LOG.info("");
        LOG.info("(replace CdrProcessor.jar with actual jar, e.g. moscdrprocessor-0.0.1-SNAPSHOT-onejar.jar)");
    }

    @Override
    protected String getDescription() {
        return "Watch a folder, process any CDR files dropped into it and send Graphite messages.";
    }

    private void startWatching() throws CmdRunException {
        LOG.info("Starting watch");

        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            Path path = Paths.get(runConfig.watchFolder);

            WatchKey registerKey = path.register(watchService, ENTRY_CREATE);

            while (true) {
                LOG.debug("Calling watchService.take");

                WatchKey key = watchService.take();

                LOG.debug("Watch service received event");

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> eventKind = event.kind();
                    LOG.debug("Got event of kind: {}", eventKind.toString());

                    if (eventKind == OVERFLOW) {
                        LOG.info("Event overflow occurred");
                        continue;
                    }

                    WatchEvent<Path> currEvent = (WatchEvent<Path>) event;
                    Path dirEntry = currEvent.context();
                    LOG.info("{} occurred on {}", eventKind, dirEntry);

                    File file = path.resolve(dirEntry).toFile(); 

                    LOG.info("File changed, adding to process queue: {}", file.getAbsolutePath());

                    addFileToQueue(file);
                
                    LOG.debug("Resuming pollEvents");
                }

                LOG.debug("All events polled, resetting WatchKey");
                boolean isKeyValid = key.reset();

                if (!isKeyValid) {
                    LOG.info("Stopped watch directory {}", runConfig.watchFolder);
                    break;
                }
            }
        } catch (IOException ioe) {
            throw new CmdRunException(
                String.format("IOException watching folder %s: %s", runConfig.watchFolder, ioe.getMessage()), ioe);
        } catch (InterruptedException inte) {
            throw new CmdRunException(
                String.format("InterruptedException watching folder %s: %s", runConfig.watchFolder, inte.getMessage()), inte);
        }
    }

    private void addFileToQueue(File file) {
        String path = file.getAbsolutePath();
        LOG.info("Adding file to queue: {}", path);

        synchronized(lock) {
            this.fileQueue.add(path);
        }
    }

    private void consume() throws InterruptedException {
        LOG.info("Entering consume");

        while (true) {
            if (stopConsuming) {
                LOG.info("Consumer caught stopConsuming, exiting");
                break;
            }

            // If no files to process, sleep for 1 second
            if (this.fileQueue.isEmpty()) {
                LOG.debug("In consumer, no files in queue, sleeping for 1 second");
                Thread.sleep(1000);
                continue;
            }

            // Check for a file to process
            String filePathToProcess = null;

            synchronized (this.lock) {
                if (!this.fileQueue.isEmpty()) {
                    filePathToProcess = this.fileQueue.remove(0);
                }
            }

            if (!Strings.isNullOrEmpty(filePathToProcess)) {
                ProcessRunner runner = null;

                RunConfig clonedConfig = cloneRunConfig(this.runConfig);
                clonedConfig.filePath = filePathToProcess;

                if (this.runConfig.useScript && !Strings.isNullOrEmpty(this.runConfig.cdrParseScript)) {
                    LOG.info("Launching background script-based processor for file: {}", filePathToProcess);
                    runner = new ProcessScriptRunner(clonedConfig, this.LOG);
                } else {
                    LOG.info("Launching background processor for file: {}", filePathToProcess);
                    runner = new ProcessRunnerImpl(clonedConfig, this.LOG);
                }

                // For now, blocking until processing current file is done, but could launch multiple threads here
                runner.run();
            }
        }

        LOG.info("Exiting consume");
    }
}
