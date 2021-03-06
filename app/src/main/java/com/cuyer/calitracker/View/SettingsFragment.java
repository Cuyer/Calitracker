package com.cuyer.calitracker.View;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.cuyer.calitracker.Model.EmailAndPass;
import com.cuyer.calitracker.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class SettingsFragment extends Fragment {


    private FirebaseAuth auth;
    String name;
    String email;
    String selectedGender = "Male";
    String selectedMetric = "Kg";
    InterstitialAd mInterstitialAd;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);


        Button genderButton = (Button)view.findViewById(R.id.settingsGenderButton);
        Button measureButton = (Button)view.findViewById(R.id.settingsMeasureButton);
        Button dateOfBirthButton = (Button)view.findViewById(R.id.settingsDateOfBirthButton);
        Button logOutButton = (Button)view.findViewById(R.id.settingsLogOutButton);
        Button privacyPolicyButton = (Button) view.findViewById(R.id.settingsPrivacyPolicy);
        Button deleteAccountButton = (Button) view.findViewById(R.id.deleteAccountButton);

        Switch pushSwitch = (Switch)view.findViewById(R.id.settingsNotificationsSwitch);
        Switch screenLockSwitch = (Switch)view.findViewById(R.id.screenLockSwitch);




        TextView nameTextView = (TextView)view.findViewById(R.id.nameLastNameTextView);
        TextView emailTextView = (TextView)view.findViewById(R.id.settingsEmailTextView);
        TextView genderTextView = (TextView)view.findViewById(R.id.genderButtonTextView);
        TextView measureTextView = (TextView)view.findViewById(R.id.measureButtonTextView);
        TextView dateOfBirthTextView = (TextView)view.findViewById(R.id.dateOfBirthButtonTextView);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();


        MobileAds.initialize(getActivity(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {}
        });
        AdRequest adRequest = new AdRequest.Builder().build();


        InterstitialAd.load(getActivity(),"ca-app-pub-4286204280518303/3252163303", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;

                        Toast.makeText(getActivity(),"In a while, we will show you a full screen ad",Toast.LENGTH_SHORT).show();

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mInterstitialAd.show(getActivity());
                            }
                        }, 3000);

                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i("TAG", loadAdError.getMessage());
                        mInterstitialAd = null;
                    }
                });





        deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteDialog();

            }

            private void showDeleteDialog(){

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                        R.style.AlertDialogTheme);
                final Context context = builder.getContext();
                final LayoutInflater layoutInflater = LayoutInflater.from(context);
                final View view = inflater.inflate(R.layout.delete_account_layout,
                        null, false);

                final EditText emailEditText = (EditText) view
                        .findViewById(R.id.emailDeleteAccount);
                final EditText passEditText = (EditText) view
                        .findViewById(R.id.passwordDeleteAccount);


                builder.setTitle("Deleting account");

                builder.setPositiveButton("DELETE ACCOUNT", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {


                        if (TextUtils.isEmpty(emailEditText.getText().toString()) || TextUtils
                                .isEmpty(passEditText.getText().toString())) {

                            Toast.makeText(getActivity(), "Please provide email and password",
                                    Toast.LENGTH_SHORT).show();
                        }else{

                            FirebaseUser user = auth.getCurrentUser();
                            String email = emailEditText.getText().toString();
                            String pass = passEditText.getText().toString();

                            AuthCredential credential = EmailAuthProvider
                                    .getCredential(email,pass);
                            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Log.d("TAG", "User re-authenticated.");
                                }
                            });

                            user.delete();




                            DocumentReference userRef = db.collection("users").
                                    document(user.getUid());

                            userRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(getActivity(),"Account deleted successfully",
                                            Toast.LENGTH_LONG).show();

                                    Intent intent = new Intent(getActivity(), LoginScreen.class);
                                    startActivity(intent);
                                }
                            });


                        }


                    }
                });

                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                builder.setCancelable(false);
                builder.setView(view);
                builder.show();
            }
        });



        privacyPolicyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse("https://calitracker-93880.web.app"));
                startActivity(intent);
            }
        });


        
        genderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOptionsDialog();
            }

            private void showOptionsDialog() {
                String[] genders = {"Male", "Female","Prefer not to say"};
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                        R.style.AlertDialogTheme);
                builder.setTitle("Gender");
                builder.setSingleChoiceItems(genders, 0,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        selectedGender = genders[i];

                    }
                });
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(selectedGender.equals("Male")){

                            Map<String, Object> selectGender = new HashMap<>();
                            selectGender.put("Gender", "Male");

                            FirebaseUser user = auth.getCurrentUser();
                            DocumentReference genderRef = db.collection("users").
                                    document(user.getUid());

                           genderRef.update(selectGender);

                           genderTextView.setText(selectedGender);

                        }else if(selectedGender.equals("Prefer not to say")){

                            Map<String, Object> selectGender = new HashMap<>();
                            selectGender.put("Gender", "Prefer not to say");

                            FirebaseUser user = auth.getCurrentUser();
                            DocumentReference genderRef = db.collection("users").
                                    document(user.getUid());

                            genderRef.update(selectGender);

                            genderTextView.setText(selectedGender);

                        }


                        else{

                            Map<String, Object> selectGender = new HashMap<>();
                            selectGender.put("Gender", "Female");

                            FirebaseUser user = auth.getCurrentUser();
                            DocumentReference genderRef = db.collection("users").
                                    document(user.getUid());

                            genderRef.update(selectGender);
                            genderTextView.setText(selectedGender);


                        }
                    }
                });
                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
            }
        });



        measureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOptionsDialog();
            }

            private void showOptionsDialog() {
                String[] metrics = {"KG", "LB"};
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                        R.style.AlertDialogTheme);
                builder.setTitle("Units of Measure");
                builder.setSingleChoiceItems(metrics, 0,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                selectedMetric = metrics[i];

                            }
                        });
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(selectedMetric.equals("KG")){

                            Map<String, Object> selectMetric = new HashMap<>();
                            selectMetric.put("Metric", "Kg");

                            FirebaseUser user = auth.getCurrentUser();
                            DocumentReference metricRef = db.collection("users").
                                    document(user.getUid());

                            metricRef.update(selectMetric);

                            measureTextView.setText(selectedMetric);

                        }else{

                            Map<String, Object> selectMetric = new HashMap<>();
                            selectMetric.put("Metric", "Lb");

                            FirebaseUser user = auth.getCurrentUser();
                            DocumentReference metricRef = db.collection("users").
                                    document(user.getUid());

                            metricRef.update(selectMetric);

                            measureTextView.setText(selectedMetric);


                        }
                    }
                });
                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
            }
        });


        dateOfBirthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new SelectDateFragment();
                newFragment.show(getParentFragmentManager(), "DatePicker");
            }
        });



        screenLockSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){

                    Map<String, Object> lockScreen = new HashMap<>();
                    lockScreen.put("LockScreen", true);


                    FirebaseUser user = auth.getCurrentUser();
                    DocumentReference screenLock = db.collection("users").
                            document(user.getUid());

                    screenLock.update(lockScreen);

                }else{

                    Map<String, Object> lockScreen = new HashMap<>();
                    lockScreen.put("LockScreen", false);


                    FirebaseUser user = auth.getCurrentUser();
                    DocumentReference screenLock = db.collection("users").
                            document(user.getUid());

                    screenLock.update(lockScreen);

                }
            }
        });






        pushSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                String topic = "PushNotifications";

                if(isChecked) {

                    FirebaseMessaging.getInstance().subscribeToTopic(topic);
                    Log.d("subscribed", "subscribed: yes ");

                    Map<String, Object> pushNotif = new HashMap<>();
                    pushNotif.put("Notifications", true);


                    FirebaseUser user = auth.getCurrentUser();
                    DocumentReference pushRef = db.collection("users").
                            document(user.getUid());

                    pushRef.update(pushNotif);




                }else{

                    FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
                    Log.d("subscribed", "subscribed: no ");

                    Map<String, Object> pushNotif = new HashMap<>();
                    pushNotif.put("Notifications", false);


                    FirebaseUser user = auth.getCurrentUser();
                    DocumentReference pushRef = db.collection("users").
                            document(user.getUid());

                    pushRef.update(pushNotif);

                }


            }
        });





        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();
                Intent intent = new Intent(getActivity(), LoginScreen.class);
                startActivity(intent);

            }
        });





        db.collection("users").document(user.getUid()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        Log.d("data", "DocumentSnapshot data: " + document.getData());

                        EmailAndPass.name = document.get("FirstName").toString();
                        EmailAndPass.lastName = document.get("LastName").toString();
                        EmailAndPass.email = document.get("Email").toString();
                        nameTextView.setText(EmailAndPass.name + " " + EmailAndPass.lastName);
                        emailTextView.setText(EmailAndPass.email);






                    }else{
                        Log.d("data", "No such document");
                    }
                }else{
                    Log.d("data", "get failed with ", task.getException());
                }
            }
        });




        db.collection("users").document(user.getUid()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if(document.exists()){

                                if(document.get("Gender") != null) {
                                    EmailAndPass.gender = document.get("Gender").toString();
                                    genderTextView.setText(EmailAndPass.gender);

                                }else{
                                    genderTextView.setText("Prefer not to say");

                                }


                                if(document.get("Metric") != null) {
                                    EmailAndPass.metric = document.get("Metric").toString()
                                            .toUpperCase(Locale.ROOT);
                                    measureTextView.setText(EmailAndPass.metric);
                                }else{
                                    measureTextView.setText("Prefer not to say");
                                }


                                if(document.get("DateOfBirth") != null) {
                                    EmailAndPass.dateOfBirth = document.get("DateOfBirth")
                                            .toString();
                                    dateOfBirthTextView.setText(EmailAndPass.dateOfBirth);
                                }

                                if(document.get("Notifications").equals(true)){
                                    pushSwitch.setChecked(true);
                                }else{
                                    pushSwitch.setChecked(false);
                                }

                                if(document.get("LockScreen").equals(true)){
                                    screenLockSwitch.setChecked(true);
                                }else if(document.get("LockScreen") == null){

                                    Map<String, Object> lockScreen = new HashMap<>();
                                    lockScreen.put("LockScreen", false);


                                    FirebaseUser user = auth.getCurrentUser();
                                    DocumentReference lockRef = db.collection("users").
                                            document(user.getUid());

                                    lockRef.update(lockScreen);

                                    screenLockSwitch.setChecked(false);

                                }else{
                                    screenLockSwitch.setChecked(false);
                                }






                            }else{
                                Log.d("data", "No such document");
                            }
                        }else{
                            Log.d("data", "get failed with ", task.getException());
                        }
                    }
                });





        return view;
    }


}