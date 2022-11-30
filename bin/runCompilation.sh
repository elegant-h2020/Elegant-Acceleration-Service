#!/usr/bin/env bash
#/home/thanos/repositories/tornadoVM2/bin/sdk/bin/tornado -cp :/home/thanos/repositories/Elegant-Acceleration-Service/examples/boilerplate/1 --jvm="-Dtornado.input.classfile.dir=/home/thanos/repositories/Elegant-Acceleration-Service/examples/boilerplate/1/TestVectorAdd.class -Dtornado.device.desc=/home/thanos/Desktop/test-virtual/test.json -Dtornado.virtual.device=True -Dtornado.cim.mode=True -Dtornado.print.kernel=True -Dtornado.print.kernel.dir=/home/thanos/repositories/Elegant-Acceleration-Service/examples/generated/1/vectorAdd.cl" uk.ac.manchester.tornado.drivers.opencl.service.frontend.TestFrontEnd --params vectorAdd

if [ "$#" -ne 5 ] 
then
        echo "$0: invalid number of arguments."
        exit 1
fi

class_path=$1
class_file=$2
device_json_file=$3
generated_kernel_file=$4
method_name=$5

## Compile input class file for virtual device 
/home/thanos/repositories/tornadoVM2/bin/sdk/bin/tornado -cp :$class_path --jvm="-Dtornado.input.classfile.dir=$class_file -Dtornado.device.desc=$device_json_file -Dtornado.virtual.device=True -Dtornado.cim.mode=True -Dtornado.print.kernel=True -Dtornado.print.kernel.dir=$generated_kernel_file" uk.ac.manchester.tornado.drivers.opencl.service.frontend.TestFrontEnd --params "$method_name"

