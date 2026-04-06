package com.codemaxxers.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "inventory_owned_items")
@IdClass(InventoryOwnedItem.InventoryOwnedItemId.class)
public class InventoryOwnedItem {

    @Id
    @Column(name = "inventory_id", nullable = false)
    private Long inventoryId;

    @Id
    @Column(name = "cosmetic_id", nullable = false)
    private Long cosmeticId;

    public InventoryOwnedItem() {
    }

    public InventoryOwnedItem(Long inventoryId, Long cosmeticId) {
        this.inventoryId = inventoryId;
        this.cosmeticId = cosmeticId;
    }

    // Getters and Setters

    public Long getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(Long inventoryId) {
        this.inventoryId = inventoryId;
    }

    public Long getCosmeticId() {
        return cosmeticId;
    }

    public void setCosmeticId(Long cosmeticId) {
        this.cosmeticId = cosmeticId;
    }

    // --- Composite Key Class ---
    public static class InventoryOwnedItemId implements Serializable {
        private Long inventoryId;
        private Long cosmeticId;

        public InventoryOwnedItemId() {}

        public InventoryOwnedItemId(Long inventoryId, Long cosmeticId) {
            this.inventoryId = inventoryId;
            this.cosmeticId = cosmeticId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InventoryOwnedItemId that = (InventoryOwnedItemId) o;
            return Objects.equals(inventoryId, that.inventoryId) &&
                   Objects.equals(cosmeticId, that.cosmeticId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(inventoryId, cosmeticId);
        }
    }
}