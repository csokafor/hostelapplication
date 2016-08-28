package com.perblo.hostel.helper;

public class HostelApplicationStatus {

    public static final int NOT_SUBMITTED = 0;
    public static final int SUBMITTED = 1;
    
    public static final int NOT_PROCESSED = 1;
    public static final int PRINTED = 2;
    public static final int SENT = 3;
    
    public static final int NOT_PAID = 1;
    public static final int PAID = 2;
    
    public static final int PENDING = 0;
    public static final int SUCCESSFUL = 1;
    public static final int UNSUCCESSFUL = 2;

    public static String getApplicationStatus(int applicationStatus) {

        if (applicationStatus == HostelApplicationStatus.SENT) {
            return new String("Sent");
        } else if (applicationStatus == HostelApplicationStatus.NOT_PROCESSED) {
            return new String("Not Processed");
        } else if (applicationStatus == HostelApplicationStatus.PRINTED) {
            return new String("Printed");
        } else {
            return new String("Not Processed");
        }

    }

    public static String getPaymentStatus(int paymentStatus) {

        if (paymentStatus == HostelApplicationStatus.PAID) {
            return new String("Paid");
        } else {
            return new String("Not Paid");
        }

    }
    
    public static String getBallotStatus(int ballotStatus) {

        if (ballotStatus == HostelApplicationStatus.SUCCESSFUL) {
            return new String("Successful");
        } else if (ballotStatus == HostelApplicationStatus.UNSUCCESSFUL) {
            return new String("Unsuccessful");
        } else if (ballotStatus == HostelApplicationStatus.PENDING) {
            return new String("Pending");
        } else {
            return new String("Unknown Ballot Status");
        }

    }
}
