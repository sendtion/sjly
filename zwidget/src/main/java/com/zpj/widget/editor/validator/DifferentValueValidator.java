package com.zpj.widget.editor.validator;

import android.text.TextUtils;
import android.widget.EditText;

/**
 * A simple validator that validates the field only if the value is the same as another one.
 *
 * @author Andrea Baccega <me@andreabaccega.com>
 */
public class DifferentValueValidator extends Validator {
    private final EditText otherEditText;

    public DifferentValueValidator(EditText otherEditText, String errorMessage) {
        super(errorMessage);
        this.otherEditText = otherEditText;
    }

    @Override
    public boolean isValid(EditText editText) {
        return !TextUtils.equals(editText.getText(), otherEditText.getText());
    }
}