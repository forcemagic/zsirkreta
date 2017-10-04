package com.speedyblur.kretaremastered.shared;

// TODO: Comment!
public interface IDataStore<T> {
    T requestFromStore(DataStore ds);
    void processRequest(T data);
    void onDecryptionFailure(DecryptionException e);
}
