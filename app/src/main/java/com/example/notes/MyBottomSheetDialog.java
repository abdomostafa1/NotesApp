package com.example.notes;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class MyBottomSheetDialog extends BottomSheetDialogFragment {

    private OnBottomSheetListener listener;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v=inflater.inflate(R.layout.bottom_sheet_order,container,false);
        FrameLayout frameLayout1 = (FrameLayout) v.findViewById(R.id.new_to_old);
        FrameLayout frameLayout2 = (FrameLayout) v.findViewById(R.id.old_to_new);
        FrameLayout frameLayout3 = (FrameLayout) v.findViewById(R.id.A_to_Z);
        FrameLayout frameLayout4 = (FrameLayout) v.findViewById(R.id.Z_to_A);

        SharedPreferences preferences=((Activity) MainActivity.context).getPreferences(Context.MODE_PRIVATE);
        FrameLayout frameLayout = null;
        int order = preferences.getInt("SortAlgorithm", 1);
        if (order == 1)
            frameLayout = frameLayout1;
        else if (order == 2)
            frameLayout = frameLayout2;
        else if (order == 3)
            frameLayout = frameLayout3;
        else
            frameLayout = frameLayout4;
        ImageView imageView = (ImageView) frameLayout.getChildAt(1);
        imageView.setImageResource(R.drawable.ic_check);

                frameLayout1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                listener.onItemClickListener(1);
            }
        });
        frameLayout2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                listener.onItemClickListener(2);
            }
        });
        frameLayout3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                listener.onItemClickListener(3);
                }

        });
        frameLayout4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            listener.onItemClickListener(4);
            }
        });
        return v;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
            listener = (OnBottomSheetListener) context;
    }

    public interface OnBottomSheetListener
    {
        void onItemClickListener(int order);
    }
}
