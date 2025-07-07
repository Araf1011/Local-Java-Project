
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimetableScreen extends JDialog {
    private int userId;
    private int roomId;
    private String roomNumber;
    private JTable timetableTable;
    private DefaultTableModel tableModel;
    private Calendar currentWeek = Calendar.getInstance();
    private JComboBox<String> dayFilter;

    public TimetableScreen(int userId, int roomId, String roomNumber) {
        this.userId = userId;
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        setTitle("CSE Classroom & Lab Management - Timetable: " + roomNumber);
        setSize(600, 400);
        setLocationRelativeTo(null);

        // Panel Setup
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(248, 249, 250));

        // Table Setup
        String[] days = {"All", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        String[] columns = new String[days.length];
        columns[0] = "Time Slot";
        System.arraycopy(days, 1, columns, 1, days.length - 1);
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        timetableTable = new JTable(tableModel);
        timetableTable.setBackground(Color.WHITE);
        timetableTable.setFont(new Font("Arial", Font.PLAIN, 12));
        timetableTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        timetableTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        timetableTable.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        panel.add(new JScrollPane(timetableTable), BorderLayout.CENTER);

        // Navigation and Filter Panel
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton prevWeekButton = new JButton("Previous Week");
        prevWeekButton.setBackground(new Color(108, 117, 125));
        prevWeekButton.setForeground(Color.WHITE);
        JButton nextWeekButton = new JButton("Next Week");
        nextWeekButton.setBackground(new Color(108, 117, 125));
        nextWeekButton.setForeground(Color.WHITE);
        JLabel weekLabel = new JLabel(getWeekLabel());
        weekLabel.setFont(new Font("Arial", Font.BOLD, 14));
        dayFilter = new JComboBox<>(days);
        dayFilter.setBackground(new Color(248, 249, 250));
        dayFilter.setFont(new Font("Arial", Font.PLAIN, 12));

        navPanel.add(prevWeekButton);
        navPanel.add(weekLabel);
        navPanel.add(nextWeekButton);
        navPanel.add(dayFilter);
        panel.add(navPanel, BorderLayout.NORTH);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton backButton = new JButton("Back");
        backButton.setBackground(new Color(108, 117, 125));
        backButton.setForeground(Color.WHITE);
        buttonPanel.add(backButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Load initial timetable
        loadTimetable();

        // Action Listeners
        prevWeekButton.addActionListener(e -> {
            currentWeek.add(Calendar.WEEK_OF_YEAR, -1);
            loadTimetable();
            weekLabel.setText(getWeekLabel());
        });
        nextWeekButton.addActionListener(e -> {
            currentWeek.add(Calendar.WEEK_OF_YEAR, 1);
            loadTimetable();
            weekLabel.setText(getWeekLabel());
        });
        backButton.addActionListener(e -> {
            dispose();
            new RoomDetailsScreen(userId, roomId, roomNumber, "").setVisible(true);
        });
        dayFilter.addActionListener(e -> loadTimetable());

        add(panel);
        setVisible(true);
    }

    private void loadTimetable() {
        tableModel.setRowCount(0);
        // Add time slots (e.g., hourly from 8:00 to 18:00)
        for (int hour = 8; hour <= 18; hour++) {
            String timeSlot = String.format("%02d:00 - %02d:00", hour, hour + 1);
            Object[] row = new Object[8]; // 7 days + time slot
            row[0] = timeSlot;
            for (int i = 1; i < 8; i++) row[i] = ""; // Initialize empty cells
            tableModel.addRow(row);
        }

        String selectedDay = (String) dayFilter.getSelectedItem();
        try (Connection conn = DBConnection.getConnection()) {
            Calendar cal = (Calendar) currentWeek.clone();
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); // Start of the week
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (int day = 0; day < 7; day++) {
                String date = sdf.format(cal.getTime());
                if (!"All".equals(selectedDay) && !selectedDay.equals(getDayName(day))) continue;
                String sql = "SELECT start_time, end_time, course_name FROM schedules WHERE room_id = ? " +
                        "AND DATE(start_time) = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, roomId);
                stmt.setString(2, date);
                ResultSet rs = stmt.executeQuery();
                System.out.println("Querying date: " + date + " for roomId: " + roomId);
                while (rs.next()) {
                    System.out.println("Found schedule: " + rs.getString("course_name") + ", start_time: " + rs.getString("start_time"));
                    String startTimeStr = rs.getString("start_time");
                    if (startTimeStr != null && startTimeStr.length() >= 16) {
                        String start = startTimeStr.substring(11, 16); // HH:mm
                        String end = rs.getString("end_time").substring(11, 16);
                        String course = rs.getString("course_name");
                        int startHour = Integer.parseInt(start.split(":")[0]);
                        int rowIndex = startHour - 8;
                        if (rowIndex >= 0 && rowIndex < tableModel.getRowCount()) {
                            tableModel.setValueAt(course + " (" + start + "-" + end + ")", rowIndex, day + 1);
                        }
                    } else {
                        System.out.println("Invalid start_time format: " + startTimeStr);
                    }
                }
                if ("All".equals(selectedDay)) cal.add(Calendar.DAY_OF_MONTH, 1);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading timetable: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.out.println("SQLException: " + e.getMessage());
        }
    }

    private String getDayName(int dayIndex) {
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        return days[dayIndex];
    }

    private String getWeekLabel() {
        Calendar start = (Calendar) currentWeek.clone();
        start.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Calendar end = (Calendar) start.clone();
        end.add(Calendar.DAY_OF_WEEK, 6);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return "Week of " + sdf.format(start.getTime()) + " - " + sdf.format(end.getTime());
    }

    private class CustomTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (column == 0) { // Time Slot column
                c.setBackground(new Color(248, 249, 250)); // Light gray for time slots
                c.setForeground(Color.BLACK);
            } else if (value != null && !value.toString().isEmpty()) { // Occupied slot
                c.setBackground(new Color(40, 167, 69)); // Green for booked
                c.setForeground(Color.WHITE);
            } else { // Free slot
                c.setBackground(new Color(248, 249, 250)); // Light gray for free
                c.setForeground(Color.BLACK);
            }
            return c;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TimetableScreen(1, 1, "CSE-101").setVisible(true));
    }
}
