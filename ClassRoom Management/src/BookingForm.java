import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class BookingForm extends JDialog {
    private int userId;
    private int roomId;
    private String roomNumber;
    JTextField startTimeField, endTimeField;
    JComboBox<String> suggestRoomCombo;
    JButton suggestButton, submitButton, cancelButton;

    public BookingForm(int userId, int roomId, String roomNumber) {
        this.userId = userId;
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        setTitle("CSE Classroom & Lab Management - Book Room: " + roomNumber);
        setSize(400, 250);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Start Time (e.g., 2025-07-01 13:00:00):"), gbc);
        gbc.gridx = 1;
        startTimeField = new JTextField(15);
        panel.add(startTimeField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("End Time (e.g., 2025-07-01 15:00:00):"), gbc);
        gbc.gridx = 1;
        endTimeField = new JTextField(15);
        panel.add(endTimeField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Suggested Room:"), gbc);
        gbc.gridx = 1;
        suggestRoomCombo = new JComboBox<>();
        suggestRoomCombo.setEnabled(false); // Enabled only after suggestion
        panel.add(suggestRoomCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        suggestButton = new JButton("Suggest Room");
        suggestButton.setBackground(new Color(0, 123, 255));
        suggestButton.setForeground(Color.WHITE);
        panel.add(suggestButton, gbc);

        gbc.gridx = 1;
        submitButton = new JButton("Submit");
        submitButton.setBackground(new Color(0, 123, 255));
        submitButton.setForeground(Color.WHITE);
        panel.add(submitButton, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        cancelButton = new JButton("Cancel");
        cancelButton.setBackground(new Color(108, 117, 125));
        cancelButton.setForeground(Color.WHITE);
        panel.add(cancelButton, gbc);

        suggestButton.addActionListener(e -> suggestRooms());
        submitButton.addActionListener(e -> bookRoom());
        cancelButton.addActionListener(e -> {
            dispose();
            new RoomDetailsScreen(userId, roomId, roomNumber, "").setVisible(true);
        });

        add(panel);
        setVisible(true);
    }

    void suggestRooms() {
        String startTime = startTimeField.getText().trim();
        String endTime = endTimeField.getText().trim();

        if (startTime.isEmpty() || endTime.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill start and end times first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setLenient(false);
        try {
            sdf.parse(startTime);
            sdf.parse(endTime);
            if (sdf.parse(endTime).before(sdf.parse(startTime))) {
                JOptionPane.showMessageDialog(this, "End time must be after start time.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid time format. Use yyyy-MM-dd HH:mm:ss.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        suggestRoomCombo.removeAllItems();
        suggestRoomCombo.setEnabled(true);
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT r.id, r.room_number FROM rooms r WHERE r.id NOT IN " +
                    "(SELECT b.room_id FROM bookings b WHERE b.status = 'approved' AND " +
                    "((? BETWEEN b.start_time AND b.end_time) OR (? BETWEEN b.start_time AND b.end_time) OR " +
                    "(b.start_time BETWEEN ? AND ?) OR (b.end_time BETWEEN ? AND ?)))";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, startTime);
            stmt.setString(2, endTime);
            stmt.setString(3, startTime);
            stmt.setString(4, endTime);
            stmt.setString(5, startTime);
            stmt.setString(6, endTime);
            ResultSet rs = stmt.executeQuery();
            boolean found = false;
            while (rs.next()) {
                suggestRoomCombo.addItem(rs.getString("room_number"));
                found = true;
            }
            if (!found) {
                suggestRoomCombo.addItem("No available rooms");
                suggestRoomCombo.setEnabled(false);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error suggesting rooms: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    void bookRoom() {
        String startTime = startTimeField.getText().trim();
        String endTime = endTimeField.getText().trim();
        String selectedRoom = (String) suggestRoomCombo.getSelectedItem();

        if (startTime.isEmpty() || endTime.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setLenient(false);
        try {
            sdf.parse(startTime);
            sdf.parse(endTime);
            if (sdf.parse(endTime).before(sdf.parse(startTime))) {
                JOptionPane.showMessageDialog(this, "End time must be after start time.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid time format. Use yyyy-MM-dd HH:mm:ss.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int newRoomId = roomId;
        if (selectedRoom != null && !selectedRoom.equals("No available rooms") && suggestRoomCombo.isEnabled()) {
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "SELECT id FROM rooms WHERE room_number = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, selectedRoom);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) newRoomId = rs.getInt("id");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error fetching room ID: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        boolean conflict = false;
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT start_time, end_time FROM bookings WHERE room_id = ? AND status = 'approved'";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, newRoomId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                java.util.Date existingStart = sdf.parse(rs.getString("start_time"));
                java.util.Date existingEnd = sdf.parse(rs.getString("end_time"));
                java.util.Date newStart = sdf.parse(startTime);
                java.util.Date newEnd = sdf.parse(endTime);

                if (newStart.before(existingEnd) && newEnd.after(existingStart)) {
                    conflict = true;
                    break;
                }
            }
        } catch (SQLException | ParseException e) {
            JOptionPane.showMessageDialog(this, "Error checking conflicts: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (conflict) {
            JOptionPane.showMessageDialog(this, "Conflict detected. This room is already booked for the selected time.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO bookings (user_id, room_id, start_time, end_time, status) VALUES (?, ?, ?, ?, 'pending')";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, newRoomId);
            stmt.setString(3, startTime);
            stmt.setString(4, endTime);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Booking request submitted!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new RoomDetailsScreen(userId, newRoomId, selectedRoom != null ? selectedRoom : roomNumber, "").setVisible(true);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Booking failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BookingForm(1, 1, "CSE-101").setVisible(true));
    }
}
