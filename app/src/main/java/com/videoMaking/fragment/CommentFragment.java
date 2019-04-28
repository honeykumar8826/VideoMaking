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
import com.videoMaking.adapter.CommentAdapter;
import com.videoMaking.animation.OnSwipeTouchListener;
import com.videoMaking.modal.CommentModal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

/*
 * A simple {@link Fragment} subclass.
 */
public class CommentFragment extends DialogFragment {
    @BindView(R.id.recycles_comment)
    RecyclerView recyclerView;
    @BindView(R.id.linear_layout_swipe)
    LinearLayout swipeLinearLayout;
    private Animation animHide;
    private View view;
    @BindView(R.id.close_comment)
    ImageView downList;

    public CommentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
        getAnimation();
    }

    @Override
    public void onStart() {
        super.onStart();
        Objects.requireNonNull(getDialog().getWindow())
                .setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT);
    }

    /*    @Override
        public void onActivityCreated(Bundle arg0) {
            super.onActivityCreated(arg0);
            getDialog().getWindow()
                    .getAttributes().windowAnimations = R.style.DialogAnimation;
        }*/
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        List<CommentModal> commentModalList = new ArrayList<>();
        view = inflater.inflate(R.layout.fragment_comment, container, false);
        ButterKnife. bind(this,view);

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
            CommentModal commentModal = new CommentModal("https://tineye.com/images/widgets/mona.jpg", "honey kamar", "harry");
            commentModalList.add(commentModal);
        }
        CommentAdapter commentAdapter = new CommentAdapter(this.getActivity(), commentModalList);
        recyclerView.setAdapter(commentAdapter);

        commentAdapter.notifyDataSetChanged();
        return view;
    }
    private void getAnimation() {
        /*  Animation animShow = AnimationUtils.loadAnimation(getActivity(), R.anim.view_show);*/
        animHide = AnimationUtils.loadAnimation(getActivity(), R.anim.view_hide);
    }
}
