package com.example.fufamilydeskwin7.svmofimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.CvSVM;
import org.opencv.ml.CvSVMParams;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "OCVSample::Activity";
    private int imgcount=1,filenumber=1;

    private String[] filename = {"純地面(平地)", "含非地面", "含非地面/包含兩種以上的非地面物", "含非地面/柱子", "含非地面/腳照片"};
    private int[] maximgcount = {56, 5, 2, 7, 15};
    private int[] maxIMAGcount = {8, 5, 0, 1, 3};
    private float[][] all_image_value;
    StringBuilder valuetext = new StringBuilder();


    private TextView textView, valuetextview;
    private Button btn,addone,learn;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        btn = (Button) findViewById(R.id.BTN);
        addone = (Button) findViewById(R.id.add_one);
        learn = (Button) findViewById(R.id.Learn);
        imageView = (ImageView) findViewById(R.id.imageView);
        textView = (TextView) findViewById(R.id.textView);
        valuetextview = (TextView) findViewById(R.id.textView2);

        btn.setOnClickListener(this);
        addone.setOnClickListener(this);
        learn.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        if (v == btn) {
            String DataDirectory = Environment.getDataDirectory().toString();
            Log.i(TAG, "Have sdcard ?" + String.valueOf(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)));
            Log.i(TAG, "getDataDirectory(): " + DataDirectory);

            Log.i(TAG, "root: " + Environment.getRootDirectory().toString());
            Log.i(TAG, "sdcard: " + Environment.getExternalStorageDirectory().toString());

