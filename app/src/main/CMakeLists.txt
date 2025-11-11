cmake_minimum_required(VERSION 3.18)
project(fortress_hypervisor_native)

# Build the Rust library as a static lib expected to be available as fortress_rust
add_library(fortress_rust STATIC IMPORTED)
set_target_properties(fortress_rust PROPERTIES IMPORTED_LOCATION "${CMAKE_SOURCE_DIR}/../rust/target/${ANDROID_ABI}/release/libhypervisor_rust.a")

add_library(hypervisor_native SHARED src/main/jni/native_stub.cpp)

find_library(log-lib log)
find_library(android-lib android)

target_link_libraries(hypervisor_native PRIVATE ${log-lib} ${android-lib} fortress_rust)

include_directories(src/main/jni)
