package com.speedyblur.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.speedyblur.kretaremastered.R;
import com.speedyblur.models.Absence;
import com.speedyblur.shared.Vars;

import java.util.ArrayList;
import java.util.Locale;

public class AbsenceAdapter extends ArrayAdapter<Absence> {

    public AbsenceAdapter(@NonNull Context context, ArrayList<Absence> items) {
        super(context, 0, items);
    }

    @NonNull
    @Override
    public View getView(int pos, View convertView, @NonNull ViewGroup parent) {
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.absencelist_item, null);
        final Absence item = getItem(pos);
        assert item != null;

        ImageView absenceProvenImage = convertView.findViewById(R.id.absenceProven);
        TextView absentClassNum = convertView.findViewById(R.id.absentClassNum);
        TextView absentSubject = convertView.findViewById(R.id.absentSubject);
        TextView absentTimes = convertView.findViewById(R.id.absentTimes);

        absenceProvenImage.setImageDrawable(ContextCompat.getDrawable(getContext(), item.proven ? R.drawable.check_circle_icon_green : R.drawable.dash_circle_icon_red));
        SpannableString absClassNum = new SpannableString(getContext().getString(R.string.class_number, item.classNum));
        if (Locale.getDefault() == Locale.ENGLISH) absClassNum.setSpan(new AbsoluteSizeSpan(80), 0, 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        absentClassNum.setText(absClassNum);
        absentSubject.setText(Vars.getLocalizedSubjectName(getContext(), item.subject));

        // Absent times calc
        // TODO: What to do with #0 classes?
        int startAbsence = 8*60;
        startAbsence += (item.classNum-1)*45+(item.classNum-1)*10;
        if (startAbsence >= 10*60-10) startAbsence += 10;
        int endAbsence = startAbsence + 45;
        absentTimes.setText(String.format(Locale.getDefault(), "%d:%02d", startAbsence / 60, startAbsence % 60)+" - "+
                String.format(Locale.getDefault(), "%d:%02d", endAbsence / 60, endAbsence % 60));

        if (pos == 0) convertView.findViewById(R.id.absenceBarTop).setVisibility(View.INVISIBLE);
        if (pos == getCount() - 1) convertView.findViewById(R.id.absenceBarBottom).setVisibility(View.INVISIBLE);

        return convertView;
    }
}
