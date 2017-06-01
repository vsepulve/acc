cmake_minimum_required(VERSION 2.8)
project( count_test )
find_package( OpenCV REQUIRED )
add_executable( count_test count_test.cpp )
target_link_libraries( count_test ${OpenCV_LIBS} )
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")

