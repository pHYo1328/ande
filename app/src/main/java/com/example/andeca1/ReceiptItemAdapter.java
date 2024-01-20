package com.example.andeca1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReceiptItemAdapter extends RecyclerView.Adapter<ReceiptItemAdapter.ViewHolder> {

    private final List<ReceiptItem> receiptItems;

    // Constructor
    public ReceiptItemAdapter(List<ReceiptItem> receiptItems) {
        this.receiptItems = receiptItems;
    }

    @NonNull
    @Override
    public ReceiptItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.receipt_item_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReceiptItemAdapter.ViewHolder holder, int position) {
        ReceiptItem item = receiptItems.get(position);
        holder.textViewProduct.setText(item.getProductName());
        holder.textViewAmount.setText(item.getAmount());
        holder.textViewQuantity.setText(item.getQuantity());
        holder.textViewTotal.setText(item.getTotal());
        holder.checkBox.setChecked(item.isChecked());

    }

    @Override
    public int getItemCount() {
        return receiptItems.size();
    }

    public List<ReceiptItem> getReceiptItems() {
        return receiptItems;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewProduct, textViewAmount, textViewQuantity, textViewTotal;
        CheckBox checkBox;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewProduct = itemView.findViewById(R.id.textViewProduct);
            textViewAmount = itemView.findViewById(R.id.textViewAmount);
            textViewQuantity = itemView.findViewById(R.id.textViewQuantity);
            textViewTotal = itemView.findViewById(R.id.textViewTotal);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }
}
