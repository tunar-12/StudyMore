package StudyMore.models;
import java.util.List;
import java.util.ArrayList;

public class MascotCat {
    private Long catId;
    private Cosmetic equippedSkin;
    private Cosmetic equippedHouse;
    private Cosmetic equippedHat;
    private List<String> quotes;

    public MascotCat(Long catId) {
        this.catId = catId;
        this.quotes = new ArrayList<>(List.of(
            "You're doing great, keep going!",
            "One session at a time.",
            "Focus. You got this.",
            "Every minute counts!",
            "Stay locked in!",
            "You're building something great.",
            "Rest is part of the process too.",
            "Consistency beats intensity."
        ));
    }

    public String getRandomQuote() {
        if (quotes.isEmpty()) return "Keep studying!";
        return quotes.get((int)(Math.random() * quotes.size()));
    }

    public void congratulate() {
        // TODO: show a message
    }

    public void changeSkin(Cosmetic newSkin) {
        this.equippedSkin = newSkin;
    }

    public void changeMascotHat(Cosmetic newHat) {
        this.equippedHat = newHat;
    }

    public void changeMascotHouse(Cosmetic newHouse) {
        this.equippedHouse = newHouse;
    }

    public Long getCatId(){ 
        return catId; 
    }
    public Cosmetic getEquippedSkin(){ 
        return equippedSkin; 
    }
    public Cosmetic getEquippedHouse(){
        return equippedHouse; 
    }
    public Cosmetic getEquippedHat(){ 
        return equippedHat; 
    }
    public List<String> getQuotes(){
        return quotes; 
    }
}
