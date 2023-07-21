public static Float2 map(Float2 value) {
        float radius = TornadoMath.sqrt(value.getX() * value.getX() + value.getY() * value.getY());
        float angle = TornadoMath.atan2(value.getX(), value.getY());
        Float2 output = new Float2(angle, radius);

        return output;
    }

    public static void customMap(VectorFloat2 in1, VectorFloat2 out) {
        for (@Parallel int i = 0; i < in1.getLength(); i++) {
            out.set(i, map(in1.get(i)));
        }
    }