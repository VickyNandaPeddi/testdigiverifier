package com.aashdit.digiverifier.config.superadmin.Enum;

public enum ReportType {
    PRE_OFFER("Pre Offer"),
    INTERIM("Interim"),
    FINAL("Final"),
    SUPPLIMENTARY("Supplimentry");

    public final String label;

    private ReportType(String label) {
        this.label = label;
    }

}
