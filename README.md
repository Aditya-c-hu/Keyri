# Keyri Keyboard ⌨️🤖

Keyri is an intelligent, privacy-first Android Input Method Editor (IME) that learns your typing behavior locally. It utilizes advanced AI techniques like pruning and quantization to deliver fast, lightweight, and highly accurate text prediction directly on your device.

## Features ✨

*   **Local AI Text Prediction:** Suggests your next words based on your personal typing history, powered by a lightweight, on-device AI model.
*   **Privacy-First Design:** All learning and prediction happen locally on your device. Your keystrokes and data are never sent to the cloud.
*   **Performance Optimized:** Uses model pruning and quantization to ensure the AI runs smoothly without draining your battery or consuming excessive memory.
*   **Customizable Layouts:** Includes robust layout configurations, emoji support, and personalized AI tool panels.
*   **Modern Android Tech Stack:** Built entirely with Kotlin and Jetpack Compose for a fluid and responsive user interface.

## Project Structure 📁

The project is structured into several key packages under `app/src/main/java/com/example/keyri/`:
*   `ai/`: Core artificial intelligence components (`NextWordPredictor`, `ModelOptimizer`, `PersonalizationTrainer`, `QuantizedNgramStore`).
*   `keyboard/`: Input Method Service (IMS) implementation, layouts, and keyboard UI components (`PrivKeyKeyboardService`, `KeyboardLayouts`).
*   `security/`: Privacy-guarding mechanisms to ensure local data isolation (`PrivacyGuard`).
*   `settings/`: Configuration screens and user preferences.
*   `ui/`: The main configuration application (built with Jetpack Compose) to manage keyboard settings.

## Getting Started 🚀

### Prerequisites
*   Android Studio
*   Android SDK minimum API level support

### Installation
1.  Clone the repository:
    ```bash
    git clone https://github.com/Aditya-c-hu/Keyri.git
    ```
2.  Open the project in Android Studio.
3.  Sync the project with Gradle files.
4.  Build and run the app on an Android emulator or physical device.

### Enabling the Keyboard
1.  Once installed, open your device **Settings**.
2.  Navigate to **System > Keyboard > On-screen keyboard** (varies by device).
3.  Enable **Keyri Keyboard**.
4.  Switch your active keyboard to Keyri when typing in any app.

## License 📄
This project is open-source.
