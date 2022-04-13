package invii.data;

/**
 * Item represents an "item" model.
 *
 * Maps to representation of item in db.
 */
public class Item
{
    /**
     * ID of item in DB.
     */
    String id;

    String parentCollectionID;

    /**
     * Item name.
     */
    String name;

    /**
     * Item description.
     */
    String description;

    /**
     * Item location.
     */
    String location;

    /**
     * Item count.
     */
    int quantity;

    /**
     * Item value or price.
     */
    float price;

    /**
     * List of pictures related to item.
     * Stores IDs of images in firebase datastore.
     */
    String[] pictures;

    public Item() {
    }

    public Item(String id, String name, String description, String location, int quantity, float price, String[] pictures) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.location = location;
        this.quantity = quantity;
        this.price = price;
        this.pictures = pictures;
    }

    /**
     * Get item ID.
     * @return
     */
    public String getId() {
        return id; // This object's id string
    }

    /**
     *
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     *
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *
     * @return
     */
    public String getLocation() {
        return location;
    }

    /**
     *
     * @param location
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     *
     * @return
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     *
     * @param quantity
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     *
     * @return
     */
    public float getPrice() {
        return price;
    }

    /**
     *
     * @param price
     */
    public void setPrice(float price) {
        this.price = price;
    }

    /**
     *
     * @return
     */
    public boolean isStocked() {
        return quantity > 0;
    }

    public void decrementQuantity()
    {

    }
}
