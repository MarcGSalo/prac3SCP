public class Item
{
    int Id;
    int Value;
    int Weight;

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public int getValue() {
        return Value;
    }

    public void setValue(int value) {
        Value = value;
    }

    public int getWeight() {
        return Weight;
    }

    public void setWeight(int weight) {
        Weight = weight;
    }

    public Item(int id, int value, int weight) {
        Id = id;
        Value = value;
        Weight = weight;
    }

}
