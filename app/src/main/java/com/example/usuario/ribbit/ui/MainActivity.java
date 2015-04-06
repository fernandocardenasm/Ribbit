package com.example.usuario.ribbit.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.usuario.ribbit.utilities.ParseConstants;
import com.example.usuario.ribbit.R;
import com.example.usuario.ribbit.adapters.SectionsPagerAdapter;
import com.parse.ParseUser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    public static final int TAKE_PHOTO_REQUEST = 0;
    public static final int TAKE_VIDEO_REQUEST = 1;
    public static final int PICK_PHOTO_REQUEST = 2;
    public static final int PICK_VIDEO_REQUEST = 3;

    public static final int MEDIA_TYPE_IMAGE = 4;
    public static final int MEDIA_TYPE_VIDEO= 5;

    public static final int FILE_SIZE_LIMIT = 1024*1024*10; // 10 MB

    private Uri mMediaUri;


    //Set which action will be triggered depending on which option is clicked
    private DialogInterface.OnClickListener mDialogListener =
            new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case 0: //Take picture Option
                    Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    //Save Image
                    mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

                    if (mMediaUri == null){
                        //Display an error
                        Toast.makeText(MainActivity.this, getString(R.string.error_accessing_external_storage), Toast.LENGTH_LONG).show();
                    }
                    else{
                        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);

                        //Get the result of the Intent
                        startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST);
                    }


                    break;
                case 1: //Take video

                    Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);

                    if (mMediaUri == null){
                        //Display an error
                        Toast.makeText(MainActivity.this, getString(R.string.error_accessing_external_storage), Toast.LENGTH_LONG).show();
                    }
                    else{
                        videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);

                        //Limit the duration to 10 seconds
                        videoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
                        //Set Low Quality video
                        videoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);

                        //Get the result of the Intent
                        startActivityForResult(videoIntent, TAKE_VIDEO_REQUEST);
                    }
                    break;
                case 2: //Choose Picture
                    Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    choosePhotoIntent.setType("image/*");
                    startActivityForResult(choosePhotoIntent, PICK_PHOTO_REQUEST);
                    break;
                case 3: //Choose Video
                    Intent chooseVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    chooseVideoIntent.setType("video/*");
                    //Warn the user that the file should be at most 10 MB
                    Toast.makeText(MainActivity.this, getString(R.string.warning_video_size_limit), Toast.LENGTH_LONG).show();
                    startActivityForResult(chooseVideoIntent, PICK_VIDEO_REQUEST);
                    break;
            }
        }

        private Uri getOutputMediaFileUri(int mediaType) {
            //To be safe, you should check that the SDCard is mounted
            //using Environment.getExternalStorageState() before doing this

            if (isExternalStorageAvailable()){
                //get the URI

                //1. Get the external  storage directory

                String appName = MainActivity.this.getString(R.string.app_name);

                File mediaStorageDir = new File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        appName);
                //2. Create our subdirectory

                if (! mediaStorageDir.exists()){
                    if (! mediaStorageDir.mkdirs()){
                        Log.e(TAG, getString(R.string.error_failed_to_create_directory));
                        return null;
                    }
                }
                //3. Create a filename
                //4. Create the file

                File mediaFile;
                Date now = new Date();
                String timestap = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(now);

                String path = mediaStorageDir.getPath() + File.separator;

                if (mediaType == MEDIA_TYPE_IMAGE){
                    mediaFile = new File(path + "IMG" + timestap + ".jpg");
                }
                else if (mediaType == MEDIA_TYPE_VIDEO){
                    mediaFile = new File(path + "VID" + timestap + ".mp4");
                }
                else{
                    return null;
                }

                Log.d(TAG, "File: " + Uri.fromFile(mediaFile));
                //5. Create the file's Uri
                return Uri.fromFile(mediaFile);
            }
            else{
                return null;
            }


        }

        private boolean isExternalStorageAvailable(){
            String state = Environment.getExternalStorageState();

            if ( state.equals(Environment.MEDIA_MOUNTED)){
                return true;
            }
            else{
                return false;
            }
        }
    };



    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ParseUser currentUser = ParseUser.getCurrentUser();

        if (currentUser == null){
            navigateToLogin();
        }
        else{
            Log.i(TAG, currentUser.getUsername());
        }



        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setIcon(mSectionsPagerAdapter.getIcon(i))
                            .setTabListener(this));
        }


    }


    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);

        //When the user clicks "back", he wont come back to the main layout

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
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
        switch (id){
            case R.id.action_logout:
                ParseUser.logOut();
                navigateToLogin();
                break;
            case R.id.action_edit_friends:
                Intent intent = new Intent(this, EditFriendsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_camera:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setItems(R.array.camera_choices, mDialogListener);
                AlertDialog dialog = builder.create();
                dialog.show();

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){


            if (requestCode == PICK_PHOTO_REQUEST || requestCode == PICK_VIDEO_REQUEST){
                //Get the Uri from the Gallery
                if (data == null){
                    Toast.makeText(this, R.string.general_error, Toast.LENGTH_LONG ).show();
                }
                else{
                    mMediaUri = data.getData();
                }
                Log.i(TAG, "Media URI: " + mMediaUri);
                if (requestCode == PICK_VIDEO_REQUEST){
                    //Make sure the file is less than 10 MB
                    int fileSize = 0;

                    InputStream inputStream = null;

                    try {
                        inputStream = getContentResolver().openInputStream(mMediaUri);
                        fileSize = inputStream.available();
                    }
                    catch (FileNotFoundException e){
                        Toast.makeText(this, getString(R.string.error_opening_file), Toast.LENGTH_LONG ).show();
                        return;
                    }catch (IOException e){
                        Toast.makeText(this, getString(R.string.error_opening_file), Toast.LENGTH_LONG ).show();
                        return;
                    }
                    finally {
                        try {
                            inputStream.close();
                        } catch (IOException e) {/*Intentionally blank*/
                            e.printStackTrace();
                        }
                    }
                    if (fileSize >= FILE_SIZE_LIMIT){
                        Toast.makeText(this, getString(R.string.error_file_size_too_large), Toast.LENGTH_LONG ).show();
                        return;
                    }
                }
            }
            else{
                //Add it to the gallery
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(mMediaUri);
                sendBroadcast(mediaScanIntent);
            }

            Intent recieptsIntent = new Intent(this, RecipientsActivity.class);
            recieptsIntent.setData(mMediaUri);

            String fileType="";

            if (requestCode == PICK_PHOTO_REQUEST || requestCode == TAKE_PHOTO_REQUEST){
                fileType = ParseConstants.TYPE_IMAGE;
            }
            else if (requestCode == PICK_VIDEO_REQUEST || requestCode == TAKE_VIDEO_REQUEST){
                fileType = ParseConstants.TYPE_VIDEO;
            }
            recieptsIntent.putExtra(ParseConstants.KEY_FILE_TYPE, fileType);
            startActivity(recieptsIntent);


        }
        else if (resultCode != RESULT_CANCELED){
            Toast.makeText(this, R.string.general_error, Toast.LENGTH_LONG ).show();
        }
    }
}