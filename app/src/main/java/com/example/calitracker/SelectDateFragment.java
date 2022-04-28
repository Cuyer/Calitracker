package com.example.calitracker;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SelectDateFragment extends DialogFragment implements DatePickerDialog
        .OnDateSetListener{

    private FirebaseAuth auth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar calendar = Calendar.getInstance();
        int yy = calendar.get(Calendar.YEAR);
        int mm = calendar.get(Calendar.MONTH);
        int dd = calendar.get(Calendar.DAY_OF_MONTH);
        return new DatePickerDialog(getActivity(), this, yy, mm, dd);
    }

    public void onDateSet(DatePicker view, int yy, int mm, int dd) {
        populateSetDate(yy, mm+1, dd);
    }
    public void populateSetDate(int year, int month, int day) {
        TextView dob = (TextView) getActivity().findViewById(R.id.dateOfBirthButtonTextView);
        dob.setText(day+"/"+month+"/"+year);

        String dateOfBirth = dob.getText().toString();
        auth = FirebaseAuth.getInstance();
        Map<String, Object> selectDoB = new HashMap<>();
        selectDoB.put("DateOfBirth", dateOfBirth);

        FirebaseUser user = auth.getCurrentUser();
        DocumentReference DobRef = db.collection("users").
                document(user.getUid());

        DobRef.update(selectDoB);


    }
}