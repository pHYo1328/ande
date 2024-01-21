package com.example.andeca1;

import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

class ReceiptItemDiffCallback extends DiffUtil.Callback {
    private final List<ReceiptItem> oldList;
    private final List<ReceiptItem> newList;

    ReceiptItemDiffCallback(List<ReceiptItem> oldList, List<ReceiptItem> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        // Return whether the items are the same (e.g., same ID)
        return oldList.get(oldItemPosition).getId().equals(newList.get(newItemPosition).getId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        // Check whether the contents of the items are the same
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }
}
