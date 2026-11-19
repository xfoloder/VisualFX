package dev.visualfx;

/** The isolated geometry harness exercises the Java path without Minecraft or JNI. */
final class RustGeometry {
    static boolean available() { return false; }

    static final class Batch {
        void add(double x,double y,double z,double endX,double endY,double endZ,double width,
                 double alpha,double glow,double hit,int color,int passes,Geometry sink) {
            throw new AssertionError("Unexpected native call in Java harness");
        }

        void flush(Geometry sink) {
            throw new AssertionError("Unexpected native call in Java harness");
        }
    }
}
