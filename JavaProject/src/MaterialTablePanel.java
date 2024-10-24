import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class MaterialTablePanel extends JPanel {
    private MaterialManager materialManager;
    private DefaultTableModel model;

    public MaterialTablePanel(MaterialManager materialManager) {
        this.materialManager = materialManager;
        setLayout(new BorderLayout());

        String[] columnNames = {"ID", "Material", "Material NO", "Pieces"};
        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0; // Make all cells editable except for the ID column
            }
        };

        JTable materialsTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(materialsTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");

        updateButton.addActionListener(e -> {
            int selectedRow = materialsTable.getSelectedRow();
            if (selectedRow != -1) {
                int id = (int) materialsTable.getValueAt(selectedRow, 0);
                String material = (String) materialsTable.getValueAt(selectedRow, 1);
                String materialNo = (String) materialsTable.getValueAt(selectedRow, 2);
                int pieces = Integer.parseInt(materialsTable.getValueAt(selectedRow, 3).toString());
                materialManager.updateMaterial(id, material, materialNo, pieces);
            }
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = materialsTable.getSelectedRow();
            if (selectedRow != -1) {
                int id = (int) materialsTable.getValueAt(selectedRow, 0);
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to delete this element?", "Delete Confirmation",
                        JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    materialManager.deleteMaterial(id);
                    model.removeRow(selectedRow);
                }
            }
        });

        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Ładowanie materiałów przy uruchomieniu
        loadMaterials();
    }

    // Metoda do odświeżania tabeli
    public void loadMaterials() {
        model.setRowCount(0); // Czyszczenie obecnych wierszy
        materialManager.loadMaterials(model); // Ponowne załadowanie danych z bazy
    }
}
