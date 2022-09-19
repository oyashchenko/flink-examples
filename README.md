# flink-examples
You can run flink examples via Docker or from locally machine.

## Flink Sink configuration
Flink example provides ability to use Ignite or Coherence as Sink for output results.
### Coherence setup as cache sink for flink
1) Download coherence jar 14.1.1 from [coherence download](https://jar-download.com/?search_box=coherence)
2) Install to locally repository
mvn install:install-file -Dfile=coherence-14.1.1-0-3.jar -DgroupId=com.oracle.coherence -DartifactId=coherence -Dversion=14.1.1-0-3 -Dpackaging=jar
3) Run coherence locally :
  - replace 192.168.0.12 ip address to your current ip in all .xml from flink-cache/src/main/resources
  - rebuild project
  - if you are on Windows machine make bat files with below content, where %CACHE_CONFIG% - path to target directory of flink-examples cache module 
    > start java -cp %CACHE_CONFIG%flink-cache-1.0-SNAPSHOT.jar;%COHERENCE_HOME%coherence-14.1.1-0-3.jar -Dtangosol.coherence.override=tangosole-coherense-server-override.xml -Dcoherence.log.level=9 -Djava.net.preferIPv4Stack=true com.tangosol.net.DefaultCacheServer
  - make scripts for running coherence proxy server
    > java -cp %CACHE_CONFIG%flink-cache-1.0-SNAPSHOT.jar;%COHERENCE_HOME%coherence-14.1.1-0-3.jar -Djava.net.preferIPv4Stack=true -Dtangosol.coherence.override=tangosole-coherense-server-override.xml -Dtangosol.coherence.cacheconfig=cache-config-proxy.xml com.tangosol.net.DefaultCacheServer
### Ignite setup as cache sink for flink
1) Run in ignite module com.oyshchenko.App 

## Kafka setup for Flink metrics module (via cygwin in case of Windows machine)
1) download tar from [official kafka site](https://downloads.apache.org/kafka/3.2.1/kafka_2.13-3.2.1.tgz)
2) extract tar
3) in config server.properties add new listener RMOFF_DOCKER 
 > listeners=PLAINTEXT://:9092,RMOFF_DOCKER://:19092
 > listener.security.protocol.map=PLAINTEXT:PLAINTEXT,RMOFF_DOCKER:PLAINTEXT
4) Start zookeeper from bin folder
  > ./zookeeper-server-start.sh -daemon ../config/zookeeper.properties
5) Start kafka
  > ./kafka-server-start.sh -daemon ../config/server.properties
6) Create metric topic
  > ./kafka-topics.sh --create --topic backPressureEventTopic --bootstrap-server localhost:9092

## Flink run
### Docker run
#### Precondition
Should be already configured docker environment on machine
(Docker desktop for Windows as example)

#### Application Run
1) Start docker compose  (open console from flink-flow module)
  > docker-compose up -f ./flink-flow/docker-compose.yaml --build 
2) Open [flink-console](http://localhost:8081/)
3) Go to [Submit New Job](http://localhost:8081/#/submit)
4) Click on **Add New** button and choose flink-flow/target/flink-flow-1.0-SNAPSHOT-jar-with-dependencies.jar
5) Once uploaded click on **Submit** button
## Locally run
com.oyashchenko.flink.workflow.PnlCalculation local
Open [flink-console](http://localhost:8081/)