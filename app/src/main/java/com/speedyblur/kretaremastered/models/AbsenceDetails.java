package com.speedyblur.kretaremastered.models;

public class AbsenceDetails {
    private final String type;
    private final String provementType;
    private final boolean isProven;

    public AbsenceDetails(String absencetype, String absenceprovementtype, boolean proven) {
        this.type = absencetype;
        this.provementType = absenceprovementtype;
        this.isProven = proven;
    }

    // Getter methods
    public String getType() {
        return type;
    }
    public String getProvementType() {
        return provementType;
    }
    public boolean isProven() {
        return isProven;
    }
}
