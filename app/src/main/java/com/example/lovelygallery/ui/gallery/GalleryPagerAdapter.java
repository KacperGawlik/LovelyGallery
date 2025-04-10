package com.example.lovelygallery.ui.gallery;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class GalleryPagerAdapter extends FragmentStateAdapter {

    public GalleryPagerAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Tworzenie odpowiedniego fragmentu dla pozycji
        switch (position) {
            case 0:
                return GalleryFragment.newInstance(GalleryFragment.GALLERY_TYPE_ALL);
            case 1:
                return GalleryFragment.newInstance(GalleryFragment.GALLERY_TYPE_PHOTOS);
            case 2:
                return GalleryFragment.newInstance(GalleryFragment.GALLERY_TYPE_VIDEOS);
            case 3:
                return GalleryFragment.newInstance(GalleryFragment.GALLERY_TYPE_FAVORITES);
            default:
                return GalleryFragment.newInstance(GalleryFragment.GALLERY_TYPE_ALL);
        }
    }

    @Override
    public int getItemCount() {
        return 4; // Wszystkie, Zdjęcia, Wideo, Ulubione
    }
}