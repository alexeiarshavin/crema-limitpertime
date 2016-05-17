# crema-limitpertime

A Java library that provides functionality for enforcing a counter limit per time.


### Example use:

Want to grant a certain 'user' to access a certain 'resource', but only a limited number of times per
time range, eg 10 times in 5 minutes, 100 times in 1 hour, and 1000 times in a day.

The first step is to create the limitation definition:
`LimitPerTime limitPerTime = LimitsPerTime.forString("10 in 5 MINUTES; 100 in 1 HOUR; 1000 in 1day");`

Then you need a key object that identifies your use case. It can be the user's identifier.
And with that you see if he's within the limits:

```
if (limitPerTime.consume("username")) {
  //ok, it was counted, go ahead
}
```

The ones that consumed within the current time interval are automatically cleared
at the end of the interval.


### ABOUT MEMORY CONSUMPTION

The implementations use a standard Java HashMap to remember the Object key as given in LimitPerTime.consume()
plus the counter as a Double for each key ever used, until the time range expires and it is cleared.

Having very long time ranges means keeping all those keys and counters for a long time.

Hint: use small/cheap Object keys, and try to limit the maximal time.


### ABOUT TIMERS

The implementations use old style Java timers for clearing the counters.

There is one timer instance per time range definition.

The timers are created with the daemon flag, therefore they don't stop your application from exiting normally.


### ABOUT TIME UNITS

The smallest permitted time range unit is MILLISECONDS.
MICROSECONDS and NANOSECONDS are not supported.


### TECHNICAL

For Java7 and later. Uses the Guava library as dependency.

