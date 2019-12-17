package com.example.mymusic.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mymusic.MainActivity;
import com.example.mymusic.R;
import com.example.mymusic.models.Song;

import java.util.ArrayList;

public class SongsAdapter  extends ArrayAdapter<Song> {

    public SongsAdapter(Context context, ArrayList<Song> songs) {
        super(context, 0, songs);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Song song = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.song_item, parent, false);
        }

        // Lookup view for data population
        TextView title = (TextView) convertView.findViewById(R.id.title);
        TextView artist = (TextView) convertView.findViewById(R.id.artist);
        ImageView album = (ImageView) convertView.findViewById(R.id.album);

        // Populate the data into the template view using the data object
        title.setText(song.getTitle());
        artist.setText(song.getArtist());
        Glide.with(getContext()).load(song.getAlbum_img_url()).into(album);

        // Return the completed view to render on screen
        return convertView;
    }
}