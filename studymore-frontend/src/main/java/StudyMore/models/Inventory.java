package StudyMore.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Inventory {
    private Long inventoryId;
    private User user; 
    private List<Cosmetic> ownedItems;
    private Map<CosmeticType, Cosmetic> equippedItems;

    public Inventory(Long inventoryId, User user) {
        this.inventoryId = inventoryId;
        this.user = user;
        this.ownedItems = new ArrayList<>();
        this.equippedItems = new HashMap<>();
    }

    public void addItem(Cosmetic item) {
        if (!ownedItems.contains(item)) {
            ownedItems.add(item);
        }
    }

    public void equipItem(Cosmetic item) {
        if (ownedItems.contains(item)) {
            equippedItems.put(item.getType(), item);
        }
    }

    public List<Cosmetic> getOwnedItems() {
        //DEBUG
        System.out.println("DEBUG");
        for (Cosmetic c : ownedItems) {
            System.out.println(c.getName());
        }
        return ownedItems;
    }

    public void unequipItem(Cosmetic item) {
        if (equippedItems.containsValue(item)) {
            equippedItems.remove(item.getType());
        }
    }

    public Cosmetic getEquipped(CosmeticType type) {
        return equippedItems.get(type);
    }

}