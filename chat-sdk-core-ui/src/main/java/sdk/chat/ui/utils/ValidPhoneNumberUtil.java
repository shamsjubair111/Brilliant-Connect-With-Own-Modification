package sdk.chat.ui.utils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

public class ValidPhoneNumberUtil {
    public static String validPhoneNumber(String mobileNumber) throws NumberParseException {
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(mobileNumber, "BD");
        mobileNumber = phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);


//        Log.d("mobileNumber", mobileNumber);
        return mobileNumber;
    }
}
