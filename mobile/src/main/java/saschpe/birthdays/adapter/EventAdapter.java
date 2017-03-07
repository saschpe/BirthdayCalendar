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

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import saschpe.android.utils.adapter.base.CursorRecyclerAdapter;
import saschpe.birthdays.R;
import saschpe.birthdays.service.CalendarSyncService;

/**
 * Displays events from this app's calendar.
 */
public final class EventAdapter extends CursorRecyclerAdapter<EventAdapter.EventViewHolder> {
    // Projection array. Creating indices for this array instead of doing
    // dynamic lookups improves performance.
    private static final String[] PROJECTION = new String[] {
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.DTSTART,
    };
    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_TITLE_INDEX = 1;
    private static final int PROJECTION_DESCRIPTION_INDEX = 2;
    private static final int PROJECTION_DT_START_INDEX = 3;
    // "My projections need selections..."
    private static final String SELECTION = "(" + CalendarContract.Events.CALENDAR_ID + " = ?)";

    private LayoutInflater inflater;

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
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.view_agenda_item, parent, false);
        return new EventViewHolder(v);
    }

    @Override
    public void onBindViewHolderCursor(final EventViewHolder holder, final Cursor cursor) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
        holder.eventId = cursor.getLong(PROJECTION_ID_INDEX);
        holder.name.setText(cursor.getString(PROJECTION_TITLE_INDEX));
        holder.date.setText(df.format(new Date(cursor.getInt(PROJECTION_DT_START_INDEX))));
        holder.description.setText(cursor.getString(PROJECTION_DESCRIPTION_INDEX));
        /*TODO: Fix eventId issue first:
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.ACTION_OPEN_EVENT)
                        .putExtra(MainActivity.EXTRA_EVENT_ID, holder.eventId);

                LocalBroadcastManager.getInstance(v.getContext())
                        .sendBroadcast(intent);
            }
        });*/
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        View container;
        long eventId;
        TextView date;
        TextView name;
        TextView description;

        EventViewHolder(View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.constraint_layout);
            date = (TextView) itemView.findViewById(R.id.date);
            name = (TextView) itemView.findViewById(R.id.title);
            description = (TextView) itemView.findViewById(R.id.description);
        }
    }
}
