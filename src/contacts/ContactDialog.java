package contacts;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.regex.Pattern;
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
    /** Something@something.tld with no spaces: loose on purpose, it catches typos. */
    private static final Pattern EMAIL = Pattern.compile("[^\\s@]+@[^\\s@]+\\.[^\\s@]+");
    /** Digits plus the usual separators: + - . ( ) and spaces. */
    private static final Pattern PHONE = Pattern.compile("[0-9+\\-.() ]+");
    private static final int MIN_PHONE_DIGITS = 7;

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
     * name. Returns null when the input is not usable. Format checks are
     * done separately by {@link #validationError}.
     */
    static Contact fromFields(String name, String phone, String email) {
        String n = name == null ? "" : name.trim();
        if (n.isEmpty()) return null;
        return new Contact(n, phone == null ? "" : phone.trim(), email == null ? "" : email.trim());
    }

    /**
     * Checks raw field text and returns a message for the first problem, or
     * null if the input is fine. Phone and email are optional, but when given
     * they must look like a phone number (7+ digits, common separators only)
     * and an email address.
     */
    static String validationError(String name, String phone, String email) {
        if (name == null || name.trim().isEmpty()) return "Please enter a name.";
        String p = phone == null ? "" : phone.trim();
        if (!p.isEmpty()) {
            long digits = p.chars().filter(Character::isDigit).count();
            if (!PHONE.matcher(p).matches() || digits < MIN_PHONE_DIGITS) {
                return "Phone should contain at least " + MIN_PHONE_DIGITS
                        + " digits and only + - . ( ) or spaces.";
            }
        }
        String e = email == null ? "" : email.trim();
        if (!e.isEmpty() && !EMAIL.matcher(e).matches()) {
            return "Email should look like name@example.com.";
        }
        return null;
    }

    private void onOk() {
        String error = validationError(nameField.getText(), phoneField.getText(), emailField.getText());
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, getTitle(), JOptionPane.WARNING_MESSAGE);
            if (error.startsWith("Phone")) phoneField.requestFocusInWindow();
            else if (error.startsWith("Email")) emailField.requestFocusInWindow();
            else nameField.requestFocusInWindow();
            return;
        }
        result = fromFields(nameField.getText(), phoneField.getText(), emailField.getText());
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
