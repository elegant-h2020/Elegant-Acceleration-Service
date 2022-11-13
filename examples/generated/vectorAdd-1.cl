#pragma OPENCL EXTENSION cl_khr_fp64 : enable  
#pragma OPENCL EXTENSION cl_khr_int64_base_atomics : enable  
__kernel void vectorAdd(__global uchar *a, __global uchar *b, __global uchar *c)
{
  ulong ul_0, ul_11, ul_9, ul_7, ul_1, ul_2; 
  int i_8, i_4, i_3, i_14, i_12, i_13, i_10; 
  long l_5, l_6; 

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

