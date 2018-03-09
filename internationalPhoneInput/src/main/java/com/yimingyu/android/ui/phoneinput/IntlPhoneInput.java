package com.yimingyu.android.ui.phoneinput;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Locale;

public class IntlPhoneInput extends RelativeLayout implements View.OnClickListener{
    private final String DEFAULT_COUNTRY = Locale.getDefault().getCountry();
    // UI Views
    private EditText mPhoneEdit;

    private PhoneNumberWatcher mPhoneNumberWatcher;

    //Util
    private PhoneNumberUtil mPhoneUtil = PhoneNumberUtil.getInstance();

    // Fields
    private Country mSelectedCountry;
    private CountriesFetcher.CountryList mCountries;
    private IntlPhoneInputListener mIntlPhoneInputListener;


    public static final String ACTION_SEND_RESULT = IntlPhoneInput.class.getName()+ ".action.SendResult";
    public static final String EXTRA_COUNTRY = IntlPhoneInput.class.getName() + ".extra.Country";
    private Context mContext;

    private TextView mCountryCode;
    private TextView mCountryName;
    private EditText mPhoneNumber;
    private ImageView countryIcon;

    /**
     * Constructor
     *
     * @param context Context
     */
    public IntlPhoneInput(Context context) {
        this(context,null);
    }

