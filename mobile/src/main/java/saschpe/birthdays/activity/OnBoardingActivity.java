/*
 * Copyright (C) 2017 Sascha Peilicke
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

import android.animation.ArgbEvaluator;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import saschpe.birthdays.R;
import saschpe.birthdays.helper.PreferencesHelper;

public final class OnBoardingActivity extends AppCompatActivity {
    private static final String STATE_PAGE = "page";
    private static final String PREF_PAGE = "page";
    private static final int PAGE_COUNT = 3;
    private final ArgbEvaluator evaluator;
    private ViewPager viewPager;
    private int[] pageColors;
    private int page;
    private Button done;
    private ImageButton next;
    private ImageView[] indicators;

    public OnBoardingActivity() {
        evaluator = new ArgbEvaluator();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding);

        if (savedInstanceState != null) {
            page = savedInstanceState.getInt(STATE_PAGE, 0);
        } else {
            // Either load from previous invocation or use default
            page = getPreferences(MODE_PRIVATE).getInt(PREF_PAGE, 0);
        }

        pageColors = new int[]{
                ContextCompat.getColor(this, R.color.accent),
                ContextCompat.getColor(this, R.color.primary),
                ContextCompat.getColor(this, R.color.primary_dark)
        };
        indicators = new ImageView[]{
                findViewById(R.id.indicator_one),
                findViewById(R.id.indicator_two),
                findViewById(R.id.indicator_three)
        };

        next = findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                page += 1;
                viewPager.setCurrentItem(page);
            }
        });
        Button skip = findViewById(R.id.skip);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(OnBoardingActivity.this, MainActivity.class));
                finish();
            }
        });
        done = findViewById(R.id.done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(OnBoardingActivity.this, MainActivity.class));
                finish();
                PreferencesHelper.setOnboardingFinished(OnBoardingActivity.this, true);
            }
        });

        viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(new OnBoardingPagerAdapter(getSupportFragmentManager()));
        viewPager.setCurrentItem(page);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                int colorUpdate = (Integer) evaluator.evaluate(positionOffset, pageColors[position], pageColors[position == 2 ? position : position + 1]);
                viewPager.setBackgroundColor(colorUpdate);
            }

            @Override
            public void onPageSelected(int position) {
                page = position;
                updateIndicators(position);
                viewPager.setBackgroundColor(pageColors[position]);
                next.setVisibility(position == PAGE_COUNT - 1 ? View.GONE : View.VISIBLE);
                done.setVisibility(position == PAGE_COUNT - 1 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        updateIndicators(page);
    }

    private void updateIndicators(final int position) {
        for (int i = 0; i < indicators.length; i++) {
            indicators[i].setBackgroundResource(
                    i == position ? R.drawable.indicator_selected : R.drawable.indicator_unselected
            );
        }
    }

    public static final class OnBoardingFragment extends Fragment {
        private static final String ARG_PAGE = "page";
        private ImageView image;
        private TextView title, description;
        private int page;

        public static OnBoardingFragment newInstance(int page) {
            OnBoardingFragment fragment = new OnBoardingFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_PAGE, page);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            page = getArguments().getInt(ARG_PAGE);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_on_boarding, container, false);
            view.setTag(page);
            image = view.findViewById(R.id.image);
            title = view.findViewById(R.id.title);
            description = view.findViewById(R.id.description);
            return view;
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            switch (page) {
                case 0:
                    image.setImageDrawable(getResources().getDrawable(R.drawable.ic_cake_variant_192dp));
                    title.setText(R.string.claim_one_title);
                    description.setText(R.string.claim_one_description);
                    break;
                case 1:
                    image.setImageDrawable(getResources().getDrawable(R.drawable.ic_puzzle_192dp));
                    title.setText(R.string.claim_two_title);
                    description.setText(R.string.claim_two_description);
                    break;
                default:
                    image.setImageDrawable(getResources().getDrawable(R.drawable.ic_alarm_on_192dp));
                    title.setText(R.string.claim_three_title);
                    description.setText(R.string.claim_three_description);
                    break;
            }
        }
    }

    static final class OnBoardingPagerAdapter extends FragmentPagerAdapter {
        OnBoardingPagerAdapter(final FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return OnBoardingFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }
    }
}
