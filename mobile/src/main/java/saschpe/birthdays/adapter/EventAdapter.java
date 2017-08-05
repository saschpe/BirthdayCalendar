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

package saschpe.birthdays.adapter;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.RequestOptions;

import java.text.DateFormat;
import java.util.Calendar;

import saschpe.android.utils.adapter.base.CursorRecyclerAdapter;
import saschpe.birthdays.R;
import saschpe.birthdays.service.CalendarSyncService;
import saschpe.birthdays.util.GlideApp;

/**
 * Displays events from this app's calendar.
 */
public final class EventAdapter extends CursorRecyclerAdapter<EventAdapter.BirthdayViewHolder> {
    // Projection array. Creating indices for this array instead of doing
    // dynamic lookups improves performance.
    private static final String[] PROJECTION = new String[] {
            CalendarContract.Instances._ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.DESCRIPTION,
            CalendarContract.Instances.DTSTART,
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.SYNC_DATA1 + " as " + CalendarContract.Instances.SYNC_DATA1
    };
    // The indices for the projection array above.
    private static final int PROJECTION_TITLE_INDEX = 1;
    private static final int PROJECTION_DESCRIPTION_INDEX = 2;
    private static final int PROJECTION_DT_START_INDEX = 3;
    private static final int PROJECTION_EVENT_ID_INDEX = 4;
    private static final int PROJECTION_SYNC_DATA1_INDEX = 5;
    // "My projections need selections..."
    private static final String SELECTION = "(" + CalendarContract.Events.CALENDAR_ID + " = ?)";

    private final LayoutInflater inflater;
    private static final DateFormat DEFAULT_DATE_FORMAT = DateFormat.getDateInstance(DateFormat.DEFAULT);

    public EventAdapter(@NonNull Context context) {
        String[] selectionArgs = new String[] {
                String.valueOf(CalendarSyncService.getCalendar(context))
        };

        // Looking one year into the future is enough to display
        // everybody's birthday once...
        Calendar now = Calendar.getInstance();
        String nowString = Long.toString(now.getTimeInMillis());
        now.add(Calendar.YEAR, 1);
        String oneYearFromNow = Long.toString(now.getTimeInMillis());

        Uri eventsUri = CalendarContract.Instances.CONTENT_URI.buildUpon()
                .appendEncodedPath(nowString)
                .appendEncodedPath(oneYearFromNow)
                .build();

        //noinspection MissingPermission
        Cursor cursor = context.getContentResolver()
                .query(eventsUri, PROJECTION, SELECTION, selectionArgs,
                        CalendarContract.Instances.DTSTART + " ASC");

        init(cursor); // See base class

        inflater = LayoutInflater.from(context);
    }

    @Override
    public BirthdayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.view_birthday, parent, false);
        return new BirthdayViewHolder(v);
    }

    @Override
    public void onBindViewHolderCursor(final BirthdayViewHolder holder, final Cursor cursor) {
        Calendar birthday = Calendar.getInstance();
        birthday.setTimeInMillis(cursor.getLong(PROJECTION_DT_START_INDEX));

        holder.name.setText(cursor.getString(PROJECTION_TITLE_INDEX));
        holder.date.setText(DEFAULT_DATE_FORMAT.format(birthday.getTime()));
        holder.description.setText(cursor.getString(PROJECTION_DESCRIPTION_INDEX));
        holder.eventId = cursor.getLong(PROJECTION_EVENT_ID_INDEX);
        holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Uri uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, holder.eventId);
                Intent intent = new Intent(Intent.ACTION_VIEW)
                        .setData(uri);
                view.getContext().startActivity(intent);
            }
        });

        // Display contact picture
        Long contactId = cursor.getLong(PROJECTION_SYNC_DATA1_INDEX);
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        if (photoUri != null) {
            GlideApp
                    .with(holder.photo.getContext())
                    .load(photoUri)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.mipmap.ic_launcher)
                    .into(holder.photo);
            holder.photo.setVisibility(View.VISIBLE);
        }
    }

    static final class BirthdayViewHolder extends RecyclerView.ViewHolder {
        final ImageView photo;
        final TextView date;
        final TextView name;
        final TextView description;
        final ConstraintLayout constraintLayout;
        Long eventId;

        BirthdayViewHolder(final View itemView) {
            super(itemView);
            constraintLayout = itemView.findViewById(R.id.constraint_layout);
            photo = itemView.findViewById(R.id.photo);
            date = itemView.findViewById(R.id.date);
            name = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
        }
    }
}
