import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

public class AdminDashboard extends JFrame {
    private final int userId;
    private JTabbedPane tabbedPane;
    private JTable carTable, userTable, bookingTable;
    private JTextField modelField, carTypeField, rateField, availabilityField;
    private JTextField searchUserField;

    public AdminDashboard(int userId) {
        this.userId = userId;
        setTitle("Car Rental Management - Admin Dashboard");
        setSize(1000, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);

        // Car Management Tab
        JPanel carPanel = new JPanel(new BorderLayout(10, 10));
        carTable = new JTable();
        carTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        carTable.getTableHeader().setReorderingAllowed(false);
        JScrollPane carScrollPane = new JScrollPane(carTable);
        carPanel.add(carScrollPane, BorderLayout.CENTER);

        JPanel carInputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        carInputPanel.add(new JLabel("Model:"), gbc);
        gbc.gridx = 1;
        modelField = new JTextField(15);
        carInputPanel.add(modelField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        carInputPanel.add(new JLabel("Car Type:"), gbc);
        gbc.gridx = 1;
        carTypeField = new JTextField(15);
        carInputPanel.add(carTypeField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        carInputPanel.add(new JLabel("Daily Rate ($):"), gbc);
        gbc.gridx = 1;
        rateField = new JTextField(10);
        carInputPanel.add(rateField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        carInputPanel.add(new JLabel("Availability:"), gbc);
        gbc.gridx = 1;
        availabilityField = new JTextField(5);
        carInputPanel.add(availabilityField, gbc);

        JPanel carButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton addCarButton = new JButton("Add Car");
        addCarButton.setBackground(new Color(0, 123, 255));
        addCarButton.setForeground(Color.WHITE);
        JButton editCarButton = new JButton("Edit Selected Car");
        editCarButton.setBackground(new Color(0, 123, 255));
        editCarButton.setForeground(Color.WHITE);
        JButton deleteCarButton = new JButton("Delete Selected Car");
        deleteCarButton.setBackground(Color.GRAY);
        JButton refreshCarButton = new JButton("Refresh");
        refreshCarButton.setBackground(Color.GRAY);
        carButtonPanel.add(addCarButton);
        carButtonPanel.add(editCarButton);
        carButtonPanel.add(deleteCarButton);
        carButtonPanel.add(refreshCarButton);
        carPanel.add(carInputPanel, BorderLayout.NORTH);
        carPanel.add(carButtonPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("Car Management", carPanel);

        // User Management Tab
        JPanel userPanel = new JPanel(new BorderLayout(10, 10));
        JPanel userSearchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchUserField = new JTextField(20);
        userSearchPanel.add(new JLabel("Search User:"));
        userSearchPanel.add(searchUserField);
        userPanel.add(userSearchPanel, BorderLayout.NORTH);
        userTable = new JTable();
        userTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        userTable.getTableHeader().setReorderingAllowed(false);
        JScrollPane userScrollPane = new JScrollPane(userTable);
        userPanel.add(userScrollPane, BorderLayout.CENTER);

        JPanel userButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton editUserButton = new JButton("Edit Selected User");
        editUserButton.setBackground(new Color(0, 123, 255));
        editUserButton.setForeground(Color.WHITE);
        JButton deleteUserButton = new JButton("Delete Selected User");
        deleteUserButton.setBackground(Color.GRAY);
        JButton refreshUserButton = new JButton("Refresh");
        refreshUserButton.setBackground(Color.GRAY);
        userButtonPanel.add(editUserButton);
        userButtonPanel.add(deleteUserButton);
        userButtonPanel.add(refreshUserButton);
        userPanel.add(userButtonPanel, BorderLayout.SOUTH);
        tabbedPane.addTab("User Management", userPanel);

        // Bookings Tab
        JPanel bookingPanel = new JPanel(new BorderLayout(10, 10));
        bookingTable = new JTable();
        bookingTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        bookingTable.getTableHeader().setReorderingAllowed(false);
        JScrollPane bookingScrollPane = new JScrollPane(bookingTable);
        bookingPanel.add(bookingScrollPane, BorderLayout.CENTER);

        JPanel bookingButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton refreshBookingButton = new JButton("Refresh");
        refreshBookingButton.setBackground(Color.GRAY);
        bookingButtonPanel.add(refreshBookingButton);
        bookingPanel.add(bookingButtonPanel, BorderLayout.SOUTH);
        tabbedPane.addTab("Bookings", bookingPanel);

        add(tabbedPane, BorderLayout.CENTER);

        loadCars();
        loadUsers();
        loadBookings();

        addCarButton.addActionListener(e -> addCar());
        editCarButton.addActionListener(e -> editCar());
        deleteCarButton.addActionListener(e -> deleteCar());
        refreshCarButton.addActionListener(e -> loadCars());
        editUserButton.addActionListener(e -> editUser());
        deleteUserButton.addActionListener(e -> deleteUser());
        refreshUserButton.addActionListener(e -> loadUsers());
        searchUserField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { loadUsers(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { loadUsers(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { loadUsers(); }
        });
        refreshBookingButton.addActionListener(e -> loadBookings());

        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(Color.GRAY);
        add(logoutButton, BorderLayout.SOUTH);
        logoutButton.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                dispose();
                new LoginFrame().setVisible(true);
            }
        });

        setVisible(true);
    }

    private void loadCars() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT id, model, car_type, daily_rate, availability FROM cars";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            Vector<String> columnNames = new Vector<>();
            columnNames.add("ID");
            columnNames.add("Model");
            columnNames.add("Type");
            columnNames.add("Daily Rate ($)");
            columnNames.add("Availability");

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

    private void loadUsers() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT id, username, role, email, phone, address FROM users WHERE username LIKE ? OR role LIKE ? OR email LIKE ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            String searchTerm = "%" + searchUserField.getText().trim() + "%";
            stmt.setString(1, searchTerm);
            stmt.setString(2, searchTerm);
            stmt.setString(3, searchTerm);
            ResultSet rs = stmt.executeQuery();

            Vector<String> columnNames = new Vector<>();
            columnNames.add("ID");
            columnNames.add("Username");
            columnNames.add("Role");
            columnNames.add("Email");
            columnNames.add("Phone");
            columnNames.add("Address");

            Vector<Vector<Object>> data = new Vector<>();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("username"));
                row.add(rs.getString("role"));
                row.add(rs.getString("email"));
                row.add(rs.getString("phone"));
                row.add(rs.getString("address"));
                data.add(row);
            }

            userTable.setModel(new javax.swing.table.DefaultTableModel(data, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            });
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load users: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadBookings() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT b.id, u.username, c.model, b.pick_up_date, b.drop_off_date, b.total_cost, b.status " +
                    "FROM bookings b JOIN users u ON b.user_id = u.id JOIN cars c ON b.car_id = c.id";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            Vector<String> columnNames = new Vector<>();
            columnNames.add("Booking ID");
            columnNames.add("Username");
            columnNames.add("Car Model");
            columnNames.add("Pick-up Date");
            columnNames.add("Drop-off Date");
            columnNames.add("Total Cost ($)");
            columnNames.add("Status");

            Vector<Vector<Object>> data = new Vector<>();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("username"));
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

