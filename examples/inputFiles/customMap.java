static class CartesianCoordinate {
    double x;
    double y;
}

// This is he output type of the UDF.
// The schema of the output stream is derived from the names of the fields
// `angle` and `radius`.
static class PolarCoordinate {
    double angle;
    double radius;
}

public PolarCoordinate map(final CartesianCoordinate value) {
    PolarCoordinate output =  new PolarCoordinate();
    output.radius = Math.sqrt(value.x * value.x + value.y * value.y);
    output.angle = Math.atan2(value.x, value.y);
    return output;
}