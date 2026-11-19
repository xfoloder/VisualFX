$ErrorActionPreference = 'Stop'
$target = 'x86_64-pc-windows-msvc'
$sysroot = & rustc --print sysroot --target $target
if ($LASTEXITCODE -ne 0) { throw 'Rust toolchain unavailable' }
$linker = Join-Path $sysroot "lib/rustlib/$target/bin/rust-lld.exe"
if (!(Test-Path -LiteralPath $linker)) { throw "Install Rust target $target using rustup" }
$project = Split-Path -Parent $PSScriptRoot
$destination = Join-Path $project 'src/main/resources/assets/visualfx/native/visualfx_geometry.dll'
$temporary = Join-Path ([IO.Path]::GetTempPath()) ('visualfx-rust-' + [guid]::NewGuid())
New-Item -ItemType Directory -Path $temporary | Out-Null
try {
    $library = Join-Path $temporary 'visualfx_geometry.dll'
    & rustc --target $target --edition=2024 --crate-type=cdylib -C opt-level=3 -C panic=abort -C "linker=$linker" -C link-arg=/NOENTRY -C link-arg=/NODEFAULTLIB -o $library (Join-Path $PSScriptRoot 'geometry.rs')
    if ($LASTEXITCODE -ne 0) { throw 'Rust build failed' }
    Copy-Item -LiteralPath $library -Destination $destination -Force
} finally {
    Get-ChildItem -LiteralPath $temporary -File | Remove-Item
    Remove-Item -LiteralPath $temporary
}
