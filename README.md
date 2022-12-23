# ELEGANT Acceleration Service

A web service that provides code that can run on hardware accelerators (e.g. GPUs).

## Description

This project aims to build a RESTful API in Java that can submit requests for compilation of heterogeneous code that can
run on hardware accelerators.

The project uses Jakarta to implement a portable API for the development, exposure, and accessing of the ELEGANT
Acceleration Webservice. All dependencies are available in the [pom.xml](pom.xml) file.

## Installation

### 1. Clone the project:

```bash 
git clone https://github.com/elegant-h2020/Elegant-Acceleration-Service.git
```

### 2. Download JDK8 (This JDK is used only to build the service.)

```bash
wget --no-check-certificate -O /tmp/openjdk-8u222b10.tar.gz https://github.com/AdoptOpenJDK/openjdk8-upstream-binaries/releases/download/jdk8u222-b10/OpenJDK8U-jdk_x64_linux_8u222b10.tar.gz
tar xzvf /tmp/openjdk-8u222b10.tar.gz --directory /usr/lib/jvm/
mv /usr/lib/jvm/openjdk-8u222-b10 /usr/lib/jvm/java-8-openjdk-amd64
```

### 3. Install dependencies

- Install TornadoVM. We have tested it with GraalVM JDK 11 and OpenCL
```bash
git clone https://github.com/beehive-lab/TornadoVM.git
cd TornadoVM
./scripts/tornadoVMInstaller.sh --graal-jdk-11 --opencl
source source.sh
cd ..
```

- Download GlassFish 6.0.0:

```bash
wget 'https://www.eclipse.org/downloads/download.php?file=/ee4j/glassfish/glassfish-6.0.0.zip' -O glassfish-6.0.0.zip
unzip glassfish-6.0.0.zip
cd glassfish6
echo "AS_JAVA=/usr/lib/jvm/openjdk-8u222-b10" >> ./glassfish/config/asenv.conf
```

### 4. Build the service (using JDK8)

```bash
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
mvn clean install
```


### 5. Set up the environment and deploy the local server

a) Set environmental variables. To deploy the service, JDK 11 is used.
```bash
export SERVICE_HOME=<path-to-repository>/Elegant-Acceleration-Service
export JAVA_HOME=<path-to-jdk11>
export GLASSFISH_HOME=<path-to-glassfish6>/bin #export GLASSFISH_HOME=~/glassfish6/glassfish/bin
export TORNADOVM_ROOT=<path-to-TornadoVM-repository>
```

b) Start GlassFish local server:

```bash
$GLASSFISH_HOME/asadmin start-domain domain1
```

c) Redeploy the local server to sync with latest service (if the service is modified):
```bash
$GLASSFISH_HOME/asadmin deploy --force=true $SERVICE_HOME/target/ElegantAccelerationService-1.0-SNAPSHOT.war
```

d) Stop GlassFish local server:

```bash
$GLASSFISH_HOME/asadmin stop-domain domain1
```

### 6. Run an example of POST/GET requests from the terminal and from the path of the repository since the examples use relative paths:

```bash
curl -X POST -H "Content-Type: multipart/form-data" -F "codeFile=@examples/inputFiles/vectorAdd.java" -F "jsonFile=@examples/inputFiles/deviceInfoJava.json" http://localhost:8080/ElegantAccelerationService-1.0-SNAPSHOT/api/acceleration/submit
```

The response contains a message regarding the outcome, followed by an identifier:
```bash
New code acceleration request has been registered (#1)
```

The `id` identifier can be used to: a) update a request entry, b) retrieve the compiled kernel via a GET request, c) query the state of a
request, or d) delete a request, as follows:

#### A. To update a request (for example instead of a Java vectorAdd method to compile a C++ vectorAdd function):

```bash
curl -X PUT -H "Content-Type: multipart/form-data" -F "codeFile=@examples/inputFiles/vectorAdd2.java" -F "jsonFile=@examples/inputFiles/deviceInfoJava2.json" http://localhost:8080/ElegantAccelerationService-1.0-SNAPSHOT/api/acceleration/1/resubmit
```

#### B. To retrieve the accelerated code:

```bash
curl http://localhost:8080/ElegantAccelerationService-1.0-SNAPSHOT/api/acceleration/1/retrieve
```

This request returns the accelerated code for request with `id` 1:

```bash
#pragma OPENCL EXTENSION cl_khr_fp64 : enable  
#pragma OPENCL EXTENSION cl_khr_int64_base_atomics : enable  
__kernel void vectorAdd2(__global uchar *a, __global uchar *b, __global uchar *c)
{
  ulong ul_0, ul_1, ul_2, ul_7, ul_9, ul_11; 
  long l_6, l_5; 
  int i_3, i_4, i_13, i_14, i_12, i_10, i_8; 

  // BLOCK 0
  ul_0  =  (ulong) a;
  ul_1  =  (ulong) b;
  ul_2  =  (ulong) c;
  i_3  =  get_global_id(0);
  // BLOCK 1 MERGES [0 2 ]
  i_4  =  i_3;
  for(;i_4 < 1024;)
  {
    // BLOCK 2
    l_5  =  (long) i_4;
    l_6  =  l_5 << 2;
    ul_7  =  ul_0 + l_6;
    i_8  =  *((__global int *) ul_7);
    ul_9  =  ul_1 + l_6;
    i_10  =  *((__global int *) ul_9);
    ul_11  =  ul_2 + l_6;
    i_12  =  i_8 + i_10;
    *((__global int *) ul_11)  =  i_12;
    i_13  =  get_global_size(0);
    i_14  =  i_13 + i_4;
    i_4  =  i_14;
  }  // B2
  // BLOCK 3
  return;
}  //  kernel
```

#### C. To retrieve the state of the submitted request:

```bash
curl http://localhost:8080/ElegantAccelerationService-1.0-SNAPSHOT/api/acceleration/1/state
```

This request returns the state of the request with `id` 1:

```bash
"COMPLETED"
```

#### D. Run an example of DELETE request from the terminal:

```bash
curl -X DELETE http://localhost:8080/ElegantAccelerationService-1.0-SNAPSHOT/api/acceleration/1/
```

## Licenses

[![License](https://img.shields.io/badge/License-Apache%202.0-red.svg)]([https://github.com/beehive-lab/TornadoVM/blob/master/LICENSE_APACHE2](https://github.com/stratika/elegant-acceleration-service/blob/main/LICENSE.txt))
