# Auto SMS Forwarder 📲

<p align="center">
  <img src="https://github.com/user-attachments/assets/02d7e132-b366-4b88-8086-0874f9d86592" width="150" alt="Auto SMS Forwarder Logo">
</p>

An open-source Android application built with Kotlin and Jetpack Compose that silently and automatically forwards incoming SMS messages to another phone number or email address. 

Unlike other forwarders that require paid third-party API keys (like SendGrid or Mailgun), this app securely connects to Google's standard SMTP servers, allowing you to automatically email yourself text messages in the background using a free Gmail account.

## ✨ Features
- **Auto-Forward via SMS**: Instantly relays incoming messages to any target phone number.
- **Auto-Forward via Email**: Silently emails messages to you using a secure Gmail App Password.
- **Unkillable Background Service**: Uses an Android Foreground Service and standard Battery Optimization bypasses to reliably listen for texts 24/7.
- **Duplicate Prevention**: Intelligently hashes incoming messages to prevent double-sending identical texts.
- **Local Logs**: Persists a history of your last 50 forwarded messages inside a local Room database so you can monitor success/failure statuses.
- **Privacy First**: Completely open-source. No hidden tracking, no telemetry, and all data remains tightly secured on your local device.

## Screenshots
<img src="https://github.com/user-attachments/assets/c15a1d6a-ab63-469a-9961-de6b17c32e71" width="250"/>
<img src="https://github.com/user-attachments/assets/5d849721-30fe-496f-a860-018fb9f9d30e" width="250"/>
<img src="https://github.com/user-attachments/assets/3380d9c2-d87c-45e5-a736-f26d8b12d88d" width="250"/>
<img src="https://github.com/user-attachments/assets/1118888f-6fe9-42cf-80db-41318e438343" width="250"/>

## 🛠 Tech Stack
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material 3)
- **Architecture:** MVVM (Model-View-ViewModel) + Manual DI
- **Asynchronous Processing:** Kotlin Coroutines & Flows
- **Local Storage:** DataStore (Preferences) & Room Database (SQL)
- **Networking:** Native `javax.mail` SMTP (Android-Mail)

## 🚀 Setup & Installation

### 1. Generating a Google "App Password"
To allow the app to send emails quietly in the background without launching the Gmail app:
1. Go to your [Google Account Security Settings](https://myaccount.google.com/security).
2. Ensure **2-Step Verification** is turned ON.
3. Search for **App Passwords** and generate a new one (call it "SMS Forwarder").
4. Google will generate a 16-character password (e.g., `abcd efgh ijkl mnop`). Save this!

### 2. Building the App
1. Clone this repository to your computer:
   ```bash
   git clone https://github.com/yourusername/sms_forward.git
   ```
2. Open the project folder in **Android Studio**.
3. Let Gradle sync and download all dependencies.
4. Click **Run** to install the APK on your physical Android device.

## 📱 Usage
1. Open the app and grant the necessary **SMS** and **Notification** permissions.
2. Under Settings:
   - Enter a **Target Phone Number** (Optional).
   - Toggle **Enable Email Forwarding**.
   - Input your Gmail address under **Sender Email Address** and paste your 16-character **App Password**.
3. Tap **Enable Forwarding** at the top.
4. *(Crucial)* Tap **Disable Battery Optimization** at the bottom so the Android OS doesn't randomly kill the background listener format!

## 🤝 Contributing
Pull requests are welcome! If you encounter any bugs or have feature requests, feel free to open an issue.

---

### </> Open source

**Feedback and Support:**
📧 Email: [manishmatwacs@gmail.com](mailto:manishmatwacs@gmail.com)  
✈️ Telegram: [@zeetron](https://t.me/zeetron)  
📸 Instagram: [@expert.py](https://instagram.com/expert.py)  

Crafted with ♥️ by **Manish Matwa Choudhary**
