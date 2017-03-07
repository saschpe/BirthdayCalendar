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

package saschpe.birthdays.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import saschpe.android.utils.adapter.FragmentListPagerAdapter;
import saschpe.android.versioninfo.widget.VersionInfoDialogFragment;
import saschpe.birthdays.BuildConfig;
import saschpe.birthdays.R;
import saschpe.birthdays.fragment.OpenSourceLicensesFragment;
import saschpe.birthdays.fragment.SocialFragment;

public class HelpActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        // Set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(null);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Set up fragment pager adapter
        FragmentListPagerAdapter pagerAdapter = new FragmentListPagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(new SocialFragment(), getString(R.string.social));
        pagerAdapter.addFragment(new OpenSourceLicensesFragment(), getString(R.string.open_source_licenses));

        // Set up nested scrollview
        NestedScrollView scrollView = (NestedScrollView) findViewById(R.id.nested_scroll);
        scrollView.setFillViewport(true);

        // Set up view pager
        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(pagerAdapter);

        // Set up  tab layout
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.help, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.version_info:
                VersionInfoDialogFragment
                        .newInstance(
                                getString(R.string.app_name),
                                BuildConfig.VERSION_NAME,
                                "Sascha Peilicke",
                                R.mipmap.ic_launcher)
                        .show(getFragmentManager(), "version_info");
                return true;
            case R.id.privacy_policy:
                Intent intent = new Intent(Intent.ACTION_VIEW)
                        .setData(Uri.parse("https://saschpe.github.io/BirthdayCalendar/docs/privacy-policy.html"));
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
