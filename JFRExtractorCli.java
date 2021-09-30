///usr/bin/env jbang "$0" "$@" ; exit $?
//REPOS mavencentral,jitpack
//DEPS info.picocli:picocli:4.5.0
//DEPS com.github.skebir:prettytable:v1.0

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import jdk.jfr.Event;
import jdk.jfr.EventType;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.Recording;
import jdk.jfr.consumer.EventStream;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;

import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.File;

import org.sk.PrettyTable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

@Command(name = "JFRExtractorCli", mixinStandardHelpOptions = true, version = "JFRExtractorCli 0.1",
        description = "JFRExtractorCli made with jbang")
class JFRExtractorCli implements Callable<Integer> {

    @CommandLine.Option(
            names = {"-i", "--input"},
            description = "The JFR File name",
            required = true)
    private String jfrFile;

    @CommandLine.Option(
            names = {"-o", "--output"},
            description = "The raw data file output name")
    private String outputFile;

    enum Kind {heap, cpu, memory}

    @CommandLine.Option(names = {"-k", "--kind"}, description = "Enum values: ${COMPLETION-CANDIDATES}", required = true)
    Kind kind = null;

    public static void main(String... args) throws IOException {
        int exitCode = new CommandLine(new JFRExtractorCli()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        Path file = Paths.get(jfrFile);

        switch (kind) {
            case heap:
                heapReport(file);
                break;
            case cpu:
                cpuReport(file);
                break;
            case memory:
                memoryReport(file);
                break;
            default:
                break;
        }
        return 0;
    }

    private void heapReport(Path file) throws IOException {
        if (outputFile != null) {
            File fout = new File(outputFile);
            FileOutputStream fos = new FileOutputStream(fout);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            writeHeapReportFile(file, bw);
        } else {
            prettyPrintHeapTable(file);
        }
    }

    private void cpuReport(Path file) throws IOException {
        if (outputFile != null) {
            File fout = new File(outputFile);
            FileOutputStream fos = new FileOutputStream(fout);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            writeCpuReportFile(file, bw);
        } else {
            prettyPrintCpuTable(file);
        }
    }

    private void memoryReport(Path file) throws IOException {
        if (outputFile != null) {
            File fout = new File(outputFile);
            FileOutputStream fos = new FileOutputStream(fout);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            writeMemoryReportFile(file, bw);
        } else {
            prettyPrintMemoryTable(file);
        }
    }

    private void writeHeapReportFile(Path file, BufferedWriter bw) throws IOException {
        bw.write("# Sample Value When GcID StartTime");
        bw.newLine();


        try (var recordingFile = new RecordingFile(file)) {

            int i = 0;
            while (recordingFile.hasMoreEvents()) {
                var e = recordingFile.readEvent();
                String eventName = e.getEventType().getName();
                if (eventName.equalsIgnoreCase("jdk.GCHeapSummary")) {
                    long heapUsed = e.getValue("heapUsed");
                    String whenDate = e.getValue("when");
                    int gcId = e.getValue("gcId");
                    long startTime = e.getValue("startTime");
                    bw.write(i + " " + String.valueOf(((float) heapUsed) / ((float) 1048576)) + " " + whenDate + " " + gcId + " " + startTime);
                    bw.newLine();
                    i++;
                }
            }
        }
        bw.close();
    }

    private void prettyPrintHeapTable(Path file) throws IOException {
        PrettyTable table = new PrettyTable("Sample", "Value", "When" , "GcID", "StartTime");
        try (var recordingFile = new RecordingFile(file)) {

            int i = 0;
            while (recordingFile.hasMoreEvents()) {
                var e = recordingFile.readEvent();
                String eventName = e.getEventType().getName();
                if (eventName.equalsIgnoreCase("jdk.GCHeapSummary")) {
                    long heapUsed = e.getValue("heapUsed");
                    String whenDate = e.getValue("when");
                    int gcId = e.getValue("gcId");
                    long startTime = e.getValue("startTime");
                    table.addRow(String.valueOf(i),String.valueOf(((float) heapUsed) / ((float) 1048576)),whenDate,String.valueOf(gcId),String.valueOf(startTime));
                    i++;
                }
            }
        }
        System.out.println(table);
    }

    private void writeCpuReportFile(Path file, BufferedWriter bw) throws IOException {
        bw.write("# Sample Total User System StartTime");
        bw.newLine();


        try (var recordingFile = new RecordingFile(file)) {

            int i = 0;
            while (recordingFile.hasMoreEvents()) {
                var e = recordingFile.readEvent();
                String eventName = e.getEventType().getName();
                if (eventName.equalsIgnoreCase("jdk.CPULoad")) {
                    long startTime = e.getValue("startTime");
                    float user = e.getValue("jvmUser");
                    float system = e.getValue("jvmSystem");
                    float total = e.getValue("machineTotal");
                    bw.write(i + " " + String.valueOf(total) + " " + String.valueOf(user) + " " + String.valueOf(system) + " " + String.valueOf(startTime));
                    bw.newLine();
                    i++;
                }
            }
        }
        bw.close();
    }

    private void prettyPrintCpuTable(Path file) throws IOException {
        PrettyTable table = new PrettyTable("Sample", "Total", "User" , "System", "StartTime");
        try (var recordingFile = new RecordingFile(file)) {

            int i = 0;
            while (recordingFile.hasMoreEvents()) {
                var e = recordingFile.readEvent();
                String eventName = e.getEventType().getName();
                if (eventName.equalsIgnoreCase("jdk.CPULoad")) {
                    long startTime = e.getValue("startTime");
                    float user = e.getValue("jvmUser");
                    float system = e.getValue("jvmSystem");
                    float total = e.getValue("machineTotal");
                    table.addRow(String.valueOf(i),String.valueOf(total), String.valueOf(user),String.valueOf(system),String.valueOf(startTime));
                    i++;
                }
            }
        }
        System.out.println(table);
    }

    private void writeMemoryReportFile(Path file, BufferedWriter bw) throws IOException {
        bw.write("# Sample Total");
        bw.newLine();


        try (var recordingFile = new RecordingFile(file)) {

            int i = 0;
            while (recordingFile.hasMoreEvents()) {
                long total = 0;
                var e = recordingFile.readEvent();
                if (isObjectAllocationEvent(e)) {
                    total = getAllocationSize(e);
                    bw.write(i + " " + String.valueOf(total));
                    bw.newLine();
                    i++;
                }
            }
        }
        bw.close();
    }

    private void prettyPrintMemoryTable(Path file) throws IOException {
        PrettyTable table = new PrettyTable("Sample", "Total");
        try (var recordingFile = new RecordingFile(file)) {

            int i = 0;
            while (recordingFile.hasMoreEvents()) {
                long total = 0;
                var e = recordingFile.readEvent();
                if (isObjectAllocationEvent(e)) {
                total = getAllocationSize(e);
                table.addRow(String.valueOf(i),String.valueOf(total));
                i++;
                }
            }
        }
        System.out.println(table);
    }

    private boolean isObjectAllocationEvent(RecordedEvent re) {
        String name = re.getEventType().getName();
        return name.equals("jdk.ObjectAllocationInNewTLAB") ||
                name.equals("jdk.ObjectAllocationOutsideTLAB");
    }

    private long getAllocationSize(RecordedEvent recordedEvent) {
        return recordedEvent.getEventType().getName()
                .equals("jdk.ObjectAllocationInNewTLAB") ?
                recordedEvent.getLong("tlabSize") :
                recordedEvent.getLong("allocationSize");
    }
}
