package com.example.app;


import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class DataViewHolder extends RecyclerView.ViewHolder {

    TextView tvDate, tvTime, tvValue;
    TableLayout tableContainer;

    public DataViewHolder(@NonNull View itemView) {
        super(itemView);

        tableContainer = itemView.findViewById(R.id.table_container);

        tvDate = itemView.findViewById(R.id.tv_value_date);
        tvTime = itemView.findViewById(R.id.tv_value_time);
        tvValue = itemView.findViewById(R.id.tv_value_value);
    }

}
