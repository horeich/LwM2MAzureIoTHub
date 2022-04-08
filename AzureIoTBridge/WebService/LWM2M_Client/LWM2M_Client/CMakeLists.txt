
project(LWM2M_Client_Library VERSION 1.0.0 LANGUAGES CXX)

#add source/header files
add_subdirectory(${PROJECT_SOURCE_DIR}/include)
add_subdirectory(${PROJECT_SOURCE_DIR}/src)

#add external libraries
add_subdirectory(${PROJECT_SOURCE_DIR}/lib/wakaama)

#add tests
add_subdirectory(${PROJECT_SOURCE_DIR}/test)

#library target
add_compile_definitions(LWM2M_CLIENT_MODE) #why do we have to add them here?

add_library(LWM2M_Client ${LWM2MCLIENT_HEADER_FILES} ${LWM2MCLIENT_SRC_FILES})

set_target_properties(LWM2M_Client PROPERTIES ARCHIVE_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib)
set_target_properties(LWM2M_Client PROPERTIES POSITION_INDEPENDENT_CODE ON)
target_link_libraries(LWM2M_Client PUBLIC wakaama)
target_include_directories(LWM2M_Client PUBLIC ${PROJECT_SOURCE_DIR}/include)
