
package Exe_Z.server;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.*;

public class DurationInputDialog extends JDialog {
    private JTextField inputField;
    private boolean confirmed = false;
    private int hours = 0;

    public DurationInputDialog(JFrame parent) {
        super(parent, "Nhập Thời Hạn", true);
        setSize(350, 200);
        setLayout(new BorderLayout());
        setLocationRelativeTo(parent);

        // Tạo ô nhập với kích thước lớn hơn và phông chữ rõ ràng hơn
        inputField = new JTextField(15);
        inputField.setFont(new Font("Arial", Font.PLAIN, 16));

        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        okButton.addActionListener(e -> {
            try {
                hours = Integer.parseInt(inputField.getText());
                confirmed = true;
            } catch (NumberFormatException ex) {
                hours = 0;
            }
            dispose();
        });

        cancelButton.addActionListener(e -> dispose());

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Thời hạn (phút), bỏ trống nếu vĩnh viễn:"), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(inputField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, gbc);

        add(panel, BorderLayout.CENTER);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public int getHours() {
        return hours;
    }
}