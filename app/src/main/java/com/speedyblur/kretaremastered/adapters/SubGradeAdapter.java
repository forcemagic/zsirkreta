package com.speedyblur.kretaremastered.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.models.Grade;

import java.util.ArrayList;

public class SubGradeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final ArrayList<Grade> grades;

    private static final int NORM_GRADE = 0;
    private static final int SPEC_GRADE = 1;

    public SubGradeAdapter(ArrayList<Grade> grades) {
        this.grades = grades;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater infl = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v;
        switch (viewType) {
            case NORM_GRADE:
                v = infl.inflate(R.layout.gradelist_item, parent, false);
                return new NormGradeVH(v);
            case SPEC_GRADE:
                v = infl.inflate(R.layout.gradelist_importantitem, parent, false);
                return new SpecGradeVH(v);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Grade g = grades.get(position);

        switch (getItemViewType(position)) {
            case NORM_GRADE:
                NormGradeVH ngvh = (NormGradeVH) holder;
                ngvh.gradeBullet.setColorFilter(ContextCompat.getColor(
                        ngvh.gradeBullet.getContext(), g.getColorId()), PorterDuff.Mode.SRC_ATOP);
                ngvh.grade.setText(String.valueOf(g.getGrade()));
                ngvh.gradeTitle.setText(g.getType());
                break;
            case SPEC_GRADE:
                SpecGradeVH sgvh = (SpecGradeVH) holder;
                sgvh.gradeLayout.setBackgroundColor(g.getColorId());
                sgvh.grade.setText(String.valueOf(g.getGrade()));
                if (g.getType().contains("végi"))
                    sgvh.gradeTitle.setText(R.string.grade_end_of_year);
                else
                    sgvh.gradeTitle.setText(R.string.grade_end_of_halfterm);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return grades.size();
    }

    @Override
    public int getItemViewType(int position) {
        Grade g = grades.get(position);
        if (g.getType().contains("végi") || g.getType().contains("Félévi")) return SPEC_GRADE;
        else return NORM_GRADE;
    }

    private static class NormGradeVH extends RecyclerView.ViewHolder {
        TextView grade;
        ImageView gradeBullet;
        TextView gradeTitle;
        TextView gradeDesc;
        TextView gradeDesc2;

        NormGradeVH(View itemView) {
            super(itemView);
            grade = itemView.findViewById(R.id.grade);
            gradeTitle = itemView.findViewById(R.id.gradeTitle);
            gradeDesc = itemView.findViewById(R.id.gradeDesc);
            gradeDesc2 = itemView.findViewById(R.id.gradeDesc2);
            gradeBullet = itemView.findViewById(R.id.gradeBullet);
        }
    }

    private static class SpecGradeVH extends RecyclerView.ViewHolder {
        RelativeLayout gradeLayout;
        TextView grade;
        TextView gradeTitle;

        SpecGradeVH(View itemView) {
            super(itemView);
            gradeLayout = itemView.findViewById(R.id.importantGradeInnerLayout);
            grade = itemView.findViewById(R.id.grade);
            gradeTitle = itemView.findViewById(R.id.gradeTitle);
        }
    }
}