package com.speedyblur.kretaremastered.shared;

import android.content.Context;

public class Common {
    // TODO: Migrate to stable API
    public static final String APIBASE = "https://www.speedyblur.com/kretaapi";
    public static String SQLCRYPT_PWD = "weeee";

    public static String getLocalizedSubjectName(Context context, String subject) {
        int gotResxId = context.getResources().getIdentifier("subject_" + subject, "string", context.getPackageName());
        return gotResxId == 0 ? subject : context.getResources().getString(gotResxId);
    }
}
