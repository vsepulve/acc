package com.example.vsepulve.test;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.BoolRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int RESULT_SETTINGS = 2;

    String full_path;
    Mat mat_image;
    Bitmap bitmap_image;
    boolean original_loaded;
    int cteThresholdValue;
    int thresholdBlocksize;
    int thresholdCte;
    boolean thresholdType;

    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }

    /*
    private File createImageFile() throws IOException {
        System.out.println("MainActivity.createImageFile - Inicio");
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "ACC_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        System.out.println("MainActivity.createImageFile - Fin ("+mCurrentPhotoPath+")");
        return image;
    }
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cteThresholdValue = 100;
        thresholdBlocksize = 5;
        thresholdCte = 1;
        System.out.println("MainActivity.onCreate ("+cteThresholdValue+", "+thresholdBlocksize+", "+thresholdCte+")");
        setContentView(R.layout.activity_main);
        mat_image = null;
        bitmap_image = null;
        original_loaded = false;

        showUserSettings();

    }

    //Agregacion para settings

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivityForResult(i, RESULT_SETTINGS);
                break;

        }

        return true;
    }

    private void showUserSettings() {

        System.out.println("MainActivity.showUserSettings - Inicio");

        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        StringBuilder builder = new StringBuilder();

        String str = sharedPrefs.getString("cteThresholdValue", "100");
        cteThresholdValue = new Integer(str);
        System.out.println("MainActivity.showUserSettings - cteThresholdValue: "+str);
        builder.append("\n cteThresholdValue: " + str);

        str = sharedPrefs.getString("thresholdBlocksize", "7");
        thresholdBlocksize = new Integer(str);
        System.out.println("MainActivity.showUserSettings - thresholdBlocksize: "+str);
        builder.append("\n thresholdBlocksize: " + str);

        str = sharedPrefs.getString("thresholdCte", "1");
        thresholdCte = new Integer(str);
        System.out.println("MainActivity.showUserSettings - thresholdCte: "+str);
        builder.append("\n thresholdCte: " + str);

        thresholdType = sharedPrefs.getBoolean("thresholdType", true);
        System.out.println("MainActivity.showUserSettings - thresholdType: "+thresholdType);
        builder.append("\n thresholdType: " + thresholdType);

        TextView settingsTextView = (TextView) findViewById(R.id.textUserSettings);

        settingsTextView.setText(builder.toString());
        System.out.println("MainActivity.showUserSettings - Fin");
    }

    //Fin Agregacion para settings

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("MainActivity.onResume ("+cteThresholdValue+", "+thresholdBlocksize+", "+thresholdCte+")");
    }

    public void buttonAction(View view){
        System.out.println("MainActivity.buttonAction - Inicio");
        dispatchTakePictureIntent();
        System.out.println("MainActivity.buttonAction - Fin");
    }

    private void dispatchTakePictureIntent() {
        System.out.println("MainActivity.dispatchTakePictureIntent - Inicio");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private String getLastImagePath(){
        final String[] imageColumns = {
                MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA
        };
        final String imageOrderBy = MediaStore.Images.Media._ID+" DESC";
        Cursor imageCursor = managedQuery(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                imageColumns, null, null, imageOrderBy
        );
        if(imageCursor.moveToFirst()){
            int id = imageCursor.getInt(imageCursor.getColumnIndex(MediaStore.Images.Media._ID));
            String fullPath = imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
            //System.out.println("getLastImageId - fullPath: \""+fullPath+"\"");
            imageCursor.close();
            return fullPath;
        }
        else{
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("MainActivity.onActivityResult - Inicio (Code "+requestCode+")");
        // Primera opcion es para settings
        if(requestCode == RESULT_SETTINGS){
            showUserSettings();
        }
        else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            System.out.println("MainActivity.onActivityResult - Image Capture");
            ImageView image_view = null;
			/*
			Bundle extras = data.getExtras();
			bitmap_image = (Bitmap) extras.get("data");
			image_view = (ImageView) findViewById(R.id.image_view);
			image_view.setImageBitmap(bitmap_image);
			*/

            full_path = getLastImagePath();
            if(full_path == null){
                System.out.println("MainActivity.onActivityResult - full_path null, saliendo");

            }
            else{

                System.out.println("MainActivity.onActivityResult - full_path : \""+full_path+"\"");
                image_view = (ImageView) findViewById(R.id.image_view);
                // bitmap_image = BitmapFactory.decodeFile(full_path);
                bitmap_image = loadImage(full_path, image_view);
                original_loaded = true;
                System.out.println("MainActivity.onActivityResult - setImageBitmap...");
                image_view.setImageBitmap(bitmap_image);

                /*
                System.out.println("MainActivity.onActivityResult - Preparando bitmap...");
                try {
                    ExifInterface exif = new ExifInterface(full_path);
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                    //Log.d("EXIF", "Exif: " + orientation);
                    Matrix matrix = new Matrix();
                    if (orientation == 6) {
                        matrix.postRotate(90);
                    }
                    else if (orientation == 3) {
                        matrix.postRotate(180);
                    }
                    else if (orientation == 8) {
                        matrix.postRotate(270);
                    }
                    // rotating bitmap
                    bitmap_image = Bitmap.createBitmap(
                            bitmap_image, 0, 0, bitmap_image.getWidth(), bitmap_image.getHeight(), matrix, true
                    );
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                */

            }

            System.out.println("MainActivity.onActivityResult - Fin de Image Capture");

        }
    }

    public void threshold(View view) {
        if( full_path == null || full_path.length() < 1){
            System.out.println("MainActivity.threshold - path vacio");
            return;
        }

        System.out.println("MainActivity.threshold - Leyendo value");

        // EditText edit_text = (EditText) findViewById(R.id.threshold_value);
        // int threshold_value = new Integer(edit_text.getText().toString());
        int threshold_value = cteThresholdValue;
        if(threshold_value < 30){
            threshold_value = 30;
        }
        else if(threshold_value > 230){
            threshold_value = 230;
        }

        int block_size = thresholdBlocksize;
        if(block_size < 1){
            block_size = 1;
        }
        else if(block_size > 21){
            block_size = 21;
        }

        int threshold_cte = thresholdCte;
        if(threshold_cte < 1){
            threshold_cte = 1;
        }
        else if(threshold_cte > 10){
            threshold_cte = 10;
        }

        System.out.println("MainActivity.threshold - Cargando mat");
        //mat_image = Highgui.imread(full_path);
        if(bitmap_image == null || !original_loaded){
            System.out.println("MainActivity.threshold - bitmap_image null, preparando carga");
            ImageView image_view = (ImageView) findViewById(R.id.image_view);
            System.out.println("MainActivity.threshold - loadImage...");
            bitmap_image = loadImage(full_path, image_view);
            original_loaded = true;
        }
        if(mat_image == null){
            mat_image = new Mat();
        }
        System.out.println("MainActivity.threshold - bitmapToMat...");
        Utils.bitmapToMat(bitmap_image, mat_image);

        System.out.println("MainActivity.threshold - Gris");
        Imgproc.cvtColor(mat_image, mat_image, Imgproc.COLOR_BGR2GRAY);
        if(thresholdType){
            System.out.println("MainActivity.threshold - Aplicando threshold adaptativo "+threshold_value+" ...");
            Imgproc.adaptiveThreshold(mat_image, mat_image, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, block_size, threshold_cte);
        }
        else{
            System.out.println("MainActivity.threshold - Aplicando threshold binario "+threshold_value+" ...");
            Imgproc.threshold(mat_image, mat_image, threshold_value, 255, Imgproc.THRESH_BINARY );
        }
        //if(bitmap_image == null) {
        //    System.out.println("MainActivity.threshold - Creando imagen para conversion");
        //    bitmap_image = Bitmap.createBitmap(mat_image.width(), mat_image.height(), Bitmap.Config.ARGB_8888);
        //}
        System.out.println("MainActivity.threshold - Convirtiendo");
        Utils.matToBitmap(mat_image, bitmap_image);
        original_loaded = false;
        System.out.println("MainActivity.threshold - Desplegando (size: "+bitmap_image.getWidth()+", "+bitmap_image.getHeight()+")");
        ImageView image_view = (ImageView) findViewById(R.id.image_view);
        System.out.println("MainActivity.threshold - image_view size: ("+image_view.getWidth()+", "+image_view.getHeight()+")");
        image_view.setImageBitmap(bitmap_image);
    }

    public void count(View view) {
        threshold(view);

        if (mat_image == null || full_path == null) {
            System.out.println("MainActivity.count - mat_image vacio");
            return;
        }

        System.out.println("MainActivity.count - Analizando");

        // findContours(bin, contours, hierarchy, CV_RETR_TREE, CHAIN_APPROX_NONE, Point(0, 0) );
        // Quizas sea necesario clonar la imagen
        // La siguente llamada deja un clon incorrecto si se llama dos veces
        // Eso es porque asume que en este punto, mat != null es una imagen binaria
        // Es necesario entonces no modificar el mat (para dejarlo binario) o usar otro para recargar el original
        Mat clon_image = mat_image.clone();
        //mat_image = Highgui.imread(full_path);
        if(bitmap_image == null || !original_loaded){
            ImageView image_view = (ImageView) findViewById(R.id.image_view);
            bitmap_image = loadImage(full_path, image_view);
            original_loaded = true;
        }
        // if(mat_image == null){
        //    mat_image = new Mat();
        // }
        Mat original = new Mat();
        Utils.bitmapToMat(bitmap_image, original);

        List<MatOfPoint> contours = new LinkedList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(clon_image, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE, new Point(0, 0));

        System.out.println("MainActivity.count - contours: "+contours.size());
        int n = 0;
        // Estos límites deben ser proporcionales al area total
        // int min_area = 2000;
        // int max_area = 250000;
        int min_area = (original.width() * original.height()) / 2000;
        int max_area = (original.width() * original.height()) / 15;
        System.out.println("MainActivity.count - areas ["+min_area+", "+max_area+"] de "+(original.width() * original.height()) );
        MatOfPoint fig = null;
        MatOfPoint2f fig2f = new MatOfPoint2f();
        MatOfPoint res = new MatOfPoint();
        MatOfPoint2f res2f = new MatOfPoint2f();
        Random rand = new Random();
        for(int i = 0; i < contours.size(); i++){
            fig = contours.get(i);
            double area_figura = Imgproc.contourArea(fig, false);
            if( (area_figura > min_area) && (area_figura < max_area) ){
                n++;
                System.out.println("MainActivity.count - Convirtiendo fig a 2f");
                fig.convertTo(fig2f, CvType.CV_32FC2);
                System.out.println("MainActivity.count - approxPolyDP...");
                Imgproc.approxPolyDP(fig2f, res2f, 3, true);
                System.out.println("MainActivity.count - creando arr_res");
                List<MatOfPoint> arr_res = new LinkedList<MatOfPoint>();
                res2f.convertTo(res, CvType.CV_32S);
                arr_res.add(res);
                System.out.println("MainActivity.count - drawContours (-1)");
                Imgproc.drawContours(original, arr_res, 0,
                        new Scalar(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)), -1
                );
                System.out.println("MainActivity.count - ok, next");

            }
        }
        System.out.println("MainActivity.count - "+min_area+" < areas < "+max_area+": "+n);

        System.out.println("MainActivity.count - Convirtiendo");
        Utils.matToBitmap(original, bitmap_image);
        original_loaded = false;
        System.out.println("MainActivity.count - Desplegando, size: ("+bitmap_image.getWidth()+", "+bitmap_image.getHeight()+")");
        ImageView image_view = (ImageView) findViewById(R.id.image_view);
        System.out.println("MainActivity.count - image_view size: ("+image_view.getWidth()+", "+image_view.getHeight()+")");
        image_view.setImageBitmap(bitmap_image);
    }

    private Bitmap loadImage(String full_path, ImageView image_view) {
        System.out.println("MainActivity.loadImage - Inicio, path \""+full_path+"\"");
        // Get the dimensions of the View
        int targetW = image_view.getWidth();
        int targetH = image_view.getHeight();
        System.out.println("MainActivity.loadImage - target Size: ("+targetW+", "+targetH+")");

        // Get the dimensions of the bitmap
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(full_path, options);
        int photoW = options.outWidth;
        int photoH = options.outHeight;
        System.out.println("MainActivity.loadImage - photo Size: ("+photoW+", "+photoH+")");

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);
        System.out.println("MainActivity.loadImage - scaleFactor: "+scaleFactor+"");

        // Decode the image file into a Bitmap sized to fill the View
        options.inJustDecodeBounds = false;
        options.inSampleSize = scaleFactor;
        options.inPurgeable = true;

        System.out.println("MainActivity.loadImage - decodeFile...");
        Bitmap image = BitmapFactory.decodeFile(full_path, options);
        System.out.println("MainActivity.loadImage - Fin (null? "+(image==null)+")");
        return image;
    }

}
