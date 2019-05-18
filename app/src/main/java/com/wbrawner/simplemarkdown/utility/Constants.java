package com.wbrawner.simplemarkdown.utility;

@SuppressWarnings("WeakerAccess")
public class Constants {

    // Request codes
    public static final int REQUEST_OPEN_FILE = 1;
    public static final int REQUEST_SAVE_FILE = 2;
    public static final int REQUEST_DARK_MODE = 3;

    // Extras
    public static final String EXTRA_FILE = "EXTRA_FILE";
    public static final String EXTRA_FILE_PATH = "EXTRA_FILE_PATH";
    public static final String EXTRA_REQUEST_CODE = "EXTRA_REQUEST_CODE";
    public static final String EXTRA_EXPLORER = "EXTRA_EXPLORER";

    // Settings keys
    public static final String KEY_AUTOSAVE = "autosave";
    public static final String KEY_DOCS_PATH = "defaultRootDir";

    // Settings values
    public static final String VALUE_EDIT_VIEW = "0";
    public static final String VALUE_FILE_VIEW = "1";

}
