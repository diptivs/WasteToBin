package com.diptivs.android.wastetobin;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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


    /**
     * Passes the recognized speech into the query search.
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handles voice results
        if (requestCode == Constants.REQ_VOICE && resultCode == RESULT_OK) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String query = result.get(0);
            AutoCompleteTextView actv = (AutoCompleteTextView) findViewById(R.id.search);
            actv.setText(query);
        }

        //TODO implement image recognition
        // if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

        // }
    }
}
