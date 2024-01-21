package com.example.andeca1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;
import java.util.Objects;

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

        // Set the text for TextViews
        holder.textViewProduct.setText(item.getProductName());
        holder.textViewAmount.setText(item.getAmount());
        holder.textViewQuantity.setText(item.getQuantity());
        holder.textViewTotal.setText(item.getTotal());

        // Set the text for EditTexts (hidden initially)
        holder.editTextProduct.setText(item.getProductName());
        holder.editTextAmount.setText(item.getAmount());
        holder.editTextQuantity.setText(item.getQuantity());
        holder.editTextTotal.setText(item.getTotal());

        holder.checkBox.setChecked(item.isChecked());
    }



    @Override
    public int getItemCount() {
        return receiptItems.size();
    }

    public List<ReceiptItem> getReceiptItems() {
        return receiptItems;
    }
    public void updateReceiptItems(List<ReceiptItem> newReceiptItems) {
        // Calculate the diff between the old and the new list
        ReceiptItemDiffCallback diffCallback = new ReceiptItemDiffCallback(this.receiptItems, newReceiptItems);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        // Update the list and notify the adapter
        this.receiptItems.clear();
        this.receiptItems.addAll(newReceiptItems);
        diffResult.dispatchUpdatesTo(this); // This will automatically do the add/remove/move animations
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewProduct, textViewAmount, textViewQuantity, textViewTotal;
        EditText editTextProduct, editTextAmount, editTextQuantity, editTextTotal;
        CheckBox checkBox;

        public ViewHolder(View itemView) {
            super(itemView);
            // Initialize TextViews
            textViewProduct = itemView.findViewById(R.id.textViewProduct);
            textViewAmount = itemView.findViewById(R.id.textViewAmount);
            textViewQuantity = itemView.findViewById(R.id.textViewQuantity);
            textViewTotal = itemView.findViewById(R.id.textViewTotal);

            // Initialize EditTexts
            editTextProduct = itemView.findViewById(R.id.editTextProduct);
            editTextAmount = itemView.findViewById(R.id.editTextAmount);
            editTextQuantity = itemView.findViewById(R.id.editTextQuantity);
            editTextTotal = itemView.findViewById(R.id.editTextTotal);

            checkBox = itemView.findViewById(R.id.checkBox);
        }

        public ReceiptItem getCurrentData() {
            ReceiptItem item = new ReceiptItem();
            // Use EditText if it's visible (being edited), else use TextView
            item.setProductName(isEditTextVisible(editTextProduct) ?
                    Objects.requireNonNull(editTextProduct.getText()).toString() :
                    textViewProduct.getText().toString());
            // Repeat the same logic for other fields

            item.setChecked(checkBox.isChecked());
            return item;
        }

        private boolean isEditTextVisible(EditText editText) {
            return editText.getVisibility() == View.VISIBLE;
        }
    }

}
