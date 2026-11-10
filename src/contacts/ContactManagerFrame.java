package contacts;

import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;

/**
 * The main window: a menu bar, a toolbar (Add / Edit / Delete + a search box),
 * a sortable table of contacts and a status line. Every change is saved
 * straight back to the CSV file. Sorting and searching change the view only;
 * the model always holds every contact in file order, so view row indices are
 * converted before touching the model.
 */
public class ContactManagerFrame extends JFrame {
    private final ContactStore store;
    private final ContactTableModel model;
    private final JTable table;
    private final JLabel status = new JLabel();
    private final Action addAction = action("Add...", KeyEvent.VK_N, this::addContact);
    private final Action editAction = action("Edit...", KeyEvent.VK_E, this::editSelected);
    private final Action deleteAction = action("Delete", KeyEvent.VK_BACK_SPACE, this::deleteSelected);
    private final JTextField searchField = new JTextField(16);
    private final TableRowSorter<ContactTableModel> sorter;

    public ContactManagerFrame(Path file, List<Contact> contacts) {
        super("Contact Manager - " + file.getFileName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        store = new ContactStore(file);
        model = new ContactTableModel(contacts);
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);

        // Click a column header to sort by it (click again to reverse).
        sorter = createSorter(model);
        table.setRowSorter(sorter);

        // Double-clicking a row opens it for editing.
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) editSelected();
            }
        });
        // Edit and Delete only make sense with a row selected.
        table.getSelectionModel().addListSelectionListener(e -> updateButtons());

        updateButtons();

        searchField.setToolTipText("Filter by name, phone or email");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(new JButton(addAction));
        toolbar.add(new JButton(editAction));
        toolbar.add(new JButton(deleteAction));
        toolbar.addSeparator();
        toolbar.add(new JLabel("Search: "));
        toolbar.add(searchField);

        status.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        updateStatus();

        setJMenuBar(buildMenuBar());
        add(toolbar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);

        setSize(640, 400);
        setLocationRelativeTo(null);
    }

    /**
     * A sorter that orders every column ignoring case and starts sorted by
     * name. It also does the search filtering (see applyFilter).
     */
    static TableRowSorter<ContactTableModel> createSorter(ContactTableModel model) {
        TableRowSorter<ContactTableModel> s = new TableRowSorter<>(model);
        for (int i = 0; i < model.getColumnCount(); i++) {
            s.setComparator(i, String.CASE_INSENSITIVE_ORDER);
        }
        s.setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
        return s;
    }

    /** File (Find, Quit) and Contact (Add, Edit, Delete) menus with shortcuts. */
    private JMenuBar buildMenuBar() {
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(action("Find", KeyEvent.VK_F, () -> searchField.requestFocusInWindow()));
        fileMenu.addSeparator();
        fileMenu.add(action("Quit", KeyEvent.VK_Q, this::dispose));

        JMenu contactMenu = new JMenu("Contact");
        contactMenu.add(addAction);
        contactMenu.add(editAction);
        contactMenu.add(deleteAction);

        JMenuBar bar = new JMenuBar();
        bar.add(fileMenu);
        bar.add(contactMenu);
        return bar;
    }

    /** An action with a menu shortcut (Cmd on macOS, Ctrl elsewhere). */
    private static Action action(String name, int key, Runnable run) {
        Action a = new AbstractAction(name) {
            @Override
            public void actionPerformed(ActionEvent e) {
                run.run();
            }
        };
        if (!GraphicsEnvironment.isHeadless()) {
            int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
            a.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(key, mask));
        }
        return a;
    }

    private void addContact() {
        Contact c = ContactDialog.show(this, "Add Contact", null);
        if (c == null) return;
        int row = model.addContact(c);
        // The new contact may be hidden by the current search; select it only if visible.
        int viewRow = table.convertRowIndexToView(row);
        if (viewRow >= 0) {
            table.setRowSelectionInterval(viewRow, viewRow);
            table.scrollRectToVisible(table.getCellRect(viewRow, 0, true));
        }
        save();
    }

    private void editSelected() {
        int row = selectedModelRow();
        if (row < 0) return;
        Contact c = ContactDialog.show(this, "Edit Contact", model.getContactAt(row));
        if (c == null) return;
        model.updateContact(row, c);
        save();
    }

    private void deleteSelected() {
        int row = selectedModelRow();
        if (row < 0) return;
        Contact c = model.getContactAt(row);
        int answer = JOptionPane.showConfirmDialog(this, "Delete " + c.getName() + "?",
                "Delete Contact", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) return;
        model.removeContact(row);
        save();
    }

    /** The selected row as a model index, or -1 if nothing is selected. */
    private int selectedModelRow() {
        int viewRow = table.getSelectedRow();
        return viewRow < 0 ? -1 : table.convertRowIndexToModel(viewRow);
    }

    private void applyFilter() {
        String query = searchField.getText();
        sorter.setRowFilter(new RowFilter<ContactTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends ContactTableModel, ? extends Integer> entry) {
                return ContactTableModel.matches(entry.getModel().getContactAt(entry.getIdentifier()), query);
            }
        });
        updateStatus();
    }

    private void updateButtons() {
        boolean selected = table.getSelectedRow() >= 0;
        editAction.setEnabled(selected);
        deleteAction.setEnabled(selected);
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
        String text = n + (n == 1 ? " contact" : " contacts");
        int shown = table.getRowCount();
        if (shown != n) text = "Showing " + shown + " of " + text;
        status.setText(text);
    }
}
