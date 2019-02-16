# SparkJava Instrumentation Agent
> Quickly add data-tracking metrics to [SparkJava](http://sparkjava.com/) responses without modifying any code

SJIA is a Java Agent that allows for instantaneous metric monitoring without adding any code in your project.  Responses are modified with headers that indicate metrics such as round trip time.  SJIA also includes an optional built-in monitoring application that allows you to see live updates of the performance of your [SparkJava](http://sparkjava.com/) routes.

## Usage
Download a release package of SJIA, unzip and execute your application with the javaagent jvm flag set to point to the SJIA jar executable.

#### Response Headers Only
```bash
java -javagent:sjia.jar -jar application.jar
```

#### Open Monitoring Application
```bash
java -javagent:sjia.jar=monitor -jar application.jar
```