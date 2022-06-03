package com.ahmadabuhasan.mypets_app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ahmadabuhasan.mypets_app.databinding.ListItemBinding;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by Ahmad Abu Hasan on 30/12/2020
 */

public class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> implements Filterable {

    List<Pets> pets, petsFilter;
    private final Context context;
    final RecyclerViewClickListener mListener;
    CustomFilter filter;

    public Adapter(List<Pets> pets, Context context, RecyclerViewClickListener listener) {
        this.pets = pets;
        this.petsFilter = pets;
        this.context = context;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemBinding binding = ListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MyViewHolder(binding, null);
    }

    @SuppressLint("CheckResult")
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        holder.binding.name.setText(pets.get(position).getName());
        holder.binding.type.setText(String.format("%s / %s", pets.get(position).getBreed(), pets.get(position).getSpecies()));
        holder.binding.date.setText(pets.get(position).getBirth());

        Glide.with(context)
                .load(pets.get(position).getPicture())
                .apply(RequestOptions.placeholderOf(R.drawable.ic_loading)
                        .error(R.drawable.ic_error))
                .into(holder.binding.picture);

        final Boolean love = pets.get(position).getLove();
        if (love) {
            holder.binding.love.setImageResource(R.drawable.likeon);
        } else {
            holder.binding.love.setImageResource(R.drawable.likeof);
        }
    }

    @Override
    public int getItemCount() {
        return pets.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new CustomFilter((ArrayList<Pets>) petsFilter, this);
        }
        return filter;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ListItemBinding binding;
        private final RecyclerViewClickListener mListener;

        public MyViewHolder(ListItemBinding binding, RecyclerViewClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            mListener = listener;
            binding.rowContainer.setOnClickListener(this);
            binding.love.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.row_container:
                    mListener.onRowClick(binding.rowContainer, getAdapterPosition());
                    break;
                case R.id.love:
                    mListener.onLoveClick(binding.love, getAdapterPosition());
                    break;
                default:
                    break;
            }
        }
    }

    public interface RecyclerViewClickListener {
        void onRowClick(View view, int position);

        void onLoveClick(View view, int position);
    }
}