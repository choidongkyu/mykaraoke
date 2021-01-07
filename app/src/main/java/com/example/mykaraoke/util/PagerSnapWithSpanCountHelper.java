package com.example.mykaraoke.util;

import android.graphics.PointF;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

/**
 * ---- Description by VO NAD------
 */
public class PagerSnapWithSpanCountHelper
        extends SnapHelper {
    private int mSpanCount;
    private boolean mIsEnableCenter;

    public PagerSnapWithSpanCountHelper(int mSpanCount,
                                        boolean mIsEnableCenter) {
        // spanCount always >=1
        this.mSpanCount = mSpanCount;
        this.mIsEnableCenter = mIsEnableCenter;
    }

    public PagerSnapWithSpanCountHelper(int mSpanCount) {
        // spanCount always >=1
        this.mSpanCount = mSpanCount;
    }

    // Orientation helpers are lazily created per LayoutManager.
    @Nullable
    private OrientationHelper mVerticalHelper;
    @Nullable
    private OrientationHelper mHorizontalHelper;

    @Nullable
    @Override
    public int[] calculateDistanceToFinalSnap(
            @NonNull final RecyclerView.LayoutManager layoutManager,
            @NonNull final View targetView) {
        int[] out = new int[2];
        if (layoutManager.canScrollHorizontally()) {
            out[0] = mIsEnableCenter ?
                    distanceToCenter(layoutManager,
                            targetView,
                            getHorizontalHelper(layoutManager)) :
                    distanceToStart(targetView,
                            getHorizontalHelper(layoutManager));
        } else {
            out[0] = 0;
        }

        if (layoutManager.canScrollVertically()) {
            out[1] = mIsEnableCenter ?
                    distanceToCenter(layoutManager,
                            targetView,
                            getVerticalHelper(layoutManager)) :
                    distanceToStart(targetView,
                            getVerticalHelper(layoutManager));
        } else {
            out[1] = 0;
        }
        return out;
    }

    private View findStartView(RecyclerView.LayoutManager layoutManager,
                               OrientationHelper helper) {

        if (layoutManager instanceof LinearLayoutManager) {
            int firstChild = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();

            boolean isLastItem = ((LinearLayoutManager) layoutManager)
                    .findLastCompletelyVisibleItemPosition()
                    == layoutManager.getItemCount() - 1;
            if (firstChild == RecyclerView.NO_POSITION || isLastItem) {
                return null;
            }

            View child = layoutManager.findViewByPosition(firstChild);
            if (helper.getDecoratedEnd(child) >= helper.getDecoratedMeasurement(child) / 2
                    && helper.getDecoratedEnd(child) > 0) {

                return child;
            } else {
                if (((LinearLayoutManager) layoutManager).findLastCompletelyVisibleItemPosition()
                        == layoutManager.getItemCount() - 1) {

                    return null;
                } else {

                    return layoutManager.findViewByPosition(firstChild + 1);
                }
            }
        }

        return findSnapView(layoutManager);
    }

    /*
    center item.
    * */
    @Nullable
    private View findCenterView(RecyclerView.LayoutManager layoutManager,
                                OrientationHelper helper) {
        int childCount = layoutManager.getChildCount();
        if (childCount == 0) {
            return null;
        }

        View closestChild = null;
        final int center;
        if (layoutManager.getClipToPadding()) {
            center = helper.getStartAfterPadding() + helper.getTotalSpace() / 2;
        } else {
            center = helper.getEnd() / 2;
        }
        int absClosest = Integer.MAX_VALUE;

        for (int i = 0; i < childCount; i++) {
            final View child = layoutManager.getChildAt(i);
            int childCenter = helper.getDecoratedStart(child)
                    + (helper.getDecoratedMeasurement(child) / 2);
            int absDistance = Math.abs(childCenter - center);

            if (absDistance < absClosest) {
                absClosest = absDistance;
                closestChild = child;
            }
        }
        return closestChild;
    }

    @Nullable
    @Override
    public View findSnapView(final RecyclerView.LayoutManager layoutManager) {
        if (layoutManager.canScrollVertically()) {
            return mIsEnableCenter ?
                    findCenterView(layoutManager,
                            getVerticalHelper(layoutManager)) :
                    findStartView(layoutManager,
                            getVerticalHelper(layoutManager));
        } else if (layoutManager.canScrollHorizontally()) {
            return mIsEnableCenter ?
                    findCenterView(layoutManager,
                            getHorizontalHelper(layoutManager)) :
                    findStartView(layoutManager,
                            getHorizontalHelper(layoutManager));
        }
        return null;
    }

    @Override
    public int findTargetSnapPosition(final RecyclerView.LayoutManager layoutManager,
                                      final int velocityX,
                                      final int velocityY) {
        final int itemCount = layoutManager.getItemCount();
        if (itemCount == 0) {
            return RecyclerView.NO_POSITION;
        }

        View mStartMostChildView = null;
        if (layoutManager.canScrollVertically()) {
            mStartMostChildView = mIsEnableCenter ?
                    findCenterView(layoutManager,
                            getVerticalHelper(layoutManager)) :
                    findStartView(layoutManager,
                            getVerticalHelper(layoutManager));
        } else if (layoutManager.canScrollHorizontally()) {
            mStartMostChildView = mIsEnableCenter ?
                    findCenterView(layoutManager,
                            getHorizontalHelper(layoutManager)) :
                    findStartView(layoutManager,
                            getHorizontalHelper(layoutManager));
        }

        if (mStartMostChildView == null) {
            return RecyclerView.NO_POSITION;
        }
        final int centerPosition = layoutManager.getPosition(mStartMostChildView);
        if (centerPosition == RecyclerView.NO_POSITION) {
            return RecyclerView.NO_POSITION;
        }

        final boolean forwardDirection;
        if (layoutManager.canScrollHorizontally()) {
            forwardDirection = velocityX > 0;
        } else {
            forwardDirection = velocityY > 0;
        }
        boolean reverseLayout = false;
        if ((layoutManager instanceof RecyclerView.SmoothScroller.ScrollVectorProvider)) {
            RecyclerView.SmoothScroller.ScrollVectorProvider vectorProvider =
                    (RecyclerView.SmoothScroller.ScrollVectorProvider) layoutManager;
            PointF vectorForEnd = vectorProvider.computeScrollVectorForPosition(itemCount - 1);
            if (vectorForEnd != null) {
                reverseLayout = vectorForEnd.x < 0 || vectorForEnd.y < 0;
            }
        }

        return reverseLayout
                ?
                (forwardDirection ?
                        centerPosition - mSpanCount :
                        centerPosition)
                :
                (forwardDirection ?
                        centerPosition + mSpanCount :
                        centerPosition);
    }

    @NonNull
    private OrientationHelper getVerticalHelper(@NonNull RecyclerView.LayoutManager layoutManager) {
        if (mVerticalHelper == null) {
            mVerticalHelper = OrientationHelper.createVerticalHelper(layoutManager);
        }
        return mVerticalHelper;
    }

    /*
     * distance to start.
     * */
    private int distanceToStart(View targetView,
                                OrientationHelper helper) {

        return helper.getDecoratedStart(targetView) - helper.getStartAfterPadding();
    }

    /*
     * distance to center.
     * */
    private int distanceToCenter(@NonNull RecyclerView.LayoutManager layoutManager,
                                 @NonNull View targetView,
                                 OrientationHelper helper) {
        final int childCenter = helper.getDecoratedStart(targetView)
                + (helper.getDecoratedMeasurement(targetView) / 2);
        final int containerCenter;
        if (layoutManager.getClipToPadding()) {
            containerCenter = helper.getStartAfterPadding() + helper.getTotalSpace() / 2;
        } else {
            containerCenter = helper.getEnd() / 2;
        }
        return childCenter - containerCenter;
    }

    @NonNull
    private OrientationHelper getHorizontalHelper(
            @NonNull RecyclerView.LayoutManager layoutManager) {
        if (mHorizontalHelper == null) {
            mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager);
        }
        return mHorizontalHelper;
    }

}