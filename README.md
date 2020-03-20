# kstreams-perf-test
Performance testing for KStreamsApplications

## Quick-Start

### Step by step approach:

+ start the `kafka-producer-perf` test to generate some test data into topic t1.

First, you should prepare your environment so that you can use the Confluent platform tools.
If not done yet, please define `JAVA_HOME` and `CONFLUENT_HOME` environment variables.
```
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_221.jdk/Contents/Home
export CONFLUENT_HOME=/Users/mkampf/bin/confluent-5.4.0
```
The message producer can be executed with this command:
```
$CONFLUENT_HOME/bin/kafka-producer-perf-test --topic t1 --num-records 10 --record-size 1024 --throughput -1 --producer-props acks=1 bootstrap.servers=127.0.0.1:9092 buffer.memory=67108864 batch.size=8196
```
In case you are interested in metrics in deep details, please append the argument: `--print-metrics`.

+  start the KStreams performance testing app in this project, using the default settings, defined in the `pom.xml`file.

```
mvn clean compile exec:java
```

+ you can also specify your own arguments like this:

```
mvn clean compile exec:java -Dexec.args="-it tt2 -ot t2REV --bootstrap.servers localhost:9092 -cg byte-reverse-app-1"
```

### Clean-Up procedure

```
$CONFLUENT_HOME/bin/kafka-topics --delete --topic t1 --bootstrap-server=127.0.0.1:9092
$CONFLUENT_HOME/bin/kafka-topics --delete --topic t1REVERS --bootstrap-server=127.0.0.1:9092
```


