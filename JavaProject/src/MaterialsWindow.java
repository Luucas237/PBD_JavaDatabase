import java.awt.*;
import javax.swing.*;

public class MaterialsWindow extends JFrame {
    public MaterialsWindow(MaterialManager materialManager) {
        setTitle("Materials Management");
        setSize(800, 400);
        setLayout(new GridLayout(1, 2));

        // Tworzenie panelu do tworzenia materiałów
        MaterialCreationPanel creationPanel = new MaterialCreationPanel(materialManager);
        add(creationPanel);

        // Tworzenie panelu tabeli materiałów
        MaterialTablePanel tablePanel = new MaterialTablePanel(materialManager);
        add(tablePanel);

        setVisible(true);
    }
}
