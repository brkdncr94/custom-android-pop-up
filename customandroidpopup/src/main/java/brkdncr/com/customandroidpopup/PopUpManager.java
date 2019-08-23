package brkdncr.com.customandroidpopup;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

public class PopUpManager implements PopupWindow.OnDismissListener {

    public static final int POPUP_LEFT = 1;
    public static final int POPUP_RIGHT = 2;
    public static final int POPUP_UP = 3;
    public static final int POPUP_DOWN = 4;

    // The padding on edges expressed in dp
    private static final float EDGE_PADDING_DP = 10.0f;
    private static final float ARROWPOINT_PADDING_DP = 5.0f;
    private final float scale;
    private final int edgePadding;
    private final int arrowPointPadding;
    private final int arrowHorizontalEdgePadding;

    private PopupWindow myPopUp;
    private Context context;
    private PopUpParams[] popUpParams;
    private int index;
    private LayoutInflater inflater;

    public PopUpManager(Context newContext, PopUpParams[] newParams)
    {
        this.myPopUp = new PopupWindow();
        this.myPopUp.setFocusable(true);
        this.myPopUp.setTouchable(true);
        this.myPopUp.setOutsideTouchable(true);
        this.myPopUp.setOnDismissListener(this);
        this.myPopUp.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        this.myPopUp.setAnimationStyle(android.R.style.Animation_Dialog);

        this.context = newContext;
        this.popUpParams = newParams;
        this.index = 0;

        // Get the screen's density scale
        this.scale = context.getResources().getDisplayMetrics().density;
        // Convert the dps to pixels based on scale
        edgePadding = convertDPtoPX(EDGE_PADDING_DP); // offsets for the popup windows will be adjusted if they will touch the edges
        // Set the values that will be used as margins for the arrow edge and point
        arrowPointPadding = convertDPtoPX(ARROWPOINT_PADDING_DP);
        arrowHorizontalEdgePadding = context.getResources().getDimensionPixelSize(R.dimen.spacing_big); // dimension value converted to number of pixels
        // We set the radius of the rounded corners of the TextView to "R.dimen.spacing_big" in our XML files

        inflater = LayoutInflater.from(context);
        DrawPopUp(popUpParams[index]);
    }

