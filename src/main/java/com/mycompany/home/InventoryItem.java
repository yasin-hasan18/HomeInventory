package com.mycompany.home;

public abstract class InventoryItem {
    protected int id;
    protected String itemName;
    protected String category;
    protected String location;
    protected int quantity;
    protected double unitPrice;
    protected String extraInfo;

    public InventoryItem(int id, String itemName, String category, String location, int quantity, double unitPrice, String extraInfo) {
        this.id = id;
        this.itemName = itemName;
        this.category = category;
        this.location = location;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.extraInfo = extraInfo;
    }

    public abstract String generateDescription();

    public double getCurrentValue(double depreciationRate) {
        double totalInitialValue = quantity * unitPrice;
        return totalInitialValue - (totalInitialValue * depreciationRate);
    }
}