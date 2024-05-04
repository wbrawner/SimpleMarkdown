cmake_minimum_required(VERSION 3.22.1)

project("md4k")

add_subdirectory("lib/md4c")
set(BUILD_MD2HTML_EXECUTABLE OFF)

add_library(${CMAKE_PROJECT_NAME} SHARED
        src/main/cpp/md4k.c)

target_link_libraries(${CMAKE_PROJECT_NAME}
        md4c
        md4c-html
)

target_include_directories(${CMAKE_PROJECT_NAME} PUBLIC
        ${PROJECT_SOURCE_DIR}/lib/md4c/src
)