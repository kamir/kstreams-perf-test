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
package confluent.io;

import org.apache.commons.cli.*;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.ValueMapper;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * In this example, we implement a simple ByteReverse program using the high-level Streams DSL
 *
 * that reads from a source topic "streams-plaintext-input", where the values of messages represent lines of text;
 * the code split each text line in string into words and then write back into a sink topic "streams-linesplit-output" where
 * each record represents a single word.
 */
public class ByteLevelReverse {

    public static void main(String[] args) throws Exception {

        Options options = new Options();
        options.addOption(Option.builder("it")
                .longOpt("inputTopic")
                .hasArg(true)
                .desc("input topic ... the topic from which data is loaded [REQUIRED]")
                .required(true)
                .build());

        options.addOption(Option.builder("ot")
                .longOpt("outputTopic")
                .hasArg(true)
                .desc("output topic ... the topic to which result data is written [REQUIRED]")
                .required(true)
                .build());
/*
        options.addOption(Option.builder("cg")
                .longOpt("consumer-group")
                .hasArg(true)
                .desc("the Kafka consumer group id ... [REQUIRED]")
                .required(true)
                .build());

        options.addOption(Option.builder("bss")
                .longOpt("bbotstrap.servers")
                .hasArg(true)
                .desc("the Kafka bootstrap.servers ... [REQUIRED]")
                .required(true)
                .build());
*/

        CommandLineParser parser = new DefaultParser();

        CommandLine cmd = null;

        String it = null;
        String ot = null;
        String client_id = "kstreams-perf-test-cg1";
        String bss = "localhost:9092";


        try {

            cmd = parser.parse(options, args);

            if (cmd.hasOption("it")) {
                it = cmd.getOptionValue("it");
                System.out.println("--inputTopic option = " + it);
            }
            if (cmd.hasOption("ot")) {
                ot = cmd.getOptionValue("ot");
                System.out.println("--outputTopic option = " + ot);
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

        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-byte-reverse");

        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bss);
        props.put(StreamsConfig.CLIENT_ID_CONFIG, client_id);

        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());


        final StreamsBuilder builder = new StreamsBuilder();

        builder.<String, String>stream( it )
            .map(new KeyValueMapper<String, String, KeyValue<?, ?>>() {
                @Override
                public KeyValue<?, ?> apply(String key, String value) {
                    return new KeyValue( key, ReverseString.process(value) );
                }
            })
            .to( ot );

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


