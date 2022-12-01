#!/usr/bin/env bash

if [ "$#" -ne 6 ] 
then
        echo "$0: invalid number of arguments."
        exit 1
fi

class_path=$1
class_file=$2
device_json_file=$3
parameter_size_json_file=$4
generated_kernel_file=$5
method_name=$6

## Compile input class file for virtual device 
/home/thanos/repositories/tornadoVM2/bin/sdk/bin/tornado -cp :$class_path --jvm="-Dtornado.input.classfile.dir=$class_file -Dtornado.device.desc=$device_json_file -Dtornado.parameter.size.dir=$parameter_size_json_file -Dtornado.virtual.device=True -Dtornado.cim.mode=True -Dtornado.print.kernel=True -Dtornado.print.kernel.dir=$generated_kernel_file" uk.ac.manchester.tornado.drivers.opencl.service.frontend.TestFrontEnd --params "$method_name"

