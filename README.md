# Vonage Redirect Troubleshooter

<img src="https://developer.nexmo.com/images/logos/vbc-logo.svg" height="48px" alt="Vonage" />


An Android application designed to help developers and support teams debug HTTP redirect issues by providing detailed information about redirect chains and responses.

## Features

- **HTTP Redirect Analysis**: Trace and analyze HTTP redirect chains
- **Detailed Response Information**: View headers, status codes, and response details
- **Export Functionality**: Save debug information to text files for sharing and analysis

## Technologies Used

- **Kotlin**: Primary programming language
- **Jetpack Compose**: Modern Android UI toolkit
- **OkHttp**: HTTP client for network requests
- **Gradle**: Build system with Kotlin DSL

## Requirements

- Android API level 21+ (Android 5.0 Lollipop)
- Internet permission for network requests

## Installation

### Prerequisites

- Android Studio Arctic Fox or later
- JDK 8 or later
- Android SDK with API level 21+

### Building the Project

1. Clone the repository:
   ```bash
   git clone https://github.com/Vonage-Community/tool-android-redirect_debugger.git
   cd tool-android-redirect_debugger
   ```

2. Open the project in Android Studio

3. Sync the project with Gradle files

4. Build and run the application:
   ```bash
   ./gradlew assembleDebug
   ```

## Usage

1. Launch the Vonage Redirect Troubleshooter app
2. Enter a URL in the input field
3. Tap the debug button to analyze the redirect chain
4. View the detailed information about each redirect step
5. Export the results to a text file if needed for further analysis

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/vonage/redirecttroubleshooter/
│   │   │   ├── MainActivity.kt          # Main activity with UI logic
│   │   │   └── ui/theme/               # Material Design 3 theme files
│   │   ├── res/                        # Android resources
│   │   └── AndroidManifest.xml         # App configuration
│   ├── test/                           # Unit tests
│   └── androidTest/                    # Instrumentation tests
├── build.gradle.kts                    # App-level build configuration
└── proguard-rules.pro                  # ProGuard configuration
```

## Key Dependencies

- **Compose BOM**: 2024.09.00
- **Activity Compose**: 1.8.0
- **Material3**: Latest from Compose BOM
- **Core KTX**: 1.10.1
- **Lifecycle Runtime KTX**: 2.6.1

## Development

### Running Tests

```bash
# Unit tests
./gradlew test

# Instrumentation tests
./gradlew connectedAndroidTest
```

### Code Style

This project follows standard Kotlin coding conventions and Android development best practices.

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the terms specified in the LICENSE file.

## Support

For support and questions, please refer to the Vonage Community resources or open an issue in this repository.

---

**Note**: This tool is designed for debugging and troubleshooting HTTP redirects. Ensure you have proper permissions before testing URLs that are not your own.