//            img.recycle();
//            System.gc();
        }else if (v == addone) {
            imgcount++;
            if (imgcount > 56) {
                imgcount = 1;
            }
        }else if (v == learn) {
            if (checkSDCard()) {
                learn.setEnabled(false);
                all_image_value=new float[200][5];
                valuetext.append("                  mask  G7_C80100  G11_C80100\n");
                Mat imgmat = new Mat();

//                imgmat = readimagg("img", filename[2], 1, maximgcount[2]);
//                getimagevalue(imgmat);
//                Bitmap bm = Bitmap.createBitmap(320, 240, Bitmap.Config.ARGB_8888);
//                Utils.matToBitmap(imgmat, bm);
//                imageView.setImageBitmap(bm);

                for (int folder = 0; folder <5; folder++) {
                    for (int number = 1; number <= maximgcount[folder]; number++) {
                        Log.i(TAG, filename[folder] + "img " + String.valueOf(number) + ".jpg");
                        valuetext.append(filename[folder] + "img " + String.valueOf(number) + ".jpg      ");

                        imgmat = readimagg("img", filename[folder], number, maximgcount[folder]);
                        if (imgmat != null) {
                            if (filename[folder] == "純地面(平地)") {
                                getimagevalue(imgmat, -1);

                            } else {
                                getimagevalue(imgmat, 1);

                            }
                        }
                        valuetext.append("\n");
                    }
                    for (int number = 1; number <= maxIMAGcount[folder]; number++) {
                        Log.i(TAG, filename[folder] + "IMAG " + String.valueOf(number) + ".jpg");
                        valuetext.append(filename[folder] + "IMAG " + String.valueOf(number) + ".jpg      ");

                        imgmat = readimagg("IMAG", filename[folder], number, maxIMAGcount[folder]);
                        if (imgmat != null) {
                            if (filename[folder] == "純地面(平地)") {
                                getimagevalue(imgmat, -1);

                            } else {
                                getimagevalue(imgmat, 1);
                            }

                        }
                        valuetext.append("\n");
                    }
                }
                StringBuilder imagevaluetext=new StringBuilder();
                for (int i = 0; i < 200; i++) {
                    imagevaluetext.append(String.valueOf(all_image_value[i][0])+"  ");
                    imagevaluetext.append(String.valueOf(all_image_value[i][1])+"  ");
                    imagevaluetext.append(String.valueOf(all_image_value[i][2])+"  ");
                    imagevaluetext.append(String.valueOf(all_image_value[i][3])+"  ");
                    imagevaluetext.append(String.valueOf(all_image_value[i][4])+"  ");
                    imagevaluetext.append("\n");
                }


                textView.setText(valuetext);
                valuetextview.setText(imagevaluetext);

//                Log.i(TAG, "output to file finish");
//                writeToFile("train.txt", valuetext.toString());
                Mat trainingDataMat, responsesMat;
                Log.i(TAG, "onClick");
                trainingDataMat = new Mat(10, 2, CvType.CV_32FC1);
                Log.i(TAG, "new Mat");
                for (int row = 0; row <= filenumber; row++) {
                    for (int col = 0; col <= 2; col++) {
                        //,put(int row, int col, data)
                        trainingDataMat.put(row, col, all_image_value[row+1][col+2]);

                    }
                }
                Log.i(TAG, "tain to Mat finish");
                responsesMat = new Mat(10, 1, CvType.CV_32FC1);
                Log.i(TAG, "new Mat-responsesMat");
                for (int row = 0; row <= filenumber; row++) {
                    for (int col = 0; col < 1; col++) {

                        //,put(int row, int col, data)
                        responsesMat.put(row,col, all_image_value[row+1][1]);
                    }
                }
                Log.i(TAG, "labels to Mat finish");
//        svm = new CvSVM(trainingDataMat, responsesMat);
                CvSVM svm;
                CvSVMParams params;
                svm = new CvSVM();
                params = new CvSVMParams();
                Log.i(TAG, "new CvSVMParams");
                //設定CvSVMParams
                params.set_svm_type(CvSVM.C_SVC);
                params.set_kernel_type(CvSVM.LINEAR);
                params.set_term_crit(new TermCriteria(TermCriteria.MAX_ITER, 100, 1e-6));
                Log.i(TAG, "params finish");

                Log.i(TAG, "new CvSVM");
                //做 SVM 訓練
                svm.train(trainingDataMat, responsesMat, new Mat(), new Mat(), params);

//        svm.train(trainingDataMat, responsesMat);
                Log.i(TAG, "SVM train OK");





                learn.setEnabled(true);
            }
        }


    }
    private void writeToFile(String Filename, String data) {
        Log.i(TAG, "write to file star");
//        try {
//            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.txt", Context.MODE_PRIVATE));
//            outputStreamWriter.write(data);
//            outputStreamWriter.close();
//        }
//        catch (IOException e) {
//            Log.e("Exception", "File write failed: " + e.toString());
//        }


//        String filename = "myfile";
//        String string = "Hello world!";
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(Filename, Context.MODE_PRIVATE);
            outputStream.write(data.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, "write to file finish");
    }
    public static Mat readimagg(String type,String filename, int count, int maxcount) {
        Bitmap imgbm;
        Mat readimageMat = new Mat();
        String imagefile = Environment.getExternalStorageDirectory().toString() + "/104專題/蒐集的照片(重新命名版)/" + filename + "/" + type + " " + String.valueOf(count) + ".jpg";
        File file = new File(imagefile);
        if (count <= maxcount && file.exists()) {

            imgbm = BitmapFactory.decodeFile(imagefile);
            Utils.bitmapToMat(imgbm, readimageMat);
            imgbm.recycle();
            System.gc();
            Imgproc.resize(readimageMat, readimageMat, new Size(320, 240));
//            imageView.setImageBitmap(img);
            return readimageMat;
        } else {
            return null;
        }

    }
    public void getimagevalue(Mat orgimageMat,float TorF) {
        Log.i(TAG, "getimagevalue star");

        int HSV_Spoint_value, G7_C80100point_value, G11_C80100point_value;
        Mat halforg = orgimageMat.submat(0, orgimageMat.height(), 0, orgimageMat.width() / 3);

        Mat dst = new Mat();

//        valuetext.append("hsv_h,hsv_s,hsv_v,rgbcuthsv_s,G7_C80100, G11_C80100\n\n");
//        valuetext.append("Data ID : ").append(Dataname).append("\n");
        //HSV---------------------------------------------------------------------------
//        Log.i(TAG, "HSV: star");
        Mat hsv = new Mat();
        Mat hsv_s = new Mat();
        Mat mask_s = new Mat(halforg.size(), CvType.CV_8UC1);
//        Log.i(TAG, "HSV: RGB2HSV");
        Imgproc.cvtColor(halforg, hsv, Imgproc.COLOR_RGB2HSV);
//            Core.split(makeMat, 1);
//        Log.i(TAG, "HSV: take one channel");
        //HSV_H---------------------------------------------------------------------------
        Mat hsv_h = new Mat();
        Core.extractChannel(hsv, hsv_h, 0);
        Imgproc.cvtColor(hsv_h, hsv_h, Imgproc.COLOR_GRAY2RGBA);
        //HSV_S---------------------------------------------------------------------------
        //多層矩陣轉換成幾個單一矩陣
        Core.extractChannel(hsv, hsv_s, 1);
//        Log.i(TAG, "HSV: do mask");
        Core.inRange(hsv_s, new Scalar(76), new Scalar(255), mask_s);
//        Log.i(TAG, "HSV: copy to");

        hsv_s.copyTo(hsv_s, mask_s); //將原圖片經由遮罩過濾後，得到結果dst
        Imgproc.cvtColor(hsv_s, hsv_s, Imgproc.COLOR_GRAY2RGBA);
//        Log.i(TAG, "HSV: finish!");
        //output Mat is hsv_s
        //HSV_V---------------------------------------------------------------------------
        Mat hsv_v = new Mat();
        Core.extractChannel(hsv, hsv_v, 2);
        Imgproc.cvtColor(hsv_v, hsv_v, Imgproc.COLOR_GRAY2RGBA);


        //RGB cut HSV_S--------------------------------------------------------------------
//        Log.i(TAG, "RGB cut HSV_s: star");
        Mat rgbcuthsv_s = new Mat();
//        Log.i(TAG, "RGB cut HSV_s: copy to");
        halforg.copyTo(rgbcuthsv_s, mask_s);
//        Log.i(TAG, "RGB cut HSV_s: finish");
        Scalar maskvalue = Core.sumElems(mask_s);

//            valuetext.append("mask count: " + String.valueOf(maskvalue));
//            valuetext.append("\nmask count: " + maskvalue.toString());
        HSV_Spoint_value = Core.countNonZero(mask_s);
        valuetext.append(String.valueOf(HSV_Spoint_value) + "  ");
        //output Mat is rgbcuthsv_s

        //Gauss and Canny  ==================================================================
        //Gauss3 Canny(80,100)
        Mat G7_C80100 = new Mat();
//        Log.i(TAG, "Gauss: star");
        Imgproc.GaussianBlur(halforg, G7_C80100, new Size(5, 5), 3, 3);
//
//        Log.i(TAG, "Canny: star");
        Imgproc.Canny(G7_C80100, G7_C80100, 80, 100);
//            Log.i(TAG, "Canny: do canny count");
//            cannycount1 = Core.countNonZero(makeMat);
//            countT.setText("canny count" + String.valueOf(cannycount1));
        G7_C80100point_value = Core.countNonZero(G7_C80100);
        valuetext.append(String.valueOf(G7_C80100point_value) + "  ");
        Imgproc.cvtColor(G7_C80100, G7_C80100, Imgproc.COLOR_GRAY2RGBA);
//        Log.i(TAG, "Canny: canny finish");
        //Scalar G7_C80100value= Core.sumElems(G7_C80100);

        //output Mat is G7_C80100

        //Gauss5 Canny(80,100)
        Mat G11_C80100 = new Mat();
//        Log.i(TAG, "Gauss: star");
        Imgproc.GaussianBlur(halforg, G11_C80100, new Size(11, 11), 3, 3);

//        Log.i(TAG, "Canny: star");
        Imgproc.Canny(G11_C80100, G11_C80100, 80, 100);
//            Log.i(TAG, "Canny: do canny count");
        G11_C80100point_value = Core.countNonZero(G11_C80100);
        valuetext.append(String.valueOf(G11_C80100point_value) + "  ");
        Imgproc.cvtColor(G11_C80100, G11_C80100, Imgproc.COLOR_GRAY2RGBA);
//        Log.i(TAG, "Canny: canny finish");

        //output Mat is G11_C80100

        //============================================================
//            Mat space =  Mat.zeros(halforg.width(), halforg.height(), CvType.CV_8UC1);
//            Imgproc.cvtColor(space, space, Imgproc.COLOR_GRAY2RGBA);

//        Log.i(TAG, "hconcat: star new list");
//        List<Mat> src = Arrays.asList(hsv_h, hsv_s, hsv_v, rgbcuthsv_s, G7_C80100, G11_C80100);
//        Log.i(TAG, "hconcat: do hconcat");
//        Core.hconcat(src, dst);
//        Log.i(TAG, "hconcat: finish!");
        all_image_value[filenumber][0]=filenumber;
        all_image_value[filenumber][1]=TorF;
        all_image_value[filenumber][2]=(float)HSV_Spoint_value;
        all_image_value[filenumber][3]=(float)G7_C80100point_value;
        all_image_value[filenumber][4]=(float)G11_C80100point_value;
        filenumber++;
        Log.i(TAG, "getimagevalue finish");
    }

    //確認是否有插入SDCard
    private static boolean checkSDCard() {
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
            return true;
        }
        return false;
    }
    //OpenCV类库加载并初始化成功后的回调函数，在此我们不进行任何操作
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            Log.i(TAG, "onManagerConnected star ");
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");

                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };
    /** Call on every application resume **/
    @Override
    protected void onResume()
    {
        Log.i(TAG, "Called onResume");
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, mLoaderCallback);

        Log.i(TAG, " onResume OK");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
