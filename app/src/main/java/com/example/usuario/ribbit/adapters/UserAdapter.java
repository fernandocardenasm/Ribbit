package com.example.usuario.ribbit.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.usuario.ribbit.R;
import com.example.usuario.ribbit.utilities.MD5Util;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by usuario on 23/03/2015.
 */
public class UserAdapter extends ArrayAdapter<ParseUser> {

    private Context mContext;
    private List<ParseUser> mUsers;

    public UserAdapter(Context context, List<ParseUser> users){
        super(context, R.layout.message_item, users);
        mContext = context;
        mUsers = users;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.user_item, null);
            holder = new ViewHolder();
            holder.userImageView = (ImageView) convertView.findViewById(R.id.userImageView);
            holder.nameLabel = (TextView) convertView.findViewById(R.id.nameLabel);
            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }

        ParseUser user = mUsers.get(position);

        holder.nameLabel.setText(user.getUsername());

        //Get the avatar image

        String email = user.getEmail().toLowerCase();

        if (email.equals("")){
            holder.userImageView.setImageResource(R.mipmap.avatar_empty);
        }
        else{
            String hash = MD5Util.md5Hex(email);
            String gravatarUrl = "http://www.gravatar.com/avatar/" + hash +
                    "?s=204&d=404";

            //Picasso to fetch the image from the web

            Picasso.with(mContext)
                    .load(gravatarUrl)
                    .placeholder(R.mipmap.avatar_empty)
                    .into(holder.userImageView);
        }

        return convertView;
    }

    private static class ViewHolder{
        ImageView userImageView;
        TextView nameLabel;
    }

    public void refill (List<ParseUser> users){
        mUsers.clear();
        mUsers.addAll(users);
        notifyDataSetChanged();
    }
}
