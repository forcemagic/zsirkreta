package com.speedyblur.kretaremastered.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.models.SubjectGradeGroup;

import java.util.ArrayList;

public class SubjectExpandableGradeAdapter extends RecyclerView.Adapter<SubjectExpandableGradeAdapter.ListHeaderVH> {
    private final ArrayList<SubjectGradeGroup> subjectGradeGroups;

    public SubjectExpandableGradeAdapter(ArrayList<SubjectGradeGroup> subjectGradeGroups) {
        this.subjectGradeGroups = subjectGradeGroups;
    }

    @Override
    public ListHeaderVH onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater infl = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = infl.inflate(R.layout.gradegroup_item, parent, false);
        return new ListHeaderVH(v);
    }

    @Override
    public void onBindViewHolder(final ListHeaderVH holder, int position) {
        final SubjectGradeGroup sgg = subjectGradeGroups.get(position);

        holder.headerTitle.setText(sgg.getSubject());
        holder.expandToggler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.subView.setAdapter(new SubGradeAdapter(sgg.getGrades()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return subjectGradeGroups.size();
    }

    public static class ListHeaderVH extends RecyclerView.ViewHolder {
        public TextView headerTitle;
        public ImageView expandToggler;
        public View gradeGroupBar;
        public RecyclerView subView;

        public ListHeaderVH(View itemView) {
            super(itemView);
            headerTitle = itemView.findViewById(R.id.gradeGroupTitle);
            expandToggler = itemView.findViewById(R.id.gradeGroupExpandIcon);
            gradeGroupBar = itemView.findViewById(R.id.gradeGroupBar);
            subView = itemView.findViewById(R.id.gradeGroupInnerLayout);
        }
    }
}