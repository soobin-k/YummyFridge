package com.example.yummyfridge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.*;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;

import org.opencv.dnn.Dnn;
import org.opencv.utils.Converters;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.nio.Buffer;
import java.util.Random;
public class YoloActivity extends AppCompatActivity {

    final private static String TAG = "GILBOMI";
    Button btn_photo, btn_addFood;
    ImageView iv_photo;
    EditText addIngredients;
    DatePicker date;
    Spinner addType;
    final static int TAKE_PICTURE = 1;
    String mCurrentPhotoPath;
    final static int REQUEST_TAKE_PHOTO = 1;
    String yolo_name="";

    boolean startYolo = false;
    boolean firstTimeYolo = false;
    Net Yolov3;
    private final int GET_GALLERY_IMAGE = 200;

    //assets 파일 가져오기
    private static String getPath(String file, Context context) {
        AssetManager assetManager = context.getAssets();
        BufferedInputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(assetManager.open(file));
            byte[] data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();
            File outFile = new File(context.getFilesDir(), file);
            FileOutputStream os = new FileOutputStream(outFile);
            os.write(data);
            os.close();
            return outFile.getAbsolutePath();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void YOLO() {
        if (startYolo == false) {
            startYolo = true;
            if (firstTimeYolo == false) {
                firstTimeYolo = true;
                String yoloCfg = getPath("yolov3_yummy.cfg", this);
                String yoloWeights = getPath("yolov3_yummy_best.weights", this);

                Yolov3 = Dnn.readNetFromDarknet(yoloCfg, yoloWeights);
            }

        } else {
            startYolo = false;
        }
    }


    // 욜로 이미지 디텍션&화면에 출력
    public void detect_food(Bitmap bitmap){
        Mat frame = new Mat();
        Utils.bitmapToMat(bitmap, frame);

        //Imgproc을 이용해 이미지 프로세싱을 한다.
        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2RGB);//rgba 체계를 rgb로 변경
        //Imgproc.Canny(frame, frame, 100, 200);
        //Mat gray=Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY)
        Mat imageBlob = Dnn.blobFromImage(frame, 0.00392, new Size(416, 416), new Scalar(0, 0, 0),false, false);
        //뉴런 네트워크에 이미지 넣기

        Yolov3.setInput(imageBlob);

        //cfg 파일에서 yolo layer number을 확인하여 이를 순전파에 넣어준다.
        //yolv3-tiny는 yolo layer가 2개라서 initialCapacity를 2로 준다.
        java.util.List<Mat> result = new java.util.ArrayList<Mat>(3);

        List<String> outBlobNames = new java.util.ArrayList<>();
        outBlobNames.add(0, "yolo_82");
        outBlobNames.add(1, "yolo_94");
        outBlobNames.add(2, "yolo_106");

        //순전파를 진행
        Yolov3.forward(result, outBlobNames);

        //30%이상의 확률만 출력해준다.
        float confThreshold = 0.3f;

        //class id
        List<Integer> clsIds = new ArrayList<>();
        //
        List<Float> confs = new ArrayList<>();
        //draw rectanglelist
        List<Rect> rects = new ArrayList<>();


        for (int i = 0; i < result.size(); ++i) {

            Mat level = result.get(i);

            for (int j = 0; j < level.rows(); ++j) { //iterate row
                Mat row = level.row(j);
                Mat scores = row.colRange(5, level.cols());

                Core.MinMaxLocResult mm = Core.minMaxLoc(scores);


                float confidence = (float) mm.maxVal;

                //여러개의 클래스들 중에 가장 정확도가 높은(유사한) 클래스 아이디를 찾아낸다.
                Point classIdPoint = mm.maxLoc;


                if (confidence > confThreshold) {
                    int centerX = (int) (row.get(0, 0)[0] * frame.cols());
                    int centerY = (int) (row.get(0, 1)[0] * frame.rows());
                    int width = (int) (row.get(0, 2)[0] * frame.cols());
                    int height = (int) (row.get(0, 3)[0] * frame.rows());


                    int left = centerX - width / 2;
                    int top = centerY - height / 2;

                    clsIds.add((int) classIdPoint.x);
                    confs.add((float) confidence);


                    rects.add(new Rect(left, top, width, height));
                }
            }
        }
        int ArrayLength = confs.size();

        if (ArrayLength >= 1) {
            // Apply non-maximum suppression procedure.
            float nmsThresh = 0.2f;


            MatOfFloat confidences = new MatOfFloat(Converters.vector_float_to_Mat(confs));


            Rect[] boxesArray = rects.toArray(new Rect[0]);

            MatOfRect boxes = new MatOfRect(boxesArray);

            MatOfInt indices = new MatOfInt();


            Dnn.NMSBoxes(boxes, confidences, confThreshold, nmsThresh, indices);


            // Draw result boxes:
            int[] ind = indices.toArray();
            for (int i = 0; i < ind.length; ++i) {

                int idx = ind[i];
                Rect box = boxesArray[idx];

                int idGuy = clsIds.get(idx);

                float conf = confs.get(idx);


                //List<String> cocoNames = Arrays.asList("a person", "a bicycle", "a motorbike", "an airplane", "a bus", "a train", "a truck", "a boat", "a traffic light", "a fire hydrant", "a stop sign", "a parking meter", "a car", "a bench", "a bird", "a cat", "a dog", "a horse", "a sheep", "a cow", "an elephant", "a bear", "a zebra", "a giraffe", "a backpack", "an umbrella", "a handbag", "a tie", "a suitcase", "a frisbee", "skis", "a snowboard", "a sports ball", "a kite", "a baseball bat", "a baseball glove", "a skateboard", "a surfboard", "a tennis racket", "a bottle", "a wine glass", "a cup", "a fork", "a knife", "a spoon", "a bowl", "a banana", "an apple", "a sandwich", "an orange", "broccoli", "a carrot", "a hot dog", "a pizza", "a doughnut", "a cake", "a chair", "a sofa", "a potted plant", "a bed", "a dining table", "a toilet", "a TV monitor", "a laptop", "a computer mouse", "a remote control", "a keyboard", "a cell phone", "a microwave", "an oven", "a toaster", "a sink", "a refrigerator", "a book", "a clock", "a vase", "a pair of scissors", "a teddy bear", "a hair drier", "a toothbrush");
                List<String> cocoNames = Arrays.asList("carrot", "egg", "garlic", "kimchi", "leek", "onion", "pepper", "potato");
                List<String> cocoNamesKR = Arrays.asList("당근", "계란", "마늘", "김치", "대파", "양파", "고추", "감자");
                int intConf = (int) (conf * 100);

                Imgproc.putText(frame, cocoNames.get(idGuy) + " " + intConf + "%", box.tl(), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 255, 0), 2);
                addIngredients.setText(cocoNamesKR.get(idGuy));
                addType.setSelection(1);
                Imgproc.rectangle(frame, box.tl(), box.br(), new Scalar(255, 0, 0), 2);
            }

        }

