import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ApproveBookingsForm extends JDialog {
    private int userId;
    DefaultTableModel tableModel;
    JTable bookingsTable;

    public ApproveBookingsForm(int userId) {
        this.userId = userId;
        setTitle("CSE Classroom & Lab Management - Approve Bookings");
        setSize(500, 300);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"ID", "Room", "User", "Start Time", "End Time", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        bookingsTable = new JTable(tableModel);
        bookingsTable.setBackground(Color.WHITE);
        loadBookings();
        panel.add(new JScrollPane(bookingsTable), BorderLayout.CENTER);

        JButton approveButton = new JButton("Approve");
        approveButton.setBackground(new Color(0, 123, 255));
        approveButton.setForeground(Color.WHITE);
        JButton backButton = new JButton("Back");
        backButton.setBackground(Color.GRAY);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(approveButton);
        buttonPanel.add(backButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        approveButton.addActionListener(e -> approveSelectedBooking());
        backButton.addActionListener(e -> {
            dispose();
            new AdminDashboard(userId).setVisible(true);
        });

        add(panel);
        setVisible(true);
    }

    void loadBookings() {
        tableModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT b.id, r.room_number, u.username, b.start_time, b.end_time, b.status " +
                    "FROM bookings b JOIN rooms r ON b.room_id = r.id JOIN users u ON b.user_id = u.id " +
                    "WHERE b.status = 'pending'";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("room_number"),
                        rs.getString("username"),
                        rs.getString("start_time"),
                        rs.getString("end_time"),
                        rs.getString("status")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading bookings: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    void approveSelectedBooking() {
        int selectedRow = bookingsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking to approve.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int bookingId = (int) tableModel.getValueAt(selectedRow, 0);
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE bookings SET status = 'approved' WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, bookingId);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Booking approved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadBookings(); // Refresh table
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Approval failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ApproveBookingsForm(1).setVisible(true));
    }
}