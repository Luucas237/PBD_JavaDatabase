import javax.swing.*;

public class MaterialCreationPanel extends JPanel {
    private MaterialManager materialManager;
    private JTextField materialField;
    private JTextField materialNoField;
    private JTextField piecesField;

    public MaterialCreationPanel(MaterialManager materialManager) {
        this.materialManager = materialManager;
        setLayout(null);

        JLabel createTitle = new JLabel("Add Material");
        createTitle.setBounds(100, 20, 200, 30);
        add(createTitle);

        materialField = createInputField("Material:", 70);
        materialNoField = createInputField("Material NO:", 120);
        piecesField = createInputField("Pieces:", 170);

        JButton confirmButton = new JButton("Confirm");
        confirmButton.setBounds(100, 220, 100, 30);
        confirmButton.addActionListener(e -> {
            String material = materialField.getText();
            String materialNo = materialNoField.getText();
            int pieces = Integer.parseInt(piecesField.getText());
            materialManager.saveToDatabase(material, materialNo, pieces);
        });
        add(confirmButton);
    }

    private JTextField createInputField(String label, int y) {
        JLabel jLabel = new JLabel(label);
        jLabel.setBounds(20, y, 100, 30);
        add(jLabel);

        JTextField textField = new JTextField();
        textField.setBounds(120, y, 200, 30);
        add(textField);
        return textField;
    }
}
