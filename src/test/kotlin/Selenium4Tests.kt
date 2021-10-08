import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WindowType
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.devtools.HasDevTools
import org.openqa.selenium.devtools.v94.emulation.Emulation
import org.openqa.selenium.devtools.v94.network.Network
import org.openqa.selenium.remote.Augmenter
import java.util.*


class Selenium4Tests {
    lateinit var chrome: ChromeDriver

    @BeforeEach
    fun startBrowser() {
        System.setProperty("webdriver.chrome.driver", "/opt/WebDriver/bin/chromedriver");
        val options = ChromeOptions()
        val prefs: MutableMap<String, Any> = HashMap()
        prefs["profile.default_content_settings.geolocation"] = 1

        options.setExperimentalOption("prefs", prefs)
        chrome = ChromeDriver(options)
    }

    @AfterEach
    fun close() {
        chrome.getRemoteDevTools().close()
        chrome.quit()
    }

    @Test
    fun `start browser`() {
        chrome.navigate().to("http://google.com")
        chrome.switchTo().newWindow(WindowType.WINDOW).navigate().to("http://jetbrains.com")
        chrome.switchTo().newWindow(WindowType.TAB).navigate().to("http://yandex.ru")
        val handles = chrome.windowHandles
        for (handle in handles) {
            println(handle)
            chrome.switchTo().window(handle)
        }
        chrome.quit()
    }

    @Test
    fun `change location`() {
        chrome.navigate().to("https://www.gps-coordinates.net/my-location")
        Thread.sleep(2000)
        chrome.setGeolocation(51.5055, 0.0754, 1.2)
        chrome.navigate().refresh()
        Thread.sleep(2000)
    }

    @Test
    fun `change timezone`() {
        chrome.navigate().to("http://whatismytimezone.com")
        chrome.setTimezone("UTC")
        Thread.sleep(2000)
        chrome.navigate().refresh()
        Thread.sleep(2000)
    }

    @Test
    fun `set mobile view`() {
        chrome.navigate().to("http://whatismytimezone.com")
        chrome.setTimezone("UTC")
        Thread.sleep(2000)
        chrome.navigate().refresh()
        chrome.setMobileView(width = 500, height = 800)
        Thread.sleep(2000)
        chrome.setMobileView(width = 500, height = 1000, isPortrait = false)
        Thread.sleep(2000)
    }

    @Test
    fun `simulate slow network`() {
        chrome.emulateNetwork(
            offline = false,
            latency = 20,
            downloadThroughput = 5000,
            uploadThroughput = 5000
        )
        chrome.get("https://www.google.com")
    }

    fun WebDriver.getRemoteDevTools() = (Augmenter().augment(this) as HasDevTools).devTools

    fun WebDriver.setGeolocation(latitude: Double, longitude: Double, accuracy: Double) {
        val devTools = this.getRemoteDevTools()
        devTools.createSessionIfThereIsNotOne()
        devTools.send(
            Emulation.setGeolocationOverride(
                Optional.of(latitude), // latitude
                Optional.of(longitude), // longitude
                Optional.of(accuracy) // accuracy
            )
        )
    }

    fun WebDriver.setTimezone(timezone: String) {
        val devTools = this.getRemoteDevTools()
        devTools.createSessionIfThereIsNotOne()
        devTools.send(Emulation.setTimezoneOverride(timezone))
    }

    fun WebDriver.setMobileView(width: Int, height: Int, deviceScaleFactor: Int = 50, isPortrait: Boolean = true) {
        val devTools = this.getRemoteDevTools()
        devTools.createSessionIfThereIsNotOne()
        devTools.send(
            Emulation.setDeviceMetricsOverride(
                width,
                height,
                deviceScaleFactor,
                true,      // isMobile
                Optional.empty(), // scale
                Optional.empty(), // screenWidth
                Optional.empty(), // screenHeight
                Optional.empty(), // positionX
                Optional.empty(), // positionY
                Optional.empty(), // dontSetVisibleSize
                Optional.of(
                    if (isPortrait) {
                        org.openqa.selenium.devtools.v94.emulation.model.ScreenOrientation(
                            org.openqa.selenium.devtools.v94.emulation.model.ScreenOrientation.Type.PORTRAITPRIMARY,
                            0
                        )
                    } else {
                        org.openqa.selenium.devtools.v94.emulation.model.ScreenOrientation(
                            org.openqa.selenium.devtools.v94.emulation.model.ScreenOrientation.Type.LANDSCAPEPRIMARY,
                            0
                        )
                    }
                ), // screenOrientation
                Optional.empty(), // viewport
                Optional.empty() // displayFeature
            )
        )
    }

    fun WebDriver.emulateNetwork(offline: Boolean, latency: Int, downloadThroughput: Int, uploadThroughput: Int) {
        val devTools = this.getRemoteDevTools()
        devTools.createSessionIfThereIsNotOne()
        devTools.send(
            Network.emulateNetworkConditions(
                offline,
                latency,
                downloadThroughput,
                uploadThroughput,
                Optional.empty() // connectionType
            )
        )
    }
}