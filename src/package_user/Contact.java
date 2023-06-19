package package_user;

public class Contact {
    private String contact;
    private String type;

    public Contact(String c, String s) {
        contact = c;
        type = s;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
