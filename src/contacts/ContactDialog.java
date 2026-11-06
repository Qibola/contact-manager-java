package contacts;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A modal dialog for adding a new contact or editing an existing one.
 * Use {@link #show(Window, String, Contact)}: it returns the entered
 * contact, or null if the user cancelled.
 */
public class ContactDialog extends JDialog {
    private final JTextField nameField = new JTextField(24);
    private final JTextField phoneField = new JTextField(24);
    private final JTextField emailField = new JTextField(24);
    private Contact result;

    private ContactDialog(Window owner, String title, Contact initial) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        if (initial != null) {
            nameField.setText(initial.getName());
            phoneField.setText(initial.getPhone());
            emailField.setText(initial.getEmail());
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 12));
        addRow(form, 0, "Name:", nameField);
        addRow(form, 1, "Phone:", phoneField);
        addRow(form, 2, "Email:", emailField);

        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        ok.addActionListener(e -> onOk());
        cancel.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(ok);
        buttons.add(cancel);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(ok); // Enter presses OK

        pack();
        setLocationRelativeTo(owner);
    }

    /** Opens the dialog and blocks until it closes. Returns null on cancel. */
    public static Contact show(Window owner, String title, Contact initial) {
        ContactDialog dialog = new ContactDialog(owner, title, initial);
        dialog.setVisible(true);
        return dialog.result;
    }

    /**
     * Builds a contact from raw field text: trims whitespace and requires a
     * name. Returns null when the input is not usable. (Phone/email format
     * checks come in a later roadmap step.)
     */
    static Contact fromFields(String name, String phone, String email) {
        String n = name == null ? "" : name.trim();
        if (n.isEmpty()) return null;
        return new Contact(n, phone == null ? "" : phone.trim(), email == null ? "" : email.trim());
    }

    private void onOk() {
        Contact c = fromFields(nameField.getText(), phoneField.getText(), emailField.getText());
        if (c == null) {
            JOptionPane.showMessageDialog(this, "Please enter a name.", getTitle(), JOptionPane.WARNING_MESSAGE);
            nameField.requestFocusInWindow();
            return;
        }
        result = c;
        dispose();
    }

    private static void addRow(JPanel form, int row, String label, JTextField field) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = row;
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.LINE_END;
        form.add(new JLabel(label), gc);
        gc.gridx = 1;
        gc.anchor = GridBagConstraints.LINE_START;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        form.add(field, gc);
    }
}
