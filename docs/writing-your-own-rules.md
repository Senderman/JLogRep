To write your own rules, you should create a .yml file and fill it with your rules.
You can see the JSON schema (and use it e.g. in Intellij IDEA to validate it), [here](/schema/jlogrep-rules.json).

The file itself is a list of Rule objects.

[[_TOC_]]

# The Rule object

Every Rule object consists of several properties.

| Required | Name       | Description                                                                              | Default value |
|----------|------------|------------------------------------------------------------------------------------------|---------------|
| YES      | name       | Name of the problem                                                                      |               |
| NO       | show       | How many examples to show in the output                                                  | 5             |
| NO       | type       | Could be SIMPLE, EXTRACT, or MULTILINE. See [rule types](#rule-types).                   | SIMPLE        |
| NO       | showAlways | If true, and the given rule is matched at least once, the rule will be always shown      | false         |
| YES      | patterns   | List of Pattern objects                                                                  |               |
| YES      | tags       | List of tags for this rule. Tags are useful to grep only for rules marked with giventags |               |
| NO       | filters    | Filters for the type of the rule. See [rule filters](#rule-filters)                      |               |

# The Pattern object

Every Pattern object consists of a file name part, and a list of regexes, which will be applied against every line in
the matched file.

| Required | Name | Description                                                                                                            |
|----------|------|------------------------------------------------------------------------------------------------------------------------|
| YES      | name | File name part for this Pattern, will match against any file whose name contains it. Set to `*` to match any file      |
| YES      | show | Fist of regexes in Java dialect. The way this list is handled depends on the `type` of the parent Rule of this Pattern |

# Example

```yaml
- name: "Timeouts"
  show: 2
  showAlways: true
  patterns:
    - file: logcat
      regexes:
        - "EventLogger.*SocketTimeoutException"
    - file: service_spaceship
      regexes:
        - "ERROR.*amazonaws.com.*Timeout"
  tags:
    - net
    - wifi

- name: "Unable to resolve resources"
  show: 2
  patterns:
    - file: service_spaceship
      regexes:
        - "ERROR.*plbl.m9.lfstrm.tv.*resolve"
        - "ERROR.*amazonaws.com.*resolve"
  tags:
    - hw

- name: "Bad bluetooth signal"
  type: EXTRACT
  patterns:
    - file: logcat
      regexes:
        - 'BluetoothDiscovery.*?DEVICE:(.*?),.*NAME:(.*?),.*(RSSI:(?:-[789]\d{1,2}|-1[0-2][0-6]))'
  tags:
    - bt
    - hw
  filters:
    - unique
```

# Rule types

Depending on the `type` property of the Rule object, patterns will be handled differently.

## SIMPLE

This is the default Rule type. In this case, every line in the matched file will be checked against all regexes for this
file, wrapped with `.*`, joined by `|`. Explanation in java:

```java
class Main {
    public static void main(String[] args) {
        String line = "some log line";
        String joinedRegex = regexes
                .stream()
                .map(r -> ".*" + r + ".*")
                .collect(Collectors.joining("|"));
        boolean matches = Pattern.compile(joinedRegex).matcher(line).matches();
    }
}
```

(Of course it's done by more optimized way, with pre-compiled patterns and parallel streams, it's just a reference
implementation).

So, given the rule

```yaml
- name: "Unable to resolve resources"
  show: 2
  patterns:
    - file: service_spaceship
      regexes:
        - "ERROR.*plbl.m9.lfstrm.tv.*resolve"
        - "ERROR.*amazonaws.com.*resolve"
  tags:
    - hw
```

Every log line in every file, which contains `service_spaceship` in the name, will be checked against the following
regex:

```regex
.*ERROR.*plbl.m9.lfstrm.tv.*resolve.*|.*ERROR.*amazonaws.com.*resolve.*`
```

## EXTRACT

This type of rule is handled differently from SIMPLE, because it doesn't just show the matched log line in the output.
Instead, for every line in the log file, it checks for every regex (wrapped with `.*` of course) if it is matched
against this line, and soon as the first regex is matched, it extracts all capturing groups from the log line, and
prints it in the following format:
`date: group1, group2, ..., groupN`
If several rule's regexes will match in the same line, the groups from the one regex will be joined by comma,
and the groups from each regex will be joined by `<br>`

So, given the rule

```yaml
- name: "User wants or needs something tasty"
  type: EXTRACT
  patterns:
    - file: logcat
      regexes:
        - 'want (.*) and (.*) for (.*)'
        - 'need (.*) and (.*)'
  tags:
    - bt
    - hw
```

And a log line `12.03.2022 15:46 I want an apple and an orange for breakfast`, the program will:

1. check if the log line matches against `.*want (.*) and (.*) for (.*).*` regex
2. extract groups from the log line
3. print `12.03.2022 15:46: an apple, an orange, breakfast`

Then it will check the log line against the `.*need (.*) and (.*).*` regex, and if it matches, it will join both
extracted
groups with `<br>`

## MULTILINE

This type of rule is used when you have to grep multiple log line as one problem.
The `regexes` array in this rule **should** only contain 2 regexes: one for the first line, and second for the last
line.
The date of the example will be the date of the first line.

So, given the rule

```yaml
- name: "Shopping list"
  show: 3
  type: MULTILINE
  patterns:
    - file: service_shopping
      regexes:
        - 'To buy'
        - 'Shopping list end'
  tags:
    - shop
```

and the following log:

```text
12.03.2022 15:46 Wash the dishes
12.03.2022 15:47 Clean up
12.03.2022 15:48 To buy:
 - apples
 - eggs
 - canned beef
Shopping list end
12.03.2022 15:50 Go to bed
```

The program will:

1. Skip the log file to the `12.03.2022 15:48 To buy` line (by checking every line against `To buy` regex)
2. Save all lines until `Shopping list end` line (**exclusive**).
3. Print the first line and the saved lines from pt.2, separated with `<br>`

**Note:** since all these lines will be shown as **one** example, the `show` parameter in the rule will determine
how many multiline results will be shown, **not the amount of lines in the example itself!**

# Rule filters

Rule filters are applied every time a problem is grepped from file.

You can add filters names to the `filters` list of the rule.
If some filters are passed through the request parameters (via frontend), they will be applied to all rules,
so it's possible to e.g. make all rules show only unique values.

**Note that the order of filters is not determined**

Also, note that filters only change what lines will go to the output, but not the counter of the matched lines!

## Unique

You can add `UNIQUE` filter to the `filters` list of the rule to make this rule show only unique matched lines (ignoring
the date). It works for any type of the rule.

