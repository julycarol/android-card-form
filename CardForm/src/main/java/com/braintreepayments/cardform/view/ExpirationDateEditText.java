package com.braintreepayments.cardform.view;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;

import com.braintreepayments.cardform.R;
import com.braintreepayments.cardform.utils.DateValidator;

/**
 * An {@link android.widget.EditText} for entering dates, used for card expiration dates.
 * Will automatically format input as it is entered.
 */
public class ExpirationDateEditText extends ErrorEditText implements TextWatcher, View.OnClickListener {

    private static final int MAX_NUM_CHARS = 4;

    private boolean mChangeWasAddition;
    private OnClickListener mClickListener;

    public ExpirationDateEditText(Context context) {
        super(context);
        init();
    }

    public ExpirationDateEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ExpirationDateEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setInputType(InputType.TYPE_CLASS_NUMBER);
        setInputFilters();
        addTextChangedListener(this);
        super.setOnClickListener(this);
    }

    private void setInputFilters() {
        LengthFilter lengthFilter = new LengthFilter(MAX_NUM_CHARS);
        DigitsOnlyFilter digitsOnlyFilter = DigitsOnlyFilter.newInstance(MAX_NUM_CHARS);
        InputFilter[] filters = { lengthFilter, digitsOnlyFilter};
        setFilters(filters);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        mClickListener = l;
    }

    @Override
    public void onClick(View v) {
        if (mClickListener != null) {
            mClickListener.onClick(v);
        }
    }

    /**
     * @return the 2-digit month, formatted with a leading zero if necessary. If no month has been
     * specified, an empty string is returned.
     */
    public String getMonth() {
        String string = getString();
        if (string.length() < 2) {
            return "";
        }
        return getString().substring(0,2);
    }

    /**
     * @return the 2- or 4-digit year depending on user input.
     * If no year has been specified, an empty string is returned.
     */
    public String getYear() {
        String string = getString();
        if (string.length() == 4 || string.length() == 6) {
            return getString().substring(2);
        }
        return "";
    }

    /**
     * @return whether or not the input is a valid card expiration date.
     */
    @Override
    public boolean isValid() {
        return isOptional() || DateValidator.isValid(getMonth(), getYear());
    }

    @Override
    public String getErrorMessage() {
        if (TextUtils.isEmpty(getText())) {
            return getContext().getString(R.string.bt_expiration_required);
        } else {
            return getContext().getString(R.string.bt_expiration_invalid);
        }
    }

    @Override
    public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        mChangeWasAddition = lengthAfter > lengthBefore;
    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (mChangeWasAddition) {
            if (editable.length() == 1 && Character.getNumericValue(editable.charAt(0)) >= 2) {
                prependLeadingZero(editable);
            }
        }

        Object[] paddingSpans = editable.getSpans(0, editable.length(), SlashSpan.class);
        for (Object span : paddingSpans) {
            editable.removeSpan(span);
        }

        addDateSlash(editable);

        if (((getSelectionStart() == 4 && !editable.toString().endsWith("20")) || getSelectionStart() == 6)
                && isValid()) {
            focusNextView();
        }
    }

    private void prependLeadingZero(Editable editable) {
        char firstChar = editable.charAt(0);
        editable.replace(0, 1, "0").append(firstChar);
    }

    private void addDateSlash(Editable editable) {
        final int index = 2;
        final int length = editable.length();
        if (index <= length) {
            editable.setSpan(new SlashSpan(), index - 1, index,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    /**
     * Convenience method to get the input text as a {@link String}.
     */
    private String getString() {
        Editable editable = getText();
        return editable != null ? editable.toString() : "";
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
}
