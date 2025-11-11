#![allow(unused)]
use std::ffi::{CStr, CString};
use std::os::raw::{c_char, c_int};

#[no_mangle]
pub extern "C" fn rust_scan_system(_paths: *const c_char) -> c_int {
    // Simplified stub: return 0 threats
    0
}

#[no_mangle]
pub extern "C" fn rust_inspect_packet(_packet: *const u8, _length: c_int) -> c_int {
    0
}

#[no_mangle]
pub extern "C" fn rust_init_threat_db(_ips: *const c_char, _domains: *const c_char) {
}

#[no_mangle]
pub extern "C" fn rust_calculate_file_hash(_path: *const c_char) -> *mut c_char {
    let s = CString::new("error").unwrap();
    s.into_raw()
}

#[no_mangle]
pub extern "C" fn rust_analyze_memory(_address: u64, _size: u64) -> c_int {
    0
}

#[no_mangle]
pub extern "C" fn rust_get_version() -> *mut c_char {
    let s = CString::new("1.0.0").unwrap();
    s.into_raw()
}

#[no_mangle]
pub extern "C" fn rust_free_string(s: *mut c_char) {
    if s.is_null() { return; }
    unsafe { CString::from_raw(s); }
}
