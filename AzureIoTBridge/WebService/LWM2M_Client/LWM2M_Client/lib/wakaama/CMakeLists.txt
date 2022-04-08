cmake_minimum_required(VERSION 3.13)

project(wakaama_C_Library VERSION 1.0.0 LANGUAGES C)

set(CMAKE_C_STANDARD 11)

if(DTLS)
    message(FATAL_ERROR "DTLS option is not supported." )
endif()

include(${CMAKE_CURRENT_LIST_DIR}/core/wakaama.cmake)
include(${CMAKE_CURRENT_LIST_DIR}/coap/coap.cmake)
include(${CMAKE_CURRENT_LIST_DIR}/data/data.cmake)
include(${CMAKE_CURRENT_LIST_DIR}/examples/shared/shared.cmake)
include(${CMAKE_CURRENT_LIST_DIR}/examples/lightclient/lightclient.cmake)

add_compile_definitions(LWM2M_CLIENT_MODE)

if(LWM2M_VERSION VERSION_GREATER "1.0")
    add_compile_definitions(LWM2M_SUPPORT_SENML_JSON)
endif()

add_subdirectory(${PROJECT_SOURCE_DIR}/include)


###########
# add the library:
add_library(wakaama STATIC ${WAKAAMA_SOURCES} ${COAP_SOURCES} ${DATA_SOURCES} ${SHARED_SOURCES} ${LIGHTCLIENT_SOURCES} ${WAKAAMA_HEADER_FILES})

target_include_directories(wakaama PUBLIC ${PROJECT_SOURCE_DIR}/include)
target_include_directories(wakaama PUBLIC ${WAKAAMA_HEADERS_DIR} ${COAP_HEADERS_DIR} ${DATA_HEADERS_DIR} ${WAKAAMA_SOURCES_DIR} ${SHARED_INCLUDE_DIRS})

set_target_properties(wakaama PROPERTIES ARCHIEVE_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib)
set_target_properties(wakaama PROPERTIES POSITION_INDEPENDENT_CODE ON)
set_target_properties(wakaama PROPERTIES LINKER_LANGUAGE C)
