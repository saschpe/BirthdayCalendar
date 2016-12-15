/*
 * Copyright (C) 2016 Sascha Peilicke
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
