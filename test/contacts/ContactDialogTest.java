package contacts;

/**
 * Tests the dialog's input handling without opening a window:
 *   java -Djava.awt.headless=true -cp out contacts.ContactDialogTest
 */
public class ContactDialogTest {
    private static int failures = 0;

    public static void main(String[] args) {
        Contact c = ContactDialog.fromFields("  Ada Lovelace ", " 555-0100", "ada@example.com  ");
        check("fields are trimmed", new Contact("Ada Lovelace", "555-0100", "ada@example.com").equals(c));
        check("blank name rejected", ContactDialog.fromFields("   ", "1", "a@x") == null);
        check("null name rejected", ContactDialog.fromFields(null, "1", "a@x") == null);
        Contact only = ContactDialog.fromFields("Grace", null, "");
        check("phone/email optional", only != null && only.getPhone().isEmpty() && only.getEmail().isEmpty());

        if (failures > 0) {
            System.out.println(failures + " test(s) FAILED");
            System.exit(1);
        }
        System.out.println("All tests passed");
    }

    static void check(String name, boolean ok) {
        System.out.println((ok ? "PASS " : "FAIL ") + name);
        if (!ok) failures++;
    }
}
