package com.speedyblur.kretaremastered.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.models.SubjectGradeGroup;
import com.speedyblur.kretaremastered.shared.Common;
import com.speedyblur.kretaremastered.shared.GradeSeparatorDecoration;

import java.util.ArrayList;

public class SubjectExpandableGradeAdapter extends RecyclerView.Adapter<SubjectExpandableGradeAdapter.ListHeaderVH> {
    private final ArrayList<SubjectGradeGroup> subjectGradeGroups;
    private int currentOpened = -1;

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
        final Context ctxt = holder.headerTitle.getContext();

        Typeface tf = Typeface.createFromAsset(ctxt.getAssets(), "fonts/OpenSans-Light.ttf");
        holder.headerTitle.setTypeface(Typeface.create(tf, Typeface.BOLD));
        holder.headerTitle.setText(Common.getLocalizedSubjectName(ctxt, sgg.getSubject()));
        holder.subView.setHasFixedSize(true);
        holder.subView.setLayoutManager(new LinearLayoutManager(ctxt));
        holder.subView.setAdapter(new SubGradeAdapter(sgg.getGrades()));
        holder.subView.addItemDecoration(new GradeSeparatorDecoration(ctxt));
        if (position == currentOpened) {
            holder.expandToggler.setRotation(180f);
            holder.subView.setVisibility(View.VISIBLE);
            Drawable bar = ContextCompat.getDrawable(ctxt, R.drawable.grade_group_bar).mutate();
            bar.setColorFilter(ContextCompat.getColor(ctxt, R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
            holder.gradeGroupBar.setBackground(bar);
        } else {
            holder.expandToggler.setRotation(0f);
            holder.subView.setVisibility(View.GONE);
            holder.gradeGroupBar.setBackgroundResource(R.drawable.grade_group_bar);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int lastOpened = currentOpened;
                if (currentOpened == holder.getAdapterPosition()) currentOpened = -1;
                else currentOpened = holder.getAdapterPosition();
                notifyItemChanged(lastOpened); notifyItemChanged(currentOpened);
            }
        });
    }



    @Override
    public int getItemCount() {
        return subjectGradeGroups.size();
    }

    static class ListHeaderVH extends RecyclerView.ViewHolder {
        final TextView headerTitle;
        final ImageView expandToggler;
        final View gradeGroupBar;
        final RecyclerView subView;

        ListHeaderVH(View itemView) {
            super(itemView);
            headerTitle = itemView.findViewById(R.id.gradeGroupTitle);
            expandToggler = itemView.findViewById(R.id.gradeGroupExpandIcon);
            gradeGroupBar = itemView.findViewById(R.id.gradeGroupBar);
            subView = itemView.findViewById(R.id.gradeGroupSubView);
        }
    }
}