    /**
     * Constructor
     *
     * @param context Context
     * @param attrs   AttributeSet
     */
    public IntlPhoneInput(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public IntlPhoneInput(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        inflate(getContext(), R.layout.intl_phone_input, this);

        mCountries = CountriesFetcher.getCountries(getContext());
        /**
         * Phone text field
         */
        mPhoneEdit = (EditText) findViewById(R.id.intl_phone_edit__phone);
//        PhoneInputView view = (PhoneInputView) findViewById(R.id.phone_input);
//        view.setCountrySelectorType(PhoneInputView.SELECTOR_TYPE_ACTIVITY);
        mContext = context;
        mCountryName = (TextView) findViewById(R.id.country_name);
        mCountryCode = (TextView) findViewById(R.id.country_code);
        mPhoneNumber = (EditText) findViewById(R.id.intl_phone_edit__phone);
        countryIcon=(ImageView)findViewById(R.id.country_icon);

        findViewById(R.id.rl_country).setOnClickListener(this);
        setDefault();
        updateRegion();
    }

    /**
     * Receive country select result
     */
    private BroadcastReceiver mResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mSelectedCountry = intent.getParcelableExtra(EXTRA_COUNTRY);
            updateRegion();
        }
    };
    private int getFlagResource(Country country) {
        return getContext().getResources().getIdentifier("country_" + country.getIso().toLowerCase(), "drawable", getContext().getPackageName());
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter filter = new IntentFilter(ACTION_SEND_RESULT);
        mContext.registerReceiver(mResultReceiver, filter);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mContext.unregisterReceiver(mResultReceiver);
    }

    /**
     * Set default value
     * Will try to retrieve phone number from device
     */
    public void setDefault() {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
            String iso = telephonyManager.getNetworkCountryIso();
            Log.e("IntlPhoneInput","通过TelephonyManager获取的iso是："+iso);
            setEmptyDefault(iso);
            try{
                String phone = telephonyManager.getLine1Number();
                if (phone != null && !phone.isEmpty()) {
                    this.setNumber(phone);
                }
            }catch (SecurityException e) {
                Log.e("IntlPhoneInput","没有读取到手机号，不做任何处理");
            }
        } catch (SecurityException e) {
            Log.e("IntlPhoneInput","没有读取通讯录权限，默认iso为Locale设置的语言");
            setEmptyDefault();
        }
    }

    /**
     * Set default value with
     *
     * @param iso ISO2 of country
     */
    public void setEmptyDefault(String iso) {
        if (iso == null || iso.isEmpty()) {
            iso = DEFAULT_COUNTRY;
        }
        int defaultIdx = mCountries.indexOfIso(iso);
        mSelectedCountry = mCountries.get(defaultIdx);
    }

    /**
     * Alias for setting empty string of default settings from the device (using locale)
     */
    private void setEmptyDefault() {
        setEmptyDefault(null);
    }

    private void updateRegion(){
        countryIcon.setImageResource(getFlagResource(mSelectedCountry));
        mCountryName.setText(mSelectedCountry.getName());
        mCountryCode.setText("+"+mSelectedCountry.getDialCode());

        if(mPhoneNumberWatcher!=null) mPhoneEdit.removeTextChangedListener(mPhoneNumberWatcher);
        mPhoneNumberWatcher = new PhoneNumberWatcher(mSelectedCountry.getIso());
        mPhoneEdit.addTextChangedListener(mPhoneNumberWatcher);
        mPhoneEdit.setText(mPhoneEdit.getText());
    }


    /**
     * Set hint number for country
     */
    private void setHint() {
        if (mPhoneEdit != null && mSelectedCountry != null && mSelectedCountry.getIso() != null) {
            Phonenumber.PhoneNumber phoneNumber = mPhoneUtil.getExampleNumberForType(mSelectedCountry.getIso(), PhoneNumberUtil.PhoneNumberType.MOBILE);
            if (phoneNumber != null) {
                mPhoneEdit.setHint(mPhoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL));
            }
        }
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId==R.id.rl_country||viewId == R.id.country_icon || viewId== R.id.country_name || viewId == R.id.country_other) {
            Intent intent = new Intent(mContext, CountrySelectorActivity.class);
            mContext.startActivity(intent);
        }
    }

    /**
     * Phone number watcher
     */
    private class PhoneNumberWatcher extends PhoneNumberFormattingTextWatcher {
        private boolean lastValidity;

        @SuppressWarnings("unused")
        public PhoneNumberWatcher() {
            super();
        }

        //TODO solve it! support for android kitkat
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public PhoneNumberWatcher(String countryCode) {
            super(countryCode);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            super.onTextChanged(s, start, before, count);
            try {
                String iso = null;
                if (mSelectedCountry != null) {
                    iso = mSelectedCountry.getIso();
                }
                Phonenumber.PhoneNumber phoneNumber = mPhoneUtil.parse(s.toString(), iso);
                iso = mPhoneUtil.getRegionCodeForNumber(phoneNumber);
                if (iso != null) {
                    int countryIdx = mCountries.indexOfIso(iso);
                }
            } catch (NumberParseException ignored) {
            }

            if (mIntlPhoneInputListener != null) {
                boolean validity = isValid();
                if (validity != lastValidity) {
                    mIntlPhoneInputListener.onValidityChange(validity);
                }
                lastValidity = validity;
            }
        }
    }

    /**
     * Set Number
     *
     * @param number E.164 format or national format(for selected country)
     */
    public void setNumber(String number) {
        try {
            String iso = null;
            if (mSelectedCountry != null) {
                iso = mSelectedCountry.getIso();
            }
            Phonenumber.PhoneNumber phoneNumber = mPhoneUtil.parse(number, iso);

            int countryIdx = mCountries.indexOfIso(mPhoneUtil.getRegionCodeForNumber(phoneNumber));
            String format=mPhoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
            mPhoneEdit.setText(format);
        } catch (NumberParseException ignored) {
        }
    }

    /**
     * Get number
     *
     * @return Phone number in E.164 format | null on error
     */
    @SuppressWarnings("unused")
    public String getNumber() {
        Phonenumber.PhoneNumber phoneNumber = getPhoneNumber();
        if (phoneNumber == null) {
            return null;
        }

        return mPhoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
    }

    public String getText() {
        return getNumber();
    }


    public String getUstr(){
        return mCountryCode.getText().toString()+"-"+mPhoneNumber.getText().toString().replace(" ","");
    }


    /**
     * Get PhoneNumber object
     *
     * @return PhonenUmber | null on error
     */
    @SuppressWarnings("unused")
    public Phonenumber.PhoneNumber getPhoneNumber() {
        //此处如果dialCode有4位数，电话号码就会转换
        try {
            String iso = null;
            if (mSelectedCountry != null) {
                iso = mSelectedCountry.getIso();
            }
            Log.e("IntlPhoneInput","默认国家是"+DEFAULT_COUNTRY+"当前选择国家的iso是"+iso);
            return mPhoneUtil.parse(mPhoneEdit.getText().toString(), iso);
        } catch (NumberParseException ignored) {
            return null;
        }
    }

    /**
     * Get selected country
     *
     * @return Country
     */
    @SuppressWarnings("unused")
    public Country getSelectedCountry() {
        return mSelectedCountry;
    }

    /**
     * Check if number is valid
     *
     * @return boolean
     */
    @SuppressWarnings("unused")
    public boolean isValid() {
        Phonenumber.PhoneNumber phoneNumber = getPhoneNumber();
        return phoneNumber != null && mPhoneUtil.isValidNumber(phoneNumber);
    }

    /**
     * Add validation listener
     *
     * @param listener IntlPhoneInputListener
     */
    public void setOnValidityChange(IntlPhoneInputListener listener) {
        mIntlPhoneInputListener = listener;
    }


    /**
     * Simple validation listener
     */
    public interface IntlPhoneInputListener {
        void onValidityChange(boolean isValid);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mPhoneEdit.setEnabled(enabled);
    }



    /**
     * Hide keyboard from phoneEdit field
     */
    public void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mPhoneEdit.getWindowToken(), 0);
    }

    /**
     * Set keyboard onValidityChange listener to detect when the user click "DONE" on his keyboard
     *
     * @param listener IntlPhoneInputListener
     */
    public void setOnKeyboardDone(final IntlPhoneInputListener listener) {
        mPhoneEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    listener.onValidityChange(isValid());
                }
                return false;
            }
        });
    }
}
