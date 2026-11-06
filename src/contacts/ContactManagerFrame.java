package contacts;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;

/**
 * The main window: a toolbar (Add / Edit), a table of contacts and a status
 * line. Every add or edit is saved straight back to the CSV file.
 */
public class ContactManagerFrame extends JFrame {
    private final ContactStore store;
    private final ContactTableModel model;
    private final JTable table;
    private final JLabel status = new JLabel();
    private final JButton editButton = new JButton("Edit...");

    public ContactManagerFrame(Path file, List<Contact> contacts) {
        super("Contact Manager - " + file.getFileName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        store = new ContactStore(file);
        model = new ContactTableModel(contacts);
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);

        // Double-clicking a row opens it for editing.
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) editSelected();
            }
        });
        // Edit only makes sense with a row selected.
        table.getSelectionModel().addListSelectionListener(
                e -> editButton.setEnabled(table.getSelectedRow() >= 0));

        JButton addButton = new JButton("Add...");
        addButton.addActionListener(e -> addContact());
        editButton.addActionListener(e -> editSelected());
        editButton.setEnabled(false);
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(addButton);
        toolbar.add(editButton);

        status.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        updateStatus();

        add(toolbar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);

        setSize(640, 400);
        setLocationRelativeTo(null);
    }

    private void addContact() {
        Contact c = ContactDialog.show(this, "Add Contact", null);
        if (c == null) return;
        int row = model.addContact(c);
        table.setRowSelectionInterval(row, row);
        table.scrollRectToVisible(table.getCellRect(row, 0, true));
        save();
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        Contact c = ContactDialog.show(this, "Edit Contact", model.getContactAt(row));
        if (c == null) return;
        model.updateContact(row, c);
        save();
    }

    /** Writes the table back to the CSV file, reporting any failure. */
    private void save() {
        try {
            store.save(model.getContacts());
            updateStatus();
        } catch (IOException ex) {
            status.setText("Save failed: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Could not save contacts:\n" + ex.getMessage(),
                    "Save failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStatus() {
        int n = model.getRowCount();
        status.setText(n + (n == 1 ? " contact" : " contacts"));
    }
}
