package saschpe.birthdays.helper;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public final class DisplayHelper {
    private DisplayHelper() {
        // Intentionally left blank
    }

    public static int getWidestScreenEdgeInPixels(@NonNull Context context) {
        DisplayMetrics displayMetrics = getDisplayMetrics(context);

        if (displayMetrics.widthPixels > displayMetrics.heightPixels) {
            return displayMetrics.widthPixels;
        }
        return displayMetrics.heightPixels;
    }

    public static DisplayMetrics getDisplayMetrics(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return getRealMetrics(context);
        } else {
            return getMetricsWithoutNavigationBar(context);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private static DisplayMetrics getRealMetrics(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(outMetrics);

        return outMetrics;
    }

    private static DisplayMetrics getMetricsWithoutNavigationBar(Context context) {
        return context.getResources().getDisplayMetrics();
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    public static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    public static boolean isSW320DP(Context context) {
        return context.getResources().getConfiguration().smallestScreenWidthDp >= 320;
    }

    public static boolean isSW400DP(Context context) {
        return context.getResources().getConfiguration().smallestScreenWidthDp >= 400;
    }

    public static boolean isW600DP(Context context) {
        return context.getResources().getConfiguration().screenWidthDp >= 600;
    }

    public static boolean isLandscape(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static RecyclerView.LayoutManager getSuitableLayoutManager(Context context) {
        if (DisplayHelper.isXLargeTablet(context)) {
            return new GridLayoutManager(context, DisplayHelper.isLandscape(context) ? 3 : 2);
        } else if (DisplayHelper.isW600DP(context)) {
            return new GridLayoutManager(context, DisplayHelper.isLandscape(context) ? 2 : 1);
        } else {
            return new LinearLayoutManager(context);
        }
    }
}
