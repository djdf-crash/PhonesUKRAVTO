package ua.in.ukravto.kb.utils;

import android.content.Context;

public class Contacts {
    private static final String LOGTAG = "FIND_CONTACT";
    public static Context context;

//    public static void addPhoneContact(Context ctx, Account account, ItemContact itemContact) {
//        Exception e;
//        context = ctx;
//        ArrayList<ContentProviderOperation> cntProOper = new ArrayList();
//        int contactIndex = cntProOper.size();
//        cntProOper.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI).withValue("account_type", account.type).withValue("account_name", account.name).build());
//        cntProOper.add(ContentProviderOperation.newInsert(Data.CONTENT_URI).withValueBackReference("raw_contact_id", contactIndex).withValue("mimetype", "vnd.android.cursor.item/name").withValue("data1", itemContact.getDisplayName()).build());
//        cntProOper.add(ContentProviderOperation.newInsert(Data.CONTENT_URI).withValueBackReference("raw_contact_id", contactIndex).withValue("mimetype", "vnd.android.cursor.item/organization").withValue("data1", itemContact.getOrganizationName()).withValue("data4", itemContact.getPostName()).build());
//        cntProOper.add(ContentProviderOperation.newInsert(Data.CONTENT_URI).withValueBackReference("raw_contact_id", contactIndex).withValue("mimetype", "vnd.android.cursor.item/email_v2").withValue("data1", itemContact.geteMailName()).withValue("data2", Integer.valueOf(2)).build());
//        cntProOper.add(ContentProviderOperation.newInsert(Data.CONTENT_URI).withValueBackReference("raw_contact_id", contactIndex).withValue("mimetype", "vnd.android.cursor.item/contact_event").withValue("data2", Integer.valueOf(3)).withValue("data1", itemContact.getBirthDay()).build());
//        cntProOper.add(ContentProviderOperation.newInsert(Data.CONTENT_URI).withValueBackReference("raw_contact_id", contactIndex).withValue("mimetype", "vnd.android.cursor.item/phone_v2").withValue("data1", itemContact.getMobileTelephones()).withValue("data2", Integer.valueOf(17)).build());
//        if (!itemContact.getExternalTelephones().equals(BuildConfig.FLAVOR)) {
//            cntProOper.add(ContentProviderOperation.newInsert(Data.CONTENT_URI).withValueBackReference("raw_contact_id", contactIndex).withValue("mimetype", "vnd.android.cursor.item/phone_v2").withValue("data1", itemContact.getExternalTelephones()).withValue("data2", Integer.valueOf(3)).build());
//        }
//        if (!itemContact.getInternalTelephone().equals(BuildConfig.FLAVOR)) {
//            cntProOper.add(ContentProviderOperation.newInsert(Data.CONTENT_URI).withValueBackReference("raw_contact_id", contactIndex).withValue("mimetype", "vnd.android.cursor.item/phone_v2").withValue("data1", itemContact.getInternalTelephone()).withValue("data2", Integer.valueOf(10)).build());
//        }
//        try {
//            context.getContentResolver().applyBatch(MainActivity.AUTHORITY, cntProOper);
//            return;
//        } catch (RemoteException e2) {
//            e = e2;
//        } catch (OperationApplicationException e3) {
//            e = e3;
//        }
//        e.printStackTrace();
//    }

//    private static void updateContact(Context context, ItemContact itemContact, String rawContactId) {
//        ArrayList<ContentProviderOperation> cntProOper = new ArrayList();
//        Builder builder = null;
//        String whereDisplayName = "contact_id=? AND mimetype=?";
//        cntProOper.add(ContentProviderOperation.newUpdate(Data.CONTENT_URI).withSelection(whereDisplayName, new String[]{rawContactId, "vnd.android.cursor.item/name"}).withValue("data1", itemContact.getDisplayName()).build());
//        cntProOper.add(ContentProviderOperation.newUpdate(Data.CONTENT_URI).withSelection(whereDisplayName, new String[]{rawContactId, "vnd.android.cursor.item/organization"}).withValue("mimetype", "vnd.android.cursor.item/organization").withValue("data1", itemContact.getOrganizationName()).withValue("data4", itemContact.getPostName()).build());
//        cntProOper.add(ContentProviderOperation.newUpdate(Data.CONTENT_URI).withSelection(whereDisplayName + " AND " + "data2" + "=?", new String[]{rawContactId, "vnd.android.cursor.item/email_v2", String.valueOf(2)}).withValue("mimetype", "vnd.android.cursor.item/email_v2").withValue("data1", itemContact.geteMailName()).build());
//        cntProOper.add(ContentProviderOperation.newUpdate(Data.CONTENT_URI).withSelection(whereDisplayName + " AND " + "data2" + "=?", new String[]{rawContactId, "vnd.android.cursor.item/contact_event", String.valueOf(3)}).withValue("mimetype", "vnd.android.cursor.item/contact_event").withValue("data1", itemContact.getBirthDay()).build());
//        if (!itemContact.getExternalTelephones().equals(BuildConfig.FLAVOR)) {
//            builder = ContentProviderOperation.newUpdate(Data.CONTENT_URI).withSelection(whereDisplayName + " AND " + "data2" + "=?", new String[]{rawContactId, "vnd.android.cursor.item/phone_v2", String.valueOf(3)}).withValue("mimetype", "vnd.android.cursor.item/phone_v2").withValue("data1", itemContact.getExternalTelephones());
//            cntProOper.add(builder.build());
//        }
//        if (!itemContact.getInternalTelephone().equals(BuildConfig.FLAVOR)) {
//            builder = ContentProviderOperation.newUpdate(Data.CONTENT_URI).withSelection(whereDisplayName + " AND " + "data2" + "=?", new String[]{rawContactId, "vnd.android.cursor.item/phone_v2", String.valueOf(10)}).withValue("mimetype", "vnd.android.cursor.item/phone_v2").withValue("data1", itemContact.getInternalTelephone());
//            cntProOper.add(builder.build());
//        }
//        if (builder != null) {
//            try {
//            } catch (RemoteException e) {
//                Exception e2 = e;
//            } catch (OperationApplicationException e3) {
//                e2 = e3;
//            }
//        }
//        context.getContentResolver().applyBatch(MainActivity.AUTHORITY, cntProOper);
//        return;
//        e2.printStackTrace();
//    }

//    public static Boolean getContact(Context context, Account account, ItemContact itemContact) {
//        Cursor phones = context.getContentResolver().query(Phone.CONTENT_URI, new String[0], "data1 LIKE '%" + itemContact.getMobileTelephones() + "%'", null, null, null);
//        if (phones.getCount() == 0) {
//            addPhoneContact(context, account, itemContact);
//        } else {
//            try {
//                if (phones.moveToFirst()) {
//                    String contactId = phones.getString(phones.getColumnIndexOrThrow("contact_id"));
//                    phones = context.getContentResolver().query(RawContacts.CONTENT_URI, new String[]{"account_name", "account_type"}, "contact_id=?", new String[]{contactId}, null);
//                    Log.d(LOGTAG, "phone.getCount: " + phones.getCount());
//                    if (phones.moveToFirst()) {
//                        String contactAccountName = phones.getString(phones.getColumnIndexOrThrow("account_name"));
//                        String contactAccountType = phones.getString(phones.getColumnIndexOrThrow("account_type"));
//                        Log.d(LOGTAG, "contactId: " + contactId + " contactAccountName: " + contactAccountName + " " + itemContact.toString());
//                        if (!account.type.equals(contactAccountType)) {
//                            updateContact(context, itemContact, contactId);
//                        } else if (phones.moveToNext()) {
//                            String contactIdNext = phones.getString(phones.getColumnIndexOrThrow("contact_id"));
//                            Object obj = LOGTAG;
//                            Log.d(obj, "contactIdNext: " + contactIdNext + " contactAccountName: " + contactAccountName + " " + itemContact.toString());
//                            if (obj != null) {
//                                String contactAccountNameNext = phones.getString(phones.getColumnIndexOrThrow("account_name"));
//                                Log.d(LOGTAG, "contactAccountName: " + contactAccountName + " contactAccountNameNext: " + contactAccountNameNext);
//                                if (account.name.equals(contactAccountNameNext)) {
//                                    Log.d(LOGTAG, "equals(contactAccountNameNext) " + itemContact.toString());
//                                    updateContact(context, itemContact, contactIdNext);
//                                }
//                                if (account.name.equals(contactAccountName)) {
//                                    Log.d(LOGTAG, "equals(contactAccountName) " + itemContact.toString());
//                                    updateContact(context, itemContact, contactId);
//                                }
//                            }
//                        } else if (account.name.equals(contactAccountName)) {
//                            updateContact(context, itemContact, contactId);
//                        } else {
//                            addPhoneContact(context, account, itemContact);
//                        }
//                    }
//                    phones.close();
//                }
//            } finally {
//                phones.close();
//            }
//            phones.close();
//        }
//        return Boolean.valueOf(false);
//    }
}
