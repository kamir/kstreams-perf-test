/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.confluent.type2;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.type1.ReverseString;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.cli.*;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KeyValueMapper;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * In this example, we implement a simple ByteReverse program using the high-level Streams DSL
 *
 * that reads from a source topic "streams-plaintext-input", where the values of messages represent lines of text;
 * the code split each text line in string into words and then write back into a sink topic "streams-linesplit-output" where
 * each record represents a single word.
 */
public class SimpleJoinAndRepartition {

    public static void main(String[] args) throws Exception {

        Options options = new Options();
        options.addOption(Option.builder("it1")
                .longOpt("inputTopic1")
                .hasArg(true)
                .desc("input topic 1 ... the topic from which data is loaded [REQUIRED]")
                .required(true)
                .build());

        options.addOption(Option.builder("it2")
                .longOpt("inputTopic2")
                .hasArg(true)
                .desc("input topic 2 ... the topic from which data is loaded [REQUIRED]")
                .required(true)
                .build());

        options.addOption(Option.builder("ot")
                .longOpt("outputTopic")
                .hasArg(true)
                .desc("output topic ... the topic to which result data is written [REQUIRED]")
                .required(true)
                .build());

        options.addOption(Option.builder("cg")
                .longOpt("consumer-group")
                .hasArg(true)
                .desc("the Kafka consumer group id ... [REQUIRED]")
                .required(true)
                .build());

        options.addOption(Option.builder("bss")
                .longOpt("bootstrap.servers")
                .hasArg(true)
                .desc("the Kafka bootstrap.servers ... [REQUIRED]")
                .required(true)
                .build());


        CommandLineParser parser = new DefaultParser();

        CommandLine cmd = null;

        String it = null;
        String ot = null;
        String client_id = "kstreams-perf-test-cg1";
        String bss = "localhost:9092";


        try {

            cmd = parser.parse(options, args);

            if (cmd.hasOption("it1")) {
                it = cmd.getOptionValue("it1");
                System.out.println("--inputTopic 1 option = " + it);
            }
            if (cmd.hasOption("it2")) {
                it = cmd.getOptionValue("it2");
                System.out.println("--inputTopic 2 option = " + it);
            }
            if (cmd.hasOption("ot")) {
                ot = cmd.getOptionValue("ot");
                System.out.println("--outputTopic option = " + ot);
            }
            if (cmd.hasOption("bss")) {
                bss = cmd.getOptionValue("bss");
                System.out.println("--bootstrap.servers option = " + bss);
            }
            if (cmd.hasOption("cg")) {
                client_id = cmd.getOptionValue("cg");
                System.out.println("--client_id option = " + client_id);
            }

        }
        catch (ParseException pe) {
            System.out.println("Error parsing command-line arguments!");
            System.out.println("Please, follow the instructions below:");
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "Log messages to sequence diagrams converter", options );
            System.exit(1);
        }



        Properties props = new Properties();

        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-join-type2");

        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bss);
        props.put(StreamsConfig.CLIENT_ID_CONFIG, client_id);

        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StreamsBuilder builder = new StreamsBuilder();





        final Topology topology = builder.build();
        final KafkaStreams streams = new KafkaStreams(topology, props);
        final CountDownLatch latch = new CountDownLatch(1);

        // attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                System.out.println(">>> SHUTDOWN Hook ... ");
                streams.close();
                latch.countDown();
                System.out.println(">>> Done!");
            }
        });

        try {
            streams.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }

}


