package adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.owners.pet.petowners.R;
import com.owners.pet.petowners.models.Pet;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class PetsAdapter extends ArrayAdapter<Pet>{
    private Context mContext;
    private ArrayList<Pet> petList;

    public PetsAdapter(@NonNull Context context, ArrayList<Pet> petList) {
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
        TextView petAge = convertView.findViewById(R.id.pet_age_text_view);
        TextView petState = convertView.findViewById(R.id.pet_state);

        // Update UI
        if (pet != null) {
            petIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.pets_icon));
            petName.setText(pet.getName());
            petAge.setText(pet.getAge());
            petState.setText(pet.isWants_to_be_adopted() ? mContext.getString(R.string.waits_for_adoption_text)
                    : mContext.getString(R.string.no_adoption));

        }

        return convertView;
    }
}
