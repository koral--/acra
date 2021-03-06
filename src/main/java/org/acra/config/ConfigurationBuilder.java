/*
 *  Copyright 2011 Kevin Gaudin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.acra.config;

import android.app.Application;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.builder.NoOpReportPrimer;
import org.acra.builder.ReportPrimer;
import org.acra.dialog.BaseCrashReportDialog;
import org.acra.dialog.CrashReportDialog;
import org.acra.security.KeyStoreFactory;
import org.acra.sender.DefaultReportSenderFactory;
import org.acra.sender.HttpSender;
import org.acra.sender.HttpSender.Method;
import org.acra.sender.HttpSender.Type;
import org.acra.sender.ReportSenderFactory;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.acra.ACRA.LOG_TAG;
import static org.acra.ACRAConstants.*;

/**
 * Builder responsible for programmatic construction of an {@link ACRAConfiguration}.
 *
 * {@link ACRAConfiguration} should be considered immutable and in the future will be.
 *
 * @since 4.8.1
 */
@SuppressWarnings("unused")
public final class ConfigurationBuilder {

    @Nullable
    final Class<? extends Annotation> annotationType;

    private String[] additionalDropBoxTags;
    private String[] additionalSharedPreferences;
    private Integer connectionTimeout;
    private ReportField[] customReportContent;
    private final Map<ReportField, Boolean> reportContentChanges = new HashMap<ReportField, Boolean>();
    private Boolean deleteUnapprovedReportsOnApplicationStart;
    private Boolean deleteOldUnsentReportsOnApplicationStart;
    private Integer dropboxCollectionMinutes;
    private Boolean forceCloseDialogAfterToast;
    private String formUri;
    private String formUriBasicAuthLogin;
    private String formUriBasicAuthPassword;
    private Boolean includeDropBoxSystemTags;

    private String[] logcatArguments;
    private String mailTo;
    private ReportingInteractionMode reportingInteractionMode;
    private Class<? extends BaseCrashReportDialog> reportDialogClass;
    private Class<? extends ReportPrimer> reportPrimerClass;

    @StringRes private Integer resDialogPositiveButtonText;
    @StringRes private Integer resDialogNegativeButtonText;
    @StringRes private Integer resDialogCommentPrompt;
    @StringRes  private Integer resDialogEmailPrompt;
    @DrawableRes private Integer resDialogIcon;
    @StringRes private Integer resDialogOkToast;
    @StringRes private Integer resDialogText;
    @StringRes private Integer resDialogTitle;
    @DrawableRes private Integer resNotifIcon;
    @StringRes private Integer resNotifText;
    @StringRes private Integer resNotifTickerText;
    @StringRes  private Integer resNotifTitle;
    @StringRes   private Integer resToastText;
    private Integer sharedPreferencesMode;
    private String sharedPreferencesName;
    private Integer socketTimeout;
    private Boolean logcatFilterByPid;
    private Boolean sendReportsInDevMode;

    private String[] excludeMatchingSharedPreferencesKeys;
    private String[] excludeMatchingSettingsKeys;
    private Class buildConfigClass;
    private String applicationLogFile;
    private Integer applicationLogFileLines;

    private Method httpMethod;
    private Type reportType;
    private final Map<String, String> httpHeaders = new HashMap<String, String>();
    private KeyStoreFactory keyStoreFactory;
    private Class<? extends ReportSenderFactory>[] reportSenderFactoryClasses;

