import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;

public class MainFrame extends JFrame {
    private MaterialManager materialManager;
    private JPanel leftPanel;
    private JPanel rightPanel;
    private JPanel headerPanel;
    private JPanel footerPanel;
    private JPanel blocksPanel; // Panel dla głównego obszaru bloków
    private Timer refreshTimer;
    private Map<Integer, Timer> createTimers;
    private List<Block> blocks;
    private int blockHeight = 100;
    private int blockWidth = 100;
    private int blockSpacing = 20;
    private int blockLimit = 3;
    private JComboBox<String> materialComboBox;
    private String createdItemName = null; // Przechowywanie utworzonego przedmiotu

    public MainFrame() {
        setTitle("Steel Plant SCADA Application");
        setSize(810, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        materialManager = new MaterialManager();
        createTimers = new HashMap<>();
        blocks = new ArrayList<>();

        // Nagłówek
        headerPanel = new JPanel();
        JLabel titleLabel = new JLabel("Steel Plant");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 24));
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Obszar główny (dla bloków i przedmiotów)
        blocksPanel = new JPanel(new GridLayout(1, 2)); // Podział na lewą i prawą sekcję
        add(blocksPanel, BorderLayout.CENTER);

        // Lewy panel
        leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawProductionArea(g);
            }
        };
        leftPanel.setLayout(null);
        blocksPanel.add(leftPanel);

        // Prawy panel (wyświetlanie utworzonych przedmiotów)
        rightPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawItem(g);
            }
        };
        blocksPanel.add(rightPanel);

        // Stopka (Nawigacja)
        footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        add(footerPanel, BorderLayout.SOUTH);

        // Dodanie rozwijanej listy z nazwami materiałów
        materialComboBox = new JComboBox<>();
        loadMaterialsToComboBox();
        footerPanel.add(materialComboBox);

        // Dodanie przycisków
        JButton addButton = new JButton("Add");
        JButton deleteButton = new JButton("Delete");
        JButton createButton = new JButton("Create");
        JButton stopButton = new JButton("Stop");
        JButton resetButton = new JButton("Reset");
        JButton showMaterialsButton = new JButton("Show Materials");
        JButton createItemButton = new JButton("Create Item");
        JButton showItemsButton = new JButton("Show Items");

        footerPanel.add(addButton);
        footerPanel.add(deleteButton);
        footerPanel.add(createButton);
        footerPanel.add(stopButton);
        footerPanel.add(resetButton);
        footerPanel.add(showMaterialsButton);
        footerPanel.add(createItemButton);
        footerPanel.add(showItemsButton);

        // Funkcjonalność przycisku Add
        addButton.addActionListener(e -> {
            String selectedMaterial = (String) materialComboBox.getSelectedItem();
            if (selectedMaterial != null) {
                int id = materialManager.getMaterialIdByName(selectedMaterial);
                if (id != -1 && blocks.size() < blockLimit && !isBlockExists(id)) {
                    addBlock(selectedMaterial, id);
                    checkIfItemCanBeCreated();
                } else if (isBlockExists(id)) {
                    JOptionPane.showMessageDialog(this, "Block with this material already exists on screen.", "Duplicate Block", JOptionPane.WARNING_MESSAGE);
                } else if (blocks.size() >= blockLimit) {
                    JOptionPane.showMessageDialog(this, "Maximum of 3 blocks allowed on screen.", "Limit Reached", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        // Inne funkcjonalności przycisków
        deleteButton.addActionListener(e -> {
            String selectedMaterial = (String) materialComboBox.getSelectedItem();
            if (selectedMaterial != null) {
                int id = materialManager.getMaterialIdByName(selectedMaterial);
                if (id != -1 && createTimers.get(id) == null) {
                    removeBlock(id);
                } else {
                    JOptionPane.showMessageDialog(this, "Cannot delete block with active Create operation.", "Delete Block", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        createButton.addActionListener(e -> {
            String selectedMaterial = (String) materialComboBox.getSelectedItem();
            if (selectedMaterial != null) {
                int id = materialManager.getMaterialIdByName(selectedMaterial);
                if (id != -1) {
                    startCreateTimer(id);
                }
            }
        });

        stopButton.addActionListener(e -> {
            String selectedMaterial = (String) materialComboBox.getSelectedItem();
            if (selectedMaterial != null) {
                int id = materialManager.getMaterialIdByName(selectedMaterial);
                if (id != -1) {
                    stopCreateTimer(id);
                }
            }
        });

        resetButton.addActionListener(e -> {
            String selectedMaterial = (String) materialComboBox.getSelectedItem();
            if (selectedMaterial != null) {
                int id = materialManager.getMaterialIdByName(selectedMaterial);
                if (id != -1) {
                    materialManager.resetMaterialPieces(id);
                }
            }
        });

        showMaterialsButton.addActionListener(e -> {
            MaterialsWindow materialsWindow = new MaterialsWindow(materialManager);
            materialsWindow.setVisible(true);
        });

        createItemButton.addActionListener(e -> {
            if (createdItemName != null) {
                boolean success = materialManager.reduceMaterialQuantities(createdItemName);
                if (success) {
                    JOptionPane.showMessageDialog(this, createdItemName + " has been successfully created!", "Item Created", JOptionPane.INFORMATION_MESSAGE);
                    materialManager.recordProductionLog(createdItemName);
                    createdItemName = null; // Resetowanie utworzonego przedmiotu
                    rightPanel.repaint();
                    leftPanel.repaint();
                } else {
                    JOptionPane.showMessageDialog(this, "Not enough materials to create " + createdItemName, "Creation Failed", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "No item ready to create.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        showItemsButton.addActionListener(e -> {
            new ItemsWindow(materialManager);
        });

        // Timer do automatycznego odświeżania ekranu co sekundę
        refreshTimer = new Timer(1000, e -> {
            leftPanel.repaint();
            rightPanel.repaint();
        });
        refreshTimer.start();
    }

    private void loadMaterialsToComboBox() {
        materialManager.loadMaterialsToComboBox(materialComboBox);
    }

// Dodawanie bloku

private void addBlock(String material, int id) {
    int y = calculateBlockYPosition();
    Block block = new Block(material, id, 50, y, 100, 100, Color.GRAY); // Użyj właściwego koloru, jeśli masz różne kolory
    blocks.add(block);
    leftPanel.repaint();
    checkIfItemCanBeCreated();
}


// Usuwanie bloku
private void removeBlock(int id) {
    blocks.removeIf(block -> block.getId() == id);
    adjustBlockPositions(); // Przesuwanie pozostałych bloków w górę
    leftPanel.repaint();
    checkIfItemCanBeCreated();
}

// Metoda do obliczenia Y dla nowego bloku
private int calculateBlockYPosition() {
    int y = 50;
    for (Block block : blocks) {
        y += block.getHeight() + 20; // Dodajemy odstęp między blokami
    }
    return y;
}

// Metoda do dostosowania pozycji bloków po usunięciu
private void adjustBlockPositions() {
    int y = 50;
    for (Block block : blocks) {
        block.setY(y);
        y += block.getHeight() + 20; // Dodajemy odstęp między blokami
    }
}
    

    private void checkIfItemCanBeCreated() {
        List<Integer> materialIds = new ArrayList<>();
        for (Block block : blocks) {
            materialIds.add(block.getId());
        }
    
        // Sprawdź, czy istnieje przepis z dokładnie odpowiednią liczbą materiałów
        String createdItem = materialManager.checkIfItemCanBeCreated(materialIds);
        if (createdItem != null) {
            // Pobierz przepis na utworzony przedmiot
            Map<String, Integer> recipe = materialManager.getRecipeForItem(createdItem);
            boolean allMaterialsPresent = true;
    
            // Sprawdź, czy wszystkie materiały z przepisu są nadal obecne na ekranie
            for (Map.Entry<String, Integer> entry : recipe.entrySet()) {
                int materialId = materialManager.getMaterialIdByName(entry.getKey());
                if (!materialIds.contains(materialId)) {
                    allMaterialsPresent = false;
                    break;
                }
            }
    
            // Jeśli wszystkie materiały są nadal obecne na ekranie, ustaw nazwę przedmiotu
            if (allMaterialsPresent) {
                createdItemName = createdItem;
            } 
        } 
        // Odśwież panel, aby wyświetlić lub usunąć przedmiot
        rightPanel.repaint();
    }
    
    
    

    private boolean isBlockExists(int id) {
        return blocks.stream().anyMatch(block -> block.getId() == id);
    }

    private void drawProductionArea(Graphics g) {
        // Rysowanie dynamicznych bloków z listy
        for (Block block : blocks) {
            block.draw(g, materialManager);
        }
    }

// Metoda rysowania przedmiotu i przepisu
private void drawItem(Graphics g) {
    if (createdItemName != null) {
        g.setColor(Color.ORANGE); // Kolor utworzonego przedmiotu
        g.fillRect(50, 50, 150, 100); // Rysowanie bloku przedmiotu
        g.setColor(Color.BLACK);
        g.drawString(createdItemName, 70, 90); // Wyświetlenie nazwy przedmiotu

        // Pobieranie przepisu na utworzony przedmiot
        Map<String, Integer> recipe = materialManager.getRecipeForItem(createdItemName);

        // Rysowanie przepisu
        int yPosition = 180;
        g.drawString("Recipe:", 50, yPosition);
        yPosition += 20;
        boolean canCreate = true;

        for (Map.Entry<String, Integer> entry : recipe.entrySet()) {
            String material = entry.getKey();
            int quantityNeeded = entry.getValue();
            int currentQuantity = materialManager.getPiecesById(materialManager.getMaterialIdByName(material));

            String status = currentQuantity >= quantityNeeded ? " (OK)" : " (Not enough)";
            if (currentQuantity < quantityNeeded) {
                canCreate = false;
            }

            g.drawString(quantityNeeded + " x " + material + ": " + currentQuantity + status, 50, yPosition);
            yPosition += 20;
        }

        g.setColor(canCreate ? Color.GREEN : Color.RED);
        g.drawString(canCreate ? "Ready to create!" : "Not enough materials!", 50, yPosition);
    }
}

    private void startCreateTimer(int id) {
        Timer createTimer = new Timer(1000, e -> materialManager.increaseMaterialPieces(id));
        createTimers.put(id, createTimer);
        createTimer.start();
    }

    private void stopCreateTimer(int id) {
        Timer createTimer = createTimers.remove(id);
        if (createTimer != null) {
            createTimer.stop();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }

    class Block {
        private String material;
        private int id, x, y, width, height;
        private Color color;
    
        public Block(String material, int id, int x, int y, int width, int height, Color color) {
            this.material = material;
            this.id = id;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.color = color;
        }
    
        public String getMaterial() {
            return material;
        }
    
        public int getId() {
            return id;
        }
    
        public void setY(int y) {
            this.y = y;
        }
    
        public int getHeight() { // Dodanie metody getHeight
            return this.height;
        }
    
        public void draw(Graphics g, MaterialManager materialManager) {
            g.setColor(color);
            g.fillRect(x, y, width, height);
            g.setColor(Color.BLACK);
            g.drawString(material, x + 10, y + 20);
    
            // Rysowanie linii produkcyjnej
            g.drawLine(x + width, y + height / 2, x + width + 150, y + height / 2);
    
            // Pobranie liczby elementów (pieces) z bazy danych
            int piecesCount = materialManager.getPiecesById(id);
            g.drawString("pieces: " + piecesCount, x + width + 160, y + height / 2 + 5);
        }
    }
    
}
