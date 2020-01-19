package com.example.hrvmore;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class MyListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private  int[] mResIds;
    private Context context;

    public MyListAdapter(int[] mResIds, Context context) {
        this.mResIds = mResIds;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_list_style1, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MyViewHolder myViewHolder= (MyViewHolder) holder;
        myViewHolder.imageView.setImageResource(mResIds[position]);
    }

    @Override
    public int getItemCount() {
        return mResIds.length;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder{

        private ImageView imageView;

        public MyViewHolder(View itemView) {
            super(itemView);

            imageView=itemView.findViewById(R.id.iv_1);
        }
    }
}
