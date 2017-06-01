# acc

Requires OpenCV

To compile OpenCV:
  - mkdir opencv_path/build
  - cd opencv_path/build
  - cmake -D CMAKE_BUILD_TYPE=RELEASE -D CMAKE_INSTALL_PREFIX=/usr/local -D WITH_TBB=ON -D BUILD_NEW_PYTHON_SUPPORT=ON -D WITH_V4L=ON -D WITH_GTK=ON -D WITH_OPENGL=ON ..
  - make
  - sudo make install



