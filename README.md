# SparkJava Instrumentation Agent
> Quickly add data-tracking metrics to [SparkJava](http://sparkjava.com/) responses without modifying any code

[![Build Status](https://travis-ci.com/argonotix/sjia.svg?token=BmwMrNHAsN7s3fbyKJQG&branch=master)](https://travis-ci.com/argonotix/sjia)

SJIA is a Java Agent that allows for instantaneous metric monitoring without adding any code in your project.  Responses are modified with headers that indicate metrics such as round trip time.  SJIA also includes an optional built-in monitoring application that allows you to see live updates of the performance of your [SparkJava](http://sparkjava.com/) routes.

## Usage
Download a release package of SJIA, unzip/untar and execute your application with the javaagent jvm flag set to point to the SJIA jar executable.

#### Response Headers Only
```bash
java -javagent:sjia.jar -jar application.jar
```

#### Open Monitoring Application
```bash
java -javagent:sjia.jar=monitor -jar application.jar
```
The monitoring application will open a web server on port 45000.  The latest statistics can be retrieved by issuing a GET command to http://localhost:45000/stats.   Doing so will retrieve a JSON object structured like: 
```javascript
{
 "*": {
  "max": 51.0,
  "min": 50.0,
  "avg": 50.2
 },
  "/metrix": {
  "max": 51.0,
  "min": 50.0,
  "avg": 50.2
 }
}
```
Visiting the address http://localhost:45000 in a web browser will open the the metric monitoring utility.

#### Response Headers
Metrics in the form of HTTP Response Headers are added to every route.

| Header            | Description                |
| ----------------- | -------------------------- |
| X-metric-time-min | Minimum Response Time      |
| X-metric-time-max | Maximum Response Time      |
| X-metric-time-avg | Mean Response Time         |
| X-metric-time     | Response Time              |
| X-metric-body-min | Minimum Response Body Size |
| X-metric-body-max | Maximum Response Body Size |
| X-metric-body-mvg | Mean Response Body Size    |
| X-metric-body     | Response Body Size (bytes) |

#### Example
Download the release file, unzip the sjia.jar and example.jar.  Execute the following command:
```bash
java -javagent:sjia.jar=monitor -jar example.jar
```
The example webapp is accessible via http://localhost:4567, and the SJIA monitoring application is
accessible via http://localhost:45000.


#### Building
```
git clone https://github.com/argonotix/sjia.git
cd sjia
chmod +x ./gradlew
./gradlew build shadowJar
cp ./agent/build/libs/agent-0.1-all.jar ./sjia.jar
```

## License

Distributed under the MIT license.