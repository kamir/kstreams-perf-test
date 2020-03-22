#
#
#
export WORKDIR=$(pwd)
echo $WORKDIR

#############################################################################
##
## BOOTSTRAP PHASE
##
#
# Clone the two projects this work depends on
#
##cd ..
##git clone https://github.com/semanpix/kafka-streams-examples
##git clone https://github.com/jeanlouisboudart/kafka-platform-prometheus

#############################################################################
#
# PATH to kafka-streams-examples project:
#
export KAFKA_STREAMS_EXAMPLES_PROJECT_PATH=./../kafka-streams-examples
#
# PATH to kafka-platform-prometheus project:
#
export KAFKA_PROMETHEUS_PROJECT_PATH=./../kafka-platform-prometheus

#############################################################################
#
# Build Kafka-Streams-Examples and grab a copy of the UBER JAR
#
cd $KAFKA_STREAMS_EXAMPLES_PROJECT_PATH
mvn clean compile package install assembly:single -DskipTests
cp target/.*jar $WORKDIR/lib

#############################################################################
#
cd $WORKDIR
ls lib


#############################################################################
#
# copy our modified docker-compose.yml file into the cluster-operations-directory
#
cp docker-compose.yml $KAFKA_PROMETHEUS_PROJECT_PATH


#############################################################################
#
# Create the kstreams-perf-test docker image
#
##
##  !!! WARNING !!! THE IMAGE MAY CONTAIN SENSITIVE INFORMATION
##
##  >>> CCloud credentials are stored in the ccloud.props file.
##  >>> We work on a version with encrypted config files.
##
mvn clean compile package assembly:single
docker build . -t kstreams-perf-test-app


#############################################################################
#
# Update / Start the environment
#
cd $KAFKA_PROMETHEUS_PROJECT_PATH
docker-compose up -d

echo
echo '#####################################'
echo '# Environment is READY for the next #'
echo '# round of experiments              #'
echo '#####################################'
echo
