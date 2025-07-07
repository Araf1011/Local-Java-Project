import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RoomDetailsScreen extends JDialog {
    private int userId;
    private int roomId;
    private String roomNumber;
    private String type;

    public RoomDetailsScreen(int userId, int roomId, String roomNumber, String type) {
        this.userId = userId;
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.type = type;
        setTitle("CSE Classroom & Lab Management - Room: " + roomNumber);
        setSize(400, 300);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel(roomNumber + " (" + type + ")", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);

        JTextArea statusArea = new JTextArea();
        statusArea.setEditable(false);
        statusArea.setBackground(Color.WHITE);
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT day, start_time, end_time, course_name FROM schedules WHERE room_id = ? LIMIT 1";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, roomId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                statusArea.setText("Current: " + rs.getString("course_name") + " (" +
                        rs.getString("start_time") + "-" + rs.getString("end_time") + " on " +
                        rs.getString("day") + ")");
            } else {
                statusArea.setText("No classes scheduled.");
            }
        } catch (SQLException e) {
            statusArea.setText("Error loading schedule: " + e.getMessage());
        }
        panel.add(new JScrollPane(statusArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton viewTimetableButton = new JButton("View Timetable");
        viewTimetableButton.setBackground(new Color(0, 123, 255));
        viewTimetableButton.setForeground(Color.WHITE);
        buttonPanel.add(viewTimetableButton);
        JButton bookButton = new JButton("Book Room");
        bookButton.setBackground(new Color(0, 123, 255));
        bookButton.setForeground(Color.WHITE);
        buttonPanel.add(bookButton);
        JButton backButton = new JButton("Back");
        backButton.setBackground(Color.GRAY);
        buttonPanel.add(backButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        viewTimetableButton.addActionListener(e -> {
            dispose();
            new TimetableScreen(userId, roomId, roomNumber).setVisible(true);
        });
        bookButton.addActionListener(e -> {
            dispose();
            new BookingForm(userId, roomId, roomNumber).setVisible(true);
        });
        backButton.addActionListener(e -> {
            dispose();
            new RoomSearchScreen(userId).setVisible(true);
        });

        add(panel);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RoomDetailsScreen(1, 1, "CSE-101", "classroom").setVisible(true));
    }
}