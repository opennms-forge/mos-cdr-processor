MosCdrProcessor
=============

Overview
--------

MosCdrProcessor is a Java CLI application to watch a "drop" directory, parse CDR (Call Data Record) files
(generally in CSV format), parse out MOS (Mean Opinion Score) values and send to OpenNMS via Graphite messages.
Values are then stored in a timeseries database (RRD, JRobin or Cortex) and can be viewed via Grafana/HELM.


Features
--------

These features are in various stages of development.

* **CDR Files**

    Read CDR formatted files

* **MOS**

    Read MOS values from CDR formatted files

* **Graphite**

    Send Graphite messages (basically, path - numeric value - timestamp) to one or more Graphite servers,
    typically an OpenNMS instance.

* **Folder processing**

    Process all files in a folder in timeseries order.

* **File Watching**

    Watch a folder for new CDR files and automatically process them

* **Archiving**

    Archive processed files.


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

There are a few unit tests, they can be run using `mvn test`.

Debugging
---------
Can debug using Visual Studio Code. See `sample_launch.json`, put this or similar into `.vscode/launch.json`.

Make sure to install Extensions for Java which installs various Java extensions into VS Code, including "Debugger for Java"

In VS Code, go to Run and Debug icon, make sure "Launch Starter" is selected, click Run button.

Make sure to have already put a breakpoint in Starter.main.

Should already have run `mvn clean package -DskipTests`.

It's not guaranteed that this project will build in VS Code; it will build via Maven, and can be debugged in VS Code.

VS Code may pop up a message: "ConfigError: The project 'moscdrprocessor' is not a valid java project." Clicking "Fix" should work.

Also helpful on Macbook - go into System Preferences, Keyboard, Shortcuts, Function Keys.
Add "Visual Studio Code.app", this will enable function keys on TouchBar while debugging.

After this, F11 for "Step Into" doesn't work right.
Go into System Preferences, Keyboard, Shortcuts, Mission Control, deselect F11 (unmaps it from "Show Desktop").

The Groovy script file log messages will display in the VS Code Terminal, these can be used as a debugging aid.


Running and Configuration
-------------------------

Configure using `data/runConfig.json`. See `RunConfig` class for more info. A sample can be found in `data/runConfig.json`.

Use `recipients` to set one or more OpenNMS instance/shards or other Graphite server instances to send messages to.
Define `hostName` and `port` for each. The script handling Graphite messages in OpenNMS will drop messages unless they can be associated with a node being managed by that OpenNMS instance.

When run in `watch` mode, the application will watch for any CDR files dropped into the `data/drop` folder 
(or wherever specified in `runConfig.json`, `dropFolder` parameter).

When run in `folder` mode, the application will find all CDR records in the `drop` folder and process them
in alphabetical order, which should mean they are processed in timeseries order and will be correctly
saved in-order in the timeseries database (RRD, JRobin or Cortex).

Files can be processed using the built-in CDR processor, or else via a Groovy script, `cdr-basic-message-generator.groovy`.

For the built-in processor, you must supply a CDR header CSV file for AcctStatusType of 2 and specify in `runConfig.headerFile`.
This is used to determine which column the data will be found in.
Support is only provided for AcctStatusType of 2.

If `enableArchive` is set to `true` and an `archiveFolder` is set, as files are processed they will be moved from the 
`dropFolder` folder to the `archiveFolder`. This saves the original data files plus ensures they are not processed multiple times.

If `enableDelete` is set to `true`, processed files will be deleted after processing. Use with caution!
If `enableArchive` is set, archiving will take precedence.

Several fields in the CDR will be used to determine the IP address of the OpenNMS-managed device to associate messages with. The `sourceIpFiltersAnyOf` value in the `runConfig.json` is used to select only appropriate IP addresses or address ranges to use. You can specify exact address, use `*` to specify all addresses within an octet, or `a-b` to specify a range of addresses within an octet.

Example:

```
    "sourceIpFiltersAnyOf": [
        "10.0.0.1",
        "10.0.0.98-99",
        "10.0.1.*",
        "192.168.6.12-24"
    ]
```


Horizon and Cortex Setup
------------------------

Get latest Horizon 30.x. Need at least 30.0.4 due to GraphiteAdapter changes.

Copy [mos-cdr-graphite-telemetry-interface.groovy](https://github.com/opennms-forge/mos-cdr-processor/blob/main/assets/opennms/etc/telemetryd-adapters/mos-cdr-graphite-telemetry-interface.groovy), put into OpenNMS `/etc/telemetryd-adapters` directory.

Edit OpenNMS `etc/telemetryd-configuration.xml`, see Graphite listener and queue sections.

Set listener to `enabled`:

```
<listener name="Graphite-UDP-2003" class-name="org.opennms.netmgt.telemetry.listeners.UdpListener" enabled="true">
```

Update `queue` section to be enabled; set correct script filename. Modify `rrd` section as needed.

```
<queue name="Graphite">
    <adapter name="Graphite" class-name="org.opennms.netmgt.telemetry.protocols.graphite.adapter.GraphiteAdapter" enabled="true">
        <parameter key="script" value="/Users/stheleman-opennms/projects/opennms/target/opennms-30.0.4-SNAPSHOT/etc/telemetryd-adapters/mos-cdr-graphite-telemetry-interface.groovy"/>
         <package name="Graphite-Default">
            <rrd step="300">
                <rra>RRA:AVERAGE:0.5:1:2016</rra>
                <rra>RRA:AVERAGE:0.5:12:1488</rra>
                <rra>RRA:AVERAGE:0.5:288:366</rra>
                <rra>RRA:MAX:0.5:288:366</rra>
                <rra>RRA:MIN:0.5:288:366</rra>
            </rrd>
        </package>
    </adapter>
</queue>
```

Cortex Setup
------------

You will need the OpenNMS Cortex TSS Plugin, found here: [opennms-cortex-tss-plugin](https://github.com/OpenNMS/opennms-cortex-tss-plugin).

Follow instructions found there. You will need to build the plugin locally, add the `/etc/opennms.properties.d/cortex.properties` as well as to run the karaf commands to install.


Grafana/HELM Setup
------------------

To view the values in Grafana using HELM, you'll need to install the OpenNMS HELM plugin and OpenNMS Entities Datasource in Grafana. Currenty support is for Grafana 8.

Example dashboard will be uploaded here or sent directly.



