package setgame;

/** 
 * ADT representing the coordinates of a given square in a Board.
 */
public class Square {
    
    private final int row;
    private final int col;
    
    /*
     * Abstraction function:
     *    AF(row, col): a set of coordinates, where x=row and y=col
     * 
     * Representation invariant:
     *    row and col are both positive integers
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
     * @param row the row number
     * @param col the column number
     */
    public Square(int row, int col) {
        this.row = row;
        this.col = col;
        checkRep();
    }
    
    private void checkRep() {
        assert row >= 0;
        assert col >= 0;
    }

    public int getRow() {
        return row;
    }
    
    public int getCol() {
        return col;
    }
    
    @Override
    public String toString() {
        return row+","+col;
    }
    
    @Override
    public boolean equals(Object that) {
        return that instanceof Square && this.sameValue((Square) that);
    }
    
    private boolean sameValue(Square that) {
        return this.row == that.row && this.col == that.col;
    }
    
    @Override
    public int hashCode() {
        return row*col;
    }
}
