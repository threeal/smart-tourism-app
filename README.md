# Smart Tourism App

Location-based [augmented reality (AR)](https://en.wikipedia.org/wiki/Augmented_reality) app using [Android Studio](https://developer.android.com/studio) and [Kotlin](https://kotlinlang.org/).
This project is based on [dat-ng/ar-location-based-android](https://github.com/dat-ng/ar-location-based-android) which is rewritten in Kotlin with some modifications like login using [QR code](https://en.wikipedia.org/wiki/QR_code), getting location data from HTTP server, and icon click.
But the AR still doesn't have a good virtual impression as sometimes it shows a large error in the accuracy of the location's direction.
Although, the error actually depends on the updated data from the location service and orientation sensor in the device and it probably could be fixed with a better data acquisition like using [Kalman Filters](https://en.wikipedia.org/wiki/Kalman_filter).

We created this project as part of the Smart Tourism project that consists of several parts like [database server](https://github.com/iruz17/project-telematika), [card scanner device](https://github.com/iruz17/smart-tourism-device), [administrator website](https://github.com/niaangellina/smart-tourism-web), and [location's direction app](https://github.com/threeal/smart-tourism-app).
Smart Tourism project is a system for smart tourism that improves the impression of tourism sites using an electronic card to enter and exit locations, a website that monitors and show statistic for each location, and AR app for visitors to show location's direction.
This project is undertaken to fulfill the Telematics Projects course in the [Computer Engineering department](https://www.its.ac.id/study-at-its/faculties-and-departments/faculty-electrical-technology/computer-engineering/) of [Sepuluh Nopember Institute of Technology](https://www.its.ac.id/).

## Usage

- Install Android Studio as in their [official guide](https://developer.android.com/studio/install).
- Open this project in Android Studio.
- Run [the database server](https://github.com/iruz17/project-telematika) and set the `server_address` string according to the database server address.
- Sync Gradle, Build the project, and test it on your device.
