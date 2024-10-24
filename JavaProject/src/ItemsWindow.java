import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ItemsWindow extends JFrame {
    public ItemsWindow(MaterialManager materialManager) {
        setTitle("Created Items");
        setSize(600, 400);
        setLayout(new BorderLayout());

        String[] columnNames = {"ID", "Item Name", "Creation Time"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        JTable itemsTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(itemsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Ładowanie przedmiotów z tabeli `ProductionLog`
        materialManager.loadItems(model);

        // Ustawienie domyślnych opcji zamknięcia i widoczności okna
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }
}

