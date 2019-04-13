package com.diptivs.android.wastetobin;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions;
import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabel;
import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabelDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private PopupWindow popupWindow;
    public static Bitmap bitmapImage; // User image
    private ImageView imageDisplay; // Displayed Image
    private FirebaseVisionImage imageFirebase; // Firebase Vision Image
    private ListView textPrediction; // Predicted names
    private List<String> totalPredictionList = new ArrayList<>();
    private String predict;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bitmapImage = BitmapFactory.decodeResource(getResources(), R.drawable.what_goes_where);
        bitmapImage = resizeImage(bitmapImage);

        // Image View
        imageDisplay = (ImageView) findViewById(R.id.imageView);
        imageDisplay.setImageBitmap(bitmapImage);

        textPrediction = (ListView) findViewById(R.id.textViewPrediction);

        this.initiateDatabase();
        // fills the auto-complete selections
        List<String> autoCompleteList = Database.getInstance().getTotalList();
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, autoCompleteList);

        AutoCompleteTextView actv = (AutoCompleteTextView) findViewById(R.id.search);
        actv.setAdapter(adapter);
        // receives query if coming from database activity
        Intent intent = getIntent();
        if (intent.getStringExtra(DatabaseActivity.EXTRA_ITEM) != "") {
            actv.setText(intent.getStringExtra(DatabaseActivity.EXTRA_ITEM));
        }
    }

    private Bitmap resizeImage(Bitmap image) {
        float aspectRatio = image.getWidth() /
                (float) image.getHeight();
        int width = 480;
        int height = Math.round(width / aspectRatio);

        image = Bitmap.createScaledBitmap(
                image, width, height, false);

        return image;
    }

    public void initiateFileInDatabase(String filePath, Constants.Categories itemType) {
        try {
            Database.getInstance().retrieveInformationFromCategoryFile(getApplicationContext().getAssets().open(filePath), itemType);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void displayDatabase(View view) {
        Intent intent = new Intent(this, DatabaseActivity.class);
        startActivity(intent);
    }

    public void initiateDatabase() {
        String pathInAssetsFolder = null;
        for (int i = 0; i < Constants.supportedCategoriesLanguages.length; i++) {
            // For every supported language we should have a translation of each object which goes into
            // the specified categories in Constants.Categories. Also since we're storing all of this information
            // locally, we'll be storing collecting this from informatino from the android assets folder.
            pathInAssetsFolder = String.format("categories/%s/Blue.txt", Constants.supportedCategoriesLanguages[i]);
            this.initiateFileInDatabase(pathInAssetsFolder, Constants.Categories.BLUE_BIN);

            pathInAssetsFolder = String.format("categories/%s/BTTSWD.txt", Constants.supportedCategoriesLanguages[i]);
            this.initiateFileInDatabase(pathInAssetsFolder, Constants.Categories.BRING_TO_TRANSFER_STATION_OR_WASTE_DEPOT);

            pathInAssetsFolder = String.format("categories/%s/EWaste.txt", Constants.supportedCategoriesLanguages[i]);
            this.initiateFileInDatabase(pathInAssetsFolder, Constants.Categories.E_WASTE);

            pathInAssetsFolder = String.format("categories/%s/Green.txt", Constants.supportedCategoriesLanguages[i]);
            this.initiateFileInDatabase(pathInAssetsFolder, Constants.Categories.GREEN_BIN);

            pathInAssetsFolder = String.format("categories/%s/Grey.txt", Constants.supportedCategoriesLanguages[i]);
            this.initiateFileInDatabase(pathInAssetsFolder, Constants.Categories.GREY_BIN);

            pathInAssetsFolder = String.format("categories/%s/HHW.txt", Constants.supportedCategoriesLanguages[i]);
            this.initiateFileInDatabase(pathInAssetsFolder, Constants.Categories.HOUSEHOLD_HAZARDOUS_WASTE);

            pathInAssetsFolder = String.format("categories/%s/OW.txt", Constants.supportedCategoriesLanguages[i]);
            this.initiateFileInDatabase(pathInAssetsFolder, Constants.Categories.OVERSIZED_WASTE);

            pathInAssetsFolder = String.format("categories/%s/SM.txt", Constants.supportedCategoriesLanguages[i]);
            this.initiateFileInDatabase(pathInAssetsFolder, Constants.Categories.SCRAP_METAL);

            pathInAssetsFolder = String.format("categories/%s/YW.txt", Constants.supportedCategoriesLanguages[i]);
            this.initiateFileInDatabase(pathInAssetsFolder, Constants.Categories.YARD_WASTE);
        }
        Database.getInstance().sortTotalList();
    }

    public void showImageResult(String bin) {
        // builds pop-up window
        LayoutInflater layoutInflater =
                (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.result_popup, null);
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        // displays result bin
        TextView resultText = (TextView) popupView.findViewById(R.id.resultText);
        resultText.setText(bin);
        bin = bin.replace(' ', '_');
        // gets image from assets folder
        InputStream is = null;
        try {
            is = getAssets().open("images/img_" + bin + ".png");
        } catch (IOException ioException) {
            System.out.println("images/img_" + bin + ".png");
        }
        ImageView result = (ImageView) popupView.findViewById(R.id.resultImg);
        result.setImageBitmap(BitmapFactory.decodeStream(is));
        // initializes dismiss button for pop-up
        Button btnDismiss = (Button) popupView.findViewById(R.id.dismiss);
        btnDismiss.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
        popupWindow.setBackgroundDrawable(null);
        AutoCompleteTextView actv = (AutoCompleteTextView) findViewById(R.id.search);
        // displays the pop-up
        popupWindow.showAsDropDown(actv, 0, 0);
    }


    /**
     * Searches the database for query. If found, displays an image for the resulting bin. If not,
     * displays possible items from the database close to the initial query.
     *
     * @param query Item to be searched for.
     */
    public void showResult(String query) {
        Log.d(Constants.TAGDipti,"showResult: "+query);
        int duration = Toast.LENGTH_LONG;
        Context context = getApplicationContext();
        String bin = Database.getInstance().initialQuery(query);
        Toast toast = Toast.makeText(context, bin, duration);
        // The user query is found within the database
        if (bin != null) {
            toast.show();
            showImageResult(bin);
            MediaPlayer mp = MediaPlayer.create(context, R.raw.ding);
            mp.start();
        } else { // attempt to give relevant suggestions
            // finds the most relevant matches in the database
            List<String> suggestions = Database.getInstance().secondaryQuery(query);
            if (suggestions == null) {
                bin = "no bin";
                toast = Toast.makeText(context, bin, duration);
            } else {
                bin = "";
                for (int i = 0; i < suggestions.size(); i++) {
                    bin += "Did you mean:" + suggestions.get(i) + "?\n";
                }
                toast = Toast.makeText(context, bin, duration);
            }
        }
        toast.show();
    }

    private void hideSoftKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        // verify if the soft keyboard is open
        if (imm.isAcceptingText()) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    public void getQuery(View view) {
        EditText searchQuery = (EditText) findViewById(R.id.search);
        CharSequence text = searchQuery.getText();
        // handles queries
        String query = text.toString();
        Log.d(Constants.TAGDipti,"getQuery: "+query);
        if (popupWindow != null) {
            popupWindow.dismiss();
        }
        // The user has entered something!
        // so now we work with their query
        if (!query.equals("")) {
            showResult(query);
        }
        hideSoftKeyBoard();
        bitmapImage = BitmapFactory.decodeResource(getResources(), R.drawable.what_goes_where);
        bitmapImage = resizeImage(bitmapImage);
        imageDisplay.setImageBitmap(bitmapImage);

    }

    public void voice(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
        try {
            startActivityForResult(intent, Constants.REQ_VOICE);
        } catch (Exception exception) {
            Toast.makeText(this, "Error initializing speech to text engine.", Toast.LENGTH_LONG).show();
        }
    }

    public void camera(View view) {
        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    Constants.MY_CAMERA_REQUEST_CODE);
        }
        Intent iCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try{
            if (iCamera.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(iCamera, Constants.REQ_CAPTURE_IMAGE);
            }
        }catch (Exception exception) {
            Toast.makeText(this, "Error initializing Camera Capture.", Toast.LENGTH_LONG).show();
        }

    }


    /**
     * Passes the recognized speech into the query search.
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == RESULT_OK)
        {
            // handles voice results
            if (requestCode == Constants.REQ_VOICE) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String query = result.get(0);
                AutoCompleteTextView actv = (AutoCompleteTextView) findViewById(R.id.search);
                actv.setText(query);
            }

            if (requestCode == Constants.REQ_CAPTURE_IMAGE){
                //PHOTO FROM CAMERA
                bitmapImage = (Bitmap) data.getExtras().get("data");
                imageDisplay.setImageBitmap(bitmapImage);
                bitmapImage = resizeImage(bitmapImage);
                imageFirebase = FirebaseVisionImage.fromBitmap(bitmapImage);
                textPrediction.setVisibility(View.VISIBLE);
                textPrediction.setFastScrollEnabled(true);
                textPrediction.setFilterText("Loading ...");
                labelImagesCloud(imageFirebase);
            }

        }

    }

    private void labelImagesCloud(FirebaseVisionImage image) {

        // [START set_detector_options_cloud]
        FirebaseVisionCloudDetectorOptions options = new FirebaseVisionCloudDetectorOptions.Builder()
                .setModelType(FirebaseVisionCloudDetectorOptions.LATEST_MODEL)
                .setMaxResults(5)
                .build();
        // [END set_detector_options_cloud]

        // [START get_detector_cloud]
        FirebaseVisionCloudLabelDetector detector = FirebaseVision.getInstance()
                .getVisionCloudLabelDetector(options);
        // [END get_detector_cloud]
        // [START run_detector_cloud]
        Task<List<FirebaseVisionCloudLabel>> result =
                detector.detectInImage(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<FirebaseVisionCloudLabel>>() {
                                    @Override
                                    public void onSuccess(List<FirebaseVisionCloudLabel> labels) {
                                        // Task completed successfully
                                        // [START_EXCLUDE]
                                        // [START get_labels_cloud]

                                        for (FirebaseVisionCloudLabel label : labels) {
                                            predict="";
                                            String text = label.getLabel();
                                            Log.d(Constants.TAGDipti, text);
                                            String entityId = label.getEntityId();
                                            float confidence = label.getConfidence();
                                            predict = text + " " + String.format("%.3f", confidence) + "\n";
                                            totalPredictionList.add(predict);
                                        }

                                        final ListView listView = (ListView) findViewById(R.id.textViewPrediction);
                                        Log.d(Constants.TAGDipti,totalPredictionList.toString());
                                        listView.setVisibility(View.VISIBLE);
                                        listView.setFastScrollEnabled(true);
                                        IndexerAdapter<String> adapter = new IndexerAdapter<String>(getApplicationContext(),
                                                android.R.layout.simple_list_item_1, totalPredictionList);
                                        listView.setAdapter(adapter);
                                        // listens for a selection
                                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                System.out.println(parent.getItemAtPosition(position));
                                                String temp = (parent.getItemAtPosition(position)).toString();
                                                String query = temp.substring(0, temp.lastIndexOf(" "));

                                                AutoCompleteTextView actv = (AutoCompleteTextView) findViewById(R.id.search);
                                                actv.setText(query);

                                                listView.setVisibility(View.GONE);
                                            }
                                        });
                                        // [END get_labels_cloud]
                                        // [END_EXCLUDE]
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        predict="";
                                        predict = "An unsuccessful attempt to connect to the server. Check your Internet connection.";
                                        totalPredictionList.add(predict);
                                    }
                                });
        // [END run_detector_cloud]


    }
}
