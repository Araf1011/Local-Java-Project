import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Vector;

public class CustomerDashboard extends JFrame {
    private final int userId;
    private JTable carTable, bookingTable;
    private JLabel welcomeLabel;
    private JButton bookCarButton, profileButton, refreshButton, logoutButton;
    private JFormattedTextField pickUpDateField, dropOffDateField;

    public CustomerDashboard(int userId) {
        this.userId = userId;
        setTitle("Car Rental Management - Customer Dashboard");
        setSize(900, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // North Panel for Welcome
        JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        welcomeLabel = new JLabel("Welcome, [Loading...]!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        northPanel.add(welcomeLabel);
        add(northPanel, BorderLayout.NORTH);

        // Center Panel for Tabs
        JTabbedPane tabbedPane = new JTabbedPane();

        // Car Catalog Tab
        JPanel carPanel = new JPanel(new BorderLayout(10, 10));
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        try {
            datePanel.add(new JLabel("Pick-up Date (yyyy-MM-dd):"));
            pickUpDateField = new JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
            pickUpDateField.setColumns(10);
            datePanel.add(pickUpDateField);
            datePanel.add(new JLabel("Drop-off Date (yyyy-MM-dd):"));
            dropOffDateField = new JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
            dropOffDateField.setColumns(10);
            datePanel.add(dropOffDateField);
        } catch (Exception e) {
            datePanel.add(new JLabel("Date fields unavailable."));
        }
        carPanel.add(datePanel, BorderLayout.NORTH);

        carTable = new JTable();
        carTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        carTable.getTableHeader().setReorderingAllowed(false);
        JScrollPane carScrollPane = new JScrollPane(carTable);
        carPanel.add(carScrollPane, BorderLayout.CENTER);

        JPanel carButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bookCarButton = new JButton("Book Selected Car");
        bookCarButton.setBackground(new Color(0, 123, 255));
        bookCarButton.setForeground(Color.WHITE);
        refreshButton = new JButton("Refresh");
        refreshButton.setBackground(Color.GRAY);
        carButtonPanel.add(bookCarButton);
        carButtonPanel.add(refreshButton);
        carPanel.add(carButtonPanel, BorderLayout.SOUTH);
        tabbedPane.addTab("Car Catalog", carPanel);

        // Bookings Tab
        JPanel bookingPanel = new JPanel(new BorderLayout(10, 10));
        bookingTable = new JTable();
        bookingTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        bookingTable.getTableHeader().setReorderingAllowed(false);
        JScrollPane bookingScrollPane = new JScrollPane(bookingTable);
        bookingPanel.add(bookingScrollPane, BorderLayout.CENTER);
        tabbedPane.addTab("My Bookings", bookingPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // South Panel for Buttons
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        profileButton = new JButton("Edit Profile");
        profileButton.setBackground(new Color(0, 123, 255));
        profileButton.setForeground(Color.WHITE);
        logoutButton = new JButton("Logout");
        logoutButton.setBackground(Color.GRAY);
        southPanel.add(profileButton);
        southPanel.add(logoutButton);
        add(southPanel, BorderLayout.SOUTH);

        loadUserDetails();
        loadCars();
        loadBookings();

        bookCarButton.addActionListener(e -> {
            int selectedRow = carTable.getSelectedRow();
            if (selectedRow >= 0) {
                int carId = (int) carTable.getValueAt(selectedRow, 0);
                String pickUpDate = pickUpDateField.getText().trim();
                String dropOffDate = dropOffDateField.getText().trim();
                if (isValidDate(pickUpDate) && isValidDate(dropOffDate)) {
                    new BookingFrame(userId, carId, pickUpDate, dropOffDate).setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid date format. Use yyyy-MM-dd.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a car to book.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            }
        });

        refreshButton.addActionListener(e -> {
            loadCars();
            loadBookings();
        });

        profileButton.addActionListener(e -> new EditProfileFrame(userId, false).setVisible(true));

        logoutButton.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                dispose();
                new LoginFrame().setVisible(true);
            }
        });

        setVisible(true);
    }

    private void loadUserDetails() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT full_name FROM users WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                welcomeLabel.setText("Welcome, " + rs.getString("full_name") + "!");
            }
        } catch (SQLException e) {
            welcomeLabel.setText("Welcome, User!");
            JOptionPane.showMessageDialog(this, "Failed to load user details: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCars() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT id, model, car_type, daily_rate, availability FROM cars WHERE availability > 0";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            Vector<String> columnNames = new Vector<>();
            columnNames.add("ID");
            columnNames.add("Model");
            columnNames.add("Type");
            columnNames.add("Daily Rate ($)");
            columnNames.add("Available");

            Vector<Vector<Object>> data = new Vector<>();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("model"));
                row.add(rs.getString("car_type"));
                row.add(rs.getDouble("daily_rate"));
                row.add(rs.getInt("availability"));
                data.add(row);
            }

            carTable.setModel(new javax.swing.table.DefaultTableModel(data, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            });
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load cars: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadBookings() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT b.id, c.model, b.pick_up_date, b.drop_off_date, b.total_cost, b.status " +
                    "FROM bookings b JOIN cars c ON b.car_id = c.id WHERE b.user_id = ? AND b.status != 'completed'";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            Vector<String> columnNames = new Vector<>();
            columnNames.add("Booking ID");
            columnNames.add("Car Model");
            columnNames.add("Pick-up Date");
            columnNames.add("Drop-off Date");
            columnNames.add("Total Cost ($)");
            columnNames.add("Status");

            Vector<Vector<Object>> data = new Vector<>();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("model"));
                row.add(rs.getDate("pick_up_date"));
                row.add(rs.getDate("drop_off_date"));
                row.add(rs.getDouble("total_cost"));
                row.add(rs.getString("status"));
                data.add(row);
            }

            bookingTable.setModel(new javax.swing.table.DefaultTableModel(data, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            });
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load bookings: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isValidDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return false;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        try {
            sdf.parse(dateStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}