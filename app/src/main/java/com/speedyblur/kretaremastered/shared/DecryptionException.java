package com.speedyblur.kretaremastered.shared;

public class DecryptionException extends Exception {
    public DecryptionException() {
        super("Unable to open database. (Is encrypted or is not a DB) --> Assuming incorrect password");
    }
}
