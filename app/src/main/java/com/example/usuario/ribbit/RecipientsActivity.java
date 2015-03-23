package com.example.usuario.ribbit;

import android.app.ListActivity;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.usuario.ribbit.file_handlers.FileHelper;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class RecipientsActivity extends ListActivity {

    static public final String TAG = RecipientsActivity.class.getSimpleName();

    private List<ParseUser> mFriends;
    private ParseRelation<ParseUser> mFriendsRelation;
    private ParseUser mCurrentUser;
    private MenuItem mSendMenuItem;
    private Uri mMediaUri;
    private String mFileType;

    @InjectView(R.id.friendsRetrieveProgressBar) ProgressBar mFriendsRetrieveProgressBar;
    @InjectView(android.R.id.empty) TextView mEmptyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipients);
        ButterKnife.inject(this);


        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

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
                ParseObject message = createMessage();
                //send(message);
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    private void loadListOfCurrentFriends() {

        mFriendsRetrieveProgressBar.setVisibility(View.VISIBLE);
        mEmptyTextView.setText(R.string.loading_current_friends_label);



        mCurrentUser = ParseUser.getCurrentUser();
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

        ParseQuery<ParseUser> query = mFriendsRelation.getQuery();
        query.addAscendingOrder(ParseConstants.KEY_USERNAME);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {

                mFriendsRetrieveProgressBar.setVisibility(View.INVISIBLE);
                mEmptyTextView.setText(R.string.empty_friends_label);
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

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            getListView().getContext(),
                            android.R.layout.simple_list_item_checked,
                            usernames);
                    setListAdapter(adapter);
                } else {
                    AlertDialogGenerator dialog = new AlertDialogGenerator();
                    dialog.showAlertDialog(RecipientsActivity.this, e.getMessage(), getString(R.string.error_title));
                }

            }
        });
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if (l.getCheckedItemCount() > 0){
            mSendMenuItem.setVisible(true);
        }
        else{
            mSendMenuItem.setVisible(false);
        }

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
        for (int i = 0; i < getListView().getCount(); i++){
            if (getListView().isItemChecked(i)){
                recipientIds.add(mFriends.get(i).getObjectId());
            }
        }

        return recipientIds;
    }
}
