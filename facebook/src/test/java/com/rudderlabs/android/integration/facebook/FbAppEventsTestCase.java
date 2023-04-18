package com.rudderlabs.android.integration.facebook;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockStatic;

import android.app.Application;

import com.facebook.appevents.AppEventsLogger;
import com.rudderstack.android.sdk.core.RudderMessage;
import com.rudderstack.android.testio.Operation;
import com.rudderstack.android.testio.TestMyIO;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.Nullable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.InputStream;
import java.util.Map;
@RunWith(PowerMockRunner.class)
@PrepareForTest({FbAppEventsTestCase.class, FacebookIntegrationFactory.class, AppEventsLogger.class })
public class FbAppEventsTestCase {
    private final Map<String, String> TEST_JSON_PATH_MAP =
    Map.of(
            "identify_input_1.json", "identify_output_1.json"
    );
    private final TestMyIO testMyIO = new TestMyIO(getClass().getClassLoader());
    private FacebookIntegrationFactory facebookIntegrationFactory;
    Application application;
    private AutoCloseable closeable;
    AppEventsLogger appEventsLogger;

//    MockedStatic<AppEventsLogger> mockedAppEventsLogger;

    @Captor
    ArgumentCaptor<String> eventNameCaptor;
    @Captor
    ArgumentCaptor<String> userIdCaptor;
    @Before
    public void setup() throws Exception {
        closeable = MockitoAnnotations.openMocks(this);
        application = Mockito.mock(Application.class);
        PowerMockito.spy(AppEventsLogger.class);

        facebookIntegrationFactory = new FacebookIntegrationFactory();
        appEventsLogger = Mockito.mock(AppEventsLogger.class);
        facebookIntegrationFactory.setup(appEventsLogger);

//        mockedAppEventsLogger = mockStatic(AppEventsLogger.class);
    }
    @After
    public void release() throws Exception {
        closeable.close();
    }

    @Test
    public void assertJsonsAreReadCorrectly(){
        String firstInput = TEST_JSON_PATH_MAP.keySet().toArray(new String[TEST_JSON_PATH_MAP.size()])[0];
        MatcherAssert.assertThat(firstInput, notNullValue());
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(firstInput);
        MatcherAssert.assertThat(inputStream, notNullValue());
    }

    @Test
    public void testEvents() throws Exception {
        for (Map.Entry<String, String> inputJsonPathToOutputJsonPath: TEST_JSON_PATH_MAP.entrySet()) {
            ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> firstNameCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> lastNameCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> phoneCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> dateOfBirthCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> genderCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> cityCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> stateCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> zipCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> countryCaptor = ArgumentCaptor.forClass(String.class);



            PowerMockito.doNothing().when(AppEventsLogger.class, "setUserID",
                    userIdCaptor.capture())/*.thenAnswer((Answer<Void>) invocation -> Void.TYPE.newInstance())*/;
            PowerMockito.doNothing().when(AppEventsLogger.class, "setUserData",
                    emailCaptor.capture(),
                    firstNameCaptor.capture(),
                    lastNameCaptor.capture(),
                    phoneCaptor.capture(),
                    dateOfBirthCaptor.capture(),
                    genderCaptor.capture(),
                    cityCaptor.capture(),
                    stateCaptor.capture(),
                    zipCaptor.capture(),
                    countryCaptor.capture()
            )/*.thenAnswer((Answer<Void>) invocation -> Void.TYPE.newInstance())*/;
            testMyIO.test(inputJsonPathToOutputJsonPath.getKey(),
                    inputJsonPathToOutputJsonPath.getValue(), RudderMessage.class,
                    TestOutput.class, input -> {
                        facebookIntegrationFactory.dump(input);
                        return new TestOutput(
                                userIdCaptor.getValue(),
                                new TestOutput.Traits(
                                        emailCaptor.getValue(),
                                        firstNameCaptor.getValue(),
                                        lastNameCaptor.getValue(),
                                        phoneCaptor.getValue(),
                                        dateOfBirthCaptor.getValue(),
                                        genderCaptor.getValue(),
                                        cityCaptor.getValue(),
                                        countryCaptor.getValue(),
                                        zipCaptor.getValue(),
                                        stateCaptor.getValue()
                                )
                        );
                    });
        }
    }
}
