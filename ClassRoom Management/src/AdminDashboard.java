
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminDashboard extends JFrame {
    private int userId;

    public AdminDashboard(int userId) {
        this.userId = userId;
        setTitle("CSE Classroom & Lab Management - Admin Dashboard");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main Panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(248, 249, 250));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Sidebar
        JPanel sidebar = new JPanel(new GridLayout(4, 1, 10, 10));
        sidebar.setBackground(new Color(248, 249, 250));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton manageScheduleButton = new JButton("Manage Schedule");
        JButton approveBookingsButton = new JButton("Approve Bookings");
        JButton viewUsageReportButton = new JButton("View Usage Report");
        JButton logoutButton = new JButton("Logout");

        sidebar.add(manageScheduleButton);
        sidebar.add(approveBookingsButton);
        sidebar.add(viewUsageReportButton);
        sidebar.add(logoutButton);

        for (JButton button : new JButton[]{manageScheduleButton, approveBookingsButton, viewUsageReportButton, logoutButton}) {
            button.setFont(new Font("Arial", Font.PLAIN, 12));
            button.setBackground(new Color(108, 117, 125));
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.addActionListener(e -> {
                if (e.getSource() == manageScheduleButton) {
                    dispose();
                    new ManageScheduleForm(userId).setVisible(true);
                } else if (e.getSource() == approveBookingsButton) {
                    dispose();
                    new ApproveBookingsForm(userId).setVisible(true);
                } else if (e.getSource() == viewUsageReportButton) {
                    showUsageReport();
                } else if (e.getSource() == logoutButton) {
                    dispose();
                    new LoginFrame().setVisible(true);
                }
            });
        }

        // Welcome Label
        JLabel welcomeLabel = new JLabel("Welcome to Admin Dashboard!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.add(welcomeLabel, BorderLayout.NORTH);

        mainPanel.add(sidebar, BorderLayout.WEST);
        add(mainPanel);
        setVisible(true);
    }

    private void showUsageReport() {
        JDialog dialog = new JDialog(this, "Room Utilization Report", true);
        dialog.setSize(500, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Room Number", "Total Bookings", "Peak Day", "Usage %"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable reportTable = new JTable(model);
        reportTable.setBackground(Color.WHITE);
        reportTable.setFont(new Font("Arial", Font.PLAIN, 12));
        reportTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(new JScrollPane(reportTable), BorderLayout.CENTER);

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT r.room_number, " +
                    "COUNT(b.id) as total_bookings, " +
                    "DAYNAME(MAX(b.start_time)) as peak_day, " +
                    "ROUND((COUNT(b.id) / (SELECT COUNT(*) * 7 FROM schedules WHERE room_id = r.id)) * 100, 2) as usage_percent " +
                    "FROM rooms r LEFT JOIN bookings b ON r.id = b.room_id AND b.status = 'approved' " +
                    "GROUP BY r.id, r.room_number";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("room_number"),
                        rs.getInt("total_bookings"),
                        rs.getString("peak_day"),
                        rs.getDouble("usage_percent") + "%"
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(dialog, "Error loading report: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        JButton closeButton = new JButton("Close");
        closeButton.setBackground(new Color(108, 117, 125));
        closeButton.setForeground(Color.WHITE);
        closeButton.addActionListener(e -> dialog.dispose());
        panel.add(closeButton, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminDashboard(1).setVisible(true));
    }
}
