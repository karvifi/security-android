mod packet_inspection;
mod scanner;
mod memory_analysis;

use std::ffi::{CStr, CString};
use std::os::raw::c_char;

// JNI interface for packet inspection
#[no_mangle]
pub extern "C" fn Java_com_fortress_hypervisor_services_HypervisorVpnService_processPacket(
    env: *mut jni::JNIEnv,
    _class: *mut jni::sys::jclass,
    packet_data: jni::sys::jbyteArray,
    data_len: jni::sys::jint,
) -> jni::sys::jboolean {
    // Convert Java byte array to Rust slice
    let mut packet_bytes = vec![0u8; data_len as usize];
    unsafe {
        (**env).GetByteArrayRegion.unwrap()(env, packet_data, 0, data_len, packet_bytes.as_mut_ptr() as *mut i8);
    }

    // Process packet using Rust implementation
    let result = packet_inspection::process_packet(&packet_bytes);

    // Return result as boolean (true if packet should be blocked)
    if result.block_packet { 1 } else { 0 }
}

#[no_mangle]
pub extern "C" fn Java_com_fortress_hypervisor_services_HypervisorVpnService_analyzeHttpRequest(
    env: *mut jni::JNIEnv,
    _class: *mut jni::sys::jclass,
    request_data: jni::sys::jbyteArray,
    data_len: jni::sys::jint,
) -> *mut c_char {
    // Convert Java byte array to Rust string
    let mut request_bytes = vec![0u8; data_len as usize];
    unsafe {
        (**env).GetByteArrayRegion.unwrap()(env, request_data, 0, data_len, request_bytes.as_mut_ptr() as *mut i8);
    }

    let request_str = match String::from_utf8(request_bytes) {
        Ok(s) => s,
        Err(_) => return std::ptr::null_mut(),
    };

    // Analyze HTTP request
    let analysis = packet_inspection::analyze_http_request(&request_str);

    // Convert result to JSON string
    let json_result = format!(
        r#"{{"malicious": {}, "threat_type": "{}", "confidence": {:.2}}}"#,
        analysis.malicious, analysis.threat_type, analysis.confidence
    );

    // Return as C string
    match CString::new(json_result) {
        Ok(c_string) => c_string.into_raw(),
        Err(_) => std::ptr::null_mut(),
    }
}

#[no_mangle]
pub extern "C" fn Java_com_fortress_hypervisor_services_HypervisorVpnService_checkDomain(
    env: *mut jni::JNIEnv,
    _class: *mut jni::sys::jclass,
    domain: *const c_char,
) -> jni::sys::jboolean {
    let domain_str = unsafe {
        match CStr::from_ptr(domain).to_str() {
            Ok(s) => s,
            Err(_) => return 0,
        }
    };

    // Check domain against blocklist
    let blocked = packet_inspection::is_domain_blocked(domain_str);

    if blocked { 1 } else { 0 }
}

// JNI interface for malware scanner
#[no_mangle]
pub extern "C" fn Java_com_fortress_hypervisor_utils_MalwareScanner_scanFile(
    env: *mut jni::JNIEnv,
    _class: *mut jni::sys::jclass,
    file_path: *const c_char,
) -> *mut c_char {
    let path_str = unsafe {
        match CStr::from_ptr(file_path).to_str() {
            Ok(s) => s,
            Err(_) => return std::ptr::null_mut(),
        }
    };

    // Perform file scan
    let scan_result = scanner::scan_file_internal(path_str);

    match scan_result {
        Ok(result) => {
            let json_result = format!(
                r#"{{"file_path": "{}", "is_malicious": {}, "threat_name": "{}", "confidence": {:.2}, "scan_time_ms": {}}}"#,
                result.file_path,
                result.is_malicious,
                result.threat_name.unwrap_or_else(|| "None".to_string()),
                result.confidence,
                result.scan_time_ms
            );

            match CString::new(json_result) {
                Ok(c_string) => c_string.into_raw(),
                Err(_) => std::ptr::null_mut(),
            }
        }
        Err(_) => std::ptr::null_mut(),
    }
}

