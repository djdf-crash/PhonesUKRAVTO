package ua.in.ukravto.kb.utils.contacts;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;

/**
 * This class handles execution of batch mOperations on Contacts provider.
 */
public class BatchOperation {
    private final String TAG = "BatchOperation";

    private final ContentResolver mResolver;
    // List for storing the batch mOperations
    ArrayList<ContentProviderOperation> mOperations;

    public BatchOperation(ContentResolver resolver) {
        mResolver = resolver;
        mOperations = new ArrayList<>();
    }

    public int size() {
        return mOperations.size();
    }

    public void add(ContentProviderOperation cpo) {
        mOperations.add(cpo);
    }

    public void clear(){
        mOperations.clear();
    }

    public void execute() {
        if (mOperations.size() == 0) {
            return;
        }
        // Apply the mOperations to the content provider

        try {
            mResolver.applyBatch(ContactsContract.AUTHORITY, mOperations);
        } catch (RemoteException | OperationApplicationException e) {
            Log.e(TAG, "storing contact data failed", e);
        }


        mOperations.clear();
    }

}
