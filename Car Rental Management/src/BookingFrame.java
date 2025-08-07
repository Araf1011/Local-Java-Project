import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class BookingFrame extends JFrame {
    private final int userId, carId;
    private final String pickUpDate, dropOffDate;
    private JLabel totalCostLabel;
    private JButton confirmButton, cancelButton;

    public BookingFrame(int userId, int carId, String pickUpDate, String dropOffDate) {
        this.userId = userId;
        this.carId = carId;
        this.pickUpDate = pickUpDate;
        this.dropOffDate = dropOffDate;
        setTitle("Car Rental Management - Confirm Booking");
        setSize(350, 200);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Pick-up Date:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(pickUpDate), gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Drop-off Date:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(dropOffDate), gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Total Cost:"), gbc);
        gbc.gridx = 1;
        totalCostLabel = new JLabel("[Calculating...]");
        panel.add(totalCostLabel, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        confirmButton = new JButton("Confirm Booking");
        confirmButton.setBackground(new Color(0, 123, 255));
        confirmButton.setForeground(Color.WHITE);
        cancelButton = new JButton("Cancel");
        cancelButton.setBackground(Color.GRAY);
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);

        add(panel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        calculateTotalCost();
        confirmButton.addActionListener(e -> confirmBooking());
        cancelButton.addActionListener(e -> dispose());

        setVisible(true);
    }

    private void calculateTotalCost() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT daily_rate FROM cars WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, carId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double dailyRate = rs.getDouble("daily_rate");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                long diff = sdf.parse(dropOffDate).getTime() - sdf.parse(pickUpDate).getTime();
                long days = (diff / (1000 * 60 * 60 * 24)) + 1;
                double totalCost = dailyRate * days;
                totalCostLabel.setText(String.format("$%.2f", totalCost));
            }
        } catch (Exception e) {
            totalCostLabel.setText("$0.00");
            JOptionPane.showMessageDialog(this, "Error calculating cost: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void confirmBooking() {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            String checkSql = "SELECT availability FROM cars WHERE id = ? AND availability > 0";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, carId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt("availability") > 0) {
                String sql = "INSERT INTO bookings (user_id, car_id, pick_up_date, drop_off_date, total_cost, status) VALUES (?, ?, ?, ?, ?, 'pending')";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, userId);
                stmt.setInt(2, carId);
                stmt.setString(3, pickUpDate);
                stmt.setString(4, dropOffDate);
                stmt.setDouble(5, Double.parseDouble(totalCostLabel.getText().replace("$", "")));
                stmt.executeUpdate();

                String updateSql = "UPDATE cars SET availability = availability - 1 WHERE id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, carId);
                updateStmt.executeUpdate();

                conn.commit();
                JOptionPane.showMessageDialog(this, "Booking confirmed! Please complete payment.", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                new CustomerDashboard(userId).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Car not available.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error confirming booking: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}