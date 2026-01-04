# ParkMate

ParkMate is a modern, professional-grade Android application designed for a kiosk or front-desk environment to efficiently issue, manage, and audit guest parking tickets. The app features a simple, user-friendly interface for the front-desk operator and a comprehensive, password-protected admin backend for advanced configuration and management.

## Features

### Front-Desk (User-Facing)

*   **Simple & Fast Ticket Creation:** Quickly issue a parking ticket by entering a guest's name, surname, license plate, and selecting their destination department.
*   **Silent, One-Tap Printing:** A parking ticket is printed silently and instantly on a configured Bluetooth thermal printer with a single tap, bypassing the system print dialog for a true kiosk-style experience.
*   **Modern & Clean UI:** A visually appealing interface built with modern Material Design components.
*   **Custom Welcome Message:** Display a custom headline and message on the main screen to greet users or provide instructions.
*   **Robust Input Validation:** License plate input is filtered to allow only valid characters (letters, numbers, and hyphens) and is automatically converted to uppercase for data consistency.

### Admin Backend

The admin section is protected by a PIN and provides access to a powerful suite of features for management and auditing:

*   **History & Auditing:**
    *   View a complete, searchable history of all issued parking tickets.
    *   **Omni-Search:** Search the entire history by name, surname, or license plate from a single search bar.
    *   **Ticket Preview:** Visually preview a generated ticket bitmap directly from the history before printing.
    *   **Reprint Tickets:** Easily reprint any ticket from the history.
    *   **Excel Export:** Export the entire ticket history to a shareable Excel document (`.xlsx`).
    *   **Audit Status:** See at a glance whether a ticket has been successfully reported to the live audit endpoint.

*   **Ticket Validation:**
    *   **QR Code Scanner:** A built-in QR code scanner allows for fast and easy ticket validation.
    *   **Customizable Validity:** Set how long a ticket is valid (in hours, from 1-999) to match your company's policy.
    *   **Time-Sensitive Logic:** The validation logic checks not only if a ticket is authentic but also if it is being used within its configured validity period.
    *   **Validation Log:** A running history of all validation attempts (including status, time, and expiry) is displayed for a complete audit trail.

*   **Printing Configuration:**
    *   **Bluetooth Printer Discovery:** Automatically discover and list all paired Bluetooth printers.
    *   **Set Default Printer:** Select and save a default thermal printer by its MAC address for direct, silent printing.
    *   **Custom Imprint:** Add a custom, multi-line imprint or disclaimer to be printed at the bottom of every ticket.

*   **Display & App Settings:**
    *   Set a custom welcome headline and body text for the main screen.
    *   Configure user-management settings, such as changing the admin PIN.

## Technology Stack

*   **Language:** 100% [Kotlin](https://kotlinlang.org/)
*   **UI:**
    *   Modern Android XML Layouts with Material Design Components.
    *   `RecyclerView` for efficiently displaying lists of data (history, validation log).
*   **Printing:**
    *   **Direct Bluetooth Communication:** Bypasses the standard Android Print Framework for a true, silent POS/kiosk printing experience.
    *   **Custom ESC/POS Implementation:** Sends print commands directly to the selected Bluetooth thermal printer.
    *   **Dynamic Bitmap Generation:** Creates a high-contrast (black on white) bitmap of the ticket, correctly formatted for a 58mm printer, regardless of the device's theme.
*   **Data Persistence:**
    *   **Room Database:** For storing and querying the parking ticket history, including the audit status.
    *   **SharedPreferences:** For persisting all application settings (admin PIN, default printer MAC address, ticket validity, etc.).
*   **Networking & Auditing:**
    *   **Live Audit Webhook:** Silently sends a JSON payload of each new ticket to a configurable endpoint for real-time tracking.
    *   Uses standard `HttpURLConnection` for a lightweight and reliable implementation.
*   **CI/CD:**
    *   A **GitHub Actions** workflow is configured to automatically build the app, run unit and instrumented tests, and publish a signed release APK to GitHub Releases on every tag push.