    /**
     * Constructs a ConfigurationBuilder that is prepopulated with any
     * '@ReportCrashes' annotation declared on the Application class.
     *
     * @param app Current Application, from which any annotated config will be gleaned.
     */
    public ConfigurationBuilder(@NonNull Application app) {

        // Populate with annotated config
        final ReportsCrashes annotationConfig = app.getClass().getAnnotation(ReportsCrashes.class);

        if (annotationConfig != null) {
            annotationType = annotationConfig.annotationType();

            additionalDropBoxTags = annotationConfig.additionalDropBoxTags();
            additionalSharedPreferences = annotationConfig.additionalSharedPreferences();
            connectionTimeout = annotationConfig.connectionTimeout();
            customReportContent = annotationConfig.customReportContent();
            deleteUnapprovedReportsOnApplicationStart = annotationConfig.deleteUnapprovedReportsOnApplicationStart();
            deleteOldUnsentReportsOnApplicationStart = annotationConfig.deleteOldUnsentReportsOnApplicationStart();
            dropboxCollectionMinutes = annotationConfig.dropboxCollectionMinutes();
            forceCloseDialogAfterToast = annotationConfig.forceCloseDialogAfterToast();
            formUri = annotationConfig.formUri();
            formUriBasicAuthLogin = annotationConfig.formUriBasicAuthLogin();
            formUriBasicAuthPassword = annotationConfig.formUriBasicAuthPassword();
            includeDropBoxSystemTags = annotationConfig.includeDropBoxSystemTags();
            logcatArguments = annotationConfig.logcatArguments();
            mailTo = annotationConfig.mailTo();
            reportingInteractionMode = annotationConfig.mode();
            resDialogIcon = annotationConfig.resDialogIcon();
            resDialogPositiveButtonText = annotationConfig.resDialogPositiveButtonText();
            resDialogNegativeButtonText = annotationConfig.resDialogNegativeButtonText();
            resDialogCommentPrompt = annotationConfig.resDialogCommentPrompt();
            resDialogEmailPrompt = annotationConfig.resDialogEmailPrompt();
            resDialogOkToast = annotationConfig.resDialogOkToast();
            resDialogText = annotationConfig.resDialogText();
            resDialogTitle = annotationConfig.resDialogTitle();
            resNotifIcon = annotationConfig.resNotifIcon();
            resNotifText = annotationConfig.resNotifText();
            resNotifTickerText = annotationConfig.resNotifTickerText();
            resNotifTitle = annotationConfig.resNotifTitle();
            resToastText = annotationConfig.resToastText();
            sharedPreferencesMode = annotationConfig.sharedPreferencesMode();
            sharedPreferencesName = annotationConfig.sharedPreferencesName();
            socketTimeout = annotationConfig.socketTimeout();
            logcatFilterByPid = annotationConfig.logcatFilterByPid();
            sendReportsInDevMode = annotationConfig.sendReportsInDevMode();
            excludeMatchingSharedPreferencesKeys = annotationConfig.excludeMatchingSharedPreferencesKeys();
            excludeMatchingSettingsKeys = annotationConfig.excludeMatchingSettingsKeys();
            buildConfigClass = annotationConfig.buildConfigClass();
            applicationLogFile = annotationConfig.applicationLogFile();
            applicationLogFileLines = annotationConfig.applicationLogFileLines();
            reportDialogClass = annotationConfig.reportDialogClass();
            reportPrimerClass = annotationConfig.reportPrimerClass();
            httpMethod = annotationConfig.httpMethod();
            reportType = annotationConfig.reportType();
            reportSenderFactoryClasses = annotationConfig.reportSenderFactoryClasses();
        } else {
            annotationType = null;
        }
    }

    /**
     * @return new ACRAConfiguration containing all the properties configured on this builder.
     */
    @NonNull
    public ACRAConfiguration build() {
        return new ACRAConfiguration(this);
    }

    /**
     * Set custom HTTP headers to be sent by the provided {@link HttpSender}.
     * This should be used also by third party senders.
     *
     * @param headers A map associating HTTP header names to their values.
     * @return this instance
     */
    @NonNull
    public ConfigurationBuilder setHttpHeaders(@NonNull Map<String, String> headers) {
        this.httpHeaders.clear();
        this.httpHeaders.putAll(headers);
        return this;
    }

    /**
     * @param additionalDropboxTags the additionalDropboxTags to set
     * @return this instance
     */
    @NonNull
    public ConfigurationBuilder setAdditionalDropboxTags(@NonNull String[] additionalDropboxTags) {
        this.additionalDropBoxTags = additionalDropboxTags;
        return this;
    }

    /**
     * @param additionalSharedPreferences the additionalSharedPreferences to set
     * @return this instance
     */
    @NonNull
    public ConfigurationBuilder setAdditionalSharedPreferences(@NonNull String[] additionalSharedPreferences) {
        this.additionalSharedPreferences = additionalSharedPreferences;
        return this;
    }

