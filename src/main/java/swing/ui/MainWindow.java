package swing.ui;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
    public MainWindow(ContextBar contextBar, JTabbedPane tabs) {
        super("Marketplace (Swing Modular)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);

        getContentPane().setLayout(new BorderLayout());
        JPanel north = new JPanel(new BorderLayout());
        north.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        north.add(contextBar, BorderLayout.CENTER);
        getContentPane().add(north, BorderLayout.NORTH);
        getContentPane().add(tabs, BorderLayout.CENTER);
    }
}
