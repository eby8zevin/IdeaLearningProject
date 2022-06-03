package com.ahmadabuhasan.mypets_app;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ahmadabuhasan.mypets_app.databinding.ActivityMainBinding;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
 * Created by Ahmad Abu Hasan on 30/12/2020
 */

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private Adapter adapter;
    private List<Pets> petsList;

    Adapter.RecyclerViewClickListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listener = new Adapter.RecyclerViewClickListener() {
            @Override
            public void onRowClick(View view, final int position) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                intent.putExtra("id", petsList.get(position).getId());
                intent.putExtra("name", petsList.get(position).getName());
                intent.putExtra("species", petsList.get(position).getSpecies());
                intent.putExtra("breed", petsList.get(position).getBreed());
                intent.putExtra("gender", petsList.get(position).getGender());
                intent.putExtra("picture", petsList.get(position).getPicture());
                intent.putExtra("birth", petsList.get(position).getBirth());
                startActivity(intent);
            }

            @Override
            public void onLoveClick(View view, int position) {
                final int id = petsList.get(position).getId();
                final Boolean love = petsList.get(position).getLove();
                final ImageView mLove = view.findViewById(R.id.love);

                if (love) {
                    mLove.setImageResource(R.drawable.likeof);
                    petsList.get(position).setLove(false);
                    updateLove("update_love", id, false);
                    adapter.notifyDataSetChanged();
                } else {
                    mLove.setImageResource(R.drawable.likeon);
                    petsList.get(position).setLove(true);
                    updateLove("update_love", id, true);
                    adapter.notifyDataSetChanged();
                }
            }
        };

        binding.fab.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, EditorActivity.class)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);

        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName())
        );
        searchView.setQueryHint("Search Pet...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        searchMenuItem.getIcon().setVisible(false, false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void getPets() {
        Call<List<Pets>> call = ApiClient.getApiInterface().getPets();
        call.enqueue(new Callback<List<Pets>>() {
            @Override
            public void onResponse(@NonNull Call<List<Pets>> call, @NonNull Response<List<Pets>> response) {
                binding.progress.setVisibility(View.GONE);
                petsList = response.body();
                Log.i(MainActivity.class.getSimpleName(), Objects.requireNonNull(response.body()).toString());
                adapter = new Adapter(petsList, MainActivity.this, listener);
                binding.recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(@NonNull Call<List<Pets>> call, @NonNull Throwable t) {
                Toast.makeText(MainActivity.this, "rp :" +
                                t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateLove(final String key, final int id, final Boolean love) {
        Call<Pets> call = ApiClient.getApiInterface().updateLove(key, id, love);
        call.enqueue(new Callback<Pets>() {
            @Override
            public void onResponse(@NonNull Call<Pets> call, @NonNull Response<Pets> response) {
                Log.i(MainActivity.class.getSimpleName(), "Response " + response.toString());
                String value = Objects.requireNonNull(response.body()).getValue();
                String message = response.body().getMassage();

                if (value.equals("1")) {
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Pets> call, @NonNull Throwable t) {
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPets();
    }
}