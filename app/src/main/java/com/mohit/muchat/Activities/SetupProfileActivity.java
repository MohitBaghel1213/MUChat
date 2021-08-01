package com.mohit.muchat.Activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mohit.muchat.Models.User;
import com.mohit.muchat.R;
import com.mohit.muchat.databinding.ActivitySetupProfileBinding;

import org.jetbrains.annotations.NotNull;

public class SetupProfileActivity extends AppCompatActivity {

    ActivitySetupProfileBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;

    Uri selectedImage;
    ProgressDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetupProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();
        storage=FirebaseStorage.getInstance();
        getSupportActionBar().hide();
        dialog=new ProgressDialog(this);
        dialog.setMessage("Updating profile...");
        dialog.setCancelable(false);



//        database.getReference().child("users")
//                .child(auth.getUid())
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
//                        User user=snapshot.getValue(User.class);
//                        binding.nameBox.setText(user.getName());
//                        binding.aboutBox.setText(user.getAbout());
////                        if(!user.getProfileImage().equals("No Image")) {
////                            Glide.with(SetupProfileActivity.this).load(user.getProfileImage())
////                                    .placeholder(R.drawable.avatar)
////                                    .into(binding.imageView);
////                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
//
//                    }
//                });


        binding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                someActivityResultLauncher.launch(intent);
            }
        });

        binding.continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name=binding.nameBox.getText().toString();
                String about=binding.aboutBox.getText().toString();
                if(name.isEmpty()){
                    binding.nameBox.setError("Please type a name");
                    return;

                }
                dialog.show();

                if(selectedImage!=null){
                    StorageReference reference=storage.getReference().child("Profiles").child(auth.getUid());
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();

                                    String uid = auth.getUid();
                                    String phone=auth.getCurrentUser().getPhoneNumber();
                                    String name=binding.nameBox.getText().toString();
                                    String about=binding.aboutBox.getText().toString();

                                    User user=new User(uid,name,phone,imageUrl,about);

                                    database.getReference()
                                            .child("users")
                                            .child(uid)
                                            .setValue(user)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    dialog.dismiss();

                                                    Intent intent=new Intent(SetupProfileActivity.this,MainActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            });

                                }
                            });

                        }
                    });
                }
                else{
                    String uid = auth.getUid();
                    String phone=auth.getCurrentUser().getPhoneNumber();


                    User user=new User(uid,name,phone,"No Image",about);

                    database.getReference()
                            .child("users")
                            .child(uid)
                            .setValue(user)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    dialog.dismiss();

                                    Intent intent=new Intent(SetupProfileActivity.this,MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                }
            }
        });
    }


    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        if(data!=null){
                            if(data.getData()!=null){
                                binding.imageView.setImageURI(data.getData());
                                selectedImage=data.getData();
                            }
                        }
                    }
                }
            });
}