/*
 * Copyright 2016 Sascha Peilicke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package saschpe.birthdays.provider;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import saschpe.birthdays.BuildConfig;

class AccountContract {
    interface Columns {
        String ACCOUNT_NAME = "account_name";
        String ACCOUNT_TYPE = "account_type";
    }

    static final String CONTENT_AUTHORITY;
    static {
        StringBuilder contentAuthorityBuilder = new StringBuilder("saschpe.birthdays");
        if (BuildConfig.FLAVOR.equals("open")) {
            contentAuthorityBuilder.append(".open");
        }
        if (BuildConfig.DEBUG) {
            contentAuthorityBuilder.append(".debug");
        }
        CONTENT_AUTHORITY = contentAuthorityBuilder.toString();
    }

    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    static final String PATH_ACCOUNT_LIST = "account_list";

    static class AccountList implements Columns, BaseColumns {
        static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_ACCOUNT_LIST).build();

        /**
         * Use if multiple items are returned
         */
        static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.contactreminder.account_list";

        /**
         * Use if single item is returned
         */
        static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.contactreminder.account";

        /**
         * Default "ORDER BY" clause.
         */
        static final String DEFAULT_SORT = Columns.ACCOUNT_TYPE + " ASC";

        static Uri buildUri(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }

        public static String getId(Uri uri) {
            return uri.getLastPathSegment();
        }
    }

    /**
     * Simplify life and return a cursor on account list.
     */
    static Cursor getAccountListCursor(Context context, String selection, String[] selectionArgs) {
        return context.getContentResolver().query(
                AccountList.CONTENT_URI,
                new String[]{
                        AccountList._ID,
                        AccountList.ACCOUNT_NAME,
                        AccountList.ACCOUNT_TYPE
                },
                null,
                selectionArgs,
                AccountList.DEFAULT_SORT);
    }

    /**
     * Private constructor, only static members.
     */
    private AccountContract() {}
}
