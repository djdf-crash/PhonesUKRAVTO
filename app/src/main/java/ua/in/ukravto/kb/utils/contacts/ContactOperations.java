package ua.in.ukravto.kb.utils.contacts;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;

import ua.in.ukravto.kb.R;
import ua.in.ukravto.kb.repository.database.model.EmployeePhoneModel;

/**
 * Created by djdf.crash on 23.06.2018.
 */
public class ContactOperations {

    private final ContentValues mValues;
    private final BatchOperation mBatchOperation;
    private final Context mContext;
    private boolean mIsSyncOperation;
    private long mRawContactId;
    private int mBackReference;
    private boolean mIsNewContact;

    /**
     * Since we're sending a lot of contact provider operations in a single
     * batched operation, we want to make sure that we "yield" periodically
     * so that the Contact Provider can write changes to the DB, and can
     * open a new transaction.  This prevents ANR (application not responding)
     * errors.  The recommended time to specify that a yield is permitted is
     * with the first operation on a particular contact.  So if we're updating
     * multiple fields for a single contact, we make sure that we call
     * withYieldAllowed(true) on the first field that we update. We use
     * mIsYieldAllowed to keep track of what value we should pass to
     * withYieldAllowed().
     */
    private boolean mIsYieldAllowed;

    /**
     * Returns an instance of ContactOperations instance for adding new contact
     * to the platform contacts provider.
     *
     * @param context the Authenticator Activity context
     * @param userId the userId of the sample SyncAdapter user object
     * @param accountName the username for the SyncAdapter account
     * @param isSyncOperation are we executing this as part of a sync operation?
     * @return instance of ContactOperations
     */
    public static ContactOperations createNewContact(Context context, long userId,
                                                     String accountName, boolean isSyncOperation, BatchOperation batchOperation) {
        return new ContactOperations(context, userId, accountName, isSyncOperation, batchOperation);
    }

    /**
     * Returns an instance of ContactOperations for updating existing contact in
     * the platform contacts provider.
     *
     * @param context the Authenticator Activity context
     * @param rawContactId the unique Id of the existing rawContact
     * @param isSyncOperation are we executing this as part of a sync operation?
     * @return instance of ContactOperations
     */
    public static ContactOperations updateExistingContact(Context context, long rawContactId,
                                                          boolean isSyncOperation, BatchOperation batchOperation) {
        return new ContactOperations(context, rawContactId, isSyncOperation, batchOperation);
    }

    private ContactOperations(Context context, boolean isSyncOperation,
                              BatchOperation batchOperation) {
        mValues = new ContentValues();
        mIsYieldAllowed = true;
        mIsSyncOperation = isSyncOperation;
        mContext = context;
        mBatchOperation = batchOperation;
    }

    private ContactOperations(Context context, long userId, String accountName,
                              boolean isSyncOperation, BatchOperation batchOperation) {
        this(context, isSyncOperation, batchOperation);
        mBackReference = mBatchOperation.size();
        mIsNewContact = true;
        mValues.put(ContactsContract.RawContacts.SOURCE_ID, userId);
        mValues.put(ContactsContract.RawContacts.ACCOUNT_TYPE, context.getString(R.string.ACCOUNT_TYPE));
        mValues.put(ContactsContract.RawContacts.ACCOUNT_NAME, accountName);
        ContentProviderOperation.Builder builder =
                newInsertCpo(ContactsContract.RawContacts.CONTENT_URI, mIsSyncOperation, true).withValues(mValues);
        mBatchOperation.add(builder.build());
    }

    private ContactOperations(Context context, long rawContactId, boolean isSyncOperation,
                              BatchOperation batchOperation) {
        this(context, isSyncOperation, batchOperation);
        mIsNewContact = false;
        mRawContactId = rawContactId;
    }


    public ContactOperations addName(String fullName) {
        mValues.clear();

        if (!TextUtils.isEmpty(fullName)) {
            mValues.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, fullName);
            mValues.put(ContactsContract.CommonDataKinds.StructuredName.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        }
        if (mValues.size() > 0) {
            addInsertOp();
        }
        return this;
    }


