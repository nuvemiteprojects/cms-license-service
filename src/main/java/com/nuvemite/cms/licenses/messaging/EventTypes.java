package com.nuvemite.cms.licenses.messaging;

public final class EventTypes {

    public static final String CONSUMER_GROUP = "cms-licenses";

    public static final String LICENSE_GRANTED = "cms.license.granted.v1";
    public static final String LICENSE_APPLICATION_SUBMITTED = "cms.license.application.submitted.v1";
    public static final String LICENSE_ANNUAL_INSPECTION_DUE = "cms.license.annual-inspection.due.v1";
    public static final String INSPECTION_DATE_CONFIRMED = "cms.inspection.date.confirmed.v1";
    public static final String LICENSE_INSPECTION_PROPOSED = "cms.license.inspection.proposed.v1";
    public static final String LICENSE_INSPECTION_SCHEDULED = "cms.license.inspection.scheduled.v1";

    public static final String PAYMENT_COMPLETED = "cms.payment.completed.v1";
    public static final String VISIT_SCHEDULED = "cms.visit.scheduled.v1";

    private EventTypes() {}
}
