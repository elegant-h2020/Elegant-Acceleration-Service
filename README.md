# ELEGANT Acceleration Service

A web service that provides code that can run on hardware accelerators (e.g. GPUs).

## Description

This project aims to build a RESTful API in Java that can submit requests for compilation of heterogeneous code that can
run on hardware accelerators.

The project uses JAX-RS to implement a portable API for the development, exposure, and accessing of the ELEGANT
Acceleration Webservice.

## Installation

### 1. Clone the project:

```bash 
git clone https://github.com/elegant-h2020/Elegant-Acceleration-Service.git
```

### 2. Build the service:

```bash
mvn clean install
```

### 3. Deploy a GlassFish server

a) Download GlassFish 5.1.0:

```bash
$ wget 'https://www.eclipse.org/downloads/download.php?file=/glassfish/glassfish-5.1.0.zip&r=1' -O glassfish-5.1.0.zip
$ unzip glassfish-5.1.0.zip
$ cd glassfish5
$ echo "AS_JAVA=<path-to-JDK8>" >> ./glassfish/config/asenv.conf
```

b) Start GlassFish local server:

```bash
./bin/asadmin start-domain domain1
```

c) Stop GlassFish local server:

```bash
./bin/asadmin stop-domain domain1
```

### 4. Run an example of POST/GET requests from the terminal:

```bash
curl -F file=@./examples/inputFiles/vectorAdd.java -F file=@./examples/inputFiles/deviceInfo.json http://localhost:8080/ElegantAccelerationService-1.0-SNAPSHOT/api/acceleration/requests/submit
```

The response contains the input json file of the POST request, followed by an identifier:

```bash
{"deviceInfo":{"availableProcessors":2048,"deviceAddressBits":64,"deviceExtensions":"cl_khr_int64_base_atomics","deviceName":"Nvidia GPU","deviceType":"CL_DEVICE_TYPE_GPU","doubleFPSupport":true,"maxWorkItems":{"dim1":16,"dim2":1,"dim3":1}},"fileInfo":{"functionName":"vectorAdd","programmingLanguage":"Java"},"id":1,"state":"SUBMITTED"}
```

The `id` identifier can be used to: i) update a request entry, ii) retrieve a GET request, or iii) the state of a
request, as follows:

#### A. To update a request (for example instead of a Java vectorAdd method to compile a C++ vectorAdd function):

```bash
curl -F file=@./examples/inputFiles/vectorAdd.cpp -F file=@./examples/inputFiles/deviceInfo2.json http://localhost:8080/ElegantAccelerationService-1.0-SNAPSHOT/api/acceleration/requests/1/resubmit
```

#### B. To retrieve the accelerated code:

```bash
curl http://localhost:8080/ElegantAccelerationService-1.0-SNAPSHOT/api/acceleration/requests/1/retrieve
```

This request returns the accelerated code for request with `id` 1:

```bash
#pragma OPENCL EXTENSION cl_khr_fp64 : enable
__kernel void add(__global long *_kernel_context, __constant uchar *_constant_region, __local uchar *_local_region, __global int *_atomics, __global uchar *a, __global uchar *b, __global uchar *c)
{
  ulong ul_8, ul_10, ul_1, ul_0, ul_2, ul_12;
  long l_5, l_7, l_6;
  int i_11, i_9, i_15, i_14, i_13, i_3, i_4;

  // BLOCK 0
  ul_0  =  a;
  ul_1  =  b;
  ul_2  =  c;
  i_3  =  get_global_id(0);
  // BLOCK 1 MERGES [0 2 ]
  i_4  =  i_3;
  for(;i_4 < 8;)  {
    // BLOCK 2
    l_5  =  (long) i_4;
    l_6  =  l_5 << 2;
    l_7  =  l_6 + 24L;
    ul_8  =  ul_0 + l_7;
    i_9  =  *((__global int *) ul_8);
    ul_10  =  ul_1 + l_7;
    i_11  =  *((__global int *) ul_10);
    ul_12  =  ul_2 + l_7;
    i_13  =  i_9 + i_11;
    *((__global int *) ul_12)  =  i_13;
    i_14  =  get_global_size(0);
    i_15  =  i_14 + i_4;
    i_4  =  i_15;
  }
  // BLOCK 3
  return;
}
```

#### C. To retrieve the state of the submitted request:

```bash
curl http://localhost:8080/ElegantAccelerationService-1.0-SNAPSHOT/api/acceleration/requests/1/state
```

This request returns the state of the request with `id` 1:

```bash
"SUBMITTED"
```

### 5. Run an example of DELETE request from the terminal:

```bash
curl -X DELETE http://localhost:8080/ElegantAccelerationService-1.0-SNAPSHOT/api/acceleration/requests/1/
```

## Licenses

[![License](https://img.shields.io/badge/License-Apache%202.0-red.svg)]([https://github.com/beehive-lab/TornadoVM/blob/master/LICENSE_APACHE2](https://github.com/stratika/elegant-acceleration-service/blob/main/LICENSE.txt))
