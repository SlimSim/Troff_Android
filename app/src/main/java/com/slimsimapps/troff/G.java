package com.slimsimapps.troff;

import android.content.Context;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

/**
 * Created on 2016-12-25, by Slim Sim Apps.
 */
class G {

    private Context context;

    G(Context context) {
        this.context = context;
    }

    @SuppressWarnings("unused")
    private static final String TAG = "G";


    final void showKeyboard() {
        InputMethodManager imm = (InputMethodManager)
                context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null){
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    final void hideKeyboard(Window window) {
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
    }
}
