import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ManageScheduleForm extends JDialog {
    private int userId;
    JComboBox<String> roomCombo;
    JTextField dayField, startTimeField, endTimeField, courseField;
    JButton addButton, editButton, deleteButton, cancelButton;
    JTable scheduleTable;
    DefaultTableModel tableModel;

    public ManageScheduleForm(int userId) {
        this.userId = userId;
        setTitle("CSE Classroom & Lab Management - Manage Schedule");
        setSize(600, 400);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Table Panel
        String[] columns = {"ID", "Room", "Day", "Start Time", "End Time", "Course"};
        tableModel = new DefaultTableModel(columns, 0);
        scheduleTable = new JTable(tableModel);
        scheduleTable.setBackground(Color.WHITE);
        loadSchedules();
        panel.add(new JScrollPane(scheduleTable), BorderLayout.CENTER);

        // Input Panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Room:"), gbc);
        gbc.gridx = 1;
        roomCombo = new JComboBox<>();
        loadRooms();
        inputPanel.add(roomCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Day:"), gbc);
        gbc.gridx = 1;
        dayField = new JTextField(15);
        inputPanel.add(dayField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Start Time (e.g., 10:00:00):"), gbc);
        gbc.gridx = 1;
        startTimeField = new JTextField(15);
        inputPanel.add(startTimeField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        inputPanel.add(new JLabel("End Time (e.g., 12:00:00):"), gbc);
        gbc.gridx = 1;
        endTimeField = new JTextField(15);
        inputPanel.add(endTimeField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        inputPanel.add(new JLabel("Course:"), gbc);
        gbc.gridx = 1;
        courseField = new JTextField(15);
        inputPanel.add(courseField, gbc);

        panel.add(inputPanel, BorderLayout.NORTH);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        addButton = new JButton("Add");
        addButton.setBackground(new Color(0, 123, 255));
        addButton.setForeground(Color.WHITE);
        editButton = new JButton("Edit");
        editButton.setBackground(new Color(0, 123, 255));
        editButton.setForeground(Color.WHITE);
        deleteButton = new JButton("Delete");
        deleteButton.setBackground(new Color(0, 123, 255));
        deleteButton.setForeground(Color.WHITE);
        cancelButton = new JButton("Cancel");
        cancelButton.setBackground(Color.GRAY);
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addSchedule());
        editButton.addActionListener(e -> editSchedule());
        deleteButton.addActionListener(e -> deleteSchedule());
        cancelButton.addActionListener(e -> {
            dispose();
            new AdminDashboard(userId).setVisible(true);
        });

        // Add selection listener to populate fields
        scheduleTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && scheduleTable.getSelectedRow() != -1) {
                int selectedRow = scheduleTable.getSelectedRow();
                int scheduleId = (int) tableModel.getValueAt(selectedRow, 0);
                loadScheduleDetails(scheduleId);
            }
        });

        add(panel);
        setVisible(true);
    }

    void loadRooms() {
        roomCombo.removeAllItems();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT room_number FROM rooms";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                roomCombo.addItem(rs.getString("room_number"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading rooms: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    void loadSchedules() {
        tableModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT s.id, r.room_number, s.day, s.start_time, s.end_time, s.course_name " +
                    "FROM schedules s JOIN rooms r ON s.room_id = r.id";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("room_number"),
                        rs.getString("day"),
                        rs.getString("start_time"),
                        rs.getString("end_time"),
                        rs.getString("course_name")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading schedules: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    void loadScheduleDetails(int scheduleId) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT r.room_number, s.day, s.start_time, s.end_time, s.course_name " +
                    "FROM schedules s JOIN rooms r ON s.room_id = r.id WHERE s.id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, scheduleId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                roomCombo.setSelectedItem(rs.getString("room_number"));
                dayField.setText(rs.getString("day"));
                startTimeField.setText(rs.getString("start_time"));
                endTimeField.setText(rs.getString("end_time"));
                courseField.setText(rs.getString("course_name"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading schedule details: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    void addSchedule() {
        String roomNumber = (String) roomCombo.getSelectedItem();
        String day = dayField.getText().trim();
        String startTime = startTimeField.getText().trim();
        String endTime = endTimeField.getText().trim();
        String course = courseField.getText().trim();

        if (roomNumber == null || day.isEmpty() || startTime.isEmpty() || endTime.isEmpty() || course.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO schedules (room_id, day, start_time, end_time, course_name) " +
                    "SELECT id, ?, ?, ?, ? FROM rooms WHERE room_number = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, day);
            stmt.setString(2, startTime);
            stmt.setString(3, endTime);
            stmt.setString(4, course);
            stmt.setString(5, roomNumber);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Schedule added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearFields();
            loadSchedules();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Add failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    void editSchedule() {
        int selectedRow = scheduleTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a schedule to edit.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int scheduleId = (int) tableModel.getValueAt(selectedRow, 0);
        String roomNumber = (String) roomCombo.getSelectedItem();
        String day = dayField.getText().trim();
        String startTime = startTimeField.getText().trim();
        String endTime = endTimeField.getText().trim();
        String course = courseField.getText().trim();

        if (roomNumber == null || day.isEmpty() || startTime.isEmpty() || endTime.isEmpty() || course.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE schedules s JOIN rooms r ON s.room_id = r.id SET s.day = ?, s.start_time = ?, s.end_time = ?, s.course_name = ? " +
                    "WHERE s.id = ? AND r.room_number = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, day);
            stmt.setString(2, startTime);
            stmt.setString(3, endTime);
            stmt.setString(4, course);
            stmt.setInt(5, scheduleId);
            stmt.setString(6, roomNumber);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Schedule updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearFields();
                loadSchedules();
            } else {
                JOptionPane.showMessageDialog(this, "No schedule updated.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Edit failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    void deleteSchedule() {
        int selectedRow = scheduleTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a schedule to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int scheduleId = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this schedule?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "DELETE FROM schedules WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, scheduleId);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Schedule deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearFields();
                    loadSchedules();
                } else {
                    JOptionPane.showMessageDialog(this, "No schedule deleted.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Delete failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    void clearFields() {
        dayField.setText("");
        startTimeField.setText("");
        endTimeField.setText("");
        courseField.setText("");
        roomCombo.setSelectedIndex(-1);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ManageScheduleForm(1).setVisible(true));
    }
}