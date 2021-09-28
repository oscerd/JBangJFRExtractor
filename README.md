# JBangJFRExtractor
A JFR raw data Extractor made with JBang

```shell script
jbang JFRExtractorCli.java -h
Usage: JFRExtractorCli [-hV] -i=<jfrFile> -k=<kind> [-o=<outputFile>]
JFRExtractorCli made with jbang
  -h, --help              Show this help message and exit.
  -i, --input=<jfrFile>   The number JFR File name
  -k, --kind=<kind>       Enum values: heap, cpu
  -o, --output=<outputFile>
                          The raw data file output name
  -V, --version           Print version information and exit.
```

## Plotting with GNUPlot

Suppose you created an output file named for example cpu.jfr, you can plot a png through that with the following command:

```shell script
gnuplot -e "filename='cpu.jfr';output_file='file.png'" gnuplot_script/cpu_graph 
```
