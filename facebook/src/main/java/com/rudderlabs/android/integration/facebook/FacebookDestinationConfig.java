package com.rudderlabs.android.integration.facebook;

import android.text.TextUtils;

public class FacebookDestinationConfig {
    String appID;
    Boolean limitedDataUse;
    Integer dpoCountry;
    Integer dpoState;

    public FacebookDestinationConfig(String appID, Boolean limitedDataUse, String dpoCountry, String dpoState) {
        this.appID = appID;
        this.limitedDataUse = limitedDataUse != null ? limitedDataUse : false;
        this.dpoCountry = validateCountry(dpoCountry);
        this.dpoState = validateState(dpoState);
    }

    Integer validateCountry(String dpoCountry) {
        if (TextUtils.isEmpty(dpoCountry))
            return 0;
        Integer country = Integer.parseInt(dpoCountry);
        if (country != 0 && country != 1) {
            return 0;
        }
        return country;
    }

    Integer validateState(String dpoState) {
        if(TextUtils.isEmpty(dpoState))
            return 0;
        Integer state = Integer.parseInt(dpoState);
        if (state != 0 && state != 1000) {
            return 0;
        }
        return state;
    }

}