    public ContactOperations addEmail(String email) {
        mValues.clear();
        if (!TextUtils.isEmpty(email)) {
            mValues.put(ContactsContract.CommonDataKinds.Email.DATA, email);
            mValues.put(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK);
            mValues.put(ContactsContract.CommonDataKinds.Email.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
            addInsertOp();
        }
        return this;
    }

    /**
     * Adds a phone number
     *
     * @param phone new phone number for the contact
     * @param phoneType the type: cell, home, etc.
     * @return instance of ContactOperations
     */
    public ContactOperations addPhone(String phone, int phoneType) {
        mValues.clear();
        if (!TextUtils.isEmpty(phone)) {
            mValues.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phone);
            mValues.put(ContactsContract.CommonDataKinds.Phone.TYPE, phoneType);
            mValues.put(ContactsContract.CommonDataKinds.Phone.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
            addInsertOp();
        }
        return this;
    }

    public ContactOperations addOrganizationAndDepartmentAndPost(EmployeePhoneModel rawContact) {
        mValues.clear();

        StringBuilder dep = new StringBuilder();
        if (!TextUtils.isEmpty(rawContact.getDepartment())){
            dep.append(rawContact.getDepartment());
//            if (!TextUtils.isEmpty(rawContact.getSection())){
//                dep.append(", ").append(rawContact.getSection());
//            }
        }else if (!TextUtils.isEmpty(rawContact.getSection())){
            dep.append(rawContact.getSection());
        }

        if (!TextUtils.isEmpty(dep.toString())) {
            dep.append(", ").append(rawContact.getPost());
        }else {
            dep.append(rawContact.getPost());
        }

        if (!TextUtils.isEmpty(rawContact.getOrganizationName())) {
            mValues.put(ContactsContract.CommonDataKinds.Organization.COMPANY, rawContact.getOrganizationName());
            mValues.put(ContactsContract.CommonDataKinds.Organization.DEPARTMENT, dep.toString());
            mValues.put(ContactsContract.CommonDataKinds.Organization.TITLE, dep.toString());
            mValues.put(ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.Organization.TYPE_WORK);
            mValues.put(ContactsContract.CommonDataKinds.Organization.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE);
            addInsertOp();
        }
        return this;
    }


    public ContactOperations addPost(String postName) {
        mValues.clear();
        if (!TextUtils.isEmpty(postName)) {
            mValues.put(ContactsContract.CommonDataKinds.Organization.TITLE, postName);
            mValues.put(ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.Organization.TYPE_OTHER);
            mValues.put(ContactsContract.CommonDataKinds.Organization.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE);
            addInsertOp();
        }
        return this;
    }

    /**
     * Updates contact's serverId
     *
     * @param serverId the serverId for this contact
     * @param uri Uri for the existing raw contact to be updated
     * @return instance of ContactOperations
     */
    public ContactOperations updateServerId(long serverId, Uri uri) {
        mValues.clear();
        mValues.put(ContactsContract.RawContacts.SOURCE_ID, serverId);
        addUpdateOp(uri);
        return this;
    }

    public ContactOperations updateIsDelete(int isDeleted, Uri uri) {
        mValues.clear();
        mValues.put(ContactsContract.RawContacts.DELETED, isDeleted);
        addUpdateOp(uri);
        return this;
    }

    /**
     * Updates contact's email
     *
     * @param email email id of the sample SyncAdapter user
     * @param uri Uri for the existing raw contact to be updated
     * @return instance of ContactOperations
     */
    public ContactOperations updateEmail(String email, String existingEmail, Uri uri) {
        if (!TextUtils.equals(existingEmail, email)) {
            mValues.clear();
            mValues.put(ContactsContract.CommonDataKinds.Email.DATA, email);
            addUpdateOp(uri);
        }
        return this;
    }

    /**
     * Updates contact's name. The caller can either provide first-name
     * and last-name fields or a full-name field.
     *
     * @param uri Uri for the existing raw contact to be updated
     * @param existingFirstName the first name stored in provider
     * @param existingLastName the last name stored in provider
     * @param existingFullName the full name stored in provider
     * @param firstName the new first name to store
     * @param lastName the new last name to store
     * @param fullName the new full name to store
     * @return instance of ContactOperations
     */
    public ContactOperations updateName(Uri uri,
                                        String existingFirstName,
                                        String existingLastName,
                                        String existingFullName,
                                        String firstName,
                                        String lastName,
                                        String fullName) {

        mValues.clear();
        if (TextUtils.isEmpty(fullName)) {
            if (!TextUtils.equals(existingFirstName, firstName)) {
                mValues.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, firstName);
            }
            if (!TextUtils.equals(existingLastName, lastName)) {
                mValues.put(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, lastName);
            }
        } else {
            if (!TextUtils.equals(existingFullName, fullName)) {
                mValues.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, fullName);
            }
        }
        if (mValues.size() > 0) {
            addUpdateOp(uri);
        }
        return this;
    }

    public ContactOperations updatePostName(Uri uri, String existingPost, String post) {

        mValues.clear();
        if (!TextUtils.isEmpty(post)) {
            if (!TextUtils.equals(existingPost, post)) {
                mValues.put(ContactsContract.CommonDataKinds.Organization.TITLE, post);
            }
        }
        if (mValues.size() > 0) {
            addUpdateOp(uri);
        }
        return this;
    }

    public ContactOperations updateOrganizationAndDepartmentAndPost(Uri uri, String existingOrg, String existingPost, String existingDep, String nameOrganization, String post, String department) {

        mValues.clear();
        if (!TextUtils.isEmpty(nameOrganization)) {
            if (!TextUtils.equals(existingOrg, nameOrganization)) {
                mValues.put(ContactsContract.CommonDataKinds.Organization.COMPANY, nameOrganization);
            }
            if (!TextUtils.equals(existingPost, post)) {
                mValues.put(ContactsContract.CommonDataKinds.Organization.TITLE, post);
            }
            if (!TextUtils.equals(existingDep, department)) {
                mValues.put(ContactsContract.CommonDataKinds.Organization.DEPARTMENT, department);
            }
        }
        if (mValues.size() > 0) {
            addUpdateOp(uri);
        }
        return this;
    }

    public ContactOperations updateDirtyFlag(boolean isDirty, Uri uri) {
        int isDirtyValue = isDirty ? 1 : 0;
        mValues.clear();
        mValues.put(ContactsContract.RawContacts.DIRTY, isDirtyValue);
        addUpdateOp(uri);
        return this;
    }

    /**
     * Updates contact's phone
     *
     * @param existingNumber phone number stored in contacts provider
     * @param phone new phone number for the contact
     * @param uri Uri for the existing raw contact to be updated
     * @return instance of ContactOperations
     */
    public ContactOperations updatePhone(String existingNumber, String phone, Uri uri) {
        if (!TextUtils.equals(phone, existingNumber)) {
            mValues.clear();
            mValues.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phone);
            addUpdateOp(uri);
        }
        return this;
    }


    /**
     * Adds an insert operation into the batch
     */
    private void addInsertOp() {

        if (!mIsNewContact) {
            mValues.put(ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID, mRawContactId);
        }
        ContentProviderOperation.Builder builder =
                newInsertCpo(ContactsContract.Data.CONTENT_URI, mIsSyncOperation, mIsYieldAllowed);
        builder.withValues(mValues);
        if (mIsNewContact) {
            builder.withValueBackReference(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, mBackReference);
        }
        mIsYieldAllowed = false;
        mBatchOperation.add(builder.build());
    }

    /**
     * Adds an update operation into the batch
     */
    private void addUpdateOp(Uri uri) {
        ContentProviderOperation.Builder builder =
                newUpdateCpo(uri, mIsSyncOperation, mIsYieldAllowed).withValues(mValues);
        mIsYieldAllowed = false;
        mBatchOperation.add(builder.build());
    }

    public static ContentProviderOperation.Builder newInsertCpo(Uri uri,
                                                                boolean isSyncOperation, boolean isYieldAllowed) {
        return ContentProviderOperation
                .newInsert(addCallerIsSyncAdapterParameter(uri, isSyncOperation))
                .withYieldAllowed(isYieldAllowed);
    }

    public static ContentProviderOperation.Builder newUpdateCpo(Uri uri,
                                                                boolean isSyncOperation, boolean isYieldAllowed) {
        return ContentProviderOperation
                .newUpdate(addCallerIsSyncAdapterParameter(uri, isSyncOperation))
                .withYieldAllowed(isYieldAllowed);
    }

    public static ContentProviderOperation.Builder newDeleteCpo(Uri uri,
                                                                boolean isSyncOperation, boolean isYieldAllowed) {
        return ContentProviderOperation
                .newDelete(addCallerIsSyncAdapterParameter(uri, isSyncOperation))
                .withYieldAllowed(isYieldAllowed);
    }

    private static Uri addCallerIsSyncAdapterParameter(Uri uri, boolean isSyncOperation) {
        if (isSyncOperation) {
            // If we're in the middle of a real sync-adapter operation, then go ahead
            // and tell the Contacts provider that we're the sync adapter.  That
            // gives us some special permissions - like the ability to really
            // delete a contact, and the ability to clear the dirty flag.
            //
            // If we're not in the middle of a sync operation (for example, we just
            // locally created/edited a new contact), then we don't want to use
            // the special permissions, and the system will automagically mark
            // the contact as 'dirty' for us!
            return uri.buildUpon()
                    .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
                    .build();
        }
        return uri;
    }

}
