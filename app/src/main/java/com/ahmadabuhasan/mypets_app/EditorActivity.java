package com.ahmadabuhasan.mypets_app;

import static com.ahmadabuhasan.mypets_app.R.*;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.ahmadabuhasan.mypets_app.databinding.ActivityEditorBinding;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
 * Created by Ahmad Abu Hasan on 30/12/2020
 */

public class EditorActivity extends AppCompatActivity {

    private ActivityEditorBinding binding;

    private int mGender = 0;
    public static final int GENDER_UNKNOWN = 0;
    public static final int GENDER_MALE = 1;
    public static final int GENDER_FEMALE = 2;

    private int id, gender;
    private String name, species, breed, picture, birth;
    private Menu action;
    private Bitmap bitmap;

    Calendar myCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        binding.birth.setFocusableInTouchMode(false);
        binding.birth.setFocusable(false);
        binding.birth.setOnClickListener(v -> new DatePickerDialog(EditorActivity.this, date,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show());

        binding.fabChoosePic.setOnClickListener(v -> chooseFile());

        setupSpinner();

        Intent intent = getIntent();
        id = intent.getIntExtra("id", 0);
        name = intent.getStringExtra("name");
        species = intent.getStringExtra("species");
        breed = intent.getStringExtra("breed");
        birth = intent.getStringExtra("birth");
        picture = intent.getStringExtra("picture");
        gender = intent.getIntExtra("gender", 0);

        setDataFromIntentExtra();
    }

    private void setDataFromIntentExtra() {
        if (id != 0) {
            readMode();
            Objects.requireNonNull(getSupportActionBar()).setTitle("Edit " + name);

            binding.name.setText(name);
            binding.species.setText(species);
            binding.breed.setText(breed);
            binding.birth.setText(birth);

            Glide.with(EditorActivity.this)
                    .load(picture)
                    .apply(RequestOptions.placeholderOf(drawable.ic_refresh)
                            .error(drawable.ic_error))
                    .into(binding.picture);

            switch (gender) {
                case GENDER_MALE:
                    binding.gender.setSelection(1);
                    break;
                case GENDER_FEMALE:
                    binding.gender.setSelection(2);
                    break;
                default:
                    binding.gender.setSelection(0);
                    break;
            }
        } else {
            Objects.requireNonNull(getSupportActionBar()).setTitle("Add a Pet");
        }
    }

    private void setupSpinner() {
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this, array.array_gender_options, android.R.layout.simple_spinner_item);
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        binding.gender.setAdapter(genderSpinnerAdapter);

