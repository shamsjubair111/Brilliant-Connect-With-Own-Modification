package sdk.chat.ui.utils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

public class ValidPhoneNumberUtil {
    public static String validPhoneNumber(String mobileNumber) {
        String validPhoneNumber = mobileNumber;
        try {
            PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
            Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(validPhoneNumber, "BD");
            validPhoneNumber = phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException e) {
            e.printStackTrace();
            return mobileNumber;
        }
        return validPhoneNumber;
    }
}
