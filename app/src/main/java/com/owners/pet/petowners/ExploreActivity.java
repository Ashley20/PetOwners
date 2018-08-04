package com.owners.pet.petowners;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.owners.pet.petowners.adapters.CardAdapter;
import com.owners.pet.petowners.models.Pet;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.SwipeDirection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class ExploreActivity extends AppCompatActivity {
    public static final String TAG = ExploreActivity.class.getSimpleName();
    private FirebaseAuth mAuth;
    private CardStackView cardStackView;
    private DatabaseReference mDatabase;
    private CardAdapter adapter;
    private List<Pet> petList = new ArrayList<>();
    private HashMap<String, String> filtersMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        ButterKnife.bind(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        adapter = new CardAdapter(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setup();
        arrangeFilters(sharedPreferences);
        applyFilters();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void applyFilters() {

        mDatabase.child(getString(R.string.COLLECTION_PETS))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Log.d(TAG, dataSnapshot.toString());
                            for (DataSnapshot entrySnapshot : dataSnapshot.getChildren()) {
                                for (DataSnapshot propertySnapshot : entrySnapshot.getChildren()) {
                                    Pet pet = propertySnapshot.getValue(Pet.class);
                                    petList.add(pet);
                                }
                            }

                            if (filtersMap.containsKey(getString(R.string.adoption_state_filter))) {
                                applyAdoptionStateFilter();
                            }

                            if (filtersMap.containsKey(getString(R.string.show_me_filter))) {
                                applyShowMeFilter();
                            }

                            if (filtersMap.containsKey(getString(R.string.pet_type_filter))) {
                                applyPetTypeFilter();
                            }


                            // Finally load the data
                            load();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void applyAdoptionStateFilter() {
        for (Iterator<Pet> iterator = petList.iterator(); iterator.hasNext();) {
            Pet pet = iterator.next();

            if(!pet.getAdoptionState()) {
                iterator.remove();
            }
        }
    }

    private void applyShowMeFilter(){
        for (Iterator<Pet> iterator = petList.iterator(); iterator.hasNext();) {
            Pet pet = iterator.next();

            if(!filtersMap.get(getString(R.string.show_me_filter)).equals(pet.getGender())) {
                iterator.remove();
            }
        }
    }

    private void applyPetTypeFilter(){
        for (Iterator<Pet> iterator = petList.iterator(); iterator.hasNext();) {
            Pet pet = iterator.next();

            if(!filtersMap.get(getString(R.string.pet_type_filter)).equals(pet.getType())) {
                iterator.remove();
            }
        }
    }

    @OnClick(R.id.swipe_right_fab)
    public void swipeToTheRight() {
        swipeRight();
    }

    @OnClick(R.id.view_pet_profile_fab)
    public void viewPetProfile() {
        Intent intent = new Intent(getApplicationContext(), PetProfileActivity.class);

        Pet topPet = adapter.getItem(cardStackView.getTopIndex());
        if(topPet != null){
            String uid = topPet.getPetUid();
            String petOwnerUid = topPet.getOwnerUid();

            intent.putExtra(getString(R.string.EXTRA_PET_UID), uid);
            intent.putExtra(getString(R.string.EXTRA_PET_OWNER_UID), petOwnerUid);

            startActivity(intent);
        }

    }

    @OnClick(R.id.swipe_left_fab)
    public void swipeToTheLeft() {
        swipeLeft();
    }


    private void arrangeFilters(SharedPreferences sharedPreferences) {

        Boolean isAdoptionStateCheckboxChecked = sharedPreferences.getBoolean(getString(R.string.key_adoption_state),
                getResources().getBoolean(R.bool.default_pet_adoption_state));
        String petType = sharedPreferences.getString(getString(R.string.key_select_type),
                getString(R.string.default_pet_type));

        Boolean switchPreferenceBoyActive = sharedPreferences.getBoolean(getString(R.string.key_gender_boy),
                getResources().getBoolean(R.bool.default_show_me_gender_boy));
        Boolean switchPreferenceGirlActive = sharedPreferences.getBoolean(getString(R.string.key_gender_girl),
                getResources().getBoolean(R.bool.default_show_me_gender_girl));

        if (isAdoptionStateCheckboxChecked) {
            filtersMap.put(getString(R.string.adoption_state_filter), "");
        }

        if (!switchPreferenceBoyActive || !switchPreferenceGirlActive) {
            if (switchPreferenceBoyActive) {
                filtersMap.put(getString(R.string.show_me_filter), getString(R.string.gender_boy));
            } else {
                filtersMap.put(getString(R.string.show_me_filter), getString(R.string.gender_girl));
            }
        }

        switch (petType) {
            case "1":
                filtersMap.put(getString(R.string.pet_type_filter), getString(R.string.type_cat));
                break;
            case "2":
                filtersMap.put(getString(R.string.pet_type_filter), getString(R.string.type_dog));
                break;
        }

    }


    private void setup() {
        cardStackView = (CardStackView) findViewById(R.id.card_stack_view);
        cardStackView.setCardEventListener(new CardStackView.CardEventListener() {
            @Override
            public void onCardDragging(float percentX, float percentY) {
            }

            @Override
            public void onCardSwiped(SwipeDirection direction) {
                if (cardStackView.getTopIndex() == adapter.getCount() - 1) {
                    paginate();
                }
            }

            @Override
            public void onCardReversed() {
            }

            @Override
            public void onCardMovedToOrigin() {
            }

            @Override
            public void onCardClicked(int index) {
                Intent intent = new Intent(getApplicationContext(), PetProfileActivity.class);
                Pet topPet = adapter.getItem(index);
                if(topPet != null){
                    String uid = topPet.getPetUid();
                    String petOwnerUid = topPet.getOwnerUid();

                    intent.putExtra(getString(R.string.EXTRA_PET_UID), uid);
                    intent.putExtra(getString(R.string.EXTRA_PET_OWNER_UID), petOwnerUid);

                    startActivity(intent);
                }
            }


        });
    }

    private void load() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.addAll(petList);
                cardStackView.setAdapter(adapter);
            }
        }, 1000);
    }


    private LinkedList<Pet> extractRemainingTouristSpots() {
        LinkedList<Pet> spots = new LinkedList<>();
        for (int i = cardStackView.getTopIndex(); i < adapter.getCount(); i++) {
            spots.add(adapter.getItem(i));
        }
        return spots;
    }

    private void addFirst() {
        LinkedList<Pet> spots = extractRemainingTouristSpots();
        spots.addFirst(petList.get(0));
        adapter.clear();
        adapter.addAll(spots);
        adapter.notifyDataSetChanged();
    }

    private void addLast() {
        LinkedList<Pet> spots = extractRemainingTouristSpots();
        spots.addLast(petList.get(petList.size() - 1));
        adapter.clear();
        adapter.addAll(spots);
        adapter.notifyDataSetChanged();
    }

    private void removeFirst() {
        LinkedList<Pet> spots = extractRemainingTouristSpots();
        if (spots.isEmpty()) {
            return;
        }

        spots.removeFirst();
        adapter.clear();
        adapter.addAll(spots);
        adapter.notifyDataSetChanged();
    }

    private void removeLast() {
        LinkedList<Pet> spots = extractRemainingTouristSpots();
        if (spots.isEmpty()) {
            return;
        }

        spots.removeLast();
        adapter.clear();
        adapter.addAll(spots);
        adapter.notifyDataSetChanged();
    }

    private void paginate() {
        cardStackView.setPaginationReserved();
        adapter.addAll(petList);
        adapter.notifyDataSetChanged();
    }

    public void swipeLeft() {
        List<Pet> spots = extractRemainingTouristSpots();
        if (spots.isEmpty()) {
            return;
        }

        View target = cardStackView.getTopView();
        View targetOverlay = cardStackView.getTopView().getOverlayContainer();

        ValueAnimator rotation = ObjectAnimator.ofPropertyValuesHolder(
                target, PropertyValuesHolder.ofFloat("rotation", -10f));
        rotation.setDuration(200);
        ValueAnimator translateX = ObjectAnimator.ofPropertyValuesHolder(
                target, PropertyValuesHolder.ofFloat("translationX", 0f, -2000f));
        ValueAnimator translateY = ObjectAnimator.ofPropertyValuesHolder(
                target, PropertyValuesHolder.ofFloat("translationY", 0f, 500f));
        translateX.setStartDelay(100);
        translateY.setStartDelay(100);
        translateX.setDuration(500);
        translateY.setDuration(500);
        AnimatorSet cardAnimationSet = new AnimatorSet();
        cardAnimationSet.playTogether(rotation, translateX, translateY);

        ObjectAnimator overlayAnimator = ObjectAnimator.ofFloat(targetOverlay, "alpha", 0f, 1f);
        overlayAnimator.setDuration(200);
        AnimatorSet overlayAnimationSet = new AnimatorSet();
        overlayAnimationSet.playTogether(overlayAnimator);

        cardStackView.swipe(SwipeDirection.Left, cardAnimationSet, overlayAnimationSet);
    }

    public void swipeRight() {
        List<Pet> spots = extractRemainingTouristSpots();
        if (spots.isEmpty()) {
            return;
        }

        View target = cardStackView.getTopView();
        View targetOverlay = cardStackView.getTopView().getOverlayContainer();

        ValueAnimator rotation = ObjectAnimator.ofPropertyValuesHolder(
                target, PropertyValuesHolder.ofFloat("rotation", 10f));
        rotation.setDuration(200);
        ValueAnimator translateX = ObjectAnimator.ofPropertyValuesHolder(
                target, PropertyValuesHolder.ofFloat("translationX", 0f, 2000f));
        ValueAnimator translateY = ObjectAnimator.ofPropertyValuesHolder(
                target, PropertyValuesHolder.ofFloat("translationY", 0f, 500f));
        translateX.setStartDelay(100);
        translateY.setStartDelay(100);
        translateX.setDuration(500);
        translateY.setDuration(500);
        AnimatorSet cardAnimationSet = new AnimatorSet();
        cardAnimationSet.playTogether(rotation, translateX, translateY);

        ObjectAnimator overlayAnimator = ObjectAnimator.ofFloat(targetOverlay, "alpha", 0f, 1f);
        overlayAnimator.setDuration(200);
        AnimatorSet overlayAnimationSet = new AnimatorSet();
        overlayAnimationSet.playTogether(overlayAnimator);

        cardStackView.swipe(SwipeDirection.Right, cardAnimationSet, overlayAnimationSet);
    }

    private void reverse() {
        cardStackView.reverse();
    }


}

