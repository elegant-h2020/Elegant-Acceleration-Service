# ELEGANT Acceleration Service

A web service that provides code that can run on hardware accelerators (e.g. GPUs).

## Description

This project aims to build a RESTful API in Java that can submit requests for compilation of heterogeneous code that can
run on hardware accelerators.

The project uses Jakarta to implement a portable API for the development, exposure, and accessing of the ELEGANT
Acceleration Webservice. All dependencies are available in the [pom.xml](pom.xml) file.

## 1. Installation/Deployment

The service can be installed and deployed via building and launching a docker image that handles all the dependencies and builds in an automated manner (Step A).
Alternatively, it can be built locally as described in Step B.

### A. Automated deployment via the Docker containerized service

A script is provided that creates a volume that is used from the docker image, it builds the image (~5 minutes) and it launches the generated image.
```bash
Please run:
  ./docker/launchWithDocker.sh --buildAndLaunch  #to build and launch the image, or
  ./docker/launchWithDocker.sh --launch          #to launch a built image, or
  ./docker/launchWithDocker.sh --deleteVolume    #to delete the generated volume
```

### B. Run an example of POST/GET requests from the terminal and from the path of the repository since the examples use relative paths:

#### i. Clone the project:

```bash 
git clone https://github.com/elegant-h2020/Elegant-Acceleration-Service.git
```

#### ii. Download JDK8 (This JDK is used only to build the service.)

```bash
wget --no-check-certificate -O /tmp/openjdk-8u222b10.tar.gz https://github.com/AdoptOpenJDK/openjdk8-upstream-binaries/releases/download/jdk8u222-b10/OpenJDK8U-jdk_x64_linux_8u222b10.tar.gz
tar xzvf /tmp/openjdk-8u222b10.tar.gz --directory /usr/lib/jvm/
mv /usr/lib/jvm/openjdk-8u222-b10 /usr/lib/jvm/java-8-openjdk-amd64
```

#### iii. Install dependencies

- Install TornadoVM. We have tested it with GraalVM JDK 11 and OpenCL
```bash
git clone https://github.com/elegant-h2020/TornadoVM.git
cd TornadoVM
git checkout feat/service
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

#### iv. Build the service (using JDK8)

```bash
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
mvn clean install
```


#### v. Set up the environment and deploy the local server

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

## 2. Run an example of POST/GET requests from the terminal and from the path of the repository since the examples use relative paths:

### A. The service can be used to compile vanilla Java methods (e.g. vectorAdd) that contain for loops which can be compiled for parallel GPU implementations.
```bash
curl -X POST -H "Content-Type: multipart/form-data" -F "codeFile=@examples/inputFiles/vectorAdd.java" -F "jsonFile=@examples/inputFiles/deviceInfoJava.json" http://localhost:8080/ElegantAccelerationService-1.0-SNAPSHOT/api/acceleration/submit
```

The response contains a message regarding the outcome, followed by an identifier:
```bash
The request has been completed.
New code acceleration request has been registered (#1)
```

The `id` identifier can be used to: a) update a request entry, b) retrieve the compiled kernel via a GET request, c) query the state of a
request, or d) delete a request, as follows:

### B. Additionally, the service can be used to compile NebulaStream Java UDF map operators which are scaled up for parallel GPU implementations.
To post a request for a NebulaStream [Java UDF map operator](https://github.com/nebulastream/nebulastream-tutorial/blob/main/java-client-example/src/main/java/stream/nebula/example/JavaUdfExample.java)):

```bash
curl -X POST -H "Content-Type: multipart/form-data" -F "codeFile=@examples/inputFiles/customMapFloat.java" -F "jsonFile=@examples/inputFiles/deviceInfoCustomMap.json" http://localhost:8080/ElegantAccelerationService-1.0-SNAPSHOT/api/acceleration/submit
```

The response contains a message regarding the outcome, followed by an identifier:
```bash
The request has been completed.
New code acceleration request has been registered (#2)
```

### C. To retrieve the accelerated code, use the identifier:

```bash
curl http://localhost:8080/ElegantAccelerationService-1.0-SNAPSHOT/api/acceleration/2/retrieve
```

This request returns the accelerated code for request with `id` 2:

```bash
#pragma OPENCL EXTENSION cl_khr_fp64 : enable  
#pragma OPENCL EXTENSION cl_khr_int64_base_atomics : enable  
__kernel void map(__global uchar *inputmap, __global uchar *output)
{
  int i_5, i_4, i_3, i_2, i_15; 
  ulong ul_8, ul_10, ul_0, ul_1; 
  float2 v2f_12, v2f_9; 
  float f_14, f_13, f_11; 
  long l_6, l_7; 

  // BLOCK 0
  ul_0  =  (ulong) inputmap;
  ul_1  =  (ulong) output;
  i_2  =  get_global_size(0);
  i_3  =  get_global_id(0);
  // BLOCK 1 MERGES [0 2 ]
  i_4  =  i_3;
  for(;i_4 < 1024;)
  {
    // BLOCK 2
    i_5  =  i_4 << 1;
    l_6  =  (long) i_5;
    l_7  =  l_6 << 2;
    ul_8  =  ul_0 + l_7;
    v2f_9  =  vload2(0, (__global float *) ul_8);
    ul_10  =  ul_1 + l_7;
    f_11  =  atan2(v2f_9.s0, v2f_9.s1);
    f_13  =  f_11;
    f_14  =  f_11;
    v2f_12  =  (float2)(f_13, f_14);
    vstore2(v2f_12, 0, (__global float *) ul_10);
    i_15  =  i_2 + i_4;
    i_4  =  i_15;
  }  // B2
  // BLOCK 3
  return;
}  //  kernel
```

### D. To retrieve the state of the submitted request:

```bash
curl http://localhost:8080/ElegantAccelerationService-1.0-SNAPSHOT/api/acceleration/2/state
```

This request returns the state of the request with `id` 2:

```bash
"COMPLETED"
```

### E. To delete a submitted request:

```bash
curl -X DELETE http://localhost:8080/ElegantAccelerationService-1.0-SNAPSHOT/api/acceleration/2/
```

## Licenses

[![License](https://img.shields.io/badge/License-Apache%202.0-red.svg)]([https://github.com/beehive-lab/TornadoVM/blob/master/LICENSE_APACHE2](https://github.com/stratika/elegant-acceleration-service/blob/main/LICENSE.txt))