    /**
     * @param connectionTimeout the connectionTimeout to set
     * @return this instance
     */
    @NonNull
    public ConfigurationBuilder setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    /**
     * @param customReportContent the customReportContent to set
     * @return this instance
     */
    @NonNull
    public ConfigurationBuilder setCustomReportContent(@NonNull ReportField[] customReportContent) {
        this.customReportContent = customReportContent;
        return this;
    }

    /**
     * Use this if you want to keep the default configuration of reportContent, but set some fields explicitly.
     *
     * @param field  the field to set
     * @param enable if this field should be reported
     * @return this instance
     */
    @NonNull
    public ConfigurationBuilder setReportField(@NonNull ReportField field, boolean enable) {
        this.reportContentChanges.put(field, enable);
        return this;
    }

    /**
     * @param deleteUnapprovedReportsOnApplicationStart the deleteUnapprovedReportsOnApplicationStart to set
     * @return this instance
     */
    @NonNull
    public ConfigurationBuilder setDeleteUnapprovedReportsOnApplicationStart(boolean deleteUnapprovedReportsOnApplicationStart) {
        this.deleteUnapprovedReportsOnApplicationStart = deleteUnapprovedReportsOnApplicationStart;
        return this;
    }

    /**
     * @param deleteOldUnsentReportsOnApplicationStart When to delete old (unsent) reports on startup.
     * @return this instance
     */
    @NonNull
    public ConfigurationBuilder setDeleteOldUnsentReportsOnApplicationStart(boolean deleteOldUnsentReportsOnApplicationStart) {
        this.deleteOldUnsentReportsOnApplicationStart = deleteOldUnsentReportsOnApplicationStart;
        return this;
    }

    /**
     * @param dropboxCollectionMinutes the dropboxCollectionMinutes to set
     * @return this instance
     */
    @NonNull
    public ConfigurationBuilder setDropboxCollectionMinutes(int dropboxCollectionMinutes) {
        this.dropboxCollectionMinutes = dropboxCollectionMinutes;
        return this;
    }

    /**
     * @param forceCloseDialogAfterToast the forceCloseDialogAfterToast to set
     * @return this instance
     */
    @NonNull
    public ConfigurationBuilder setForceCloseDialogAfterToast(boolean forceCloseDialogAfterToast) {
        this.forceCloseDialogAfterToast = forceCloseDialogAfterToast;
        return this;
    }

    /**
     * Modify the formUri of your backend server receiving reports.
     *
     * @param formUri formUri to set.
     * @return this instance
     */
    @NonNull
    public ConfigurationBuilder setFormUri(@Nullable String formUri) {
        this.formUri = formUri;
        return this;
    }

    /**
     * @param formUriBasicAuthLogin the formUriBasicAuthLogin to set
     * @return this instance
     */
    @NonNull
    public ConfigurationBuilder setFormUriBasicAuthLogin(@Nullable String formUriBasicAuthLogin) {
        this.formUriBasicAuthLogin = formUriBasicAuthLogin;
        return this;
    }

    /**
     * @param formUriBasicAuthPassword the formUriBasicAuthPassword to set
     * @return this instance
     */
    @NonNull
    public ConfigurationBuilder setFormUriBasicAuthPassword(@Nullable String formUriBasicAuthPassword) {
        this.formUriBasicAuthPassword = formUriBasicAuthPassword;
        return this;
    }

    /**
     * @param includeDropboxSystemTags the includeDropboxSystemTags to set
     * @return this instance
     */
    @NonNull
    public ConfigurationBuilder setIncludeDropboxSystemTags(boolean includeDropboxSystemTags) {
        this.includeDropBoxSystemTags = includeDropboxSystemTags;
        return this;
    }

    /**
     * @param logcatArguments the logcatArguments to set
     * @return this instance
     */
    @NonNull
    public ConfigurationBuilder setLogcatArguments(@NonNull String[] logcatArguments) {
        this.logcatArguments = logcatArguments;
        return this;
    }

