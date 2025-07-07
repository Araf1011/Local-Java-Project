import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RegistrationFrame extends JFrame {
    JTextField usernameField;
    JPasswordField passwordField;
    JComboBox<String> roleCombo;
    JButton registerButton, backButton;

    public RegistrationFrame() {
        setTitle("CSE Classroom & Lab Management - Register");
        setSize(350, 300);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(15);
        panel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        panel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        String[] roles = {"student", "admin"};
        roleCombo = new JComboBox<>(roles);
        panel.add(roleCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        registerButton = new JButton("Register");
        registerButton.setBackground(new Color(0, 123, 255));
        registerButton.setForeground(Color.WHITE);
        panel.add(registerButton, gbc);

        gbc.gridx = 1;
        backButton = new JButton("Back");
        backButton.setBackground(Color.GRAY);
        panel.add(backButton, gbc);

        registerButton.addActionListener(e -> registerUser());
        backButton.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        add(panel);
        setVisible(true);
    }

    void registerUser() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String role = (String) roleCombo.getSelectedItem();

        if (username.isEmpty() || password.isEmpty() || role == null) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new LoginFrame().setVisible(true);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Registration failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RegistrationFrame::new);
    }
}