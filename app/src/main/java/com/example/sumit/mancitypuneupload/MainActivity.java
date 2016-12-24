package com.example.sumit.mancitypuneupload;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.kosalgeek.android.photoutil.CameraPhoto;
import com.kosalgeek.android.photoutil.GalleryPhoto;
import com.kosalgeek.android.photoutil.ImageBase64;
import com.kosalgeek.android.photoutil.ImageLoader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getName();
    private EditText feedstatus,location;
    String ext;
    private ProgressDialog progressBar;
    Snackbar snackbar;
    ImageView ivCamera, ivGallery, ivUpload, ivImage,rotate;
    Bitmap bitmap; String encodeImage;
    CameraPhoto cameraPhoto;
    float rot=90; int click = 0;
    GalleryPhoto galleryPhoto;
    String photoPath;



    String selectedPhoto;
    final int CAMERA_REQUEST = 13323;
    final int GALLERY_REQUEST = 22131;
    private final static String URL = "http://servername/manchestercityclub/upload.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//PROGRESS BAR
        progressBar = new ProgressDialog(this);
        progressBar.setMessage("File Uploading ...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);




        cameraPhoto = new CameraPhoto(getApplicationContext());
        galleryPhoto = new GalleryPhoto(getApplicationContext());


        ivImage = (ImageView)findViewById(R.id.ivImage);
        ivCamera = (ImageView)findViewById(R.id.ivCamera);
        ivGallery = (ImageView)findViewById(R.id.ivGallery);
        ivUpload = (ImageView)findViewById(R.id.ivUpload);
        rotate=(ImageView)findViewById(R.id.rotate);
        feedstatus=(EditText)findViewById(R.id.feedstatus);
        location=(EditText)findViewById(R.id.location);


        rotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                click++;

                if(click == 0){
                    ivImage.setImageBitmap(rotateBitmap(bitmap,0));
                }
                else if(click ==1){
                    ivImage.setImageBitmap(rotateBitmap(bitmap,90));
                }
                else if(click == 2){
                    ivImage.setImageBitmap(rotateBitmap(bitmap,180));
                }
                else if(click == 3){
                    ivImage.setImageBitmap(rotateBitmap(bitmap,270));
                }
                else if(click == 4){
                    ivImage.setImageBitmap(rotateBitmap(bitmap,360));
                    click = 0;
                }

            }
        });
        ivCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    startActivityForResult(cameraPhoto.takePhotoIntent(), CAMERA_REQUEST);
                    cameraPhoto.addToGallery();
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(),
                            "Something Wrong while taking photos", Toast.LENGTH_SHORT).show();
                }
            }
        });
        ivUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    if(photoPath == "" || photoPath == null){
                        Toast.makeText(getApplicationContext(),"Select Image ",Toast.LENGTH_SHORT).show();
                    }
                    else {

                        Bitmap bitmap = ImageLoader.init().from(selectedPhoto).requestSize(512, 512).getBitmap();
                        encodeImage = ImageBase64.encode(bitmap);

                        sendRequest();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        ivGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(galleryPhoto.openGalleryIntent(), GALLERY_REQUEST);
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){

            if(requestCode == CAMERA_REQUEST){
                photoPath = cameraPhoto.getPhotoPath();
                ext = photoPath.substring(photoPath.lastIndexOf(".") + 1, photoPath.length());
                selectedPhoto = photoPath;
                try {

                    bitmap = ImageLoader.init().from(photoPath).requestSize(1024, 1024).getBitmap();
                    ivImage.setImageBitmap(rotateBitmap(bitmap,90));

                } catch (FileNotFoundException e) {

                    Toast.makeText(getApplicationContext(),
                            "Something Wrong while loading photos", Toast.LENGTH_SHORT).show();
                }

            }
            else if(requestCode == GALLERY_REQUEST){
                Uri uri = data.getData();

                galleryPhoto.setPhotoUri(uri);
                photoPath = galleryPhoto.getPath();
                ext = photoPath.substring(photoPath.lastIndexOf(".") + 1, photoPath.length());
                selectedPhoto = photoPath;
                try {

                    bitmap = ImageLoader.init().from(photoPath).requestSize(1024, 1024).getBitmap();
                    ivImage.setImageBitmap(bitmap);

                } catch (FileNotFoundException e) {

                    Toast.makeText(getApplicationContext(),
                            "Something Wrong while choosing photos", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private void sendRequest(){
        progressBar.show();
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String response = null;

        final String finalResponse = response;

        StringRequest postRequest = new StringRequest(Request.Method.POST, URL,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                        progressBar.hide();
                        ivImage.setImageDrawable(null);
                        snackbar = Snackbar.make(findViewById(android.R.id.content), " Uploaded File to the Server ", Snackbar.LENGTH_LONG);
                        snackbar.show();

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("ErrorResponse", finalResponse);
                        progressBar.hide();

                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("price", encodeImage);
                params.put("map", location.getText().toString());
                params.put("ext", ext);
                params.put("text", feedstatus.getText().toString());


                return params;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(postRequest);




    }
    private Bitmap rotateBitmap(Bitmap source ,float angle){
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap bitmap1 = Bitmap.createBitmap(source,0,0,source.getWidth(),source.getHeight(),matrix,true);
        return bitmap1;

    }
}
