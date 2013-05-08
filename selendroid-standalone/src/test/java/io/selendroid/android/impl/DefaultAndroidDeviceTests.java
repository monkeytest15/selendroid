package io.selendroid.android.impl;

import io.selendroid.android.AndroidApp;
import io.selendroid.android.AndroidDevice;
import io.selendroid.android.AndroidSdk;
import io.selendroid.io.ShellCommand;
import io.selendroid.util.HttpClientUtil;
import io.selendroid.util.SelendroidAssert;

import java.io.File;
import java.util.List;

import org.apache.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.junit.Assert;
import org.junit.Test;

import com.beust.jcommander.internal.Lists;


/**
 * Works only if - android_home and java_home are set and - if one emulator is already running - and
 * selendroid project is build (apks are existent)
 * 
 * @author ddary
 * 
 */
public class DefaultAndroidDeviceTests {
  public static final String SELENDROID_SERVER_PACKAGE = "org.openqa.selendroid";
  public static final String AUT_PACKAGE = "org.openqa.selendroid.testapp";
  private String serial = "emulator-5554";
  private int port = 7070;
  private AndroidApp selendroidServer = new DefaultAndroidApp(new File(
      "../selendroid-server/target/selendroid-server-0.4-SNAPSHOT.apk"));
  private AndroidApp aut = new DefaultAndroidApp(new File(
      "../selendroid-test-app/target/selendroid-test-app-0.4-SNAPSHOT.apk"));

  private void cleanUpDevice(AndroidDevice emulator) throws Exception {
    emulator.uninstall(selendroidServer);
    emulator.uninstall(aut);
    String installedAPKs = listInstalledPackages();
    Assert.assertFalse(installedAPKs.contains(SELENDROID_SERVER_PACKAGE));
    Assert.assertFalse(installedAPKs.contains(AUT_PACKAGE));
  }

  @Test
  public void testShouldBeAbleToStartSelendroid() throws Exception {
    AndroidDevice emulator = new DefaultAndroidDevice(serial);
    cleanUpDevice(emulator);

    // install apps
    emulator.install(selendroidServer);
    emulator.install(aut);
    String installedAPKs = listInstalledPackages();
    Assert.assertTrue(installedAPKs.contains(SELENDROID_SERVER_PACKAGE));
    Assert.assertTrue(installedAPKs.contains(AUT_PACKAGE));

    // start selendroid
    emulator.startSelendroid(aut, port);
    String url = "http://localhost:" + port + "/wd/hub/status";
    HttpResponse response = HttpClientUtil.executeRequest(url,
            HttpMethod.GET);
    SelendroidAssert.assertResponseIsOk(response);
    Assert.assertTrue(emulator.isSelendroidRunning());
  }

  private String listInstalledPackages() throws Exception {
    List<String> command = Lists.newArrayList();
    command.add(AndroidSdk.adb());
    command.add("-s");
    command.add(serial);
    command.add("shell");
    command.add("pm");
    command.add("list");
    command.add("packages");
    return ShellCommand.exec(command);
  }
}