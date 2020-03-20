# kstreams-perf-test
Performance testing for KStreamsApplications

## Quick-Start

### Step by step approach:

+ start the `kafka-producer-perf` test to generate some test data into topic t1.

```
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_221.jdk/Contents/Home
export CONFLUENT_HOME=/Users/mkampf/bin/confluent-5.4.0
$CONFLUENT_HOME/bin/kafka-producer-perf-test --topic t1 --num-records 10 --record-size 1024 --throughput -1 --producer-props acks=1 bootstrap.servers=127.0.0.1:9092 buffer.memory=67108864 batch.size=8196
```
In case you are interested in metrics in deep details, please append the argument `--print-metrics`.

+  start the KStreams performance testing app in this project.

```
mvn clean compile exec:java
```

### Clean-Up procedure

```
$CONFLUENT_HOME/bin/kafka-topics --delete --topic t1 --bootstrap-server=127.0.0.1:9092
$CONFLUENT_HOME/bin/kafka-topics --delete --topic t1REVERSE --bootstrap-server=127.0.0.1:9092
```


