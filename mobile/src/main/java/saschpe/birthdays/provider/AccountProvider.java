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

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import java.util.Arrays;

import saschpe.birthdays.BuildConfig;

public class AccountProvider extends ContentProvider {
    private static final String TAG = AccountProvider.class.getSimpleName();
    private static final UriMatcher URI_MATCHER = buildUriMatcher();
    private static final int ACCOUNT_LIST = 100;
    private static final int ACCOUNT_LIST_ID = 101;

    /**
     * Build and return a {@link android.content.UriMatcher} that catches all {@link android.net.Uri} variations supported by
     * this {@link android.content.ContentProvider}.
     */
    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = AccountContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, AccountContract.PATH_ACCOUNT_LIST, ACCOUNT_LIST);
        matcher.addURI(authority, AccountContract.PATH_ACCOUNT_LIST + "/#", ACCOUNT_LIST_ID);

        return matcher;
    }

    private AccountDatabase database;

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.v(TAG, "Delete uri " + uri);

        final SQLiteDatabase db = database.getWritableDatabase();

        int count;
        final int match = URI_MATCHER.match(uri);
        switch (match) {
            case ACCOUNT_LIST:
                count = db.delete(AccountDatabase.Tables.ACCOUNT_LIST, selection, selectionArgs);
                break;
            case ACCOUNT_LIST_ID:
                final String rowId = uri.getPathSegments().get(1);
                String where = "";
                if (!TextUtils.isEmpty(selection)) {
                    where = " AND (" + selection + ")";
                }
                final String rowSelection = BaseColumns._ID + "=" + rowId + where;
                count = db.delete(AccountDatabase.Tables.ACCOUNT_LIST, rowSelection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Notify about changes in DB
        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    @Override
    public String getType(Uri uri) {
        final int match = URI_MATCHER.match(uri);
        switch (match) {
            case ACCOUNT_LIST:
                return AccountContract.AccountList.CONTENT_TYPE;
            case ACCOUNT_LIST_ID:
                return AccountContract.AccountList.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(TAG, "Insert uri " + uri + " with values: " + values.toString());

        final SQLiteDatabase db = database.getWritableDatabase();

        Uri rowUri = null;
        try {
            final int match = URI_MATCHER.match(uri);
            switch (match) {
                case ACCOUNT_LIST:
                    long rowId = db.insertOrThrow(AccountDatabase.Tables.ACCOUNT_LIST, null, values);
                    rowUri = AccountContract.AccountList.buildUri(Long.toString(rowId));
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        } catch (SQLiteConstraintException e) {
            Log.e(TAG, "Entry already existing?", e);
        }

        // Notify about changes in DB
        getContext().getContentResolver().notifyChange(uri, null);

        return rowUri;
    }

    @Override
    public boolean onCreate() {
        final Context context = getContext();
        database = new AccountDatabase(context);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        Log.v(TAG, "Query uri " + uri + " with projection: " + Arrays.toString(projection));

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        SQLiteDatabase db = database.getReadableDatabase();

        final int match = URI_MATCHER.match(uri);
        switch (match) {
            case ACCOUNT_LIST:
                qb.setTables(AccountDatabase.Tables.ACCOUNT_LIST);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        Cursor cursor = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        if (BuildConfig.DEBUG)
            DatabaseUtils.dumpCursor(cursor);

        // Notify through cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        Log.e(TAG, "Not supported");
        throw new UnsupportedOperationException("Not supported");
    }
}
