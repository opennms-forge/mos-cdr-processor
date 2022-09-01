MosCdrProcessor
=============

Overview
--------

MosCdrProcessor is a Java CLI application to watch a "drop" directory, parse CDR (Call Data Record) files
(generally in CSV format), parse out MOS (Mean Opinion Score) values and send to OpenNMS via Graphite messages.


Features
--------

These features are in various stages of development.

* **CDR Files**

    Read CDR formatted files

* **MOS**

    Read MOS values from CDR formatted files

* **Graphite**

    Send Graphite messages (basically, path - numeric value - timestamp) to a Graphite server, typically OpenNMS.

* **File Watching**

    Watch a folder for new CDR files and automatically process them


Development / Build
-------------------

This project is written using Java 11.

Run `mvn clean package -DskipTests` to build.

A "one-jar" file will be in `target`.

Can run using the following. All configuration is in a "runConfig.json" file.

```
cd target
java -jar moscdrprocessor-0.0.1-SNAPSHOT-onejar.jar watch --config "path/to/runConfig.json"
```

You can use the `run-mos-cdr-processor.sh` script to do this as well.

Debugging
---------
Can debug using VS Code. See `sample_launch.json`, put this or similar into `.vscode/launch.json`.

Make sure to install Extensions for Java which installs various Java extensions into VS Code, including "Debugger for Java"

In VS Code, go to Run and Debug icon, make sure "Launch Starter" is selected, click Run button.

Make sure to have already put a breakpoint in Starter.main.

Should already have run `mvn clean package -DskipTests`.

It's not guaranteed that this project will build in VS Code; it will build via Maven, and can be debugged in VS Code.

VS Code may pop up a message: "ConfigError: The project 'moscdrprocessor' is not a valid java project." Clicking "Fix" seems to work.

Also helpful on Macbook - go into System Preferences, Keyboard, Shortcuts, Function Keys.
Add "Visual Studio Code.app", this will enable function keys on TouchBar while debugging.

After this, F11 for "Step Into" doesn't work right.
Go into System Preferences, Keyboard, Shortcuts, Mission Control, deselect F11 (unmaps it from "Show Desktop").

The Groovy script file log messages will display in the VS Code Terminal, these can be used as a debugging aid.


Running and Configuration
-------------------------

When run in `watch` mode, the application will watch for any CDR files dropped into the `data/drop` folder 
(or wherever specified in `runConfig.json`, `dropFolder` parameter).

Files can be processed using the built-in CDR processor, or else via a Groovy script, `cdr-basic-message-generator.groovy`.

For the built-in processor, you must supply a CDR header CSV file for AcctStatusType of 2 and specify in `runConfig.headerFile`.

If `enableArchive` is set to `true` and an `archiveFolder` is set, as files are processed they will be moved from the 
`drop` folder to the `archiveFolder`. This saves the original data files plus ensures they are not processed multiple times.

If `enableDelete` is set to `true`, processed files will be deleted after processing. If `enableArchive` is set, archiving
will take precedence.
