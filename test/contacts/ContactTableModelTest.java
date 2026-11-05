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

    static void check(String name, boolean ok) {
        System.out.println((ok ? "PASS " : "FAIL ") + name);
        if (!ok) failures++;
    }
}
