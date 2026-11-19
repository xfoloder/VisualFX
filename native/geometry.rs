#![no_std]

use core::{arch::x86_64::{_mm_cvtsd_f64, _mm_set_sd, _mm_sqrt_sd}, ffi::c_void, ptr};

#[repr(C)]
struct Line {
    x: f64, y: f64, z: f64,
    end_x: f64, end_y: f64, end_z: f64,
    width: f64, alpha: f64, glow: f64, hit: f64,
    color: u32, passes: u32,
}

#[repr(C)]
struct Vertex { x: f32, y: f32, z: f32, argb: u32 }

#[panic_handler]
fn panic(_: &core::panic::PanicInfo<'_>) -> ! { loop { core::hint::spin_loop(); } }

#[unsafe(no_mangle)]
static _fltused: i32 = 0;

/// JNI batch entry point. The caller owns the aligned, nonoverlapping direct buffers
/// and keeps both alive for the entire call. Each line can write at most 24 vertices.
#[unsafe(no_mangle)]
pub unsafe extern "system" fn Java_dev_visualfx_RustGeometry_expand(
    _env: *mut c_void, _class: *mut c_void, input: i64, output: i64, count: i32,
) -> i32 {
    if input == 0 || output == 0 || !(0..=1024).contains(&count) { return -1; }
    let input = input as *const Line;
    let output = output as *mut Vertex;
    let mut written = 0;
    for index in 0..count as usize {
        let line = unsafe { &*input.add(index) };
        let dx = line.end_x - line.x;
        let dz = line.end_z - line.z;
        let len = unsafe {
            _mm_cvtsd_f64(_mm_sqrt_sd(_mm_set_sd(0.0), _mm_set_sd(dx * dx + dz * dz)))
        };
        for pass in 0..line.passes.min(3) {
            let (thickness, opacity) = match pass {
                0 => (1.0, 0.92),
                1 => (2.2, 0.2 * line.glow),
                _ => (4.8, 0.055 * line.glow),
            };
            let w = line.width * thickness * (1.0 + line.hit * 0.5);
            let alpha = (line.alpha * opacity * (1.0 + line.hit)).clamp(0.0, 1.0);
            let argb = ((alpha * 255.0) as u32) << 24 | line.color;
            let (nx, nz) = if len > 1e-7 { (-dz / len * w, dx / len * w) } else { (w, 0.0) };
            let mut emit = |x: f64, y: f64, z: f64| {
                unsafe { ptr::write(output.add(written), Vertex { x: x as f32, y: y as f32, z: z as f32, argb }) };
                written += 1;
            };
            emit(line.x + nx, line.y, line.z + nz);
            emit(line.end_x + nx, line.end_y, line.end_z + nz);
            emit(line.end_x - nx, line.end_y, line.end_z - nz);
            emit(line.x - nx, line.y, line.z - nz);
            emit(line.x, line.y + w, line.z);
            emit(line.end_x, line.end_y + w, line.end_z);
            emit(line.end_x, line.end_y - w, line.end_z);
            emit(line.x, line.y - w, line.z);
        }
    }
    written as i32
}

const _: () = assert!(core::mem::size_of::<Line>() == 88 && core::mem::size_of::<Vertex>() == 16);
