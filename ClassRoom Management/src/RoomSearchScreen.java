import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RoomSearchScreen extends JDialog {
    private int userId;
    JTextField roomNumberField;
    JButton searchButton, backButton;

    public RoomSearchScreen(int userId) {
        this.userId = userId;
        setTitle("CSE Classroom & Lab Management - Search Rooms");
        setSize(400, 200);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Room Number:"), gbc);
        gbc.gridx = 1;
        roomNumberField = new JTextField(10);
        panel.add(roomNumberField, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        searchButton = new JButton("Search");
        searchButton.setBackground(new Color(0, 123, 255));
        searchButton.setForeground(Color.WHITE);
        panel.add(searchButton, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        backButton = new JButton("Back");
        backButton.setBackground(Color.GRAY);
        panel.add(backButton, gbc);

        searchButton.addActionListener(e -> performSearch());
        backButton.addActionListener(e -> {
            dispose();
            new MainStudentDashboard(userId).setVisible(true);
        });

        add(panel);
        setVisible(true);
    }

    void performSearch() {
        String roomNumber = roomNumberField.getText().trim();
        if (roomNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a room number.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT id, type FROM rooms WHERE room_number = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, roomNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int roomId = rs.getInt("id");
                String type = rs.getString("type");
                dispose();
                new RoomDetailsScreen(userId, roomId, roomNumber, type).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Room not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RoomSearchScreen(1).setVisible(true));
    }
}