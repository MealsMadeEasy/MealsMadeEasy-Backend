# Meals Made Easy Backend API

## Setup
Before you begin, note that this server is hosted on Heroku at `https://meals-made-easy.herokuapp.com/`. You only need to follow these steps if you want to run the server on your own machine.

### Prerequisites
 - You must have version 1.8 or higher of the [Java runtime (JRE)](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html) and the [Java development kit (JDK)](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).
 - Before running the server, you'll need access to a [Firebase](https://console.firebase.google.com/) project's Admin account credentials, a [Mercury](https://mercury.postlight.com/web-parser/) API key, and an [Edamam](https://developer.edamam.com/edamam-recipe-api) API key.
    - If you want to create a new Firebase project, you can these values by navigating to [Service Accounts](https://console.firebase.google.com/project/_/settings/serviceaccounts/adminsdk?authuser=0). Then click "Generate New Private Key" at the bottom of the "Firebase Admin SDK" to generate a new key, which you'll use in the installation step.
    - To get an API key for other platforms, follow the registration steps on the corresponding website to get started.
    - **Note:** It's very important to keep these values private to prevent attackers from abusing the system.

### Dependencies
 - All dependencies are managed automatically with Gradle, which is included in this repo.

### Download
 - All code for the backend server exists in this repository. To download it, you can clone this repository, or [save it as a ZIP](https://github.com/MealsMadeEasy/Backend/archive/master.zip).
 - Save or extract this repo where you can conveniently access it, such as `Downloads/Backend/`.

### Build
 - This project uses Gradle as its build system through the Gradle Wrapper.
 - To build the application, run `gradlew jar` in a terminal from the root of the repository
   - On Windows, open the start menu, type `cmd`, and hit enter. On MacOS, open Spotlight by pressing `⌘ + space`, type `Terminal` into the search box, and hit return.
   - Open the folder where you cloned/extracted this repository in the terminal by typing `cd Downloads/Backend` and then pressing enter (Replace `Downloads/Backend` with wherever the repository is located).
   - On Windows, type `gradlew jar` and press enter. On MacOS, type `./gradlew jar` and press return.
   - Gradle will build the application and create a file in `build/libs/mealsmadeeasy-1.0.jar`.

### Installation
 - The server will read these private keys through environment variables. Learn [how to create custom environment variables](https://www.schrodinger.com/kb/1842), and add the following values:

| Key                        | Notes                                                                                          |
| -------------------------- | ---------------------------------------------------------------------------------------------- |
| `EDAMAM_APP_ID`            | 8 characters                                                                                   |
| `EDAMAM_APP_KEY`           | 32 characters                                                                                  |
| `MERCURY_API_KEY`          | ~40 characters                                                                                 |
| `MME_CLIENT_EMAIL`         | From Firebase; e.g. `firebase-adminsdk-xxxxx@mealsmadeeasy-xxxxx.gserviceaccount.com`          |
| `MME_CLIENT_ID`            | From Firebase; ~20 digits                                                                      |
| `MME_CLIENT_X509_CERT_URL` | From Firebase; e.g. `https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-...`  |
| `MME_PRIVATE_KEY`          | From Firebase; starts with `-----BEGIN PRIVATE KEY-----`                                       |
| `MME_PRIVATE_KEY_ID`       | From Firebase; ~40 characters                                                                  |

### Running the Server
 - Once you've got the application built and your environment variables setup, you're ready to run the project.
 - Using similar steps from before, open a terminal and navigate to the repository.
 - Navigate to the build folder by typing `cd build/libs/` and press enter.
 - Launch the application by typing `java -jar mealsmadeeasy-1.0.jar`.
 - The server will start on port 80 and will fulfill any requests sent to the host.
 - To stop the server, press `ctrl + C` on Windows or `control + C` on MacOS.