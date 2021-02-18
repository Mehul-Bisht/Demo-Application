package com.example.demoapplication;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;
import java.util.UUID;

public class SecondFragment extends Fragment {

    TextView addButton, selectLabel;
    TextInputLayout firstName, lastName, dateOfBirth, gender, country,
            state, homeTown, phoneNumber, telephoneNumber;
    ShapeableImageView userProfile;
    AutoCompleteTextView auto;
    TextWatcher textWatcherFirstName, textWatcherLastName, textWatcherDOB, textWatcherGender, textWatcherCountry,
            textWatcherState, textWatcherHomeTown, textWatcherTelephone, textWatcherPhone;
    private int year, month, day;

    static final String[] genderArray = {"Male","Female","Others"};

    private String downloadUrl = "";

    private static final int REQUEST_CODE = 100;

    public SecondFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addButton = view.findViewById(R.id.add_user);
        userProfile = view.findViewById(R.id.user_profile_image);
        selectLabel = view.findViewById(R.id.select_profile_label);
        firstName = view.findViewById(R.id.first_name);
        lastName = view.findViewById(R.id.last_name);
        dateOfBirth = view.findViewById(R.id.date_of_birth);
        gender = view.findViewById(R.id.gender);
        country = view.findViewById(R.id.country);
        state = view.findViewById(R.id.state);
        homeTown = view.findViewById(R.id.home_town);
        phoneNumber = view.findViewById(R.id.phone_number);
        telephoneNumber = view.findViewById(R.id.telephone_number);
        auto = view.findViewById(R.id.auto);

        setupTextWatchers();

        float radius = requireContext().getResources().getDimension(R.dimen.placeholder_corner_radius);
        ShapeAppearanceModel shapeAppearanceModel = userProfile.getShapeAppearanceModel()
                .toBuilder()
                .setAllCorners(CornerFamily.ROUNDED,radius)
                .build();

