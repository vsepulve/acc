#include <stdio.h>
#include <opencv2/opencv.hpp>

#include <chrono>
#include <thread>

using namespace cv;
using namespace std;

// Asume que out_name tiene al menos (strlen(original) + strlen(add) + 1) bytes
void add_name(const char *original, const char *add, char *out_name){
	unsigned int len = strlen(original);
	unsigned int add_len = strlen(add);
	unsigned int pos = len - 1;
	for(; pos >= 0; --pos){
		if(original[pos] == '.'){
			break;
		}
	}
	memcpy(out_name, original, pos);
	memcpy(out_name + pos, add, add_len);
	memcpy(out_name + pos + add_len, original + pos, len - pos);
	out_name[len + add_len] = 0;
}

int main(int argc, char** argv ){
	if ( argc != 2 ){
		printf("usage: display_image <Image_Path>\n");
		return -1;
	}

	Mat image;
	image = imread( argv[1], 1 );
	if ( !image.data ){
		printf("No image data \n");
		return -1;
	}
	
	Mat gray_image;
	cvtColor(image, gray_image, CV_BGR2GRAY);
	
	char grey_name[ strlen(argv[1]) + strlen("_grey") + 1 ];
	add_name(argv[1], "_grey", grey_name);
	
	cout<<"Guardando imagen en \""<<grey_name<<"\"\n";
	imwrite(grey_name, gray_image );
	
	cout<<"namedWindow...\n";
	namedWindow("Display Image", WINDOW_AUTOSIZE );
	
//	imshow("Display Image", image);
//	imshow("Display Image", gray_image);
//	this_thread::sleep_for(std::chrono::milliseconds(2000));
	
	cout<<"Presentando Original...\n";
	imshow("Display Image", image);
	waitKey(0);
	
	
	cout<<"Presentando Gris...\n";
	imshow("Display Image", gray_image);
	waitKey(0);
	
	Mat bin_image;
	
	adaptiveThreshold(gray_image, bin_image, 255, ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY, 11, 3);
	cout<<"Presentando Bin...\n";
	imshow("Display Image", bin_image);
	waitKey(0);
	
	char bin_name[ strlen(argv[1]) + strlen("_bin") + 1 ];
	add_name(argv[1], "_bin", bin_name);
	
	cout<<"Guardando imagen en \""<<bin_name<<"\"\n";
	imwrite(bin_name, bin_image );
	
//	for(int i = 1; i<=10; ++i){
//		adaptiveThreshold(gray_image, bin_image, 255, ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY, 1+2*i, 3);
//		cout<<"Presentando Bin (Block = "<<(1+2*i)<<")...\n";
//		imshow("Display Image", bin_image);
//		waitKey(0);
//	}
	
//	cout<<"Presentando Original...\n";
//	imshow("Display Image", image);
//	waitKey(0);
//	
//	cout<<"Presentando Gris...\n";
//	imshow("Display Image", gray_image);
//	waitKey(0);

	return 0;
}





