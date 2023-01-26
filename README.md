# JLogRep - Automated log parser

The name comes from Java Logs /re/p - search logs by regular expressions and pring

[[_TOC_]]

## Requirements

- JDK 11+

## Building

Windows:
`gradlew shadowJar`

Linux/Mac: `./gradlew shadowJar`

Jar file with all dependencies will be generated in build/libs

## Launch

Run: `java -jar jlogrep.jar`.

Open `http://localhost:8080` in your favourite web browser. Upload your logs and go!

## Optional parameters

| name           | description                                                                                     |
|----------------|-------------------------------------------------------------------------------------------------|
| rules.yml      | file with rules to grep                                                                         |
| dateFormat.yml | path to dateFormat.yml with date format rules.                                                  |
| date           | problem date. If not specified, the program will grep the whole bugreport                       |
| interval       | range around problem date in minutes. 10 if not specified                                       |
| year           | for files where year is missing, the value of this option will be year. Default is current year |
| show           | the value of this option overrides how many examples of the problem will be shown in the output |
| tags           | tags of the rules to grep, separated by spaces. Omit to grep everything                         |

## Supported bugreport file types:

JLogRep accepts ZIP, TAR.GZ, and plaintext files as `bugreport` parameter.
Also, it supports nested archives, so there's no problem in processing tar.gz, which is in zip archive, which is in the
tar.gz archive... and so on.
More formats can be added if necessary - just
implement [Archive](src/main/java/com/senderman/jlogrep/archive/Archive.java)
interface, and populate ArchiveType enum.

## Rules format

If not supplied, the program will use its [default rules](src/main/resources/rules.yml).
To see how to create your own grep rules - see [writing your own rules](docs/writing-your-own-rules.md)

## DateFormat file format

If not supplied, the program will use its [default dateformat](src/main/resources/dateformat.yml).
dateformat.yml is also using yaml format. Every `format` is Java 8 SimpleDateFormat.
See https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html

You can use [this schema](schema/jlogrep-dateformat.json) for validation

Example:

```yaml
default: # default (fallback) date format
  file: anyname
  format: 'yyyy-MM-dd HH:mm:ss.SSS'
  regex: '\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}:\d{3}' # regex that matches the date format

rules:
  - file: dmesg # File name part. All files that contain the value of this field in their name match
    format: 'EEE MMM d HH:mm:ss yyyy'
    regex: '\p{L}{3} \p{L}{3} \d{1,2} \d{2}:\d{2}:\d{2} \d{4}'

  - file: logcat
    format: 'yyyy-MM-dd HH:mm:ss.SSS'
    possibleMissingYear: true # set to true if it's possible for this file to have missing year in log dates
    yearSuffix: "-" # if possibleMissingYear is true, then this suffix will be added between year and the log string. See LogStringReader#enableYearPrefix()
```
