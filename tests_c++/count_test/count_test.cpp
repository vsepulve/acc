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
		printf("usage: ./count_test <Image_Path>\n");
		return -1;
	}
	
	const char *original_name = argv[1];
	
	char grey_name[ strlen(original_name) + strlen("_grey") + 1 ];
	add_name(original_name, "_grey", grey_name);
	
	char bin_name[ strlen(original_name) + strlen("_bin") + 1 ];
	add_name(original_name, "_bin", bin_name);
	
	vector<vector<Point> > contours;
	vector<Vec4i> hierarchy;
	Rect bounding_rect;
	Mat grey, bin;
	
	namedWindow("Display Image", WINDOW_AUTOSIZE );
	
	// Cargar imagen de archivo
	Mat src = imread(original_name, CV_LOAD_IMAGE_COLOR); 
	imshow("Display Image", src);
	waitKey(0);
	
	// Convertir a gris
	cvtColor(src, grey, CV_BGR2GRAY);
	imshow("Display Image", grey);
	waitKey(0);
	
	// Deteccion de bordes con un threshold (cte o adaptativo gaussiano)
	// Ojo que los parametros del filtro (11 y 3) son mas o menos arbitrarios (11 es el largo de la ventana)
//	threshold(grey, bin, 40, 255, THRESH_BINARY);
	adaptiveThreshold(grey, bin, 255, ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY, 11, 3);
	imshow("Display Image", bin);
	
	// Guardo la imagen binaria en un archivo
	imwrite(bin_name, bin);
	waitKey(0);
	
	// Busqueda de figuras en la imagen binaria
	// Notar que esta funcion modifica la imagen mientras escanea los pixeles
//	findContours(bin, contours, hierarchy, CV_RETR_EXTERNAL, CV_CHAIN_APPROX_NONE );
	findContours(bin, contours, hierarchy, RETR_LIST, CHAIN_APPROX_NONE );
	
	cout<<"contours.size(): "<<contours.size()<<"\n";
	
	unsigned int n = 0;
	
	// Iterar por figura para escoger algunas y marcarlas 
	for( int i = 0; i< contours.size(); i++ ){
		// Solo considero imagenes de una cierta area minima (que puede ser un % del area total o algo similar)
		// Notar que aqui tambien se puede comparar area y perimetro (por ejemplo) para filtrar por forma
		if( (contourArea(contours[i], false)) > 400 ){
			// Una opcion es dibujar el poligono en la imagen binaria
//			drawContours( bin, contours, i , color, CV_FILLED, 8, hierarchy );
			// Otra opcion es marcarlas en la imagen original (con un rectangulo, marca en el centroide o lo que sea)
			bounding_rect = boundingRect(contours[i]);
			// El Scalar define un color aleatorio
			rectangle(src, bounding_rect, Scalar(rand()%256, rand()%256, rand()%256), 3, 8, 0);
			++n;
		}
	}
	cout<<"Contados: "<<n<<" de "<<contours.size()<<"\n";
	
	imshow("Display Image", src);
	waitKey(0);
	return 0;
	
	
	
	
	
	
	
	
	/*
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
	*/
	
}





