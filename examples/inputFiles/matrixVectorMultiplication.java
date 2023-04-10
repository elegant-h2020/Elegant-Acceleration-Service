public static void matrixVectorMultiplication(float[] a, float[] b, float[] c, int size) {
    for (@Parallel int i = 0; i < size; i++) {
        float sum = 0.0f;
        for (int j = 0; j < size; j++) {
            sum += a[(i * size) + j] * b[j];
        }
        c[i] = sum;
    }
}