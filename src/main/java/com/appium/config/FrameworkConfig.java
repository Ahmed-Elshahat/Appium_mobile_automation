package com.appium.config;

import org.aeonbits.owner.Config;

@Config.LoadPolicy(Config.LoadType.MERGE)
@Config.Sources({
        "system:properties",
        "system:env",
        "file:${user.dir}/src/main/resources/config.properties"
})
public interface FrameworkConfig extends Config {

    @Key("appium.server.url")
    @DefaultValue("http://127.0.0.1:4723")
    String appiumServerUrl();

    @Key("platform.name")
    @DefaultValue("ANDROID")
    String platformName();

    @Key("platform.version")
    String platformVersion();

    @Key("device.name")
    @DefaultValue("emulator-5554")
    String deviceName();

    @Key("automation.name")
    @DefaultValue("UiAutomator2")
    String automationName();

    @Key("app.path")
    String appPath();

    @Key("app.package")
    String appPackage();

    @Key("app.activity")
    String appActivity();

    @Key("android.systemPort")
    @DefaultValue("8200")
    int androidSystemPort();

    @Key("ios.wdaLocalPort")
    @DefaultValue("8100")
    int iosWdaLocalPort();

    @Key("app.bundleId")
    String bundleId();
}
