package com.example.usuario.ribbit;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class EditFriendsActivity extends ListActivity {

    public static final String TAG = EditFriendsActivity.class.getSimpleName();

    private List<ParseUser> mUsers;
    private ParseRelation<ParseUser> mFriendsRelation;
    private ParseUser mCurrentUser;

    @InjectView(R.id.editFriendsProgressBar) ProgressBar mEditFriendsProgressBar;
    @InjectView(android.R.id.empty) TextView mEmptyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_friends);
        ButterKnife.inject(this);
        mEditFriendsProgressBar.setVisibility(View.INVISIBLE);

        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadFriendsList();
    }


    //Bring the list with all the friends of the system
    private void loadFriendsList() {

        mCurrentUser = ParseUser.getCurrentUser();
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.orderByAscending(ParseConstants.KEY_USERNAME);
        query.setLimit(1000);

        mEmptyTextView.setText(getString(R.string.loading_list_friends_label));


        mEditFriendsProgressBar.setVisibility(View.VISIBLE);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> parseUsers, ParseException e) {
                mEditFriendsProgressBar.setVisibility(View.INVISIBLE);
                mEmptyTextView.setText(getString(R.string.empty_friends_label));
                if (e == null){
                    mUsers = parseUsers;
                    String[] usernames = new String[mUsers.size()];

                    int i = 0;

                    for (ParseUser user: mUsers){
                        usernames[i] = user.getUsername();
                        i++;
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            EditFriendsActivity.this,
                            android.R.layout.simple_list_item_checked,
                            usernames);
                    setListAdapter(adapter);

                    addFriendCheckmarks();
                }
                else{
                    Log.e(TAG, e.getMessage());
                    AlertDialogGenerator dialog = new AlertDialogGenerator();
                    dialog.showAlertDialog(EditFriendsActivity.this, e.getMessage(), getString(R.string.error_title));
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_friends, menu);
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

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (getListView().isItemChecked(position)){
            //Add friend
            mFriendsRelation.add(mUsers.get(position));
            mCurrentUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null){
                        Log.e(TAG, e.getMessage());
                    }
                }
            });
        }
        else{
            //Remove friend
        }

    }

    //To retrieve the friends that were already checked
    private void addFriendCheckmarks() {
        mFriendsRelation.getQuery().findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {
                if (e == null){
                    //List returned - look for a match
                    for (int i = 0; i < mUsers.size(); i++){
                        ParseUser user = mUsers.get(i);

                        for (ParseUser friend: friends){
                            if (friend.getObjectId().equals(user.getObjectId())){
                                getListView().setItemChecked(i, true);
                            }
                        }
                    }
                }
                else{
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }
}
