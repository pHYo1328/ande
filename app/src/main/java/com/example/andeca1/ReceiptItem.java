package com.example.andeca1;

public class ReceiptItem {

    private String productName;
    private String amount;
    private String quantity;
    private String total;
    private boolean isChecked = false;

    // Constructor
    public ReceiptItem() {
    }

    public ReceiptItem(String productName, String amount, String quantity, String total) {

        this.productName = productName;
        this.amount = amount;
        this.quantity = quantity;
        this.total = total;
    }

    // Getters
    public String getProductName() {
        return productName;
    }

    public String getAmount() {
        return amount;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getTotal() {
        return total;
    }

    public boolean isChecked() {
        return isChecked;
    }

    // Setters
    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

}