        binding.gender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(string.gender_male))) {
                        mGender = GENDER_MALE;
                    } else if (selection.equals(getString(string.gender_female))) {
                        mGender = GENDER_FEMALE;
                    } else {
                        mGender = GENDER_UNKNOWN;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_editor, menu);
        action = menu;

        action.findItem(R.id.menu_save).setVisible(false);
        if (id == 0) {
            action.findItem(R.id.menu_edit).setVisible(false);
            action.findItem(R.id.menu_delete).setVisible(false);
            action.findItem(R.id.menu_save).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.menu_edit:
                //Edit
                editMode();

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(binding.name, InputMethodManager.SHOW_IMPLICIT);

                action.findItem(R.id.menu_edit).setVisible(false);
                action.findItem(R.id.menu_delete).setVisible(false);
                action.findItem(R.id.menu_save).setVisible(true);
                return true;
            case R.id.menu_save:
                //Save
                if (id == 0) {
                    if (TextUtils.isEmpty(binding.name.getText().toString()) ||
                            TextUtils.isEmpty(binding.species.getText().toString()) ||
                            TextUtils.isEmpty(binding.breed.getText().toString()) ||
                            TextUtils.isEmpty(binding.birth.getText().toString())) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                        alertDialog.setMessage("Please complete the field!");
                        alertDialog.setPositiveButton("Ok", (dialog, which) -> dialog.dismiss());
                        alertDialog.show();
                    } else {
                        postData("insert");
                        action.findItem(R.id.menu_edit).setVisible(true);
                        action.findItem(R.id.menu_save).setVisible(false);
                        action.findItem(R.id.menu_delete).setVisible(true);

                        readMode();
                    }
                } else {
                    updateData("update", id);
                    action.findItem(R.id.menu_edit).setVisible(true);
                    action.findItem(R.id.menu_save).setVisible(false);
                    action.findItem(R.id.menu_delete).setVisible(true);

                    readMode();
                }
                return true;
            case R.id.menu_delete:
                AlertDialog.Builder dialog = new AlertDialog.Builder(EditorActivity.this);
                dialog.setMessage("Delete this pet?");
                dialog.setPositiveButton("Yes", (dialog1, which) -> {
                    dialog1.dismiss();
                    deleteData("delete", id, picture);
                });
                dialog.setNegativeButton("Cencel", (dialog12, which) -> dialog12.dismiss());
                dialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
        // TODO Auto-generated method stub
        myCalendar.set(Calendar.YEAR, year);
        myCalendar.set(Calendar.MONTH, monthOfYear);
        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        setBirth();
    };

    private void setBirth() {
        String myFormat = "dd MMMM yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        binding.birth.setText(sdf.format(myCalendar.getTime()));
    }

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private void chooseFile() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                binding.picture.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void postData(final String key) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        readMode();

        String name = binding.name.getText().toString().trim();
        String species = binding.species.getText().toString().trim();
        String breed = binding.breed.getText().toString().trim();
        int gender = mGender;
        String birth = binding.birth.getText().toString().trim();
        String picture;
        if (bitmap == null) {
            picture = "";
        } else {
            picture = getStringImage(bitmap);
        }

        Call<Pets> call = ApiClient.getApiInterface().insertPet(key, name, species, breed, gender, birth, picture);
        call.enqueue(new Callback<Pets>() {
            @Override
            public void onResponse(@NonNull Call<Pets> call, @NonNull Response<Pets> response) {
                progressDialog.dismiss();
                Log.i(EditorActivity.class.getSimpleName(), response.toString());

                String value = response.body().getValue();
                String message = response.body().getMassage();

                if (value.equals("1")) {
                    finish();
                } else {
                    Toast.makeText(EditorActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Pets> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(EditorActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateData(final String key, final int id) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        readMode();

        String name = binding.name.getText().toString().trim();
        String species = binding.species.getText().toString().trim();
        String breed = binding.breed.getText().toString().trim();
        int gender = mGender;
        String birth = binding.birth.getText().toString().trim();
        String picture;
        if (bitmap == null) {
            picture = "";
        } else {
            picture = getStringImage(bitmap);
        }

        Call<Pets> call = ApiClient.getApiInterface().updatePet(key, id, name, species, breed, gender, birth, picture);
        call.enqueue(new Callback<Pets>() {
            @Override
            public void onResponse(@NonNull Call<Pets> call, @NonNull Response<Pets> response) {
                progressDialog.dismiss();
                Log.i(EditorActivity.class.getSimpleName(), response.toString());

                String value = Objects.requireNonNull(response.body()).getValue();
                String message = response.body().getMassage();

                if (value.equals("1")) {
                    Toast.makeText(EditorActivity.this, message, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(EditorActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Pets> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(EditorActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteData(final String key, final int id, final String pic) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting...");
        progressDialog.show();

        readMode();

        Call<Pets> call = ApiClient.getApiInterface().deletePet(key, id, pic);
        call.enqueue(new Callback<Pets>() {
            @Override
            public void onResponse(@NonNull Call<Pets> call, @NonNull Response<Pets> response) {
                progressDialog.dismiss();
                Log.i(EditorActivity.class.getSimpleName(), response.toString());

                String value = Objects.requireNonNull(response.body()).getValue();
                String message = response.body().getMassage();

                if (value.equals("1")) {
                    Toast.makeText(EditorActivity.this, message, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditorActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Pets> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(EditorActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    void readMode() {
        binding.name.setFocusableInTouchMode(false);
        binding.species.setFocusableInTouchMode(false);
        binding.breed.setFocusableInTouchMode(false);

        binding.name.setFocusable(false);
        binding.species.setFocusable(false);
        binding.breed.setFocusable(false);

        binding.gender.setEnabled(false);
        binding.birth.setEnabled(false);

        binding.fabChoosePic.setVisibility(View.INVISIBLE);
    }

    private void editMode() {
        binding.name.setFocusableInTouchMode(true);
        binding.species.setFocusableInTouchMode(true);
        binding.breed.setFocusableInTouchMode(true);

        binding.gender.setEnabled(true);
        binding.birth.setEnabled(true);

        binding.fabChoosePic.setVisibility(View.VISIBLE);
    }
}