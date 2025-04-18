// Generated by view binder compiler. Do not edit!
package com.example.lovelygallery.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.lovelygallery.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ItemPhotoBinding implements ViewBinding {
  @NonNull
  private final MaterialCardView rootView;

  @NonNull
  public final MaterialCheckBox ivFavorite;

  @NonNull
  public final ImageView ivPhoto;

  @NonNull
  public final TextView tvPhotoName;

  private ItemPhotoBinding(@NonNull MaterialCardView rootView, @NonNull MaterialCheckBox ivFavorite,
      @NonNull ImageView ivPhoto, @NonNull TextView tvPhotoName) {
    this.rootView = rootView;
    this.ivFavorite = ivFavorite;
    this.ivPhoto = ivPhoto;
    this.tvPhotoName = tvPhotoName;
  }

  @Override
  @NonNull
  public MaterialCardView getRoot() {
    return rootView;
  }

  @NonNull
  public static ItemPhotoBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ItemPhotoBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.item_photo, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ItemPhotoBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.ivFavorite;
      MaterialCheckBox ivFavorite = ViewBindings.findChildViewById(rootView, id);
      if (ivFavorite == null) {
        break missingId;
      }

      id = R.id.ivPhoto;
      ImageView ivPhoto = ViewBindings.findChildViewById(rootView, id);
      if (ivPhoto == null) {
        break missingId;
      }

      id = R.id.tvPhotoName;
      TextView tvPhotoName = ViewBindings.findChildViewById(rootView, id);
      if (tvPhotoName == null) {
        break missingId;
      }

      return new ItemPhotoBinding((MaterialCardView) rootView, ivFavorite, ivPhoto, tvPhotoName);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
