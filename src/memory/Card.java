package memory;

/** 
 * ADT representing the coordinates of a given card in a Board.
 */
public class Card {
    
    public enum Color {RED, GREEN, PURPLE}
    public enum Number {ONE, TWO, THREE}
    public enum Shading {SOLID, STRIPED, OPEN}
    public enum Shape {DIAMOND, SQUIGGLE, OVAL}
    
    private final Color color;
    private final Number number;
    private final Shading shading;
    private final Shape shape;
    
    /*
     * Abstraction function:
     *    AF(color, number, shading, shape): card in Set with those properties
     * 
     * Representation invariant:
     *    true
     * 
     * Safety from rep exposure:
     *    all fields are private and final
     *    no mutators, all methods return immutable types
     * 
     * Thread safety argument:
     *    class is threadsafe immutable
     *    all fields are private, final, and primitives
     */
    
    /**
     * Creates an instance of Square.
     * @param color 
     * @param number
     * @param shading
     * @param shape
     */
    public Card(Color color, Number number, Shading shading, Shape shape) {
        this.color = color;
        this.number = number;
        this.shading = shading;
        this.shape = shape;
        checkRep();
    }
    
    private void checkRep() {

    }

    public Color color() {
        return color;
    }
    
    public Number number() {
        return number;
    }
    
    public Shading shading() {
        return shading;
    }
    
    public Shape shape() {
        return shape;
    }
    
    @Override
    public String toString() {
        return number+"/"+color+"/"+shading+"/"+shape;
    }
    
    @Override
    public boolean equals(Object that) {
        return that instanceof Card && this.sameValue((Card) that);
    }
    
    private boolean sameValue(Card that) {
        return this.color.equals(that.color) && this.number.equals(that.number)
                && this.shading.equals(that.shading) && this.shape.equals(that.shape);
    }
    
    @Override
    public int hashCode() {
        return this.color.hashCode()+this.number.hashCode()+this.shading.hashCode()+this.shape.hashCode();
    }
}
