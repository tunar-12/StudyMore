package StudyMore.models;

import StudyMore.db.DatabaseManager;

public class Cosmetic {
    private String name;
    private CosmeticType type;
    private int price;
    private String imagePath;
    private String description;
    private Long cosmeticId;

    public Cosmetic(Long cosmeticId, String name, CosmeticType type, int price, String imagePath, String description) {
        this.cosmeticId = cosmeticId;
        this.name = name;
        this.type = type;
        this.price = price;
        this.imagePath = imagePath;
        this.description = description;
    }

    public String getName() { 
        return name; 
    }
    
    public CosmeticType getType() { 
        return type; 
    }
    
    public int getPrice() { 
        return price; 
    }
    
    public String getImagePath() { 
        return imagePath; 
    }
    
    public String getDescription() { 
        return description; 
    }
    public long getId() {
        return this.cosmeticId; 
    }

    public boolean obtain(long userId, DatabaseManager db) {
        return db.obtainCosmetic(userId, cosmeticId);
    }
}