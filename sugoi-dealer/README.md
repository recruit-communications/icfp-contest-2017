# Build

```
sbt assembly
```

# Run
```
java -jar sugoi-dealer-assembly-1.0.jar [map JSON files] [punter file path 0] [punter file path 1] ...
```

# Changelog

## 2017-08-06 08:30

- Accelerate by using Vector instead of Map
- Kill the AI which timed out or crashed 10 times
- Output internal errors to stdout

## 2017-08-06 06:48

- Remove `state` from battle log
- Stop output to stdout (the application log still be available in `app.log`)
- Output error log of the AI when the AI was crashed