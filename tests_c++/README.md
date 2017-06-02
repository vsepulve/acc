# C++ README #

install dependecies
-------------------
```
tar -xzf opencv-2.4.13.2.tar.gz
mkdir opencv-2.4.13.2/build
cd opencv-2.4.13.2/build
cmake -D CMAKE_BUILD_TYPE=RELEASE -D CMAKE_INSTALL_PREFIX=/usr/local -D WITH_TBB=ON -D BUILD_NEW_PYTHON_SUPPORT=ON -D WITH_V4L=ON -D WITH_GTK=ON -D WITH_OPENGL=ON ..
make
sudo make install
```

Note the "-D WITH_GTK=ON" is quite important to use OpenCV namedWindow function.


