package contacts;

import java.util.List;

/**
 * Tests for ContactTableModel. The model needs no display, so this runs
 * headless:
 *   java -cp out contacts.ContactTableModelTest
 */
public class ContactTableModelTest {
    private static int failures = 0;

    public static void main(String[] args) {
        exposesColumnsAndRows();
        setContactsReplacesRowsAndNotifies();
        getContactsReturnsACopy();
        addContactAppendsAndNotifies();
        updateContactReplacesRow();
        removeContactDeletesRowAndNotifies();
        matchesSearchesAllFieldsIgnoringCase();

        if (failures > 0) {
            System.out.println(failures + " test(s) FAILED");
            System.exit(1);
        }
        System.out.println("All tests passed");
    }

    static void exposesColumnsAndRows() {
        ContactTableModel m = new ContactTableModel(List.of(
                new Contact("Ada Lovelace", "555-0100", "ada@example.com"),
                new Contact("Alan Turing", "555-0101", "alan@example.com")));
        check("three columns", m.getColumnCount() == 3);
        check("column names", m.getColumnName(0).equals("Name")
                && m.getColumnName(1).equals("Phone")
                && m.getColumnName(2).equals("Email"));
        check("row count", m.getRowCount() == 2);
        check("cell values", m.getValueAt(1, 0).equals("Alan Turing")
                && m.getValueAt(1, 1).equals("555-0101")
                && m.getValueAt(1, 2).equals("alan@example.com"));
        check("cells are read-only", !m.isCellEditable(0, 0));
    }

    static void setContactsReplacesRowsAndNotifies() {
        ContactTableModel m = new ContactTableModel(List.of(new Contact("A", "1", "a@x")));
        int[] events = {0};
        m.addTableModelListener(e -> events[0]++);
        m.setContacts(List.of(new Contact("B", "2", "b@x"), new Contact("C", "3", "c@x")));
        check("rows replaced", m.getRowCount() == 2 && m.getContactAt(0).getName().equals("B"));
        check("listener notified", events[0] == 1);
    }

    static void getContactsReturnsACopy() {
        ContactTableModel m = new ContactTableModel(List.of(new Contact("A", "1", "a@x")));
        m.getContacts().clear();
        check("copy does not affect model", m.getRowCount() == 1);
    }

    static void addContactAppendsAndNotifies() {
        ContactTableModel m = new ContactTableModel(List.of(new Contact("A", "1", "a@x")));
        int[] inserted = {-1};
        m.addTableModelListener(e -> inserted[0] = e.getFirstRow());
        int row = m.addContact(new Contact("B", "2", "b@x"));
        check("add returns new row index", row == 1 && m.getRowCount() == 2);
        check("added contact is last", m.getContactAt(1).getName().equals("B"));
        check("listener told which row", inserted[0] == 1);
    }

    static void updateContactReplacesRow() {
        ContactTableModel m = new ContactTableModel(List.of(
                new Contact("A", "1", "a@x"), new Contact("B", "2", "b@x")));
        m.updateContact(0, new Contact("A2", "9", "a2@x"));
        check("row replaced", m.getValueAt(0, 0).equals("A2") && m.getValueAt(0, 1).equals("9"));
        check("other rows untouched", m.getContactAt(1).getName().equals("B") && m.getRowCount() == 2);
    }

    static void removeContactDeletesRowAndNotifies() {
        ContactTableModel m = new ContactTableModel(List.of(
                new Contact("A", "1", "a@x"), new Contact("B", "2", "b@x"), new Contact("C", "3", "c@x")));
        int[] deleted = {-1};
        m.addTableModelListener(e -> deleted[0] = e.getFirstRow());
        Contact removed = m.removeContact(1);
        check("remove returns the contact", removed.getName().equals("B"));
        check("row removed, order kept", m.getRowCount() == 2
                && m.getContactAt(0).getName().equals("A") && m.getContactAt(1).getName().equals("C"));
        check("listener told which row was deleted", deleted[0] == 1);
    }

    static void matchesSearchesAllFieldsIgnoringCase() {
        Contact ada = new Contact("Ada Lovelace", "555-0100", "ada@example.com");
        check("blank query matches", ContactTableModel.matches(ada, "  ")
                && ContactTableModel.matches(ada, null));
        check("name match ignores case", ContactTableModel.matches(ada, "LOVE"));
        check("phone match", ContactTableModel.matches(ada, "0100"));
        check("email match, query trimmed", ContactTableModel.matches(ada, " example.com "));
        check("no match", !ContactTableModel.matches(ada, "turing"));
        check("empty fields are fine", !ContactTableModel.matches(new Contact("Grace", "", ""), "555"));
    }

    static void check(String name, boolean ok) {
        System.out.println((ok ? "PASS " : "FAIL ") + name);
        if (!ok) failures++;
    }
}
