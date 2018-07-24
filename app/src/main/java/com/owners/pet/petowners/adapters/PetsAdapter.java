package com.owners.pet.petowners.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.owners.pet.petowners.PetProfileActivity;
import com.owners.pet.petowners.R;
import com.owners.pet.petowners.models.Pet;

import java.util.ArrayList;

public class PetsAdapter extends ArrayAdapter<Pet>{
    private Context mContext;
    private ArrayList<Pet> petList;

    public PetsAdapter(Context context, ArrayList<Pet> petList) {
        super(context,0, petList);
        this.mContext = context;
        this.petList = petList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.pet_list_item, parent, false);
        }

        final Pet pet = petList.get(position);

        ImageView petIcon = convertView.findViewById(R.id.pet_icon);
        TextView petName = convertView.findViewById(R.id.pet_name);
        TextView petState = convertView.findViewById(R.id.pet_state);

        // Update UI
        if (pet != null) {
            petIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.pets_icon));
            petName.setText(pet.getName());
            petState.setText(pet.getAdoptionState() ? mContext.getString(R.string.waits_for_adoption_text)
                    : mContext.getString(R.string.no_adoption));

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent petProfileIntent = new Intent(mContext, PetProfileActivity.class);
                    petProfileIntent.putExtra(mContext.getString(R.string.EXTRA_PET_OWNER_UID), pet.getOwnerUid());
                    petProfileIntent.putExtra(mContext.getString(R.string.EXTRA_PET_UID), pet.getPetUid());
                    petProfileIntent.putExtra(mContext.getString(R.string.EXTRA_PET_NAME), pet.getName());
                    petProfileIntent.putExtra(mContext.getString(R.string.EXTRA_PET_GENDER), pet.getGender());
                    petProfileIntent.putExtra(mContext.getString(R.string.EXTRA_PET_ABOUT), pet.getAbout());
                    petProfileIntent.putExtra(mContext.getString(R.string.EXTRA_PET_OWNER), pet.getOwner());
                    petProfileIntent.putExtra(mContext.getString(R.string.EXTRA_PET_TYPE), pet.getType());
                    petProfileIntent.putExtra(mContext.getString(R.string.EXTRA_PET_ADOPTION_STATE), pet.getAdoptionState());

                    mContext.startActivity(petProfileIntent);
                }
            });

        }

        return convertView;
    }

    @Override
    public int getCount() {
        if(petList != null){
            return petList.size();
        }else {
            return 0;
        }
    }
}
