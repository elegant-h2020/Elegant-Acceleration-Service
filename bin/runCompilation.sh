#!/usr/bin/env bash

if [ "$#" -ne 7 ]
then
        echo "$0: invalid number of arguments."
        exit 1
fi

tornado_command=$1
class_path=$2
class_file=$3
device_json_file=$4
parameter_size_json_file=$5
generated_kernel_file=$6
method_name=$7

## Compile input class file for virtual device
$tornado_command -cp :$class_path --jvm="-Dtornado.input.classfile.dir=$class_file -Dtornado.device.desc=$device_json_file -Dtornado.parameter.size.dir=$parameter_size_json_file -Dtornado.virtual.device=True -Dtornado.cim.mode=True -Dtornado.print.kernel=True -Dtornado.print.kernel.dir=$generated_kernel_file" uk.ac.manchester.tornado.drivers.opencl.service.frontend.TestFrontEnd --params "$method_name"

exit_code=$?

if [ $exit_code -e 0 ]
then
        exit 0
else
        exit $exit_code
fi

