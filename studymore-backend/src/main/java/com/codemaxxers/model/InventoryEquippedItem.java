package com.codemaxxers.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "inventory_equipped_items")
@IdClass(InventoryEquippedItem.InventoryEquippedItemId.class)
public class InventoryEquippedItem {

    @Id
    @Column(name = "inventory_id", nullable = false)
    private Long inventoryId;

    @Id
    @Column(name = "cosmetic_type", nullable = false)
    private String cosmeticType;

    @Column(name = "cosmetic_id", nullable = false)
    private Long cosmeticId;

    public InventoryEquippedItem() {
    }

    public InventoryEquippedItem(Long inventoryId, String cosmeticType, Long cosmeticId) {
        this.inventoryId = inventoryId;
        this.cosmeticType = cosmeticType;
        this.cosmeticId = cosmeticId;
    }

    // Getters and Setters

    public Long getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(Long inventoryId) {
        this.inventoryId = inventoryId;
    }

    public String getCosmeticType() {
        return cosmeticType;
    }

    public void setCosmeticType(String cosmeticType) {
        this.cosmeticType = cosmeticType;
    }

    public Long getCosmeticId() {
        return cosmeticId;
    }

    public void setCosmeticId(Long cosmeticId) {
        this.cosmeticId = cosmeticId;
    }

    // --- Composite Key Class ---
    public static class InventoryEquippedItemId implements Serializable {
        private Long inventoryId;
        private String cosmeticType;

        public InventoryEquippedItemId() {}

        public InventoryEquippedItemId(Long inventoryId, String cosmeticType) {
            this.inventoryId = inventoryId;
            this.cosmeticType = cosmeticType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InventoryEquippedItemId that = (InventoryEquippedItemId) o;
            return Objects.equals(inventoryId, that.inventoryId) &&
                   Objects.equals(cosmeticType, that.cosmeticType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(inventoryId, cosmeticType);
        }
    }
}