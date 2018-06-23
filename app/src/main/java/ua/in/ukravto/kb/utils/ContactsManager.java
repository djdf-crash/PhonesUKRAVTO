package ua.in.ukravto.kb.utils;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.List;

import ua.in.ukravto.kb.R;
import ua.in.ukravto.kb.repository.database.model.EmployeePhoneModel;

import static android.support.constraint.Constraints.TAG;

public class ContactsManager {
    private static final String LOGTAG = "FIND_CONTACT";
    private static Account mAccount;

    public static void addContact(Context context, Account account, EmployeePhoneModel rawContact, boolean inSync, BatchOperation batchOperation) {

        final ContactOperations contactOp = ContactOperations.createNewContact(
                context, rawContact.getID(), account.name, inSync, batchOperation);

        contactOp.addName(rawContact.getFullName(), "","")
                .addEmail(rawContact.getEmail())
                .addPhone(rawContact.getRealPhone(), ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .addOrganization(rawContact.getOrganizationName())
                .addPost(rawContact.getPost(), ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK);
    }

    private static void deleteContact(Context context, long rawContactId, BatchOperation batchOperation) {

        batchOperation.add(ContactOperations.newDeleteCpo(
                ContentUris.withAppendedId(ContactsContract.RawContacts.CONTENT_URI, rawContactId),
                true, true).build());
    }

    public static void updateContact(Context context, ContentResolver resolver,
                                     EmployeePhoneModel rawContact, boolean updateServerId,
                                     boolean inSync, long rawContactId, BatchOperation batchOperation) {

        boolean existingCellPhone = false;
        boolean existingOrganizationName = false;
        boolean existingPostName = false;
        boolean existingEmail = false;

        final Cursor c =
                resolver.query(DataQuery.CONTENT_URI, DataQuery.PROJECTION, DataQuery.SELECTION,
                        new String[] {String.valueOf(rawContactId)}, null);
        final ContactOperations contactOp =
                ContactOperations.updateExistingContact(context, rawContactId,
                        inSync, batchOperation);
        try {
            // Iterate over the existing rows of data, and update each one
            // with the information we received from the server.
            while (c.moveToNext()) {
                final long id = c.getLong(DataQuery.COLUMN_ID);
                final String mimeType = c.getString(DataQuery.COLUMN_MIMETYPE);
                final Uri uri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, id);
                if (mimeType.equals(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)) {
                    contactOp.updateName(uri,
                            c.getString(DataQuery.COLUMN_GIVEN_NAME),
                            c.getString(DataQuery.COLUMN_FAMILY_NAME),
                            c.getString(DataQuery.COLUMN_FULL_NAME),
                            "",
                            "",
                            rawContact.getFullName());
                } else if (mimeType.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
                    final int type = c.getInt(DataQuery.COLUMN_PHONE_TYPE);
                    if (type == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) {
                        existingCellPhone = true;
                        contactOp.updatePhone(c.getString(DataQuery.COLUMN_PHONE_NUMBER),
                                rawContact.getRealPhone(), uri);
                    }
                } else if (mimeType.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
                    existingEmail = true;
                    contactOp.updateEmail(rawContact.getEmail(),
                            c.getString(DataQuery.COLUMN_EMAIL_ADDRESS), uri);
                }else if (mimeType.equals(ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)){
                    existingOrganizationName = true;
                    contactOp.updateEmail(rawContact.getOrganizationName(),
                            c.getString(DataQuery.COLUMN_ORGANIZATION_NAME), uri);
                } else if (mimeType.equals(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)) {
                    final int type = c.getInt(DataQuery.COLUMN_PHONE_TYPE);
                    if (type == ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK) {
                        existingPostName = true;
                        contactOp.updatePostName(uri, c.getString(DataQuery.COLUMN_POST_NAME), rawContact.getPost());
                    }
                }
            } // while
        } finally {
            c.close();
        }

        // Add the cell phone, if present and not updated above
        if (!existingCellPhone) {
            contactOp.addPhone(rawContact.getRealPhone(), ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
        }

        if (!existingOrganizationName){
            contactOp.addOrganization(rawContact.getOrganizationName());
        }

        // Add the email address, if present and not updated above
        if (!existingEmail) {
            contactOp.addEmail(rawContact.getEmail());
        }

        if (!existingPostName){
            contactOp.addPost(rawContact.getPost(), ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK);
        }
        // If we need to update the serverId of the contact record, take
        // care of that.  This will happen if the contact is created on the
        // client, and then synced to the server. When we get the updated
        // record back from the server, we can set the SOURCE_ID property
        // on the contact, so we can (in the future) lookup contacts by
        // the serverId.
        if (updateServerId) {
            Uri uri = ContentUris.withAppendedId(ContactsContract.RawContacts.CONTENT_URI, rawContactId);
            contactOp.updateServerId(rawContact.getID(), uri);
        }

//        // If we don't have a status profile, then create one.  This could
//        // happen for contacts that were created on the client - we don't
//        // create the status profile until after the first sync...
//        final long serverId = rawContact.getID();
//        final long profileId = lookupProfile(resolver, serverId);
//        if (profileId <= 0) {
//            contactOp.addProfileAction(serverId);
//        }
    }

    public static void syncContacts(Context context, Account account, List<EmployeePhoneModel> employees) {

        mAccount = account;

        final ContentResolver resolver = context.getContentResolver();
        final BatchOperation batchOperation = new BatchOperation(context, resolver);

        for (EmployeePhoneModel rawContact : employees) {

            final long rawContactId;

            long serverContactId = rawContact.getID();
            rawContactId = lookupRawContact(resolver, serverContactId);

            if (rawContactId != 0) {
                if (!rawContact.isDelete()) {
                    updateContact(context, resolver, rawContact, false,
                            true, rawContactId, batchOperation);
                } else {
                    deleteContact(context, rawContactId, batchOperation);
                }
            } else {
                Log.d(TAG, "In addContact");
                if (!rawContact.isDelete()) {
                    addContact(context, account, rawContact, true, batchOperation);
                }
            }

        }

        batchOperation.execute();

    }

    private static long lookupRawContact(ContentResolver resolver, long serverContactId) {

        long rawContactId = 0;
        final Cursor c = resolver.query(
                UserIdQuery.CONTENT_URI,
                UserIdQuery.PROJECTION,
                UserIdQuery.SELECTION,
                new String[] {String.valueOf(serverContactId)},
                null);
        try {
            if ((c != null) && c.moveToFirst()) {
                rawContactId = c.getLong(UserIdQuery.COLUMN_RAW_CONTACT_ID);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return rawContactId;
    }

    final private static class UserIdQuery {

        private UserIdQuery() {
        }

        final static String[] PROJECTION = new String[] {
                ContactsContract.RawContacts._ID,
                ContactsContract.RawContacts.CONTACT_ID
        };

        final static int COLUMN_RAW_CONTACT_ID = 0;
        final static int COLUMN_LINKED_CONTACT_ID = 1;

        final static Uri CONTENT_URI = ContactsContract.RawContacts.CONTENT_URI;

        static final String SELECTION =
                ContactsContract.RawContacts.ACCOUNT_TYPE + "='" + mAccount.type + "' AND "
                        + ContactsContract.RawContacts.SOURCE_ID + "=?";
    }

    final private static class DataQuery {

        private DataQuery() {
        }

        static final String[] PROJECTION =
                new String[] {ContactsContract.RawContacts.Data._ID, ContactsContract.RawContacts.SOURCE_ID, ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.RawContacts.Data.DATA1,
                        ContactsContract.RawContacts.Data.DATA2, ContactsContract.RawContacts.Data.DATA3, ContactsContract.RawContacts.Data.DATA15, ContactsContract.RawContacts.Data.SYNC1};

        static final int COLUMN_ID = 0;
        public static final int COLUMN_SERVER_ID = 1;
        static final int COLUMN_MIMETYPE = 2;
        static final int COLUMN_DATA1 = 3;
        static final int COLUMN_DATA2 = 4;
        static final int COLUMN_DATA3 = 5;
        public static final int COLUMN_DATA15 = 6;
        static final int COLUMN_SYNC1 = 7;

        static final Uri CONTENT_URI = ContactsContract.Data.CONTENT_URI;

        static final int COLUMN_PHONE_NUMBER = COLUMN_DATA1;
        static final int COLUMN_PHONE_TYPE = COLUMN_DATA2;
        static final int COLUMN_EMAIL_ADDRESS = COLUMN_DATA1;
        static final int COLUMN_FULL_NAME = COLUMN_DATA1;
        static final int COLUMN_GIVEN_NAME = COLUMN_DATA2;
        static final int COLUMN_FAMILY_NAME = COLUMN_DATA3;
        static final int COLUMN_ORGANIZATION_NAME = COLUMN_DATA2;
        static final int COLUMN_POST_NAME = COLUMN_DATA2;

        static final String SELECTION = ContactsContract.RawContacts.Data.RAW_CONTACT_ID + "=?";
    }

    /**
     * Constants for a query to read basic contact columns
     */
    final public static class ContactQuery {
        private ContactQuery() {
        }

        public static final String[] PROJECTION =
                new String[] {ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME};

        public static final int COLUMN_ID = 0;
        public static final int COLUMN_DISPLAY_NAME = 1;
    }
}