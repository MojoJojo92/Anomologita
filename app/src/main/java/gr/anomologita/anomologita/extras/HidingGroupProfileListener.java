package gr.anomologita.anomologita.extras;

import android.support.v7.widget.RecyclerView;

public abstract class HidingGroupProfileListener extends RecyclerView.OnScrollListener {

    private static final float HIDE_THRESHOLD = 10;
    private static final float SHOW_THRESHOLD = 30;

    public static int mGroupProfileOffset = 0;
    private boolean mControlsVisible = true;
    private final int mGroupProfileHeight;
    private int mTotalScrolledDistance;

    public HidingGroupProfileListener(int height) {
        mGroupProfileHeight = height;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);

        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            if(mTotalScrolledDistance < mGroupProfileHeight)
                setVisible();
            if (mControlsVisible) {
                if (mGroupProfileOffset > HIDE_THRESHOLD) {
                    setInvisible();
                } else {
                    setVisible();
                }
            } else {
                if ((mGroupProfileHeight - mGroupProfileOffset) > SHOW_THRESHOLD) {
                    setVisible();
                } else {
                    setInvisible();
                }
            }
        }
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        clipGroupProfileOffset();
        onMoved(mGroupProfileOffset);

        if(mTotalScrolledDistance > mGroupProfileHeight){
            mGroupProfileOffset = mGroupProfileHeight;
        }else if(mTotalScrolledDistance <= mGroupProfileHeight){
            mGroupProfileOffset = mTotalScrolledDistance;
        }else {
            mGroupProfileOffset = 0;
        }
        if(mTotalScrolledDistance <= mGroupProfileHeight){
            if ((mGroupProfileOffset < mGroupProfileHeight && dy > 0) || (mGroupProfileOffset > 0 && dy < 0)) {
                mGroupProfileOffset += dy;
            }
        }
        mTotalScrolledDistance += dy;

    }

    private void clipGroupProfileOffset() {
        if (mGroupProfileOffset > mGroupProfileHeight) {
            mGroupProfileOffset = mGroupProfileHeight;
        } else if (mGroupProfileOffset < 0) {
            mGroupProfileOffset = 0;
        }
    }

    private void setVisible() {
        if (mGroupProfileOffset > 0) {
            onShow();
            mGroupProfileOffset = 0;
        }
        mControlsVisible = true;
    }

    private void setInvisible() {
        if (mGroupProfileOffset < mGroupProfileHeight) {
            onHide();
            mGroupProfileOffset = mGroupProfileHeight;
        }
        mControlsVisible = false;
    }

    public abstract void onMoved(int distance);

    public abstract void onShow();

    public abstract void onHide();
}