    private void DrawPopUp(PopUpParams newParams) {
        View parent = newParams.getRootView();
        View originView = newParams.getOrigin();
        int direction = newParams.getDirection();
        String popupText = newParams.getText();

        int xPos = 0;
        int yPos = 0;
        int arrowWidth = 0;
        int arrowHeight = 0;
        int textWidth = 0;
        int textHeight = 0;
        int windowXOffset = 0;
        int windowYOffset = 0;
        int arrowXOffset = 0;
        int arrowYOffset = 0;
        int windowXMultiplier = 1;// to control whether the offset value should be negative or positive
        int windowYMultiplier = 1;// to control whether the offset value should be negative or positive

        ConstraintLayout popupView = (ConstraintLayout) inflater.inflate(R.layout.popup_layout, null);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(popupView);
        Rect originRect = locateView(originView);
        Rect parentRect = locateView(parent);

        xPos = originRect.centerX() - parentRect.left;
        yPos = originRect.centerY() - parentRect.top;

        // Set the text for the TextView in the Pop-Up
        TextView textView = popupView.findViewById(R.id.PopUpText);
        textView.setText(popupText);
        textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        textView.measure(0, 0);//must call measure to get width and height
        textWidth = textView.getMeasuredWidth();
        textHeight = textView.getMeasuredHeight();

        ImageView arrowImageView = popupView.findViewById(R.id.ArrowImage);
        Drawable arrowDrawable = null;

        if(direction == POPUP_LEFT || direction == POPUP_RIGHT) {
            windowYMultiplier = -1; // to move the window up, offset needs to be negative
            if(direction == POPUP_LEFT) {
                windowXMultiplier = -1;
                arrowDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.arrow_right, null);
                arrowImageView.setImageDrawable(arrowDrawable);
                arrowWidth = arrowDrawable.getIntrinsicWidth();

                constraintSet.connect(textView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
                constraintSet.connect(textView.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
                constraintSet.connect(textView.getId(), ConstraintSet.RIGHT, arrowImageView.getId(), ConstraintSet.LEFT);
                constraintSet.connect(arrowImageView.getId(), ConstraintSet.LEFT, textView.getId(), ConstraintSet.RIGHT);
                constraintSet.connect(arrowImageView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);

                windowXOffset = arrowWidth + textWidth + arrowPointPadding;

            } else { // direction == POPUP_RIGHT
                arrowDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.arrow_left, null);
                arrowImageView.setImageDrawable(arrowDrawable);
                arrowWidth = arrowDrawable.getIntrinsicWidth();

                constraintSet.connect(arrowImageView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
                constraintSet.connect(arrowImageView.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
                constraintSet.connect(arrowImageView.getId(), ConstraintSet.RIGHT, textView.getId(), ConstraintSet.LEFT);
                constraintSet.connect(textView.getId(), ConstraintSet.LEFT, arrowImageView.getId(), ConstraintSet.RIGHT);
                constraintSet.connect(textView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);

                windowXOffset = arrowWidth + originRect.width() + arrowPointPadding;
            }

            arrowHeight = arrowDrawable.getIntrinsicHeight();
            windowYOffset = textHeight/2 + (originRect.height()/2); // Pop-up will be anchored to bottom-left corner of the anchor view
            // That is why, we need to add the height of the anchor view to make sure pop-up is centered on the Y-axis.

            if((textHeight/2) >= yPos) { // check the space to the top of the screen
                // in this case, reduce the offset to move the pop-up down
                windowYOffset = originRect.top - edgePadding;
            }

            if((textHeight/2) >= (parentRect.bottom - yPos)) { // check the space to the bottom of the screen
                int bottomSpace = (parentRect.bottom - yPos);
                int extraNeeded = (textHeight/2) - bottomSpace;
                // in this case we have to add a right margin to the textView to make sure it's fully in the screen
                windowYOffset = windowYOffset + extraNeeded + edgePadding;
            }
            // IMPORTANT: At this point, it's the programmer's responsibility to make sure the text isn't to large to fit in the screen as a whole

            // windowYOffset: distance from the top of the pop-up window to the bottom of the anchor view
            // originRect.height()/2: distance from the center of the anchor view to its bottom
            arrowYOffset = windowYOffset - (originRect.height()/2) - (arrowHeight/2);
            constraintSet.setMargin(arrowImageView.getId(), ConstraintSet.TOP, arrowYOffset);

        } else if(direction == POPUP_UP || direction == POPUP_DOWN) {
            windowXMultiplier = -1; // to move the window left, offset needs to be negative
            if(direction == POPUP_UP) {
                windowYMultiplier = -1;
                arrowDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.arrow_down, null);
                arrowImageView.setImageDrawable(arrowDrawable);
                arrowHeight = arrowDrawable.getIntrinsicHeight();

                constraintSet.connect(textView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
                constraintSet.connect(textView.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
                constraintSet.connect(textView.getId(), ConstraintSet.BOTTOM, arrowImageView.getId(), ConstraintSet.TOP);
                constraintSet.connect(arrowImageView.getId(), ConstraintSet.TOP, textView.getId(), ConstraintSet.BOTTOM);
                constraintSet.connect(arrowImageView.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);

                windowYOffset = originRect.height() + arrowHeight + textHeight + arrowPointPadding;

            } else { // direction == POPUP_DOWN
                arrowDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.arrow_up, null);
                arrowImageView.setImageDrawable(arrowDrawable);

                constraintSet.connect(arrowImageView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
                constraintSet.connect(arrowImageView.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
                constraintSet.connect(arrowImageView.getId(), ConstraintSet.BOTTOM, textView.getId(), ConstraintSet.TOP);
                constraintSet.connect(textView.getId(), ConstraintSet.TOP, arrowImageView.getId(), ConstraintSet.BOTTOM);
                constraintSet.connect(textView.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
                windowYOffset = arrowPointPadding;
            }

            arrowWidth = arrowDrawable.getIntrinsicWidth();
            windowXOffset = textWidth/2 - (originRect.width()/2); // Pop-up will be anchored to bottom-left corner of the anchor view
            // That is why, we need to subtract the width of the anchor view to make sure pop-up is centered on the X-axis.

            if( (xPos - (textWidth/2)) < edgePadding ) { // check the space to the left of the screen
                // in this case, we have to add a left padding to the pop-up to make sure it satisfies minimum distance away from the edge
                // we do that by decreasing the offset of the pop-up window
                windowXOffset = originRect.left - parentRect.left - edgePadding;
            }

            if( ((parentRect.right - xPos) - (textWidth/2)) < edgePadding) { // check the space to the right of the screen
                int rightSpace = (parentRect.right - xPos);
                int extraNeeded = (textWidth/2) - rightSpace;
                // in this case, we have to add a left padding to the pop-up to make sure it satisfies minimum distance away from the edge
                // we do that by increasing the offset of the pop-up window
                windowXOffset = windowXOffset + extraNeeded + edgePadding;
            }
            // IMPORTANT: At this point, it's the programmer's responsibility to make sure the text isn't to large to fit in the screen as a whole

            // windowXOffset: Distance to the left side of the anchor view from the start of pop-up view
            // originRect.width()/2: Distance from the left side of the anchor view to it's center
            arrowXOffset = windowXOffset + (originRect.width()/2) - (arrowWidth/2);
            if(arrowXOffset < arrowHorizontalEdgePadding) {
                arrowXOffset = arrowHorizontalEdgePadding;
            }
            constraintSet.setMargin(arrowImageView.getId(), ConstraintSet.LEFT, arrowXOffset);
            constraintSet.setMargin(arrowImageView.getId(), ConstraintSet.RIGHT, arrowHorizontalEdgePadding);
        }
        constraintSet.applyTo(popupView);

        myPopUp.setContentView(popupView);
        myPopUp.setWidth(ConstraintLayout.LayoutParams.WRAP_CONTENT);
        myPopUp.setHeight(ConstraintLayout.LayoutParams.WRAP_CONTENT);
        myPopUp.showAsDropDown(originView, (windowXMultiplier*windowXOffset), (windowYMultiplier*windowYOffset));
    }

    @Nullable
    private static Rect locateView(View v) {
        int[] loc_int = new int[2];
        if (v == null) return null;
        try
        {
            v.getLocationOnScreen(loc_int);
        } catch (NullPointerException npe)
        {
            //Happens when the view doesn't exist on screen anymore.
            return null;
        }
        Rect locationRect = new Rect(loc_int[0], loc_int[1], loc_int[0]+v.getWidth(), loc_int[1]+v.getHeight());
        return locationRect;
    }

    private int convertDPtoPX(Float dpVal) {
        return (int)(dpVal*scale + 0.5f);
    }

    @Override
    public void onDismiss() {
        index = index + 1;
        if(index < popUpParams.length)
        {
            DrawPopUp(popUpParams[index]);
        }
    }
}