package com.owners.pet.petowners.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.owners.pet.petowners.R;
import com.owners.pet.petowners.models.Pet;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CardAdapter extends ArrayAdapter<Pet> {

    public CardAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public View getView(int position, View contentView, ViewGroup parent) {
        ViewHolder holder;

        if (contentView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            contentView = inflater.inflate(R.layout.item_card_pet, parent, false);
            holder = new ViewHolder(contentView);
            contentView.setTag(holder);
        } else {
            holder = (ViewHolder) contentView.getTag();
        }

        Pet pet = getItem(position);
        if(pet != null){
            holder.name.setText(pet.getName());
            holder.location.setText(getContext()
                    .getString(R.string.adminArea_country_placeholder, pet.getAdminArea(), pet.getCountry()));
            Picasso.get()
                    .load(pet.getProfileImageUri())
                    .into(holder.image);
        }

        return contentView;
    }

    private static class ViewHolder {
        public TextView name;
        public TextView location;
        public ImageView image;

        public ViewHolder(View view) {
            this.name = view.findViewById(R.id.card_pet_name);
            this.location = view.findViewById(R.id.card_pet_location);
            this.image = view.findViewById(R.id.pet_card_image);
        }
    }

}
