FROM flink:1.14.5-scala_2.12-java8

RUN set -x && apt-get update && apt install -y iproute2
RUN set -x && apt-get update && apt install -y iputils-ping
RUN set -x && apt-get update && apt install -y netcat
RUN set -x && apt-get update && apt install -y net-tools
RUN set -x && apt-get update && apt install -y telnet
EXPOSE 9000/tcp
EXPOSE 9000/udp
EXPOSE 9001-9200
EXPOSE 9001-9200/udp
EXPOSE 7574/tcp
EXPOSE 7545/udp
EXPOSE 7/udp
EXPOSE 7/tcp