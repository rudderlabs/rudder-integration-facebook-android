package com.rudderlabs.android.integration.facebook;

import androidx.annotation.Nullable;

import java.util.Objects;

public class TestOutput {

    public TestOutput(@Nullable String userId, @Nullable Traits traits) {
        this.userId = userId;
        this.traits = traits;
    }

    @Nullable
    String userId;
    @Nullable
    Traits traits;
    static class Traits{
        public Traits(@Nullable String email, @Nullable String firstName, @Nullable String lastName,
                      @Nullable String phone, @Nullable String birthday, @Nullable String gender,
                      @Nullable String city, @Nullable String country, @Nullable String postalCode,
                      @Nullable String state) {
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.phone = phone;
            this.birthday = birthday;
            this.gender = gender;
            this.city = city;
            this.country = country;
            this.postalCode = postalCode;
            this.state = state;
        }

        @Nullable
        String email;
        @Nullable
        String firstName;
        @Nullable
        String lastName;
        @Nullable
        String phone;
        @Nullable
        String birthday;
        @Nullable
        String gender;
        @Nullable
        String city;
        @Nullable
        String country;
        @Nullable
        String postalCode;
        @Nullable
        String state;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Traits)) return false;
            Traits traits = (Traits) o;
            return Objects.equals(email, traits.email) && Objects.equals(firstName, traits.firstName) &&
                    Objects.equals(lastName, traits.lastName) && Objects.equals(phone, traits.phone) &&
                    Objects.equals(birthday, traits.birthday) && Objects.equals(gender, traits.gender) &&
                    Objects.equals(city, traits.city) && Objects.equals(country, traits.country) &&
                    Objects.equals(postalCode, traits.postalCode) && Objects.equals(state, traits.state);
        }

        @Override
        public int hashCode() {
            return Objects.hash(email, firstName, lastName, phone, birthday, gender, city, country, postalCode, state);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestOutput)) return false;
        TestOutput that = (TestOutput) o;
        return Objects.equals(userId, that.userId) && Objects.equals(traits, that.traits);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, traits);
    }
}
