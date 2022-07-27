# ELEGANT Acceleration Service
A web service that provides code that can run on hardware accelerators (e.g. GPUs).

## Description

This project aims to build a RESTful API in Java that can submit requests for compilation of heterogeneous code that can run on hardware accelerators.

The project uses JAX-RS to implement a portable API for the development, exposure, and accessing of the ELEGANT Acceleration Webservice.

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
$ wget https://www.eclipse.org/downloads/download.php?file=/glassfish/glassfish-5.1.0.zip
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
curl -X POST http://localhost:8080/ElegantAccelerationService-1.0-SNAPSHOT/api/acceleration/requests \
   -H 'Content-Type: application/json' \
   -d '{
      "fileInfo": {
        "functionName": "foo0",
        "programmingLanguage": "Java"
      },
      "deviceInfo": {
        "deviceName" : "Nvidia GPU 1",
        "doubleFPSupport" : true,
        "maxWorkItems": {
          "dim1" : 2048,
          "dim2" : 1,
          "dim3" : 1
        },
        "deviceAddressBits" : 64,
        "deviceType" : "CL_DEVICE_TYPE_GPU",
        "deviceExtensions" : "cl_khr_int64_base_atomics",
        "availableProcessors" : 2048
      }
    }'
```

The response contains the input json file of the POST request, followed by an identifier:
```bash
{"deviceInfo":{"availableProcessors":2048,"deviceAddressBits":64,"deviceExtensions":"cl_khr_int64_base_atomics","deviceName":"Nvidia GPU 1","deviceType":"CL_DEVICE_TYPE_GPU","doubleFPSupport":true,"maxWorkItems":{"dim1":2048,"dim2":1,"dim3":1}},"fileInfo":{"functionName":"foo0","programmingLanguage":"Java"},"id":1,"state":"SUBMITTED"}
```

This identifier can be used to retrieve a GET request or the state of a request, as follows:
```bash
curl http://localhost:8080/ElegantAccelerationService-1.0-SNAPSHOT/api/acceleration/requests/1
```

```bash
curl http://localhost:8080/ElegantAccelerationService-1.0-SNAPSHOT/api/acceleration/requests/1/state
```



## Licenses

[![License](https://img.shields.io/badge/License-Apache%202.0-red.svg)]([https://github.com/beehive-lab/TornadoVM/blob/master/LICENSE_APACHE2](https://github.com/stratika/elegant-acceleration-service/blob/main/LICENSE.txt))
