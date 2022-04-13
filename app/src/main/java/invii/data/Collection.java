package invii.data;
import java.util.HashMap;

public class Collection
{
    String ownerID;
    String[] authUserIDs;

    Collection[] childCollections;
    // HashMap<Integer, Collection> childCollections = new HashMap<Integer, Collection>(); //Hash storing Items
    HashMap<Integer, Item> childItems = new HashMap<Integer, Item>(); //Hash storing Items.  Hash key is the QRCode

    String id;
    String name;
    String description;
    String[] images;
    String location;

    public Collection ()
    {
        this.ownerID = "";
        // this.authUserIDs = authUserIDs;
        this.name = "";
        this.description = "";
        // this.images = images;
        this.location = "";
    }

    public Collection(String ownerID, String[] authUserIDs, String name, String description, String[] images, String location) {
        this.ownerID = ownerID;
        this.authUserIDs = authUserIDs;
        this.name = name;
        this.description = description;
        this.images = images;
        this.location = location;
    }

    public int getItemCount()
    {
        return 0;
    }

    public int getUniqueItemsCount()
    {
        return 0;
    }

    public int getChildCollectionCount()
    {
        return 0;
    }

    public String getID() {
        return this.id;
    }

    public void setID(String id) {
        this.id = id;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void addItem(Item item, boolean recursive)
    {

    }

    public void removeitem(Item item, boolean recursive)
    {

    }
}
