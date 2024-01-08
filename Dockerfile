FROM ubuntu:22.04

ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    build-essential tree ocl-icd-opencl-dev opencl-headers clinfo g++ gcc make git curl wget maven unzip rsync python3 python3-pip \
    && apt-get clean && python3 -m pip install wget

# Java 8 u222
RUN wget --no-check-certificate -O /tmp/openjdk-8u222b10.tar.gz https://github.com/AdoptOpenJDK/openjdk8-upstream-binaries/releases/download/jdk8u222-b10/OpenJDK8U-jdk_x64_linux_8u222b10.tar.gz
RUN mkdir -p /usr/lib/jvm/ \
    && tar xzvf /tmp/openjdk-8u222b10.tar.gz --directory /usr/lib/jvm/

# Glassfish 6
RUN wget --no-check-certificate 'https://www.eclipse.org/downloads/download.php?file=/ee4j/glassfish/glassfish-6.0.0.zip' -O glassfish-6.0.0.zip \
    && unzip glassfish-6.0.0.zip
RUN echo "AS_JAVA=/usr/lib/jvm/openjdk-8u222-b10" >> glassfish6/glassfish/config/asenv.conf

# TornadoVM
RUN git clone https://github.com/elegant-h2020/TornadoVM.git /root/TornadoVM && cd /root/TornadoVM && git checkout feat/service
WORKDIR /root/TornadoVM
RUN ./scripts/tornadovm-installer --jdk graalvm-jdk-11 --backend opencl
RUN /bin/sh -c . setvars.sh

# Clone the Acceleration Service
WORKDIR /root
RUN git clone https://github.com/elegant-h2020/Elegant-Acceleration-Service.git /root/Elegant-Acceleration-Service && cd /root/Elegant-Acceleration-Service && git checkout feat/demo
#COPY . /root/Elegant-Acceleration-Service # Used to debug the local repository

# Define envirornment variables
ENV SERVICE_HOME=/root/Elegant-Acceleration-Service
ENV JAVA_HOME=/root/TornadoVM/etc/dependencies/TornadoVM-graalvm-jdk-11/graalvm-ce-java11-22.3.2
ENV GLASSFISH_HOME=/glassfish6/glassfish/bin
ENV TORNADOVM_ROOT=/root/TornadoVM

# working directory
WORKDIR $SERVICE_HOME

# a directory to store the files produced by the service
VOLUME /root/Elegant-Acceleration-Service/service_db

# Build the service
RUN /root/TornadoVM/etc/dependencies/apache-maven-3.9.3/bin/mvn clean install

EXPOSE 8080

ENTRYPOINT ["./docker/start-glassfish-server.sh"]
