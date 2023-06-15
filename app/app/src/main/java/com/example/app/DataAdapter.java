package com.example.app;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DataAdapter  extends RecyclerView.Adapter<DataViewHolder> {
    private ArrayList<DataValue> dataList;
    private ItemClickCallback callback;

    public DataAdapter(ArrayList<DataValue> dataList) {
        this.dataList = dataList;
    }
    public void setDataList(ArrayList<DataValue> dataList) {
        this.dataList = dataList;
        notifyDataSetChanged();
    }
    public void removeData(ArrayList<DataValue> list) {
        for (DataValue i : list) {
            dataList.remove(i);
        }
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_center_data, parent, false);
        return new DataViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull DataViewHolder holder, int position) {
        holder.tableContainer.setBackgroundColor(Color.parseColor("#ffffff"));
        holder.tvDate.setText(dataList.get(position).date);
        holder.tvTime.setText(dataList.get(position).time);
        holder.tvValue.setText(dataList.get(position).value);

        final boolean[] isClick = {false};

        holder.tableContainer.setOnClickListener(v -> {
            if (isClick[0]) {
                holder.tableContainer.setBackgroundColor(Color.parseColor("#ffffff"));
                isClick[0] = false;
            } else {
                holder.tableContainer.setBackgroundColor(Color.parseColor("#f6bd60"));
                isClick[0] = true;
            }
            callback.onClick(position);
        });
    }
    public void setItemClickCallback(ItemClickCallback callback) {
        this.callback = callback;
    }
    interface ItemClickCallback {void onClick(int position); }
    @Override
    public int getItemCount() {
        return dataList.size();
    }
}