    /**
     * Modify the mailTo of the mail account receiving reports.
     *
     * @param mailTo mailTo to set.
     * @return this instance
     */
    @NonNull
    public ConfigurationBuilder setMailTo(@Nullable String mailTo) {
        this.mailTo = mailTo;
        return this;
    }

    /**
     * Change the current {@link ReportingInteractionMode}.
     *
     * @param mode ReportingInteractionMode to set.
     * @return this instance
     * @throws ACRAConfigurationException if a configuration item is missing for this reportingInteractionMode.
     * @deprecated since 4.8.2 use {@link #setReportingInteractionMode(ReportingInteractionMode)} instead.
     */
    @NonNull
    public ConfigurationBuilder setMode(@NonNull ReportingInteractionMode mode) throws ACRAConfigurationException {
        this.reportingInteractionMode = mode;
        return this;
    }

    /**
     * Change the current {@link ReportingInteractionMode}.
     *
     * @param mode ReportingInteractionMode to set.
     * @return this instance
     * @throws ACRAConfigurationException if a configuration item is missing for this reportingInteractionMode.
     */
    @NonNull
    public ConfigurationBuilder setReportingInteractionMode(@NonNull ReportingInteractionMode mode) throws ACRAConfigurationException {
        this.reportingInteractionMode = mode;
        return this;
    }

    @NonNull
    public ConfigurationBuilder setResDialogPositiveButtonText(@StringRes int resId) {
        resDialogPositiveButtonText = resId;
        return this;
    }

    @NonNull
    public ConfigurationBuilder setResDialogNegativeButtonText(@StringRes int resId) {
        resDialogNegativeButtonText = resId;
        return this;
    }

