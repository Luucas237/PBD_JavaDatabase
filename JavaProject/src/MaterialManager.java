import java.sql.*;
import java.util.HashMap;
import java.util.List; // Import klasy List
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableModel; // Import klasy ArrayList

public class MaterialManager {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/Armory";
    private static final String USER = "postgres";
    private static final String PASSWORD = "QWER";



    // Metoda do zwiększania ilości sztuk materiału (pieces)
    public void increaseMaterialPieces(int id) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "UPDATE materials SET pieces = pieces + 1 WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                System.out.println("Zwiększono ilość Pieces dla ID " + id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Metoda do resetowania ilości sztuk materiału (pieces)
    public void resetMaterialPieces(int id) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "UPDATE materials SET pieces = 0 WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                System.out.println("Zresetowano ilość Pieces dla ID " + id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Metoda do zapisywania materiału do bazy danych
    public void saveToDatabase(String material, String materialNo, int pieces) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "INSERT INTO materials (material, material_no, pieces) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, material);
                pstmt.setString(2, materialNo);
                pstmt.setInt(3, pieces);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(null, "Material saved successfully.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error saving material to database.");
        }
    }

    // Metoda do aktualizacji materiału w bazie danych
    public void updateMaterial(int id, String material, String materialNo, int pieces) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "UPDATE materials SET material = ?, material_no = ?, pieces = ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, material);
                pstmt.setString(2, materialNo);
                pstmt.setInt(3, pieces);
                pstmt.setInt(4, id);
                pstmt.executeUpdate();
                System.out.println("Material updated successfully.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Metoda do usunięcia materiału z bazy danych
    public void deleteMaterial(int id) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "DELETE FROM materials WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                System.out.println("Material deleted successfully.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Metoda do załadowania materiałów do tabeli
    public void loadMaterials(DefaultTableModel model) {
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
    }

    // Dodatkowe metody do pobierania nazwy materiału i liczby sztuk
    public String getMaterialNameById(int id) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "SELECT material FROM materials WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getString("material");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "error";
    }


    public void loadMaterialsToComboBox(JComboBox<String> comboBox) {
        comboBox.removeAllItems();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "SELECT material FROM materials";
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    comboBox.addItem(rs.getString("material"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getMaterialIdByName(String materialName) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "SELECT id FROM materials WHERE material = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, materialName);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int getPiecesById(int id) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "SELECT pieces FROM materials WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("pieces");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public String checkIfItemCanBeCreated(List<Integer> materialIds) {
        // Sprawdzenie, czy materiały pasują do jakiegokolwiek przedmiotu
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            // Dynamiczne tworzenie zapytania SQL w zależności od liczby dostępnych materiałów
            String sql = "SELECT i.item_name FROM items i " +
                         "JOIN itemmaterials im ON i.id = im.item_id " +
                         "WHERE im.material_id IN (?, ?, ?) " +
                         "GROUP BY i.item_name " +
                         "HAVING COUNT(DISTINCT im.material_id) = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                int size = materialIds.size();
                if (size > 3) size = 3; // Ogranicz do maksymalnie trzech materiałów
    
                // Ustaw wartości materiałów, jeśli istnieją
                for (int i = 0; i < size; i++) {
                    pstmt.setInt(i + 1, materialIds.get(i));
                }
    
                // Uzupełnij brakujące parametry wartościami 0, aby uniknąć błędów
                for (int i = size; i < 3; i++) {
                    pstmt.setInt(i + 1, 0); // 0 lub dowolna nieużywana wartość materiału
                }
    
                // Ustaw liczbę unikalnych materiałów wymaganych do utworzenia przedmiotu
                pstmt.setInt(4, size);
    
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getString("item_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    

    public Map<String, Integer> getRecipeForItem(String itemName) {
        Map<String, Integer> recipe = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "SELECT m.material, im.quantity_needed " +
                         "FROM items i " +
                         "JOIN itemmaterials im ON i.id = im.item_id " +
                         "JOIN materials m ON m.id = im.material_id " +
                         "WHERE i.item_name = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, itemName);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    String material = rs.getString("material");
                    int quantityNeeded = rs.getInt("quantity_needed");
                    recipe.put(material, quantityNeeded);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return recipe;
    }

    public boolean reduceMaterialQuantities(String itemName) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            // Pierwszy krok: sprawdzenie dostępności materiałów
            String sqlCheck = "SELECT im.material_id, im.quantity_needed, m.pieces " +
                              "FROM items i " +
                              "JOIN itemmaterials im ON i.id = im.item_id " +
                              "JOIN materials m ON m.id = im.material_id " +
                              "WHERE i.item_name = ?";
            try (PreparedStatement pstmtCheck = conn.prepareStatement(sqlCheck)) {
                pstmtCheck.setString(1, itemName);
                ResultSet rsCheck = pstmtCheck.executeQuery();
    
                // Sprawdzenie, czy wszystkie materiały są dostępne w odpowiedniej ilości
                while (rsCheck.next()) {
                    int quantityNeeded = rsCheck.getInt("quantity_needed");
                    int currentQuantity = rsCheck.getInt("pieces");
    
                    if (currentQuantity < quantityNeeded) {
                        return false; // Brak wystarczającej ilości materiału
                    }
                }
            }
    
            // Drugi krok: redukcja ilości materiałów
            String sqlUpdate = "UPDATE materials SET pieces = pieces - ? WHERE id = ?";
            try (PreparedStatement pstmtUpdate = conn.prepareStatement(sqlUpdate)) {
                try (PreparedStatement pstmtCheck = conn.prepareStatement(sqlCheck)) {
                    pstmtCheck.setString(1, itemName);
                    ResultSet rsUpdate = pstmtCheck.executeQuery();
    
                    // Wykonaj redukcję dla każdego materiału
                    while (rsUpdate.next()) {
                        int materialId = rsUpdate.getInt("material_id");
                        int quantityNeeded = rsUpdate.getInt("quantity_needed");
    
                        pstmtUpdate.setInt(1, quantityNeeded);
                        pstmtUpdate.setInt(2, materialId);
                        pstmtUpdate.executeUpdate();
                    }
                }
            }
            return true; // Materiały zredukowane pomyślnie
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    

    public void recordProductionLog(String itemName) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "INSERT INTO ProductionLog (item_name, production_time) VALUES (?, NOW())";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, itemName);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void loadItems(DefaultTableModel model) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "SELECT id, item_name, production_time FROM ProductionLog";
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String itemName = rs.getString("item_name");
                    Timestamp productionTime = rs.getTimestamp("production_time");
                    model.addRow(new Object[]{id, itemName, productionTime});
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
}