#[no_mangle]
pub extern "C" fn Java_com_fortress_hypervisor_utils_MalwareScanner_scanDirectory(
    env: *mut jni::JNIEnv,
    _class: *mut jni::sys::jclass,
    dir_path: *const c_char,
) -> *mut c_char {
    let path_str = unsafe {
        match CStr::from_ptr(dir_path).to_str() {
            Ok(s) => s,
            Err(_) => std::ptr::null_mut(),
        }
    };

    // Perform directory scan
    let scan_results = scanner::scan_directory_internal(path_str);

    let json_results: Vec<String> = scan_results.into_iter()
        .map(|result| {
            format!(
                r#"{{"file_path": "{}", "is_malicious": {}, "threat_name": "{}", "confidence": {:.2}}}"#,
                result.file_path,
                result.is_malicious,
                result.threat_name.unwrap_or_else(|| "None".to_string()),
                result.confidence
            )
        })
        .collect();

    let json_array = format!("[{}]", json_results.join(","));

    match CString::new(json_array) {
        Ok(c_string) => c_string.into_raw(),
        Err(_) => std::ptr::null_mut(),
    }
}

// JNI interface for memory analysis
#[no_mangle]
pub extern "C" fn Java_com_fortress_hypervisor_utils_MemoryAnalyzer_analyzeMemory(
    env: *mut jni::JNIEnv,
    _class: *mut jni::sys::jclass,
) -> *mut c_char {
    // Perform memory analysis
    let analysis_result = memory_analysis::analyze_memory();

    if analysis_result.is_null() {
        return std::ptr::null_mut();
    }

    let result = unsafe { &*analysis_result };

    let json_result = format!(
        r#"{{
            "total_regions": {},
            "suspicious_regions": {},
            "injected_code_detected": {},
            "hooks_detected": {},
            "analysis_time_ms": {}
        }}"#,
        result.total_regions,
        result.suspicious_regions.len(),
        result.injected_code_detected,
        result.hooks_detected.len(),
        result.analysis_time_ms
    );

    // Clean up
    memory_analysis::free_memory_result(analysis_result);

    match CString::new(json_result) {
        Ok(c_string) => c_string.into_raw(),
        Err(_) => std::ptr::null_mut(),
    }
}

#[no_mangle]
pub extern "C" fn Java_com_fortress_hypervisor_utils_MemoryAnalyzer_generateMemoryReport(
    env: *mut jni::JNIEnv,
    _class: *mut jni::sys::jclass,
) -> *mut c_char {
    // Generate detailed memory report
    let report = memory_analysis::generate_memory_report();

    if report.is_null() {
        return std::ptr::null_mut();
    }

    // The report is already a C string, just return it
    report
}

// Utility functions for string management
#[no_mangle]
pub extern "C" fn hypervisor_free_string(string: *mut c_char) {
    if !string.is_null() {
        unsafe { CString::from_raw(string) };
    }
}

// Initialize all Rust components
#[no_mangle]
pub extern "C" fn hypervisor_rust_init() -> i32 {
    // Initialize packet inspection
    packet_inspection::init_packet_inspection();

    // Initialize malware scanner
    scanner::hypervisor_scanner_init();

    // Initialize memory analysis
    memory_analysis::hypervisor_memory_init();

    0 // Success
}

// Get version information
#[no_mangle]
pub extern "C" fn hypervisor_get_version() -> *mut c_char {
    let version = env!("CARGO_PKG_VERSION");
    match CString::new(format!("Fortress Hypervisor Rust Core v{}", version)) {
        Ok(c_string) => c_string.into_raw(),
        Err(_) => std::ptr::null_mut(),
    }
}

// Performance monitoring
#[no_mangle]
pub extern "C" fn hypervisor_get_performance_stats() -> *mut c_char {
    // In a real implementation, this would collect actual performance metrics
    let stats = r#"{
        "packet_processing_rate": 125000,
        "memory_scan_rate": 500,
        "cpu_usage_percent": 15.5,
        "memory_usage_mb": 45.2
    }"#;

    match CString::new(stats) {
        Ok(c_string) => c_string.into_raw(),
        Err(_) => std::ptr::null_mut(),
    }
}