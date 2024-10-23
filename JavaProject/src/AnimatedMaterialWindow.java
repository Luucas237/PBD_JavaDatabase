import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;

public class AnimatedMaterialWindow {
    private static final int WINDOW_WIDTH = 500;
    private static final int WINDOW_HEIGHT = 400;   
    private static final int BOX_SIZE = 20;
    private static final int CONVEYOR_HEIGHT = 20;
    private static final int INTERVAL = 1000;
    private static ArrayList<Integer> boxes = new ArrayList<>();
    private static Timer boxTimer;
    private static Timer moveTimer;

    // Połączenie z bazą danych
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/Armory";
    private static final String USER = "postgres";
    private static final String PASSWORD = "QWER";

    public static void main(String[] args) {
        // Tworzenie głównego okna
        JFrame frame = new JFrame("Aplikacja Magazynowa z Animacją");
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);

        // Przycisk "Exit"
        JButton exitButton = new JButton("Exit");
        exitButton.setBounds(400, 20, 80, 30);
        exitButton.addActionListener(e -> System.exit(0));
        frame.add(exitButton);

        // Pola tekstowe
        JTextField materialField = createInputField(frame, "Material:", 50);
        JTextField materialNoField = createInputField(frame, "Material NO:", 100);
        JTextField piecesField = createInputField(frame, "Pieces:", 150);

        // Przycisk "Confirm"
        JButton confirmButton = new JButton("Confirm");
        confirmButton.setBounds(120, 200, 100, 30);
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boxes.clear();
                int numberOfBoxes = Integer.parseInt(piecesField.getText());
                addBoxes(numberOfBoxes);

                String material = materialField.getText();
                String materialNo = materialNoField.getText();
                saveToDatabase(material, materialNo, numberOfBoxes);
            }
        });
        frame.add(confirmButton);

        // Przycisk "Show Materials"
        JButton showMaterialsButton = new JButton("Show Materials");
        showMaterialsButton.setBounds(240, 200, 150, 30);
        showMaterialsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showMaterialsWindow();
            }
        });
        frame.add(showMaterialsButton);

        // Panel animacji
        JPanel animationPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawConveyor(g);
                drawBoxes(g);
            }
        };
        animationPanel.setBounds(0, 300, WINDOW_WIDTH, 100);
        frame.add(animationPanel);

        // Timer do poruszania pudełkami
        moveTimer = new Timer(50, e -> {
            moveBoxes();
            animationPanel.repaint();
        });
        moveTimer.start();

        // Wyświetlenie okna
        frame.setVisible(true);
    }

    private static JTextField createInputField(JFrame frame, String label, int y) {
        JLabel jLabel = new JLabel(label);
        jLabel.setBounds(20, y, 100, 30);
        frame.add(jLabel);

        JTextField textField = new JTextField();
        textField.setBounds(120, y, 200, 30);
        frame.add(textField);
        return textField;
    }

    private static void drawConveyor(Graphics g) {
        g.setColor(Color.GRAY);
        g.fillRect(0, 0, WINDOW_WIDTH, CONVEYOR_HEIGHT);
    }

    private static void drawBoxes(Graphics g) {
        g.setColor(Color.BLUE);
        for (int x : boxes) {
            g.fillRect(x, CONVEYOR_HEIGHT - BOX_SIZE, BOX_SIZE, BOX_SIZE);
        }
    }

    private static void addBoxes(int numberOfBoxes) {
        boxTimer = new Timer(INTERVAL, new ActionListener() {
            private int count = 0;
            private int boxX = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (count < numberOfBoxes) {
                    boxes.add(boxX);
                    boxX += BOX_SIZE + 5;
                    count++;
                } else {
                    boxTimer.stop();
                }
            }
        });
        boxTimer.start();
    }

    private static void moveBoxes() {
        for (int i = 0; i < boxes.size(); i++) {
            boxes.set(i, boxes.get(i) + 2);
            if (boxes.get(i) > WINDOW_WIDTH) {
                boxes.remove(i);
                i--;
            }
        }
    }

    private static void saveToDatabase(String material, String materialNo, int pieces) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "INSERT INTO materials (material, material_no, pieces) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, material);
                pstmt.setString(2, materialNo);
                pstmt.setInt(3, pieces);
                pstmt.executeUpdate();
                System.out.println("Dane zostały zapisane w bazie danych.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Wystąpił błąd podczas zapisu do bazy danych.");
        }
    }

    private static void showMaterialsWindow() {
        JFrame materialsFrame = new JFrame("Materials");
        materialsFrame.setSize(600, 400);
        materialsFrame.setLayout(new BorderLayout());
    
        String[] columnNames = {"ID", "Material", "Material NO", "Pieces"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0; // Make all cells editable except for the ID column
            }
        };
    
        // Pobieranie danych z bazy i dodawanie do modelu tabeli
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "SELECT * FROM materials";
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String material = rs.getString("material");
                    String materialNo = rs.getString("material_no");
                    int pieces = rs.getInt("pieces");
                    model.addRow(new Object[]{id, material, materialNo, pieces});
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        JTable materialsTable = new JTable(model);
        materialsTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); // Commit changes on focus loss
        JScrollPane scrollPane = new JScrollPane(materialsTable);
        materialsFrame.add(scrollPane, BorderLayout.CENTER);
    
        // Panel przycisków
        JPanel buttonPanel = new JPanel();
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
    
        // Aktualizacja danych w bazie
        updateButton.addActionListener(e -> {
            int selectedRow = materialsTable.getSelectedRow();
            if (selectedRow != -1) {
                int id = (int) materialsTable.getValueAt(selectedRow, 0);
                String material = (String) materialsTable.getValueAt(selectedRow, 1);
                String materialNo = (String) materialsTable.getValueAt(selectedRow, 2);
                int pieces = (int) materialsTable.getValueAt(selectedRow, 3);
    
                updateMaterialInDatabase(id, material, materialNo, pieces);
            }
        });
    
        // Usuwanie danych z bazy
        deleteButton.addActionListener(e -> {
            int selectedRow = materialsTable.getSelectedRow();
            if (selectedRow != -1) {
                int id = (int) materialsTable.getValueAt(selectedRow, 0);
    
                // Usuń rekord z bazy danych
                deleteMaterialFromDatabase(id);
    
                // Usuń wiersz z modelu tabeli
                model.removeRow(selectedRow);
            }
        });
    
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        materialsFrame.add(buttonPanel, BorderLayout.SOUTH);
    
        materialsFrame.setVisible(true);
    }
    
    private static void deleteMaterialFromDatabase(int id) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "DELETE FROM materials WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                System.out.println("Rekord został usunięty.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Wystąpił błąd podczas usuwania rekordu.");
        }
    }
    
    private static void updateMaterialInDatabase(int id, String material, String materialNo, int pieces) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "UPDATE materials SET material = ?, material_no = ?, pieces = ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, material);
                pstmt.setString(2, materialNo);
                pstmt.setInt(3, pieces);
                pstmt.setInt(4, id);
                pstmt.executeUpdate();
                System.out.println("Dane zostały zaktualizowane.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Wystąpił błąd podczas aktualizacji danych.");
        }
    }
}