        Utils.matToBitmap(frame, bitmap);
        iv_photo.setImageBitmap(bitmap);

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_item);

        //opencv 로드
        boolean load = OpenCVLoader.initDebug();
        if (load) {
            Log.i("MainActivity", "Open CV Libraries loaded...");
        } else {
            Log.i("MainActivity", "Open CV Libraries not loaded...");
        }

        //yolo 이미지 객체인식
        YOLO();

        //메인 화면으로 전환
        ImageButton btnmanager=(ImageButton)findViewById(R.id.btnmanager);
        btnmanager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent outIntent=new Intent(getApplicationContext(), MainActivity.class);
                setResult(RESULT_OK,outIntent);
                finish();
            }
        });

        //카메라 사진 촬영 소스
        iv_photo = (ImageView) findViewById(R.id.iv_photo);
        btn_photo = findViewById(R.id.btn_photo);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if(checkSelfPermission(Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) { Log.d(TAG, "권한 설정 완료"); } else { Log.d(TAG, "권한 설정 요청");
                    ActivityCompat.requestPermissions(YoloActivity.this, new String[]{
                            Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        } btn_photo.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { switch (v.getId()) {
                case R.id.btn_photo:
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, TAKE_PICTURE);
                    break; }
            }
        });


        //갤러리 사진 가져오기 소스
        Button btn_gallery = findViewById(R.id.btn_gallery);
        btn_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent. setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, GET_GALLERY_IMAGE);
            }
        });

        //식재료 이름 가져오기
        addIngredients=(EditText)findViewById(R.id.addIngredients);

        //날짜(유통기한) 가져오기
        date=(DatePicker)findViewById(R.id.addDate);
        date.init(2021, 5, 1, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker datePicker, int i, int i1, int i2) {

            }
        });

        //식재료 타입 가져오기
        addType = (Spinner)findViewById(R.id.addType);
        ArrayAdapter typeAdapter = ArrayAdapter.createFromResource(this, R.array.foodType, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        addType.setAdapter(typeAdapter);
        //스피너 초기화 date.setSelection(1);


        //식재료 추가
        btn_addFood= (Button)findViewById(R.id.btn_addFood);
        btn_addFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    MyDatabaseHelper myDB = new MyDatabaseHelper(YoloActivity.this);
                    String fulldate= date.getYear()+"/"+(date.getMonth()+1)+"/"+date.getDayOfMonth();
                    myDB.addFridge(addIngredients.getText().toString().trim(), addType.getSelectedItem().toString().trim(), fulldate.trim());
            }
        });

    }


    // 권한 요청
    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult");
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED ) {
            Log.d(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]); } }


   // 카메라로 촬영한 사진의 썸네일을 가져와 이미지뷰에 띄워줌
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case TAKE_PICTURE:
                if (resultCode == RESULT_OK && intent.hasExtra("data")) {
                    Bitmap bitmap = (Bitmap) intent.getExtras().get("data");
                    if (bitmap != null) {
                        //iv_photo.setImageBitmap(bitmap);
                        detect_food(bitmap);
                    }
                }
                break;
            case GET_GALLERY_IMAGE:
                // Make sure the request was successful
                if (resultCode == RESULT_OK) {
                    try {
                        // 선택한 이미지에서 비트맵 생성
                        InputStream in = getContentResolver().openInputStream(intent.getData());
                        Bitmap bitmap = BitmapFactory.decodeStream(in);
                        in.close();
                        detect_food(bitmap);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

        }
    }
}
