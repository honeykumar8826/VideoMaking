package com.videoMaking.fragment;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.videoMaking.R;
import com.videoMaking.adapter.ShareReuseAdapter;
import com.videoMaking.animation.OnSwipeTouchListener;
import com.videoMaking.modal.ShareReuseModal;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SharePostFragment extends DialogFragment {
    @BindView(R.id.recycles_reuse)
    RecyclerView recyclerView;
    @BindView(R.id.linear_layout_swipe)
    LinearLayout swipeLinearLayout;
    @BindView(R.id.close_reuse)
    ImageView downList;
    private Animation /*animShow, */animHide;
    private View view;


    public SharePostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
        // getAnimation();
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow()
                .setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
        getAnimation();
        List<ShareReuseModal> shareReuseModalList = new ArrayList<>();
        view = inflater.inflate(R.layout.fragment_share_post, container, false);
        ButterKnife.bind(this, view);
        swipeLinearLayout.setOnTouchListener(new OnSwipeTouchListener(getContext()) {
            @Override
            public void onSwipeDown() {
                super.onSwipeDown();
                recyclerView.startAnimation(animHide);
                getDialog().dismiss();
//                swipeLinearLayout.setVisibility(View.GONE);
            }

            @Override
            public void onSwipeUp() {
                super.onSwipeUp();
               /* recyclerView.setVisibility(View.VISIBLE);
                recyclerView.startAnimation(animShow);*/
            }
        });
        downList.setOnClickListener(v -> getDialog().dismiss());

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        for (int i = 0; i < 50; i++) {
            ShareReuseModal shareReuseModal = new ShareReuseModal("https://tineye.com/images/widgets/mona.jpg", "garry", "porter");
            shareReuseModalList.add(shareReuseModal);
        }
        ShareReuseAdapter shareReuseAdapter = new ShareReuseAdapter(this.getActivity(), shareReuseModalList);
        recyclerView.setAdapter(shareReuseAdapter);

        shareReuseAdapter.notifyDataSetChanged();
        return view;
    }

    private void getAnimation() {
        /* animShow = AnimationUtils.loadAnimation(getActivity(), R.anim.view_show);*/
        animHide = AnimationUtils.loadAnimation(getActivity(), R.anim.view_hide);
    }
}

