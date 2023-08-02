static class CartesianCoordinate {
    float x;
    float y;
}

// This is he output type of the UDF.
// The schema of the output stream is derived from the names of the fields
// `angle` and `radius`.
static class PolarCoordinate {
    float angle;
    float radius;
}

public PolarCoordinate map(final CartesianCoordinate inputmap) {
    PolarCoordinate output =  new PolarCoordinate();
    output.radius = Math.sqrt(inputmap.x * inputmap.x + inputmap.y * inputmap.y);
    output.angle = Math.atan2(inputmap.x, inputmap.y);
    return output;
}