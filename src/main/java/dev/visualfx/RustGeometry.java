package dev.visualfx;

import java.io.IOException;
import java.lang.ref.Reference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.system.MemoryUtil;

final class RustGeometry {
    static final int CAPACITY=1024, LINE_BYTES=88, VERTEX_BYTES=16, MAX_VERTICES=24;
    private static final String LIBRARY="/assets/visualfx/native/visualfx_geometry.dll";
    private static Boolean available;

    private RustGeometry() {}

    static synchronized boolean available() {
        if(available!=null)return available;
        String os=System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String arch=System.getProperty("os.arch", "");
        if(!os.startsWith("windows")||!(arch.equals("amd64")||arch.equals("x86_64")))return available=false;
        try(var resource=RustGeometry.class.getResourceAsStream(LIBRARY)) {
            if(resource==null)throw new IOException("Missing Rust library");
            byte[] library=resource.readAllBytes();
            byte[] digest=MessageDigest.getInstance("SHA-256").digest(library);
            var directory=FabricLoader.getInstance().getConfigDir().resolve("visualfx-native");
            Files.createDirectories(directory);
            var path=directory.resolve("geometry-"+HexFormat.of().formatHex(digest)+".dll");
            if(!Files.isRegularFile(path)||!MessageDigest.isEqual(library,Files.readAllBytes(path))) {
                var staged=Files.createTempFile(directory,"geometry-", ".tmp");
                try {
                    Files.write(staged,library);
                    try { Files.move(staged,path,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING); }
                    catch(java.nio.file.AtomicMoveNotSupportedException ignored) { Files.move(staged,path,StandardCopyOption.REPLACE_EXISTING); }
                } finally { Files.deleteIfExists(staged); }
            }
            System.load(path.toAbsolutePath().toString());
            if(expand(0,0,0)!=-1)throw new LinkageError("Unexpected Rust library handshake");
            VisualFX.LOG.info("VisualFX Rust geometry loaded");
            return available=true;
        } catch(IOException|NoSuchAlgorithmException|LinkageError|SecurityException error) {
            VisualFX.LOG.warn("VisualFX Rust geometry unavailable. Using Java.",error);
            return available=false;
        }
    }

    static final class Batch {
        private final ByteBuffer lines=allocate(CAPACITY*LINE_BYTES);
        private final ByteBuffer vertices=allocate(CAPACITY*MAX_VERTICES*VERTEX_BYTES);
        private int size;
        private int expectedVertices;

        private static ByteBuffer allocate(int bytes) {
            return ByteBuffer.allocateDirect(bytes).order(ByteOrder.nativeOrder());
        }

        void add(double x,double y,double z,double endX,double endY,double endZ,double width,
                 double alpha,double glow,double hit,int color,int passes,Geometry sink) {
            if(size==CAPACITY)flush(sink);
            int offset=size++*LINE_BYTES;
            lines.putDouble(offset,x).putDouble(offset+8,y).putDouble(offset+16,z);
            lines.putDouble(offset+24,endX).putDouble(offset+32,endY).putDouble(offset+40,endZ);
            lines.putDouble(offset+48,width).putDouble(offset+56,alpha).putDouble(offset+64,glow);
            lines.putDouble(offset+72,hit).putInt(offset+80,color).putInt(offset+84,passes);
            expectedVertices+=passes*8;
        }

        void flush(Geometry sink) {
            if(size==0)return;
            try {
                int count=expand(MemoryUtil.memAddress(lines),MemoryUtil.memAddress(vertices),size);
                if(count!=expectedVertices)throw new IllegalStateException("Unexpected Rust geometry vertex count");
                for(int offset=0;offset<count*VERTEX_BYTES;offset+=VERTEX_BYTES) {
                    int argb=vertices.getInt(offset+12);
                    sink.vertex(vertices.getFloat(offset),vertices.getFloat(offset+4),vertices.getFloat(offset+8),argb&0xFFFFFF,argb>>>24);
                }
            } finally {
                Reference.reachabilityFence(lines);
                Reference.reachabilityFence(vertices);
                size=0;
                expectedVertices=0;
            }
        }
    }

    private static native int expand(long lines,long vertices,int count);
}
