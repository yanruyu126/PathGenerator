
public class ItemNode extends Node {
    boolean isTarget;
    boolean checked;

    ItemNode(int row, int col, int type, boolean isTarget) {
        super(row, col, type);
        this.isTarget= isTarget;
        checked= false;
    }

}