cmake_minimum_required(VERSION 3.16.3)

project(LWM2M_Client_Application VERSION 1.0.0 LANGUAGES CXX)

#global variables

#C++ language definitions
set(CMAKE_CXX_STANDARD 14)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -Werror -pedantic -Wextra")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -pedantic -Wextra")

#Add lwm2m client
add_subdirectory(${PROJECT_SOURCE_DIR}/LWM2M_Client)

#application target
add_executable(LWM2M_client_app ${PROJECT_SOURCE_DIR}/LWM2M_client_app.cpp)
set_target_properties(LWM2M_client_app PROPERTIES RUNTIME_OUTPUT_DIRECTORY ${PROJECT_BINARY_DIR}/bin)
target_link_libraries(LWM2M_client_app LWM2M_Client)
