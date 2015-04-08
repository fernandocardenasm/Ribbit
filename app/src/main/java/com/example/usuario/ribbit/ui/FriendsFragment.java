package com.example.usuario.ribbit.ui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.usuario.ribbit.R;
import com.example.usuario.ribbit.adapters.UserAdapter;
import com.example.usuario.ribbit.utilities.ParseConstants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by usuario on 19/03/2015.
 */
public class FriendsFragment extends Fragment {

    public static final String TAG = FriendsFragment.class.getSimpleName();

    ProgressBar mLoadingFriendsProgressBar;
    TextView mEmptyTextView;

    private List<ParseUser> mFriends;
    private ParseRelation<ParseUser> mFriendsRelation;
    private ParseUser mCurrentUser;
    private GridView mGridView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.user_grid, container, false);

        mGridView = (GridView) rootView.findViewById(R.id.friendsGrid);

        mLoadingFriendsProgressBar = (ProgressBar) rootView.findViewById(R.id.loadingFriendsProgressBar);
        mEmptyTextView = (TextView) rootView.findViewById(android.R.id.empty);

        mGridView.setEmptyView(mEmptyTextView);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        loadListOfCurrentFriends();
    }

    private void loadListOfCurrentFriends() {


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
                    if (mGridView.getAdapter() == null){
                        UserAdapter adapter = new UserAdapter(getActivity(),mFriends);
                        mGridView.setAdapter(adapter);
                    }
                    else{
                        ((UserAdapter)mGridView.getAdapter()).refill(friends);
                    }

                } else {

                    //Revisar error
                    /*AlertDialogGenerator dialog = new AlertDialogGenerator();
                    dialog.showAlertDialog(getActivity(), e.getMessage(),getString(R.string.error_title));
                    */
                }

            }
        });
    }
}
