package sdk.chat.ui;

import android.graphics.Bitmap;

public class ContactList {

    public Bitmap contactImage;
    public String contactName;
    public String contactNumber;

    public ContactList( Bitmap contactImage, String contactName, String contactNumber) {
        this.contactImage = contactImage;
        this.contactName = contactName;
        this.contactNumber = contactNumber;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
}