    private void addCar() {
        String model = modelField.getText().trim();
        String carType = carTypeField.getText().trim();
        String rateStr = rateField.getText().trim();
        String availabilityStr = availabilityField.getText().trim();

        if (model.isEmpty() || carType.isEmpty() || rateStr.isEmpty() || availabilityStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double rate = Double.parseDouble(rateStr);
            int availability = Integer.parseInt(availabilityStr);
            if (rate < 0 || availability < 0) {
                JOptionPane.showMessageDialog(this, "Rate and availability must be positive.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                String sql = "INSERT INTO cars (model, car_type, daily_rate, availability) VALUES (?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, model);
                stmt.setString(2, carType);
                stmt.setDouble(3, rate);
                stmt.setInt(4, availability);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Car added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearCarFields();
                loadCars();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Rate and availability must be valid numbers.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding car: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editCar() {
        int selectedRow = carTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a car to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int carId = (int) carTable.getValueAt(selectedRow, 0);
        String model = modelField.getText().trim();
        String carType = carTypeField.getText().trim();
        String rateStr = rateField.getText().trim();
        String availabilityStr = availabilityField.getText().trim();

        if (model.isEmpty() || carType.isEmpty() || rateStr.isEmpty() || availabilityStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double rate = Double.parseDouble(rateStr);
            int availability = Integer.parseInt(availabilityStr);
            if (rate < 0 || availability < 0) {
                JOptionPane.showMessageDialog(this, "Rate and availability must be positive.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (JOptionPane.showConfirmDialog(this, "Are you sure you want to edit this car?", "Confirm Edit", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try (Connection conn = DBConnection.getConnection()) {
                    String sql = "UPDATE cars SET model = ?, car_type = ?, daily_rate = ?, availability = ? WHERE id = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, model);
                    stmt.setString(2, carType);
                    stmt.setDouble(3, rate);
                    stmt.setInt(4, availability);
                    stmt.setInt(5, carId);
                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Car updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearCarFields();
                    loadCars();
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Rate and availability must be valid numbers.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error editing car: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCar() {
        int selectedRow = carTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a car to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int carId = (int) carTable.getValueAt(selectedRow, 0);
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this car?", "Confirm Delete", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "DELETE FROM cars WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, carId);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Car deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearCarFields();
                loadCars();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting car: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (int) userTable.getValueAt(selectedRow, 0);
        new EditProfileFrame(userId, true).setVisible(true);
    }

    private void deleteUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (int) userTable.getValueAt(selectedRow, 0);
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this user?", "Confirm Delete", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "DELETE FROM users WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, userId);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "User deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadUsers();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting user: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearCarFields() {
        modelField.setText("");
        carTypeField.setText("");
        rateField.setText("");
        availabilityField.setText("");
    }
}