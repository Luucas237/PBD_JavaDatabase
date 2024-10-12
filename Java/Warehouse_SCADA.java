import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

public class AnimatedMaterialWindow {
    private static final int WINDOW_WIDTH = 500;
    private static final int WINDOW_HEIGHT = 400;   
    private static final int BOX_SIZE = 20; // Rozmiar kwadratu
    private static final int CONVEYOR_HEIGHT = 20; // Wysokość taśmy
    private static final int INTERVAL = 1000; // Interwał czasowy w ms dla dodawania nowych pudełek

    private static ArrayList<Integer> boxes = new ArrayList<>(); // Lista pozycji kwadratów
    private static Timer boxTimer; // Timer do dodawania pudełek
    private static Timer moveTimer; // Timer do poruszania pudełkami

    // Połączenie z bazą danych
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/Armory";
    private static final String USER = "postgres";
    private static final String PASSWORD = "QWER";

    public static void main(String[] args) {
        // Tworzenie okna
        JFrame frame = new JFrame("Aplikacja Magazynowa z Animacją");
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null); // Brak domyślnego layoutu, ustawianie ręczne pozycji

        // Tworzenie przycisku "Exit" w prawym górnym rogu
        JButton exitButton = new JButton("Exit");
        exitButton.setBounds(400, 20, 80, 30);
        exitButton.addActionListener(e -> System.exit(0));
        frame.add(exitButton);

        // Etykiety i pola tekstowe dla "Material", "Material NO" oraz "Pieces"
        JTextField materialField = createInputField(frame, "Material:", 50);
        JTextField materialNoField = createInputField(frame, "Material NO:", 100);
        JTextField piecesField = createInputField(frame, "Pieces:", 150);

        // Przycisk "Confirm"
        JButton confirmButton = new JButton("Confirm");
        confirmButton.setBounds(120, 200, 100, 30);
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Czyszczenie listy pudełek i uruchamianie animacji
                boxes.clear();
                int numberOfBoxes = Integer.parseInt(piecesField.getText());
                addBoxes(numberOfBoxes);
                
                // Zapis danych do bazy danych
                String material = materialField.getText();
                String materialNo = materialNoField.getText();
                saveToDatabase(material, materialNo, numberOfBoxes);
            }
        });
        frame.add(confirmButton);

        // Ustawienie panelu do rysowania animacji
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
        moveTimer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveBoxes();
                animationPanel.repaint();
            }
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
        return textField; // Zwracamy pole tekstowe dla późniejszego użycia
    }

    private static void drawConveyor(Graphics g) {
        g.setColor(Color.GRAY);
        g.fillRect(0, 0, WINDOW_WIDTH, CONVEYOR_HEIGHT); // Taśma produkcyjna rozciągnięta na całą szerokość
    }

    private static void drawBoxes(Graphics g) {
        g.setColor(Color.BLUE);
        for (int x : boxes) {
            g.fillRect(x, CONVEYOR_HEIGHT - BOX_SIZE, BOX_SIZE, BOX_SIZE); // Kwadrat poruszający się na taśmie
        }
    }

    private static void addBoxes(int numberOfBoxes) {
        boxTimer = new Timer(INTERVAL, new ActionListener() {
            private int count = 0; // Licznik dodawanych pudełek
            private int boxX = 0; // Początkowa pozycja dla każdego nowego pudełka

            @Override
            public void actionPerformed(ActionEvent e) {
                if (count < numberOfBoxes) {
                    boxes.add(boxX); // Dodanie nowego pudełka
                    boxX += BOX_SIZE + 5; // Przesunięcie pozycji dla następnego pudełka
                    count++;
                } else {
                    boxTimer.stop(); // Zatrzymanie timera po dodaniu wszystkich pudełek
                }
            }
        });
        boxTimer.start();
    }

    private static void moveBoxes() {
        // Poruszanie pudełkami po taśmie
        for (int i = 0; i < boxes.size(); i++) {
            boxes.set(i, boxes.get(i) + 2); // Prędkość poruszania
            if (boxes.get(i) > WINDOW_WIDTH) { // Jeżeli osiągnie koniec taśmy
                boxes.remove(i); // Usunięcie pudełka
                i--; // Korekta indeksu
            }
        }
    }

    private static void saveToDatabase(String material, String materialNo, int pieces) {
        // Tworzenie połączenia z bazą danych
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "INSERT INTO materials (material, material_no, pieces) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, material);
                pstmt.setString(2, materialNo);
                pstmt.setInt(3, pieces);
                pstmt.executeUpdate(); // Wykonanie zapytania
                System.out.println("Dane zostały zapisane w bazie danych.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Wystąpił błąd podczas zapisu do bazy danych.");
        }
    }
}
