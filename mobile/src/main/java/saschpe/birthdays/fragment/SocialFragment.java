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

package saschpe.birthdays.fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import saschpe.birthdays.R;

public final class SocialFragment extends Fragment {
    private static final String[] SUPPORT_EMAIL_ADDRESS = new String[] {"saschpe+birthdaycalender@mailbox.org"};
    private static final String TWITTER_NAME = "saschpe";

    private TextView provideFeedback;
    private TextView followTwitter;
    private TextView rateOnPlayStore;
    private TextView recommendToFriend;
    private TextView forkOnGithub;
    private String packageName;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        packageName = getContext().getPackageName().replace(".debug", "");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_social, container, false);
        provideFeedback = (TextView) view.findViewById(R.id.provide_feedback);
        followTwitter = (TextView) view.findViewById(R.id.follow_twitter);
        rateOnPlayStore = (TextView) view.findViewById(R.id.rate_play_store);
        recommendToFriend = (TextView) view.findViewById(R.id.recommend_to_friend);
        forkOnGithub = (TextView) view.findViewById(R.id.fork_on_github);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        provideFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String subject = getString(R.string.feedback_mail_subject_template, getString(R.string.app_name));

                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", SUPPORT_EMAIL_ADDRESS[0], null))
                        .putExtra(Intent.EXTRA_SUBJECT, subject)
                        .putExtra(Intent.EXTRA_TEXT, "")
                        .putExtra(Intent.EXTRA_EMAIL, SUPPORT_EMAIL_ADDRESS);
                startActivity(Intent.createChooser(emailIntent, view.getContext().getString(R.string.send_email)));
            }
        });
        followTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=" + TWITTER_NAME)));
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/#!/" + TWITTER_NAME)));
                }
            }
        });
        rateOnPlayStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // To count with Play market back stack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.
                int flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
                if (Build.VERSION.SDK_INT >= 21) {
                    flags |= Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
                } else {
                    //noinspection deprecation
                    flags |= Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
                }
                Intent goToMarket = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + packageName))
                        .addFlags(flags);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + packageName)));
                }
            }
        });
        recommendToFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String subject = getString(R.string.get_app_template, getString(R.string.app_name));
                String body = Uri.parse("http://play.google.com/store/apps/details?id=" + packageName).toString();

                Intent sharingIntent = new Intent(Intent.ACTION_SEND)
                        .setType("text/plain")
                        .putExtra(Intent.EXTRA_SUBJECT, subject)
                        .putExtra(Intent.EXTRA_TEXT, body);
                startActivity(Intent.createChooser(sharingIntent, view.getContext().getString(R.string.share_via)));
            }
        });
        forkOnGithub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW)
                        .setData(Uri.parse("https://github.com/saschpe/BirthdayCalendar")));
            }
        });
    }
}