    @NonNull
    public ConfigurationBuilder setReportDialogClass(@NonNull Class<? extends BaseCrashReportDialog> reportDialogClass) {
        this.reportDialogClass = reportDialogClass;
        return this;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resDialogCommentPrompt()} comes from an Android
     * Library Project.
     *
     * @param resId The resource id, see {@link ReportsCrashes#resDialogCommentPrompt()}
     * @return this instance
     */
    @NonNull
    public ConfigurationBuilder setResDialogCommentPrompt(@StringRes int resId) {
        resDialogCommentPrompt = resId;
        return this;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resDialogEmailPrompt()} comes from an Android Library Project.
     *
     * @param resId The resource id, see
     *              {@link ReportsCrashes#resDialogEmailPrompt()}
     * @return this instance
     */
    @NonNull
    public ConfigurationBuilder setResDialogEmailPrompt(@StringRes int resId) {
        resDialogEmailPrompt = resId;
        return this;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resDialogIcon()} comes from an Android Library
     * Project.
     *
     * @param resId The resource id, see {@link ReportsCrashes#resDialogIcon()}
     * @return this instance
     */
    @NonNull
    public ConfigurationBuilder setResDialogIcon(@DrawableRes int resId) {
        resDialogIcon = resId;
        return this;
    }

    /**
     * Use this method BEFORE if the id you wanted to give to
     * {@link ReportsCrashes#resDialogOkToast()} comes from an Android Library
     * Project.
     *
     * @param resId The resource id, see {@link ReportsCrashes#resDialogOkToast()}
     * @return this instance
     */
    @NonNull
    public ConfigurationBuilder setResDialogOkToast(@StringRes int resId) {
        resDialogOkToast = resId;
        return this;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resDialogText()} comes from an Android Library
     * Project.
     *
     * @param resId The resource id, see {@link ReportsCrashes#resDialogText()}
     * @return this instance
     */
    @NonNull
    public ConfigurationBuilder setResDialogText(@StringRes int resId) {
        resDialogText = resId;
        return this;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resDialogTitle()} comes from an Android Library
     * Project.
     *
     * @param resId The resource id, see {@link ReportsCrashes#resDialogTitle()}
     * @return this instance
     */
    @NonNull
    public ConfigurationBuilder setResDialogTitle(@StringRes int resId) {
        resDialogTitle = resId;
        return this;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resNotifIcon()} comes from an Android Library
     * Project.
     *
     * @param resId The resource id, see {@link ReportsCrashes#resNotifIcon()}
     * @return this instance
     */
    @NonNull
    public ConfigurationBuilder setResNotifIcon(@DrawableRes int resId) {
        resNotifIcon = resId;
        return this;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resNotifText()} comes from an Android Library
     * Project.
     *
     * @param resId The resource id, see {@link ReportsCrashes#resNotifText()}
     * @return this instance
     */
    @NonNull
    public ConfigurationBuilder setResNotifText(@StringRes int resId) {
        resNotifText = resId;
        return this;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resNotifTickerText()} comes from an Android Library
     * Project.
     *
     * @param resId The resource id, see
     *              {@link ReportsCrashes#resNotifTickerText()}
     * @return this instance
     */
    @NonNull
    public ConfigurationBuilder setResNotifTickerText(@StringRes int resId) {
        resNotifTickerText = resId;
        return this;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resNotifTitle()} comes from an Android Library
     * Project.
     *
     * @param resId The resource id, see {@link ReportsCrashes#resNotifTitle()}
     * @return this instance
     */
    @NonNull
    public ConfigurationBuilder setResNotifTitle(@StringRes int resId) {
        resNotifTitle = resId;
        return this;
    }

    /**
     * Use this method if the id you wanted to give to
     * {@link ReportsCrashes#resToastText()} comes from an Android Library
     * Project.
     *
     * @param resId The resource id, see {@link ReportsCrashes#resToastText()}
     * @return this instance
     */
    @NonNull
    public ConfigurationBuilder setResToastText(@StringRes int resId) {
        resToastText = resId;
        return this;
    }

    /**
     * @param sharedPreferenceMode the sharedPreferenceMode to set
     * @return this instance
     */
    @NonNull
    public ConfigurationBuilder setSharedPreferenceMode(int sharedPreferenceMode) {
        this.sharedPreferencesMode = sharedPreferenceMode;
        return this;
    }

    /**
     * @param sharedPreferenceName the sharedPreferenceName to set
     * @return this instance
     */
    @NonNull
    public ConfigurationBuilder setSharedPreferenceName(@NonNull String sharedPreferenceName) {
        this.sharedPreferencesName = sharedPreferenceName;
        return this;
    }

    /**
     * @param socketTimeout the socketTimeout to set
     * @return this instance
     */
    @NonNull
    public ConfigurationBuilder setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
        return this;
    }

    /**
     * @param filterByPid true if you want to collect only logcat lines related to your
     *                    application process.
     * @return this instance
     */
    @NonNull
    public ConfigurationBuilder setLogcatFilterByPid(boolean filterByPid) {
        logcatFilterByPid = filterByPid;
        return this;
    }

    /**
     * @param sendReportsInDevMode false if you want to disable sending reports in development
     *                             mode. Reports will be sent only on signed applications.
     * @return this instance
     */
    @NonNull
    public ConfigurationBuilder setSendReportsInDevMode(boolean sendReportsInDevMode) {
        this.sendReportsInDevMode = sendReportsInDevMode;
        return this;
    }

    /**
     * @deprecated since 4.8.3 no replacement. Now that we are using the SenderService in a separate process it is always safe to send at shutdown.
     */
    @NonNull
    public ConfigurationBuilder setSendReportsAtShutdown(boolean sendReportsAtShutdown) {
        return this;
    }

    /**
     * @param excludeMatchingSharedPreferencesKeys an array of Strings containing regexp defining
     *                                             SharedPreferences keys that should be excluded from the data
     *                                             collection.
     * @return this instance
     */
    @NonNull
    public ConfigurationBuilder setExcludeMatchingSharedPreferencesKeys(@NonNull String[] excludeMatchingSharedPreferencesKeys) {
        this.excludeMatchingSharedPreferencesKeys = excludeMatchingSharedPreferencesKeys;
        return this;
    }

    /**
     * @param excludeMatchingSettingsKeys an array of Strings containing regexp defining
     *                                    Settings.System, Settings.Secure and Settings.Global keys that
     *                                    should be excluded from the data collection.
     * @return this instance
     */
    @NonNull
    public ConfigurationBuilder setExcludeMatchingSettingsKeys(@NonNull String[] excludeMatchingSettingsKeys) {
        this.excludeMatchingSettingsKeys = excludeMatchingSettingsKeys;
        return this;
    }

    @NonNull
    public ConfigurationBuilder setBuildConfigClass(@Nullable Class buildConfigClass) {
        this.buildConfigClass = buildConfigClass;
        return this;
    }

    /**
     * @param applicationLogFile The path and file name of your application log file, to be
     *                           used with {@link ReportField#APPLICATION_LOG}.
     * @return this instance
     */
    @NonNull
    public ConfigurationBuilder setApplicationLogFile(@NonNull String applicationLogFile) {
        this.applicationLogFile = applicationLogFile;
        return this;
    }

    /**
     * @param applicationLogFileLines The number of lines of your application log to be collected,
     *                                to be used with {@link ReportField#APPLICATION_LOG} and
     *                                {@link ReportsCrashes#applicationLogFile()}.
     * @return this instance
     */
    @NonNull
    public ConfigurationBuilder setApplicationLogFileLines(int applicationLogFileLines) {
        this.applicationLogFileLines = applicationLogFileLines;
        return this;
    }

    /**
     * @param httpMethod The method to be used to send data to the server.
     * @return this instance
     */
    @NonNull
    public ConfigurationBuilder setHttpMethod(@NonNull Method httpMethod) {
        this.httpMethod = httpMethod;
        return this;
    }

    /**
     * @param type The type of content encoding to be used to send data to the
     *             server.
     * @return this instance
     */
    @NonNull
    public ConfigurationBuilder setReportType(@NonNull Type type) {
        reportType = type;
        return this;
    }

    /**
     * @param keyStoreFactory Set this to a factory which creates a the keystore that contains the trusted certificates
     */
    @NonNull
    public ConfigurationBuilder setKeyStoreFactory(KeyStoreFactory keyStoreFactory) {
        this.keyStoreFactory = keyStoreFactory;
        return this;
    }

    @NonNull
    public ConfigurationBuilder setReportSenderFactoryClasses(@NonNull Class<? extends ReportSenderFactory>[] reportSenderFactoryClasses) {
        this.reportSenderFactoryClasses = reportSenderFactoryClasses;
        return this;
    }

    @NonNull
    public ConfigurationBuilder setReportPrimerClass(@NonNull Class<? extends ReportPrimer> reportPrimerClass) {
        this.reportPrimerClass = reportPrimerClass;
        return this;
    }


    // Getters - used to provide values and !DEFAULTS! to ACRConfiguration during construction

    @NonNull
    String[] additionalDropBoxTags() {
        if (additionalDropBoxTags != null) {
            return additionalDropBoxTags;
        }
        return new String[0];
    }

    @NonNull
    String[] additionalSharedPreferences() {
        if (additionalSharedPreferences != null) {
            return additionalSharedPreferences;
        }
        return new String[0];
    }

    /**
     * @deprecated since 4.8.1 no replacement.
     */
    @Nullable
    Class<? extends Annotation> annotationType() {
        return annotationType; // Why would this ever be needed?
    }

    int connectionTimeout() {
        if (connectionTimeout != null) {
            return connectionTimeout;
        }
        return DEFAULT_CONNECTION_TIMEOUT;
    }

    @NonNull
    Set<ReportField> reportContent() {
        final Set<ReportField> reportContent = new HashSet<ReportField>();
        if (customReportContent != null) {
            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Using custom Report Fields");
            reportContent.addAll(Arrays.asList(customReportContent));
        } else if (mailTo == null || DEFAULT_STRING_VALUE.equals(mailTo)) {
            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Using default Report Fields");
            reportContent.addAll(Arrays.asList(DEFAULT_REPORT_FIELDS));
        } else {
            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Using default Mail Report Fields");
            reportContent.addAll(Arrays.asList(DEFAULT_MAIL_REPORT_FIELDS));
        }

        // Add or remove any extra fields.
        for (Map.Entry<ReportField, Boolean> entry : reportContentChanges.entrySet()) {
            if (entry.getValue()) {
                reportContent.add(entry.getKey());
            } else {
                reportContent.remove(entry.getKey());
            }
        }
        return reportContent;
    }

    boolean deleteUnapprovedReportsOnApplicationStart() {
        if (deleteUnapprovedReportsOnApplicationStart != null) {
            return deleteUnapprovedReportsOnApplicationStart;
        }
        return DEFAULT_DELETE_UNAPPROVED_REPORTS_ON_APPLICATION_START;
    }

    boolean deleteOldUnsentReportsOnApplicationStart() {
        if (deleteOldUnsentReportsOnApplicationStart != null) {
            return deleteOldUnsentReportsOnApplicationStart;
        }
        return DEFAULT_DELETE_OLD_UNSENT_REPORTS_ON_APPLICATION_START;
    }

    int dropboxCollectionMinutes() {
        if (dropboxCollectionMinutes != null) {
            return dropboxCollectionMinutes;
        }
        return DEFAULT_DROPBOX_COLLECTION_MINUTES;
    }

    boolean forceCloseDialogAfterToast() {
        if (forceCloseDialogAfterToast != null) {
            return forceCloseDialogAfterToast;
        }
        return DEFAULT_FORCE_CLOSE_DIALOG_AFTER_TOAST;
    }

    @NonNull
    String formUri() {
        if (formUri != null) {
            return formUri;
        }
        return DEFAULT_STRING_VALUE;
    }

    @NonNull
    String formUriBasicAuthLogin() {
        if (formUriBasicAuthLogin != null) {
            return formUriBasicAuthLogin;
        }
        return NULL_VALUE;
    }

    @NonNull
    String formUriBasicAuthPassword() {
        if (formUriBasicAuthPassword != null) {
            return formUriBasicAuthPassword;
        }
        return NULL_VALUE;
    }

    boolean includeDropBoxSystemTags() {
        if (includeDropBoxSystemTags != null) {
            return includeDropBoxSystemTags;
        }
        return DEFAULT_INCLUDE_DROPBOX_SYSTEM_TAGS;
    }

    @NonNull
    String[] logcatArguments() {
        if (logcatArguments != null) {
            return logcatArguments;
        }
        return new String[]{"-t", Integer.toString(DEFAULT_LOGCAT_LINES), "-v", "time"};
    }

    @NonNull
    String mailTo() {
        if (mailTo != null) {
            return mailTo;
        }
        return DEFAULT_STRING_VALUE;
    }

    @NonNull
    ReportingInteractionMode reportingInteractionMode() {
        if (reportingInteractionMode != null) {
            return reportingInteractionMode;
        }
        return ReportingInteractionMode.SILENT;
    }

    @StringRes
    public int resDialogPositiveButtonText() {
        if (resDialogPositiveButtonText != null) {
            return resDialogPositiveButtonText;
        }
        return DEFAULT_RES_VALUE;
    }

    @StringRes
    int resDialogNegativeButtonText() {
        if (resDialogNegativeButtonText != null) {
            return resDialogNegativeButtonText;
        }
        return DEFAULT_RES_VALUE;
    }

    @StringRes
    int resDialogCommentPrompt() {
        if (resDialogCommentPrompt != null) {
            return resDialogCommentPrompt;
        }
        return DEFAULT_RES_VALUE;
    }

    @StringRes
    int resDialogEmailPrompt() {
        if (resDialogEmailPrompt != null) {
            return resDialogEmailPrompt;
        }
        return DEFAULT_RES_VALUE;
    }

    @DrawableRes
    int resDialogIcon() {
        if (resDialogIcon != null) {
            return resDialogIcon;
        }
        return DEFAULT_DIALOG_ICON;
    }

    @StringRes
    int resDialogOkToast() {
        if (resDialogOkToast != null) {
            return resDialogOkToast;
        }
        return DEFAULT_RES_VALUE;
    }

    @StringRes
    int resDialogText() {
        if (resDialogText != null) {
            return resDialogText;
        }
        return DEFAULT_RES_VALUE;
    }

    @StringRes
    int resDialogTitle() {
        if (resDialogTitle != null) {
            return resDialogTitle;
        }
        return DEFAULT_RES_VALUE;
    }

    @DrawableRes
    int resNotifIcon() {
        if (resNotifIcon != null) {
            return resNotifIcon;
        }
        return DEFAULT_NOTIFICATION_ICON;
    }

    @StringRes
    int resNotifText() {
        if (resNotifText != null) {
            return resNotifText;
        }
        return DEFAULT_RES_VALUE;
    }

    @StringRes
    int resNotifTickerText() {
        if (resNotifTickerText != null) {
            return resNotifTickerText;
        }
        return DEFAULT_RES_VALUE;
    }

    @StringRes
    int resNotifTitle() {
        if (resNotifTitle != null) {
            return resNotifTitle;
        }
        return DEFAULT_RES_VALUE;
    }

    @StringRes
    int resToastText() {
        if (resToastText != null) {
            return resToastText;
        }
        return DEFAULT_RES_VALUE;
    }

    int sharedPreferencesMode() {
        if (sharedPreferencesMode != null) {
            return sharedPreferencesMode;
        }
        return DEFAULT_SHARED_PREFERENCES_MODE;
    }

    @NonNull
    String sharedPreferencesName() {
        if (sharedPreferencesName != null) {
            return sharedPreferencesName;
        }

        return DEFAULT_STRING_VALUE;
    }

    int socketTimeout() {
        if (socketTimeout != null) {
            return socketTimeout;
        }
        return DEFAULT_SOCKET_TIMEOUT;
    }

    boolean logcatFilterByPid() {
        if (logcatFilterByPid != null) {
            return logcatFilterByPid;
        }
        return DEFAULT_LOGCAT_FILTER_BY_PID;
    }

    boolean sendReportsInDevMode() {
        if (sendReportsInDevMode != null) {
            return sendReportsInDevMode;
        }
        return DEFAULT_SEND_REPORTS_IN_DEV_MODE;
    }

    @NonNull
    String[] excludeMatchingSharedPreferencesKeys() {
        if (excludeMatchingSharedPreferencesKeys != null) {
            return excludeMatchingSharedPreferencesKeys;
        }
        return new String[0];
    }

    @NonNull
    String[] excludeMatchingSettingsKeys() {
        if (excludeMatchingSettingsKeys != null) {
            return excludeMatchingSettingsKeys;
        }
        return new String[0];
    }

    /**
     * Will return null if no value has been configured.
     * It is up to clients to construct the recommended default value oof context.getClass().getPackage().getName() + BuildConfig.class
     */
    @Nullable
    Class buildConfigClass() {
        return buildConfigClass;
    }

    @NonNull
    String applicationLogFile() {
        if (applicationLogFile != null) {
            return applicationLogFile;
        }
        return DEFAULT_APPLICATION_LOGFILE;
    }

    int applicationLogFileLines() {
        if (applicationLogFileLines != null) {
            return applicationLogFileLines;
        }
        return DEFAULT_APPLICATION_LOGFILE_LINES;
    }

    @NonNull
    Class<? extends BaseCrashReportDialog> reportDialogClass() {
        if (reportDialogClass != null) {
            return reportDialogClass;
        }
        return CrashReportDialog.class;
    }

    @NonNull
    Class<? extends ReportPrimer> reportPrimerClass() {
        if (reportPrimerClass != null) {
            return reportPrimerClass;
        }
        return NoOpReportPrimer.class;
    }

    @NonNull
    Method httpMethod() {
        if (httpMethod != null) {
            return httpMethod;
        }
        return Method.POST;
    }

    @NonNull
    Type reportType() {
        if (reportType != null) {
            return reportType;
        }
        return Type.FORM;
    }

    @NonNull
    Class<? extends ReportSenderFactory>[] reportSenderFactoryClasses() {
        if (reportSenderFactoryClasses != null) {
            return reportSenderFactoryClasses;
        }
        //noinspection unchecked
        return new Class[]{DefaultReportSenderFactory.class};
    }

    @Nullable
    KeyStoreFactory keyStoreFactory() {
        return keyStoreFactory;
    }

    @NonNull
    Map<String, String> httpHeaders() {
        return httpHeaders;
    }
}
