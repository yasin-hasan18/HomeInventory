package com.mycompany.home;

public class FurnitureItem extends InventoryItem {

    public FurnitureItem(int id, String itemName, String category, String location, int quantity, double unitPrice, String extraInfo) {
        super(id, itemName, category, location, quantity, unitPrice, extraInfo);
    }

    @Override
    public String generateDescription() {
        return String.format("Item: %s\nCategory: %s\nLocation: %s\nQuantity: %d\nUnit Price: %.2f Tk\nDimensions: %s",
                itemName, category, location, quantity, unitPrice, extraInfo.isEmpty() ? "N/A" : extraInfo);
    }
}