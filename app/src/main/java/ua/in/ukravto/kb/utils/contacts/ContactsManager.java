package ua.in.ukravto.kb.utils.contacts;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

import ua.in.ukravto.kb.repository.database.model.EmployeePhoneModel;
import ua.in.ukravto.kb.utils.DataTimeUtils;
import ua.in.ukravto.kb.utils.Pref;

import static ua.in.ukravto.kb.service.ContactsSyncAdapterService.TAG;

public class ContactsManager {
    private final Account mAccount;
    private final static String mAccountType = "ua.in.ukravto.kb";
    private final Context ctx;

    public ContactsManager(Context ctx, Account mAccount) {
        this.mAccount = mAccount;
        this.ctx = ctx;
    }

    private void addContact(Context context, Account account, EmployeePhoneModel rawContact, boolean inSync, BatchOperation batchOperation) {

        final ContactOperations contactOp = ContactOperations.createNewContact(
                context, rawContact.getID(), account.name, inSync, batchOperation);

        contactOp.addName(rawContact.getFullName())
                .addEmail(rawContact.getEmail())
                .addPhone(rawContact.getRealPhone(), ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .addOrganizationAndDepartmentAndPost(rawContact);
    }

    private void deleteContact(long rawContactId, BatchOperation batchOperation) {

        batchOperation.add(ContactOperations.newDeleteCpo(
                ContentUris.withAppendedId(ContactsContract.RawContacts.CONTENT_URI, rawContactId),
                true, true).build());
    }

    private void updateContact(Context context, ContentResolver resolver,
                               EmployeePhoneModel rawContact,
                               long rawContactId, BatchOperation batchOperation) {

        boolean existingCellPhone = false;
        boolean existingFullName = false;
        boolean existingOrganizationName = false;
        boolean existingEmail = false;

        final ContactOperations contactOp = ContactOperations.updateExistingContact(context, rawContactId, true, batchOperation);
        try (Cursor c = resolver.query(DataQuery.CONTENT_URI, DataQuery.PROJECTION, DataQuery.SELECTION,
                new String[]{String.valueOf(rawContactId)}, null)) {
            // Iterate over the existing rows of data, and update each one
            // with the information we received from the server.
            while (c.moveToNext()) {
                final long id = c.getLong(DataQuery.COLUMN_ID);
                final String mimeType = c.getString(DataQuery.COLUMN_MIMETYPE);
                final Uri uri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, id);
                switch (mimeType) {
                    case ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE:
                        existingFullName = true;
                        contactOp.updateName(uri,
                                c.getString(DataQuery.COLUMN_GIVEN_NAME),
                                c.getString(DataQuery.COLUMN_FAMILY_NAME),
                                c.getString(DataQuery.COLUMN_FULL_NAME),
                                "",
                                "",
                                rawContact.getFullName());
                        break;
                    case ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE:
                        final int type = c.getInt(DataQuery.COLUMN_PHONE_TYPE);
                        if (type == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) {
                            existingCellPhone = true;
                            contactOp.updatePhone(c.getString(DataQuery.COLUMN_PHONE_NUMBER),
                                    rawContact.getRealPhone(), uri);
                        }
                        break;
                    case ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE:
                        existingEmail = true;
                        contactOp.updateEmail(rawContact.getEmail(),
                                c.getString(DataQuery.COLUMN_EMAIL_ADDRESS), uri);
                        break;
                    case ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE:
                        if (!TextUtils.isEmpty(c.getString(DataQuery.COLUMN_ORGANIZATION_NAME))) {
                            existingOrganizationName = true;

                            StringBuilder dep = new StringBuilder();
                            if (!TextUtils.isEmpty(rawContact.getDepartment())){
                                dep.append(rawContact.getDepartment());
                            }else if (!TextUtils.isEmpty(rawContact.getSection())){
                                dep.append(rawContact.getSection());
                            }

                        if (!TextUtils.isEmpty(dep.toString())) {
                            dep.append(", ").append(rawContact.getPost());
                        }else {
                            dep.append(rawContact.getPost());
                        }

                            contactOp.updateOrganizationAndDepartmentAndPost(
                                    uri,
                                    c.getString(DataQuery.COLUMN_ORGANIZATION_NAME),
                                    c.getString(DataQuery.COLUMN_ORGANIZATION_POST),
                                    c.getString(DataQuery.COLUMN_DEPARTMENT),
                                    rawContact.getOrganizationName(),
                                    dep.toString(),
                                    dep.toString());
                        }

                        break;
                }
            } // while
        }

        if (!existingFullName){
            contactOp.addName(rawContact.getFullName());
        }

        // Add the cell phone, if present and not updated above
        if (!existingCellPhone) {
            contactOp.addPhone(rawContact.getRealPhone(), ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
        }

        if (!existingOrganizationName){
            contactOp.addOrganizationAndDepartmentAndPost(rawContact);
        }

        // Add the email address, if present and not updated above
        if (!existingEmail) {
            contactOp.addEmail(rawContact.getEmail());
        }


        // If we need to update the serverId of the contact record, take
        // care of that.  This will happen if the contact is created on the
        // client, and then synced to the server. When we get the updated
        // record back from the server, we can set the SOURCE_ID property
        // on the contact, so we can (in the future) lookup contacts by
        // the serverId.
        if (false) {
            Uri uri = ContentUris.withAppendedId(ContactsContract.RawContacts.CONTENT_URI, rawContactId);
            contactOp.updateServerId(rawContact.getID(), uri);
        }
    }

    public long syncContacts(List<EmployeePhoneModel> employees, long lastSyncMarker, boolean syncWithPhoneOnly) {

        long currentSyncMarker = lastSyncMarker;

        final ContentResolver resolver = ctx.getContentResolver();
        BatchOperation batchOperation = new BatchOperation(resolver);

        for (EmployeePhoneModel rawContact : employees) {

            if (syncWithPhoneOnly && TextUtils.isEmpty(rawContact.getRealPhone())){
                continue;
            }

            final long rawContactId;

            if (DataTimeUtils.getMillis(rawContact.getLastUpdate()) > currentSyncMarker) {
                currentSyncMarker = DataTimeUtils.getMillis(rawContact.getLastUpdate());
            }

            long serverContactId = rawContact.getID();
            rawContactId = lookupRawContact(resolver, serverContactId);

            if (rawContactId != 0) {
                if (!rawContact.isDelete()) {
                    updateContact(ctx, resolver, rawContact,
                            rawContactId, batchOperation);
                    Log.d(TAG, "In updateContact: " + rawContact.getFullName() + " | org:" + rawContact.getOrganizationName());
                } else {
                    deleteContact(rawContactId, batchOperation);
                    Log.d(TAG, "In deleteContact: " + rawContact.getFullName() + " | org:" + rawContact.getOrganizationName());
                }
            } else {
                Log.d(TAG, "In addContact: " + rawContact.getFullName() + " | org:" + rawContact.getOrganizationName());
                if (!rawContact.isDelete()) {
                    addContact(ctx, mAccount, rawContact, true, batchOperation);
                }
            }

            if (batchOperation.size() == 15){
                Log.d(TAG, "batchOperation.execute() | org:" + rawContact.getOrganizationName());
                batchOperation.execute();
                batchOperation.clear();
            }
        }

        batchOperation.execute();

        return currentSyncMarker;
    }

    private long lookupRawContact(ContentResolver resolver, long serverContactId) {

        long rawContactId = 0;

        try (Cursor c = resolver.query(
                UserIdQuery.CONTENT_URI,
                UserIdQuery.PROJECTION,
                UserIdQuery.SELECTION,
                new String[] {String.valueOf(serverContactId)},
                null)) {
            if ((c != null) && c.moveToFirst()) {
                do {
                    if (c.getInt(UserIdQuery.COLUMN_RAW_DELETED) == 1) {
                        //restoreContact(context, rawContactId);
                    }else {
                        rawContactId = c.getLong(UserIdQuery.COLUMN_RAW_CONTACT_ID);
                        break;
                    }
                }while (c.moveToNext());
            }
        }
        return rawContactId;
    }

//    private void restoreContact(Context context, long rawContactId) {
//        final ContentResolver resolver = context.getContentResolver();
//        final BatchOperation batchOperation = new BatchOperation(context, resolver);
//        final ContactOperations contactOp = ContactOperations.updateExistingContact(context, rawContactId, true, batchOperation);
//        final Uri uri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, rawContactId);
//        contactOp.updateIsDelete(0, uri);
//        batchOperation.execute();
//    }

    public void deleteContacts(List<EmployeePhoneModel> employees) {
        final ContentResolver resolver = ctx.getContentResolver();
        final BatchOperation batchOperation = new BatchOperation(resolver);

        for (EmployeePhoneModel rawContact : employees) {
            final long rawContactId;

            long serverContactId = rawContact.getID();
            rawContactId = lookupRawContact(resolver, serverContactId);

            if (rawContactId != 0) {
                deleteContact(rawContactId, batchOperation);
            }
            if (batchOperation.size() == 15){
                Log.d(TAG, "batchOperation.execute() | org:" + rawContact.getOrganizationName());
                batchOperation.execute();
                batchOperation.clear();
            }
        }

        batchOperation.execute();
    }

    final static class UserIdQuery {

        private UserIdQuery() {
        }

        static final String[] PROJECTION = new String[] {
                ContactsContract.RawContacts._ID,
                ContactsContract.RawContacts.CONTACT_ID,
                ContactsContract.RawContacts.DELETED
        };

        static final int COLUMN_RAW_CONTACT_ID = 0;
        static final int COLUMN_RAW_DELETED = 2;

        static final Uri CONTENT_URI = ContactsContract.RawContacts.CONTENT_URI;

        static final String SELECTION =
                ContactsContract.RawContacts.ACCOUNT_TYPE + "='" + mAccountType + "' AND "
                        + ContactsContract.RawContacts.SOURCE_ID + "=?";
    }

    final private static class DataQuery {

        private DataQuery() {
        }

        static final String[] PROJECTION =
                new String[] {ContactsContract.RawContacts.Data._ID,
                        ContactsContract.RawContacts.SOURCE_ID,
                        ContactsContract.RawContacts.Data.MIMETYPE,
                        ContactsContract.RawContacts.Data.DATA1,
                        ContactsContract.RawContacts.Data.DATA2,
                        ContactsContract.RawContacts.Data.DATA3,
                        ContactsContract.RawContacts.Data.DATA4,
                        ContactsContract.RawContacts.Data.DATA5,
                        ContactsContract.RawContacts.Data.DATA15,
                        ContactsContract.RawContacts.Data.SYNC1};

        static final int COLUMN_ID = 0;
//        public static final int COLUMN_SERVER_ID = 1;
        static final int COLUMN_MIMETYPE = 2;
        static final int COLUMN_DATA1 = 3;
        static final int COLUMN_DATA2 = 4;
        static final int COLUMN_DATA3 = 5;
        static final int COLUMN_DATA4 = 6;
        static final int COLUMN_DATA5 = 7;

        static final Uri CONTENT_URI = ContactsContract.Data.CONTENT_URI;

        static final int COLUMN_PHONE_NUMBER = COLUMN_DATA1;
        static final int COLUMN_PHONE_TYPE = COLUMN_DATA2;
        static final int COLUMN_EMAIL_ADDRESS = COLUMN_DATA1;
        static final int COLUMN_FULL_NAME = COLUMN_DATA1;
        static final int COLUMN_GIVEN_NAME = COLUMN_DATA2;
        static final int COLUMN_FAMILY_NAME = COLUMN_DATA3;
        static final int COLUMN_ORGANIZATION_NAME = COLUMN_DATA1;
        static final int COLUMN_ORGANIZATION_POST = COLUMN_DATA4;
        static final int COLUMN_DEPARTMENT = COLUMN_DATA5;
//        static final int COLUMN_POST_NAME = COLUMN_DATA2;

        static final String SELECTION = ContactsContract.RawContacts.Data.RAW_CONTACT_ID + "=?";
    }
}
