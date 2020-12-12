import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

public class Panel extends JPanel implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID= 1L;
    Map myMap;

    public Panel(Map m) {
        myMap= m;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d= (Graphics2D) g;

        myMap.draw(g2d);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        repaint();
    }
}
