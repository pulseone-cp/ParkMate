# ParkMate

ParkMate is a modern Android application designed for use at a company's front desk to efficiently issue and manage parking tickets for guests. The app features a simple, user-friendly interface for the front-desk operator and a comprehensive, password-protected admin backend for configuration and management.

## Features

### Front-Desk (User-Facing)

*   **Simple Ticket Creation:** Quickly issue a parking ticket by entering a guest's name, surname, license plate, and selecting the department they are visiting.
*   **Silent Printing:** A parking ticket is printed silently on a pre-configured thermal printer with a single tap.
*   **Modern UI:** A clean, modern, and visually appealing interface built with Material Design components.
*   **Custom Welcome Message:** Display a custom headline and message on the main screen.

### Admin Backend

The admin section is protected by a PIN and provides access to the following features:

*   **Parking History:**
    *   View a complete history of all issued parking tickets.
    *   Search the history by name, surname, or license plate.
    *   Reprint any ticket from the history.
    *   Export the entire ticket history to a shareable Excel document.
*   **User Management:**
    *   Change the admin access PIN.
*   **Department Management:**
    *   Add or remove departments from the dropdown list on the main screen.
    *   Set a default (favorite) department to be pre-selected.
*   **Printing Configuration:**
    *   Select a default printer from the available Android printing services.
    *   Add a custom, multi-line imprint or disclaimer to be printed at the bottom of every ticket.
*   **Display Settings:**
    *   Set a custom welcome headline and body text for the main screen.

## Technology Stack

*   **Language:** 100% [Kotlin](https://kotlinlang.org/)
*   **UI:**
    *   Android XML Layouts with Material Design Components.
    *   Modern UI patterns including `MaterialCardView` and `TextInputLayout` with an Exposed Dropdown Menu.
*   **Data Persistence:**
    *   **Room Database:** For storing and querying the parking ticket history.
    *   **SharedPreferences:** For persisting all application settings (admin PIN, departments, printer configuration, etc.).
*   **Printing:**
    *   Custom `PrintDocumentAdapter` to generate and print tickets to any printer connected via the Android printing service.
    *   Dynamically generates a PDF formatted for a 50mm thermal printer.
*   **CI/CD:**
    *   A **GitHub Actions** workflow is configured to automatically build the app and run unit tests on every push and pull request to the `main` branch.
