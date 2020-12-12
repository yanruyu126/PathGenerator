import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class GUI extends JFrame {
    private static final long serialVersionUID= 1L;

    public GUI() throws IOException {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(R.Frame_Size, R.Frame_Size);
        setResizable(false);
        setTitle("Path Generator");

        init();
    }

    public void init() throws IOException {
        setLocationRelativeTo(null);

        setLayout(new GridLayout(1, 1, 0, 0));

        BufferedImage image;
        image= ImageIO.read(new File("src/testImage.png"));
        Map myMap= new Map(image);
        add(new Panel(myMap));

        setVisible(true);
    }

    public static void main(String[] args) throws IOException {
        new GUI();

    }
}
