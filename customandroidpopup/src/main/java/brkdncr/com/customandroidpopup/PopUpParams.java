package brkdncr.com.customandroidpopup;

import android.view.View;

public class PopUpParams {

    private View rootView;
    private View origin;
    private int direction;
    private String text;

    public PopUpParams()
    {}

    public PopUpParams(View newRoot, View newOrigin, int newDirection, String newText)
    {
        rootView = newRoot;
        origin = newOrigin;
        direction = newDirection;
        text = newText;
    }

    public void setRootView(View newRoot) {
        this.rootView = newRoot;
    }

    public void setOrigin(View newOrigin) {
        this.origin = newOrigin;
    }

    public void setDirection(int newDirection) {
        this.direction = newDirection;
    }

    public void setText(String newText){
        this.text = newText;
    }

    public View getRootView() {
        return rootView;
    }

    public View getOrigin(){
        return origin;
    }

    public int getDirection() {
        return direction;
    }

    public String getText() {
        return text;
    }
}
