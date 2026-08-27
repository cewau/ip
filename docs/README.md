# Zucc User Guide

// Update the title above to match the actual product name

// Product screenshot goes here

// Product intro goes here

## Adding deadlines

Use the `deadline` command for a task that must be completed by a particular
date and time. Enter the value in `d/M/yyyy HHmm` format after `/by`.

Example:

`deadline return book /by 2/12/2019 1800`

Zucc understands the date and time and displays them in a friendlier format:

```
[D][ ] return book (by: Dec 02 2019, 6:00PM)
```

## Adding events

Use the `event` command for an activity with a start and end. Both values use
the same `d/M/yyyy HHmm` format.

Example:

`event project meeting /from 3/12/2019 1400 /to 3/12/2019 1530`

```
[E][ ] project meeting (from: Dec 03 2019, 2:00PM to: Dec 03 2019, 3:30PM)
```

## Viewing tasks on a date

Use the `on` command with a date in `d/M/yyyy` format to see deadlines due
that day and events occurring that day.

Example:

`on 3/12/2019`

```
Here are the tasks on Dec 03 2019:
2.[E][ ] project meeting (from: Dec 03 2019, 2:00PM to: Dec 03 2019, 3:30PM)
```


## Feature XYZ

// Feature details
