package com.example.usuario.ribbit.ui;


import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.usuario.ribbit.utilities.AlertDialogGenerator;
import com.example.usuario.ribbit.utilities.ParseConstants;
import com.example.usuario.ribbit.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by usuario on 19/03/2015.
 */
public class FriendsFragment extends ListFragment {

    public static final String TAG = FriendsFragment.class.getSimpleName();

    ProgressBar mLoadingFriendsProgressBar;
    TextView mEmptyTextView;

    private List<ParseUser> mFriends;
    private ParseRelation<ParseUser> mFriendsRelation;
    private ParseUser mCurrentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friends, container, false);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        loadListOfCurrentFriends();
    }

    private void loadListOfCurrentFriends() {
        mLoadingFriendsProgressBar = (ProgressBar) getView().findViewById(R.id.loadingFriendsProgressBar);
        mEmptyTextView = (TextView) getView().findViewById(android.R.id.empty);

        //Loading Progress Bar
        mLoadingFriendsProgressBar.setVisibility(View.VISIBLE);
        mEmptyTextView.setText(R.string.loading_current_friends_label);


        //mLoadingFriendsProgressBar.setVisibility(View.VISIBLE);

        //mEmptyTextView.setText(R.string.loading_list_friends_label);


        mCurrentUser = ParseUser.getCurrentUser();
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

        ParseQuery<ParseUser> query = mFriendsRelation.getQuery();
        query.addAscendingOrder(ParseConstants.KEY_USERNAME);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {

                mLoadingFriendsProgressBar.setVisibility(View.INVISIBLE);
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
                            android.R.layout.simple_list_item_1,
                            usernames);
                    setListAdapter(adapter);
                } else {
                    AlertDialogGenerator dialog = new AlertDialogGenerator();
                    dialog.showAlertDialog(getListView().getContext(), e.getMessage(), getString(R.string.error_title));
                }

            }
        });
    }
}
