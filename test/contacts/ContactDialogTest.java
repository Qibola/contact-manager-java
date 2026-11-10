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

        check("valid input passes", ContactDialog.validationError("Ada", "+1 (555) 010-0100", "ada@example.com") == null);
        check("blank phone/email pass", ContactDialog.validationError("Ada", "  ", "") == null);
        check("blank name message", "Please enter a name.".equals(ContactDialog.validationError(" ", "", "")));
        check("letters in phone rejected", startsWith(ContactDialog.validationError("Ada", "555-CALL-ADA", ""), "Phone"));
        check("short phone rejected", startsWith(ContactDialog.validationError("Ada", "555-01", ""), "Phone"));
        check("email without @ rejected", startsWith(ContactDialog.validationError("Ada", "", "ada.example.com"), "Email"));
        check("email without domain dot rejected", startsWith(ContactDialog.validationError("Ada", "", "ada@example"), "Email"));
        check("email with space rejected", startsWith(ContactDialog.validationError("Ada", "", "ada lovelace@example.com"), "Email"));
        check("email is trimmed first", ContactDialog.validationError("Ada", "", "  ada@example.com ") == null);

        if (failures > 0) {
            System.out.println(failures + " test(s) FAILED");
            System.exit(1);
        }
        System.out.println("All tests passed");
    }

    static boolean startsWith(String s, String prefix) {
        return s != null && s.startsWith(prefix);
    }

    static void check(String name, boolean ok) {
        System.out.println((ok ? "PASS " : "FAIL ") + name);
        if (!ok) failures++;
    }
}
