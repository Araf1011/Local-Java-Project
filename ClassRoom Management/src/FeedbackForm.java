import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FeedbackForm extends JDialog {
    private int userId;
    JTextArea issueArea;
    JButton submitButton, cancelButton;

    public FeedbackForm(int userId) {
        this.userId = userId;
        setTitle("CSE Classroom & Lab Management - Submit Feedback");
        setSize(350, 250);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Issue:"), gbc);
        gbc.gridx = 1;
        issueArea = new JTextArea(3, 15);
        issueArea.setLineWrap(true);
        panel.add(new JScrollPane(issueArea), gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        submitButton = new JButton("Submit");
        submitButton.setBackground(new Color(0, 123, 255));
        submitButton.setForeground(Color.WHITE);
        panel.add(submitButton, gbc);

        gbc.gridx = 1;
        cancelButton = new JButton("Cancel");
        cancelButton.setBackground(Color.GRAY);
        panel.add(cancelButton, gbc);

        submitButton.addActionListener(e -> submitFeedback());
        cancelButton.addActionListener(e -> {
            dispose();
            new MainStudentDashboard(userId).setVisible(true);
        });

        add(panel);
        setVisible(true);
    }

    void submitFeedback() {
        String issue = issueArea.getText().trim();
        if (issue.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an issue.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO feedback (user_id, issue, status) VALUES (?, ?, 'pending')";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setString(2, issue);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Feedback submitted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new MainStudentDashboard(userId).setVisible(true);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Feedback submission failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FeedbackForm(1).setVisible(true));
    }
}