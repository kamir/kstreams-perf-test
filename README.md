# kstreams-perf-test
Performance testing for KStreamsApplications.

## Quick-Start - using Confluent Platform locally installed

### Prerequisites:
+ Confluent platform is installed
+ Docker and Docker compose are installed

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
mvn clean compile exec:java -Dexec.args="-it t1 -ot t1REV --bootstrap.servers localhost:9092 -cg byte-reverse-app-1"
```

### Clean-Up procedure

```
$CONFLUENT_HOME/bin/kafka-topics --delete --topic t1 --bootstrap-server=127.0.0.1:9092
$CONFLUENT_HOME/bin/kafka-topics --delete --topic t1REV --bootstrap-server=127.0.0.1:9092
```

## Prepare Test-Setup with a multi node cluster

The project `https://github.com/jeanlouisboudart/kafka-platform-prometheus` contains a ready to use confluent platform 
including Prometheus and Grafana for monitoring and metrics visualization.

Please follow the guide in this project to prepare your setup, in case you want to practice the procedure with a dedicated Confluent platform. 

In order to integrate the KStreams performance test app, we have to modify the docker-compose file.

### Add a demo workload

First, build the project locally and create the container.

```
mvn clean compile assembly:single
docker build . -t kstreams-perf-test-app
```

Now, append the following snippet to the existing file `kafka-platform-prometheus/docker-compose.yml`.

```
  #
  #  Example workload for KStreams
  #
  kstreams:
    image: kstreams-perf-test-app
    environment:
      JAVA_OPTS: -javaagent:/usr/share/jmx_exporter/jmx_prometheus_javaagent-0.12.0.jar=1234:/usr/share/jmx_exporter/kafka-producer.yml -Xmx256M -Xms256M
    volumes:
      - jmx-exporter-vol:/usr/share/jmx_exporter/
    depends_on:
      - jmx-exporter
      - kafka-1
      - kafka-2
      - kafka-3
```

Finally, stop the KStreams application which 
### Run Benchmark in the Test-Cluster using Docker-Compose

#### Create a topic for test data
Create the `demo-perf-topic` and `demo-perf-topic-REVERSE` with 4 partitions and 3 replicas.
``` 
docker-compose exec kafka-1 bash -c 'KAFKA_OPTS="" kafka-topics --create --partitions 4 --replication-factor 3 --topic demo-perf-topic --zookeeper zookeeper-1:2181'
docker-compose exec kafka-1 bash -c 'KAFKA_OPTS="" kafka-topics --create --partitions 4 --replication-factor 3 --topic demo-perf-topic-REVERSE --zookeeper zookeeper-1:2181'
```

#### Produce random messages into topic _demo-perf-topic_
Open a new terminal window (in the same folder where the `docker-compose.yml` is located) and generate random messages to simulate producer load.
```
docker-compose exec kafka-1 bash -c 'KAFKA_OPTS="" kafka-producer-perf-test --throughput -1 --num-records 10000000 --topic demo-perf-topic --record-size 160 --producer-props acks=1 buffer.memory=67108864 batch.size=8196 bootstrap.servers=kafka-1:9092'
```

#### Process random messages using a KStreams-Application
Open a new terminal window and start the streaming application.
```
docker-compose exec kstreams bash -c 'KAFKA_OPTS="" java -jar kstreams-perf-test-1.0-SNAPSHOT-jar-with-dependencies.jar -it demo-perf-topic -ot demo-perf-topic-REVERSE --bootstrap.servers kafka-1:9092 -cg byte-reverse-app-1'
```

## Run a KStreams-Example Applications
All commands have to be executed in your `docker-compose project folder.

The next four commands can be executed in a sequence:
```
docker-compose exec kstreams-1 bash -c 'KAFKA_OPTS="" ls'
docker-compose exec kafka-1 bash -c 'KAFKA_OPTS="" kafka-topics --create --partitions 1 --replication-factor 1 --topic PageViews --zookeeper zookeeper-1:2181'
docker-compose exec kafka-1 bash -c 'KAFKA_OPTS="" kafka-topics --create --partitions 1 --replication-factor 1 --topic UserProfiles --zookeeper zookeeper-1:2181'
docker-compose exec kafka-1 bash -c 'KAFKA_OPTS="" kafka-topics --create --partitions 1 --replication-factor 1 --topic PageViewsByRegion --zookeeper zookeeper-1:2181'
```
Now, we have to work in three parallel terminal windows:
```
docker-compose exec kstreams-1 bash -c 'KAFKA_OPTS="" java -cp ./kafka-streams-examples-5.4.1-standalone.jar io.confluent.examples.streams.PageViewRegionLambdaExample kafka-1:9092 http://schema-registry:8081'
docker-compose exec kstreams-1 bash -c 'KAFKA_OPTS="" java -cp ./kafka-streams-examples-5.4.1-standalone.jar io.confluent.examples.streams.PageViewRegionExampleDriver kafka-1:9092 http://schema-registry:8081'
docker-compose exec kafka-1 bash -c 'KAFKA_OPTS="" kafka-console-consumer --topic PageViewsByRegion --from-beginning --bootstrap-server kafka-1:9092 --property print.key=true --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer'
```




 