        userProfile.setShapeAppearanceModel(shapeAppearanceModel);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, genderArray);

        auto.setAdapter(adapter);

        addButton.setOnClickListener(view1 -> {
            User user = new User(
                    downloadUrl,
                    firstName.getEditText().getText().toString().trim(),
                    lastName.getEditText().getText().toString().trim(),
                    dateOfBirth.getEditText().getText().toString().trim(),
                    gender.getEditText().getText().toString().trim(),
                    country.getEditText().getText().toString().trim(),
                    state.getEditText().getText().toString().trim(),
                    homeTown.getEditText().getText().toString().trim(),
                    phoneNumber.getEditText().getText().toString().trim(),
                    telephoneNumber.getEditText().getText().toString().trim(),
                    System.currentTimeMillis()
            );

            if (isUserValid(user)) {
                if(downloadUrl.equals("uploading")) {
                    Toast.makeText(requireContext(),"uploading image, please try again soon",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Adding user..", Toast.LENGTH_SHORT).show();
                    FirebaseFirestore.getInstance().collection("Users")
                            .document("" + firstName.getEditText().getText().toString())
                            .set(user)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(requireContext(), "User added successfully..", Toast.LENGTH_LONG).show();
                                    resetFields();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(requireContext(), "Couldn't add this user! Please try again", Toast.LENGTH_LONG).show();
                                }
                            });
                }
            } else {
                Toast.makeText(requireContext(),"Please enter details correctly",Toast.LENGTH_SHORT).show();
            }
        });

        userProfile.setOnClickListener(view1 -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent,REQUEST_CODE);
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_CANCELED) {
            return;
        } else if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK && data.getData() != null) {

            selectLabel.setVisibility(View.GONE);

            float radius = requireContext().getResources().getDimension(R.dimen.upload_image_corner_radius);
            ShapeAppearanceModel shapeAppearanceModel = userProfile.getShapeAppearanceModel()
                    .toBuilder()
                    .setAllCorners(CornerFamily.ROUNDED,radius)
                    .build();

            userProfile.setShapeAppearanceModel(shapeAppearanceModel);

            userProfile.setImageURI(data.getData());
            uploadImage(data.getData());
        }
    }

    private void uploadImage(Uri uri) {
        downloadUrl = "uploading";
        String filename = UUID.randomUUID().toString();
        StorageReference ref =
                FirebaseStorage.getInstance().getReference("/images/" + filename);

        UploadTask uploadTask = ref.putFile(uri);
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if(!task.isSuccessful()){
                    Log.d("Upload Image Error ",task.getException().getMessage());
                }
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()) {
                    downloadUrl = task.getResult().toString();
                }
            }
        });

    }

    private void setupTextWatchers() {

        textWatcherFirstName = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if((!isNameStringValid(charSequence.toString()))) {
                    firstName.setError("required field");
                } else {
                    firstName.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        textWatcherLastName = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if((!isNameStringValid(charSequence.toString()))) {
                    lastName.setError("required field");
                }else {
                    lastName.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        textWatcherGender = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!isGenderValid(charSequence.toString())) {
                    auto.setError("missing gender");
                }else {
                    auto.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        textWatcherDOB = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!isAgeValid(charSequence.toString())) {
                    dateOfBirth.setError("incorrect age");
                }else {
                    dateOfBirth.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        textWatcherCountry = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!isNameStringValid(charSequence.toString())) {
                    country.setError("required field");
                }else {
                    country.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        textWatcherState = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!isNameStringValid(charSequence.toString())) {
                    state.setError("required field");
                }else {
                    state.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        textWatcherHomeTown = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!isNameStringValid(charSequence.toString())) {
                    homeTown.setError("required field");
                }else {
                    homeTown.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        textWatcherPhone = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!isPhoneNumberStringValid(charSequence.toString())) {
                    phoneNumber.setError("incorrect phone number");
                }else {
                    phoneNumber.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        textWatcherTelephone = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!isTelephoneNumberStringValid(charSequence.toString())) {
                    telephoneNumber.setError("incorrect telephone number");
                }else {
                    telephoneNumber.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

            firstName.getEditText().addTextChangedListener(textWatcherFirstName);
            lastName.getEditText().addTextChangedListener(textWatcherLastName);
            dateOfBirth.getEditText().addTextChangedListener(textWatcherDOB);
            gender.getEditText().addTextChangedListener(textWatcherGender);
            country.getEditText().addTextChangedListener(textWatcherCountry);
            state.getEditText().addTextChangedListener(textWatcherState);
            homeTown.getEditText().addTextChangedListener(textWatcherHomeTown);
            phoneNumber.getEditText().addTextChangedListener(textWatcherPhone);
            telephoneNumber.getEditText().addTextChangedListener(textWatcherTelephone);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        removeTextWatchers();
    }

    private void removeTextWatchers() {

            firstName.getEditText().removeTextChangedListener(textWatcherFirstName);
            lastName.getEditText().removeTextChangedListener(textWatcherLastName);
            dateOfBirth.getEditText().removeTextChangedListener(textWatcherDOB);
            gender.getEditText().removeTextChangedListener(textWatcherGender);
            country.getEditText().removeTextChangedListener(textWatcherCountry);
            state.getEditText().removeTextChangedListener(textWatcherState);
            homeTown.getEditText().removeTextChangedListener(textWatcherHomeTown);
            phoneNumber.getEditText().removeTextChangedListener(textWatcherPhone);
            telephoneNumber.getEditText().removeTextChangedListener(textWatcherTelephone);
    }

    private void resetFields() {

        float radius = requireContext().getResources().getDimension(R.dimen.placeholder_corner_radius);
        ShapeAppearanceModel shapeAppearanceModel = userProfile.getShapeAppearanceModel()
                .toBuilder()
                .setAllCorners(CornerFamily.ROUNDED,radius)
                .build();

        userProfile.setShapeAppearanceModel(shapeAppearanceModel);
        userProfile.setImageDrawable(getResources().getDrawable(R.drawable.ic_image));
        selectLabel.setVisibility(View.VISIBLE);
        firstName.getEditText().setText("");
        lastName.getEditText().setText("");
        dateOfBirth.getEditText().setText("");
        gender.getEditText().setText("");
        country.getEditText().setText("");
        state.getEditText().setText("");
        homeTown.getEditText().setText("");
        phoneNumber.getEditText().setText("");
        telephoneNumber.getEditText().setText("");
    }

    private boolean isUserValid(User user) {

        if(!user.getImageUrl().isEmpty() &&
                !user.getImageUrl().equals("uploading") &&
                isNameStringValid(user.getFirstName()) &&
                isNameStringValid(user.getLastName()) &&
                !user.getDateOfBirth().isEmpty() &&
                isAgeValid(user.getDateOfBirth()) &&
                isGenderValid(user.getGender()) &&
                isNameStringValid(user.getCountry()) &&
                isNameStringValid(user.getState()) &&
                isNameStringValid(user.getHomeTown()) &&
                isPhoneNumberStringValid(user.getPhoneNumber()) &&
                isTelephoneNumberStringValid(user.getTelephoneNumber())
        )
            return true;
        else return false;
    }

    private boolean isNameStringValid(String name) {
        String str = name.toLowerCase();
        char[] charArray = str.toCharArray();
        for (char ch : charArray) {
            if (!(ch >= 'a' && ch <= 'z')) {
                return false;
            }
        }
        return !name.isEmpty();
    }

    private boolean isGenderValid(String gender) {
        boolean isValid = false;
        for(String s: genderArray){
            if(s.equals(gender)){
                isValid = true;
            }
        }
        return isValid;
    }

    private boolean isPhoneNumberStringValid(String number) {
        if(number.isEmpty()) return false;
        String pattern = "^[0-9]{10,13}$";
        return number.matches(pattern);
    }

    private boolean isAgeValid(String age) {
        if(age.isEmpty()) return false;
        if(age.equals("0") || age.equals("00") || age.equals("000")) return false;
        if(Integer.parseInt(age) > 130) return false;
        String pattern = "^[0-9]{1,3}$";
        return age.matches(pattern);
    }

    private boolean isTelephoneNumberStringValid(String number) {
        if(number.isEmpty()) return false;
        String pattern = "^[0-9]{8,13}$";
        return number.matches(pattern);
    }
}