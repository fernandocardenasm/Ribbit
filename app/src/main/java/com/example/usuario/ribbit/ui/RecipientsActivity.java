package com.example.usuario.ribbit.ui;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.usuario.ribbit.R;
import com.example.usuario.ribbit.adapters.UserAdapter;
import com.example.usuario.ribbit.utilities.AlertDialogGenerator;
import com.example.usuario.ribbit.utilities.FileHelper;
import com.example.usuario.ribbit.utilities.ParseConstants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class RecipientsActivity extends ActionBarActivity {

    static public final String TAG = RecipientsActivity.class.getSimpleName();

    private List<ParseUser> mFriends;
    private ParseRelation<ParseUser> mFriendsRelation;
    private ParseUser mCurrentUser;
    private MenuItem mSendMenuItem;
    private Uri mMediaUri;
    private String mFileType;

    private GridView mGridView;

    @InjectView(R.id.loadingFriendsProgressBar) ProgressBar mFriendsRetrieveProgressBar;
    @InjectView(android.R.id.empty) TextView mEmptyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_grid);
        ButterKnife.inject(this);

        mGridView = (GridView) findViewById(R.id.friendsGrid);

        mGridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE);

        //getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        mGridView.setEmptyView(mEmptyTextView);

        mMediaUri = getIntent().getData();
        mFileType = getIntent().getExtras().getString(ParseConstants.KEY_FILE_TYPE);
        //Get the Uri from the Intent


    }

    @Override
    public void onResume() {
        super.onResume();

        loadListOfCurrentFriends();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recipients, menu);


        mSendMenuItem = menu.getItem(0);

        Log.d(TAG, "Show menu: " + menu.toString());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_send:

                //The item get unable while it sends the message
                mSendMenuItem.setEnabled(false);

                //Create the message
                ParseObject message = createMessage();
                //

                if (message == null){
                    //error
                    AlertDialogGenerator dialog = new AlertDialogGenerator();
                    dialog.showAlertDialog(RecipientsActivity.this,
                            getString(R.string.error_selecting_file),
                            getString(R.string.error_title));
                }
                else{
                    send(message);
                    finish();
                }

                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    private void loadListOfCurrentFriends() {

        mFriendsRetrieveProgressBar.setVisibility(View.VISIBLE);
        mEmptyTextView.setVisibility(View.VISIBLE);
        mEmptyTextView.setText(R.string.loading_current_friends_label);



        mCurrentUser = ParseUser.getCurrentUser();
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

        ParseQuery<ParseUser> query = mFriendsRelation.getQuery();
        query.addAscendingOrder(ParseConstants.KEY_USERNAME);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {

                mFriendsRetrieveProgressBar.setVisibility(View.INVISIBLE);




                //mLoadingFriendsProgressBar.setVisibility(View.INVISIBLE);

                //mLoadingFriendsProgressBar.setVisibility(View.INVISIBLE);
                //mEmptyTextView.setText(R.string.empty_friends_label);

                if (e == null) {
                    mFriends = friends;

                    String[] usernames = new String[mFriends.size()];

                    int i = 0;

                    for (ParseUser user : mFriends) {
                        usernames[i] = user.getUsername();
                        i++;
                    }

                    if (mFriends.size()==0){
                        mEmptyTextView.setText(R.string.empty_reciepts_list_message);
                        mEmptyTextView.setVisibility(View.VISIBLE);
                    }
                    else {
                        mEmptyTextView.setVisibility(View.INVISIBLE);
                    }

                    if (mGridView.getAdapter() == null){
                        UserAdapter adapter = new UserAdapter(RecipientsActivity.this ,mFriends);
                        mGridView.setAdapter(adapter);
                    }
                    else{
                        ((UserAdapter)mGridView.getAdapter()).refill(friends);
                    }



                    mGridView.setOnItemClickListener(mOnItemClickListener);

                } else {
                    AlertDialogGenerator dialog = new AlertDialogGenerator();
                    dialog.showAlertDialog(RecipientsActivity.this, e.getMessage(), getString(R.string.error_title));
                }

            }
        });
    }


    //Add the fields and data to the message

    protected ParseObject createMessage(){


        ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGES);
        message.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
        message.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
        message.put(ParseConstants.KEY_RECIPIENT_IDS, getRecipientIds());
        message.put(ParseConstants.KEY_FILE_TYPE, mFileType);

        byte[] fileBytes = FileHelper.getByteArrayFromFile(this, mMediaUri);

        if (fileBytes == null){
            return null;
        }
        else{
            if (mFileType.equals(ParseConstants.TYPE_IMAGE)){
                fileBytes = FileHelper.reduceImageForUpload(fileBytes);
            }

            String fileName = FileHelper.getFileName(this, mMediaUri, mFileType);
            ParseFile file = new ParseFile(fileName, fileBytes);

            message.put(ParseConstants.KEY_FILE, file);
            return message;
        }

    }

    //Get the Friend´s Ids that were selected
    protected ArrayList<String> getRecipientIds(){
        ArrayList<String> recipientIds = new ArrayList<String>();
        for (int i = 0; i < mGridView.getCount(); i++){
            if (mGridView.isItemChecked(i)){
                recipientIds.add(mFriends.get(i).getObjectId());
            }
        }

        return recipientIds;
    }

    protected void send(ParseObject message){


        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null){
                    Toast.makeText(RecipientsActivity.this, getString(R.string.success_message), Toast.LENGTH_SHORT).show();
                    sendPushNotifications();
                }
                else {
                    AlertDialogGenerator dialog = new AlertDialogGenerator();
                    dialog.showAlertDialog(RecipientsActivity.this,
                            getString(R.string.error_sending_message),
                            getString(R.string.error_title));
                }

                mSendMenuItem.setEnabled(true);
            }
        });
    }

    private void sendPushNotifications() {
        ParseQuery<ParseInstallation> query = ParseInstallation.getQuery();
        query.whereContainedIn(ParseConstants.KEY_USER_ID,getRecipientIds());

        //send the push notification

        ParsePush push = new ParsePush();

        push.setQuery(query);

        push.setMessage(getString(R.string.push_message, ParseUser.getCurrentUser().getUsername()));
        push.sendInBackground();
    }

    protected AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            ImageView checkImageView = (ImageView) view.findViewById(R.id.checkImageView);

            if (mGridView.getCheckedItemCount() > 0) {
                mSendMenuItem.setVisible(true);

            } else {
                mSendMenuItem.setVisible(false);
            }

            if (mGridView.isItemChecked(position)){
                //Checked a friend
                checkImageView.setVisibility(View.VISIBLE);
            }
            else{
                //Unchecked a friend
                checkImageView.setVisibility(View.INVISIBLE);
            }
        }
    };
}
