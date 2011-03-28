package org.chemodansama.engine;

import android.app.Activity;

public class NpActivityTerminator {
    
    private Activity mActivity = null;

    public NpActivityTerminator(Activity a) {
        super();
        mActivity = a;
    }
    
    public void finish() {
        if (mActivity != null) {
            mActivity.finish();
        }
    }
}
