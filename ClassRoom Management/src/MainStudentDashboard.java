import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MainStudentDashboard extends JFrame {
    private int userId;
    private JButton searchRoomsButton, viewTimetableButton, submitFeedbackButton, logoutButton;
    private JButton selectedButton = null;

    public MainStudentDashboard(int userId) {
        this.userId = userId;
        setTitle("CSE Classroom & Lab Management - Student Dashboard");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Sidebar
        JPanel sidebar = new JPanel(new GridLayout(4, 1, 10, 10));
        sidebar.setBackground(new Color(248, 249, 250));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        searchRoomsButton = new JButton("Search Rooms");
        addIcon(searchRoomsButton, "/search.png");
        viewTimetableButton = new JButton("View Timetable");
        addIcon(viewTimetableButton, "/calendar.png");
        submitFeedbackButton = new JButton("Submit Feedback");
        addIcon(submitFeedbackButton, "/feedback.png");
        logoutButton = new JButton("Logout");
        addIcon(logoutButton, "/logout.png");

        sidebar.add(searchRoomsButton);
        sidebar.add(viewTimetableButton);
        sidebar.add(submitFeedbackButton);
        sidebar.add(logoutButton);

        // Style buttons
        for (JButton button : new JButton[]{searchRoomsButton, viewTimetableButton, submitFeedbackButton, logoutButton}) {
            button.setFont(new Font("Arial", Font.PLAIN, 12));
            button.setBackground(new Color(108, 117, 125));
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    if (button != selectedButton) button.setBackground(new Color(90, 99, 107));
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    if (button != selectedButton) button.setBackground(new Color(108, 117, 125));
                }
            });
            button.addActionListener(e -> {
                if (selectedButton != null) selectedButton.setBackground(new Color(108, 117, 125));
                selectedButton = button;
                selectedButton.setBackground(new Color(0, 123, 255));
                if (e.getSource() == searchRoomsButton) {
                    dispose();
                    new RoomSearchScreen(userId).setVisible(true);
                } else if (e.getSource() == viewTimetableButton) {
                    JComboBox<String> roomCombo = new JComboBox<>();
                    loadRooms(roomCombo);
                    int result = JOptionPane.showConfirmDialog(this, roomCombo, "Select Room for Timetable", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    if (result == JOptionPane.OK_OPTION) {
                        String selectedRoom = (String) roomCombo.getSelectedItem();
                        if (selectedRoom != null) {
                            try (Connection conn = DBConnection.getConnection()) {
                                String sql = "SELECT id FROM rooms WHERE room_number = ?";
                                PreparedStatement stmt = conn.prepareStatement(sql);
                                stmt.setString(1, selectedRoom);
                                ResultSet rs = stmt.executeQuery();
                                if (rs.next()) {
                                    int roomId = rs.getInt("id");
                                    dispose();
                                    new TimetableScreen(userId, roomId, selectedRoom).setVisible(true);
                                }
                            } catch (SQLException ex) {
                                JOptionPane.showMessageDialog(this, "Error loading room: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                } else if (e.getSource() == submitFeedbackButton) {
                    dispose();
                    new FeedbackForm(userId).setVisible(true);
                } else if (e.getSource() == logoutButton) {
                    dispose();
                    new LoginFrame().setVisible(true);
                }
            });
        }

        // Main Panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(248, 249, 250));
        String username = getUsername(userId);
        JLabel welcomeLabel = new JLabel("Welcome to Student Dashboard, " + (username != null ? username : "User") + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.add(welcomeLabel, BorderLayout.CENTER);

        add(sidebar, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    private void addIcon(JButton button, String iconPath) {
        try {
            button.setIcon(new ImageIcon(getClass().getResource(iconPath)));
            button.setHorizontalTextPosition(SwingConstants.RIGHT);
            button.setVerticalTextPosition(SwingConstants.CENTER);
        } catch (Exception e) {
            System.err.println("Icon not found: " + iconPath);
        }
    }

    void loadRooms(JComboBox<String> combo) {
        combo.removeAllItems();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT room_number FROM rooms";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                combo.addItem(rs.getString("room_number"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading rooms: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    String getUsername(int userId) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT username FROM users WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("username");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching username: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainStudentDashboard(1).setVisible(true));
    }
}