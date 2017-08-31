package com.speedyblur.kretaremastered.shared;

import android.content.Context;

public class Common {
    public static final String APIBASE = "https://staging.speedyblur.com/kretaapi/dummy-withgrades";
    public static String SQLCRYPT_PWD = "weeee";

    public static String getLocalizedSubjectName(Context context, String subject) {
        int gotResxId = context.getResources().getIdentifier("subject_" + subject, "string", context.getPackageName());
        return gotResxId == 0 ? subject : context.getResources().getString(gotResxId);
    }
}
